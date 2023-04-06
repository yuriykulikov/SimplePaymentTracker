buildscript {
  repositories {
    google()
    mavenCentral()
    maven { url = uri("https://plugins.gradle.org/m2/") }
  }

  dependencies {
    classpath("com.android.tools.build:gradle:7.2.2")
    classpath("com.google.gms:google-services:4.3.15")
    classpath("de.mannodermaus.gradle.plugins:android-junit5:1.8.2.1")
  }
}

plugins {
  val kotlin = "1.7.10"
  id("com.diffplug.spotless") version "6.11.0"
  kotlin("android") version kotlin apply false
  kotlin("plugin.serialization") version kotlin apply false
}

allprojects {
  repositories {
    google()
    mavenCentral()
  }
}

spotless {
  kotlin {
    target("build.gradle.kts")
    ktfmt()
    lineEndings = com.diffplug.spotless.LineEnding.UNIX
  }
}

/** Applies spotless to app projects */
subprojects {
  apply(plugin = "com.diffplug.spotless")
  spotless {
    kotlin {
      target("build.gradle.kts", "**/*.kt")
      ktfmt()
      lineEndings = com.diffplug.spotless.LineEnding.UNIX
    }
  }
}
