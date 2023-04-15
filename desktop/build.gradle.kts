import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
  kotlin("multiplatform")
  id("org.jetbrains.compose")
  kotlin("plugin.serialization")
}

group = "org.kotlinconf.quotes"

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
    val jvmMain by getting {
      dependencies {
        implementation(project(":common"))
        implementation(compose.desktop.currentOs)
        implementation("io.insert-koin:koin-core:3.4.0")
      }
    }
    val jvmTest by getting {
      dependencies {
        implementation(compose("org.jetbrains.compose.ui:ui-test-junit4"))
        implementation("io.kotest:kotest-assertions-core-jvm:5.5.5")
      }
    }
  }
}

compose.desktop {
  application {
    mainClass = " simple.payment.tracker.Main.kt"
    nativeDistributions {
      targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
      packageName = "MovieQuotes"
      packageVersion = "1.0.0"
    }
  }
}
