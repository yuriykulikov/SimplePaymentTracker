pluginManagement {
  repositories {
    google()
    gradlePluginPortal()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
  }

  plugins {
    kotlin("multiplatform").version("1.7.10")
    id("org.jetbrains.compose").version("1.2.2")
  }
}

include(":app-android")

include(":app-desktop")

include(":compose-common")

include(":domain")

include(":logging-api")

include(":logging-logback")

include(":data")

rootProject.name = "SimplePaymentTracker"

includeBuild("./firebase-kotlin-sdk")
