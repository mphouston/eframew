/*
 * Copyright (c) Michael Houston 2020. All rights reserved.
 */

package org.simplemes.eframe.web.asset;

import asset.pipeline.AssetPipelineConfigHolder;
import asset.pipeline.fs.FileSystemAssetResolver;
import asset.pipeline.micronaut.AssetAttributes;
import asset.pipeline.micronaut.AssetPipelineService;
import asset.pipeline.micronaut.ProductionAssetCache;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.filter.ServerFilterChain;
import io.micronaut.http.server.types.files.StreamedFile;
import io.reactivex.Flowable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collection;
import java.util.Properties;

/**
 * Development mode asset pipeline service.  Works in dev mode for local (file system) assets, but
 * allows production mode assets from the .jar files (via the asset pipeline manifest).
 * This means no need to restart the server when an asset changes.
 */
@SuppressWarnings("unused")
public class EFrameAssetPipelineService extends AssetPipelineService {
  private static final Logger LOG = LoggerFactory.getLogger(EFrameAssetPipelineService.class);

  public EFrameAssetPipelineService() {
    AssetPipelineConfigHolder.registerResolver(new FileSystemAssetResolver("application", "src/assets"));
    System.out.println("exists: "+new File("src/assets").exists());
  }

  /**
   * Determines if the we should force dev mode on the given asset.  Used to detect mixed-mode scenarios
   * when some assets come from a .jar file and some from locale file system in dev mode.
   *
   * @param fileName The file to check for dev mode.
   * @param request  The request.
   * @return True if in the file is not in the manifest and should trigger dev mode.
   */
  protected boolean shouldForceDevMode(String fileName, HttpRequest<?> request) {
    if (fileName.startsWith("/")) {
      fileName = fileName.substring(1);
    }
    if (AssetPipelineConfigHolder.manifest != null) {
      // Check for digest version of the asset (contains a has that starts with '-').
      if (fileName.contains("-")) {
        // See if the digest version exists in the manifest.
        Collection values = AssetPipelineConfigHolder.manifest.values();
        if (values.contains(fileName)) {
          // Let the production mode logic handle the request.
          return false;
        }
      }

      // Trigger dev mode if the file is not in any manifest loaded for the asset pipeline.
      boolean res = AssetPipelineConfigHolder.manifest.get(fileName) == null;
      if (res) {
        // Not in manifest, but it might be in the classpath.
        // This works around the issue with .map files generated by Asset Pipeline, but not digested in the manifest.
        // We just prevent dev mode for .js.map files.
        if (fileName.indexOf(".js.map") > 0 || fileName.indexOf(".unminified.js") > 0) {
          res = false;
        }
      }
      return res;
    }

    return false;
  }

  /**
   * Handles the asset request, but falls back to dev mode if the asset is no in the manifest.
   */
  @SuppressWarnings("OptionalGetWithoutIsPresent")
  @Override
  public Flowable<MutableHttpResponse<?>> handleAsset(String filename, MediaType contentType, String encoding, HttpRequest<?> request, ServerFilterChain chain) {
    if (shouldForceDevMode(filename, request)) {
      // Not in manifest, so force dev mode.
      final String format = contentType != null ? contentType.toString() : null;
      LOG.debug("DevMode for " + filename);

      return super.handleAssetDevMode(filename, format, encoding, request).switchMap(contents -> {
        if (contents.isPresent()) {
          return Flowable.fromCallable(() -> {
            MutableHttpResponse<byte[]> response = HttpResponse.ok(contents.get());
            response.header("Cache-Control", "no-cache, no-store, must-revalidate");
            response.header("Pragma", "no-cache");
            response.header("Expires", "0");
            MediaType responseContentType = contentType != null ? contentType : MediaType.forExtension("html").get();
            response.contentType(responseContentType);
            response.contentLength(contents.get().length);

            return response;
          });
        } else {
          return chain.proceed(request);
        }
      });

    }

    // Use reflection to find the work around 265.
    Boolean workAround265 = false;
    try {
      Class waClass = Class.forName("org.simplemes.eframe.application.issues.WorkArounds");
      @SuppressWarnings("unchecked")
      Method method = waClass.getDeclaredMethod("getWorkAround265");
      workAround265 = (Boolean) method.invoke(null);
    } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {
    }

    if (workAround265) {
      return handleAssetWorkAround(filename, contentType, encoding, request, chain);
    } else {
      return super.handleAsset(filename, contentType, encoding, request, chain);
    }
  }

