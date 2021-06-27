# eframew

**This framework is a fork of the simplemes-core code-base using the Webix GUI library.**


# Build Instructions

Some common build options for the entire project includes:

* **./gradlew buildAll** - Build and test for all sub-projects (modules). 
* **./gradlew testAll** -  Run non-GUI tests for all sub-projects (modules).
* **./gradlew asciidoctorAll** - Build HTML docs for all sub-projects (modules).
* **./gradlew groovydocAll** - Build Groovy/Javadoc for all sub-projects (modules).
* **./gradlew cleanAll** - Cleans output directories for all sub-projects (modules).
* cd eframe; **./gradlew clean publishToMavenLocal generateExtensionDoc** - In a module's directory, 
  publishes the module's .jar file to local maven repository.  Works in _webix, eframe, mes-core_ 
  and _mes-assy_ modules.
* **./gradlew shadowJar** - Creates the distribution .jar file under 
  the _mes\build\libs_.  See running the Shadow (Fat) Jar below.

Each module has similar actions (e.g. build, test. etc).  
This project currently supports OpenJDK 14.0.

# Running the Shadow (Fat) Jar

The fat jar file created by the **shadowJar** task above creates the 
_mes/build/libs/mes-X.X-all.jar_ file.  This is a single executable .jar file that
you can run in any Java 9+ environment with the commands:

```
  set MICRONAUT_CONFIG_FILES=.\application.yml
  java -jar mes-0.5-all.jar 
```

This requires an _application.yml_ file to specify the encryption key (eframe.key) and
the database details.  For testing, you can probably use the _application-dev.yml_ file
in most cases.  

WARNING: NEVER, EVER use the _application-dev.yml_ in production!!!!!!!  

