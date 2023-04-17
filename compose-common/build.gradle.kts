import org.jetbrains.compose.compose

plugins {
  kotlin("multiplatform")
  id("org.jetbrains.compose")
  kotlin("plugin.serialization")
}

group = "io.github.yuriykulikov"

version = "1.0-SNAPSHOT"

repositories {
  google()
  mavenCentral()
  maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

kotlin {
  jvm {
    jvmToolchain { version = 11 }
    withJava()
  }
  sourceSets {
    val commonMain by getting { dependencies { api(project(":domain")) } }
    val jvmMain by getting {
      dependencies {
        implementation(compose.desktop.currentOs)
        api("ch.qos.logback:logback-classic:${extra["logback.version"]}")
      }
    }
    val jvmTest by getting {
      dependencies {
        implementation("io.kotest:kotest-assertions-core-jvm:5.5.5")
        implementation(compose("org.jetbrains.compose.ui:ui-test-junit4"))
      }
    }
  }
}
