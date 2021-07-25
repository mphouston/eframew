/*
 * Copyright (c) Michael Houston 2020. All rights reserved.
 */

package org.simplemes.eframe.application


import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.ser.FilterProvider
import com.fasterxml.jackson.databind.ser.PropertyFilter
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider
import groovy.util.logging.Slf4j
import io.micronaut.context.event.StartupEvent
import io.micronaut.runtime.event.annotation.EventListener
import io.micronaut.scheduling.annotation.Async
import io.micronaut.views.freemarker.FreemarkerViewsRendererConfigurationProperties
import org.simplemes.eframe.application.issues.WorkArounds
import org.simplemes.eframe.date.EFrameDateFormat
import org.simplemes.eframe.misc.TypeUtils
import org.simplemes.eframe.search.PassAllJacksonFilter
import org.simplemes.eframe.search.SearchEnginePoolExecutor
import org.simplemes.eframe.web.ui.webix.freemarker.FreemarkerDirectiveConfiguration

import javax.inject.Singleton

/**
 * This bean is executed on startup.  This is used to load initial data and handle similar actions.
 *
 * <h3>Logging</h3>
 * The logging for this class that can be enabled:
 * <ul>
 *   <li><b>debug</b> - Prints all beans found on startup. </li>
 * </ul>
 */
@Slf4j
@Singleton
class StartupHandler {

  /**
   * Executed on Startup.  Triggers the initial data load process.
   * @param event
   */
  @EventListener
  @Async
  void onStartup(StartupEvent event) {
    log.debug('Server Started with configuration {}', Holders.configuration)

    if (log.debugEnabled) {
      log.debug('All Beans: {}', Holders.applicationContext.allBeanDefinitions*.name)
    }
    //def ds = Holders.applicationContext.getBean(javax.sql.DataSource)
    //println "ds = $ds, ${ds.getClass()}"


    if (WorkArounds.list()) {
      log.warn('WorkArounds in use {}', WorkArounds.list())
    }

    SearchEnginePoolExecutor.startPool()

    // Modify the Object mapper
    waitForApplicationContext()
    // Need to wait since the handler is running in a different thread than the app context startup.
    def mapper = Holders.applicationContext.getBean(ObjectMapper)
    configureJacksonObjectMapper(mapper)

    // Register our freemarker extensions
    def config = Holders.applicationContext.getBean(FreemarkerViewsRendererConfigurationProperties)
    FreemarkerDirectiveConfiguration.addSharedVariables(config)


    // Start Initial data load.
    if (!TypeUtils.isMock(Holders.applicationContext)) {
      def loader = Holders.applicationContext.getBean(InitialDataLoader)
      loader.dataLoad()
    } else {
      log.debug("Disabled Initial Data Load for mock applicationContext")
    }
  }

  /**
   * Waits for the application context to be defined.
   */
  void waitForApplicationContext() {
    if (Holders.applicationContext) {
      return
    }

    def start = System.currentTimeMillis()
    def elapsed = 0
    while (elapsed < 5000) {
      elapsed = System.currentTimeMillis() - start
      if (Holders.applicationContext) {
        log.debug("waitForApplicationContext(): Waited {}ms", elapsed)
        return
      }
    }

    throw new IllegalStateException("waitForApplicationContext(): ApplicationContext was not created in ${elapsed}ms.  Is server started?")
  }

  /**
   * This method should be called from your Application class before the   Micronaut.run() method is called.
   * It initializes some settings that must be in place before Micronaut startup.
   */
  static void preStart() {
    // We set the default to UTC to make sure the timestamps are stored in the DB with UTC timezone.
    // All display's use the Globals.timeZone when rendering on the page.
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
  }

  /**
   * Configures the Jackson object mapper with the settings we need.
   * @param mapper The mapper.
   */
  static void configureJacksonObjectMapper(ObjectMapper mapper) {
    mapper.disable(SerializationFeature.INDENT_OUTPUT)
    mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    mapper.enable(SerializationFeature.USE_EQUALITY_FOR_OBJECT_ID)
    mapper.enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
    mapper.enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
    def format = new EFrameDateFormat()
    format.setTimeZone(Holders.globals.timeZone)
    mapper.setDateFormat(format)
    FilterProvider filters = new SimpleFilterProvider().addFilter("searchableFilter", (PropertyFilter) new PassAllJacksonFilter())
    mapper.setFilterProvider(filters)
    // No need to register a module.  It is registered by the module scan option from the file:
    //   src/main/resources/META-INF/services/com.fasterxml.jackson.databind.Module
    //mapper.registerModule(new EFrameJacksonModule())

  }

}