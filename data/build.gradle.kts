plugins {
  kotlin("multiplatform")
  kotlin("plugin.serialization")
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
        api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")
        api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
      }
    }
  }
}
