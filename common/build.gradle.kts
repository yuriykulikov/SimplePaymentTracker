import org.jetbrains.compose.compose

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
        implementation(compose.desktop.currentOs)
        api("dev.gitlive:firebase-database:1.4.3")
        api("androidx.datastore:datastore-core:1.0.0")
        // TODO move logging to own module and use it in common
        api("ch.qos.logback:logback-classic:${extra["logback.version"]}")
        api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")
        implementation("io.insert-koin:koin-core:3.4.0")
      }
    }
    val jvmTest by getting {
      dependencies {
        implementation("io.kotest:kotest-assertions-core-jvm:5.5.5")
        implementation(compose("org.jetbrains.compose.ui:ui-test-junit4"))

        implementation("com.google.auth:google-auth-library-oauth2-http:1.12.1")
        val ktor_version = "2.2.3"
        implementation("io.ktor:ktor-client-core:$ktor_version")
        implementation("io.ktor:ktor-client-cio:$ktor_version")
        implementation("io.ktor:ktor-serialization-kotlinx-json:$ktor_version")
        implementation("io.ktor:ktor-client-content-negotiation:$ktor_version")
      }
    }
  }
}
