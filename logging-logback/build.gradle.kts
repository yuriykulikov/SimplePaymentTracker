plugins { kotlin("multiplatform") }

group = "io.github.yuriykulikov"

version = "1.0-SNAPSHOT"

repositories {
  google()
  mavenCentral()
}

kotlin {
  jvm { jvmToolchain { version = 11 } }
  sourceSets {
    val jvmMain by getting {
      dependencies {
        api(project(":logging-api"))
        api("ch.qos.logback:logback-classic:${extra["logback.version"]}")
      }
    }
  }
}
