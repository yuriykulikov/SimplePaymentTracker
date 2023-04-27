plugins {
  kotlin("multiplatform")
  kotlin("plugin.serialization")
  id("org.jetbrains.kotlinx.kover")
}

group = "io.github.yuriykulikov"

version = "1.0-SNAPSHOT"

repositories {
  google()
  mavenCentral()
}

kotlin {
  jvm { jvmToolchain { version = 11 } }
  sourceSets {
    val commonMain by getting {
      dependencies {
        api(project(":logging-api"))
        api(project(":data"))
        api("androidx.datastore:datastore-core:1.0.0")
        api("io.insert-koin:koin-core:3.4.0")
      }
    }
    val jvmTest by getting {
      dependencies {
        implementation(project(":logging-logback"))
        implementation("io.kotest:kotest-assertions-core-jvm:5.5.5")
        implementation("junit:junit:4.13.2")
        implementation("io.kotest:kotest-assertions-core-jvm:5.5.5")
        api("ch.qos.logback:logback-classic:${extra["logback.version"]}")
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

kover { useKoverTool() }