  // Begin WorkArounds.getWorkAround265()
  // Remove when WorkArounds.getWorkAround265() is removed.
  static final ProductionAssetCache fileCache = new ProductionAssetCache();

  public Flowable<MutableHttpResponse<?>> handleAssetWorkAround(String filename, MediaType contentType, String encoding, HttpRequest<?> request, ServerFilterChain chain) {

    if ("".equals(filename) || filename.endsWith("/")) {
      filename += "index.html";
    }

    if (filename.startsWith("/")) {
      filename = filename.substring(1);
    }
    final Boolean isDigestVersion = isDigestVersion(filename);
    final String etagHeader = getCurrentETag(filename);
    final String acceptEncoding = request.getHeaders().get("Accept-Encoding");
    filename = AssetPipelineConfigHolder.manifest.getProperty(filename, filename);
    final String fileUri = filename;
    final AssetAttributes attributeCache = fileCache.get(filename);
    Flowable<AssetAttributes> attributeFlowable;
    if (attributeCache != null) {
      attributeFlowable = Flowable.fromCallable(() -> attributeCache);
    } else {
      attributeFlowable = Flowable.fromCallable(() -> resolveAssetAttribute(fileUri));
    }


    return attributeFlowable.switchMap(assetAttribute -> {
      if (assetAttribute.exists()) {
        final Boolean gzipStream = acceptEncoding != null && acceptEncoding.contains("gzip") && assetAttribute.gzipExists();

        String ifNoneMatch = request.getHeaders().get("If-None-Match");
        if (ifNoneMatch != null && ifNoneMatch.equals(etagHeader)) {
          LOG.debug("NOT MODIFIED!");
          return Flowable.fromCallable(() -> HttpResponse.notModified());
        } else {
          LOG.debug("Generating Response");
          return Flowable.fromCallable(() -> {
            URLConnection urlCon = gzipStream ? assetAttribute.getGzipResource().openConnection() : assetAttribute.getResource().openConnection();
            StreamedFile streamedFile = new StreamedFile(urlCon.getInputStream(), MediaType.of(contentType), urlCon.getLastModified(), urlCon.getContentLength());
            MutableHttpResponse<StreamedFile> response = HttpResponse.ok(streamedFile);
            if (gzipStream) {
              response.header("Content-Encoding", "gzip");
            }
            if (encoding != null) {
              response.characterEncoding(encoding);
            }
            response.contentType(contentType);
            response.header("ETag", etagHeader);
            response.header("Vary", "Accept-Encoding");
            if (isDigestVersion && !fileUri.endsWith(".html")) {
              response.header("Cache-Control", "public, max-age=31536000");
            } else {
              response.header("Cache-Control", "no-cache");
            }
            return response;
          });
        }

      } else {
        return chain.proceed(request);
      }
    });
  }

  private boolean isDigestVersion(String uri) {
    String manifestPath = uri;
    Properties manifest = AssetPipelineConfigHolder.manifest;
    return manifest.getProperty(manifestPath, null) == null;
  }

  private String getCurrentETag(String uri) {
    String manifestPath = uri;
    Properties manifest = AssetPipelineConfigHolder.manifest;
    return "\"" + (manifest.getProperty(manifestPath, manifestPath)) + "\"";
  }


  private AssetAttributes resolveAssetAttribute(String filename) {
    URL assetUrl = this.getClass().getClassLoader().getResource("assets/" + filename);
    URL gzipAsset = this.getClass().getClassLoader().getResource("assets/" + filename + ".gz");
    if (assetUrl == null) {
      assetUrl = this.getClass().getClassLoader().getResource("assets/" + filename + "/index.html");
      gzipAsset = this.getClass().getClassLoader().getResource("assets/" + filename + "/index.html.gz");
    }
    AssetAttributes attribute = new AssetAttributes(assetUrl != null, gzipAsset != null, false, 0L, 0L, null, assetUrl, gzipAsset);
    fileCache.put(filename, attribute);
    return attribute;
  }
  // End WorkArounds.getWorkAround265()

}
