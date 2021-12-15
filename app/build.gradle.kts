plugins {
  id("com.android.application")
  id("org.jetbrains.kotlin.android")
  id("com.google.gms.google-services")
  id("de.mannodermaus.android-junit5")
  id("kotlinx-serialization")
}

android {
  compileSdk = 30
  defaultConfig {
    applicationId = "simple.payment.tracker"
    minSdk = 28
    targetSdk = 30
    versionCode = 10100
    versionName = "1.1.0"
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }
  buildTypes {
    getByName("debug") {
      isTestCoverageEnabled = true
      isMinifyEnabled = false
    }
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
  }

  buildFeatures { compose = true }

  composeOptions { kotlinCompilerExtensionVersion = "1.0.1" }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
  kotlinOptions {
    freeCompilerArgs =
        freeCompilerArgs + "-Xopt-in=kotlin.RequiresOptIn" + "-Xopt-in=kotlin.Experimental"

    jvmTarget = "1.8"
  }
}

repositories {
  google()
  mavenCentral()
  maven { url = uri("https://plugins.gradle.org/m2/") }
  maven { url = uri("https://oss.jfrog.org/libs-snapshot") }
}

dependencies {
  implementation("org.jetbrains.kotlin:kotlin-stdlib:${project.extra["kotlin"]}")
  implementation("io.reactivex.rxjava2:rxandroid:2.1.1")
  implementation("io.reactivex.rxjava2:rxjava:2.2.19")
  implementation("io.reactivex.rxjava2:rxkotlin:2.4.0")

  implementation("org.koin:koin-core:2.2.2")
  implementation("org.koin:koin-android:2.2.2")
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.2")

  implementation("androidx.datastore:datastore:1.0.0")
  implementation("androidx.appcompat:appcompat:1.3.1")

  implementation("com.google.firebase:firebase-database:20.0.1")
}

// compose
dependencies {
  val compose_version = "1.0.1"
  implementation("androidx.compose.runtime:runtime:$compose_version")
  implementation("androidx.compose.foundation:foundation-layout:$compose_version")
  implementation("androidx.compose.animation:animation:$compose_version")
  implementation("androidx.activity:activity-compose:1.3.1")

  implementation("androidx.compose.ui:ui:1.0.1")
  // Tooling support (Previews, etc.)
  implementation("androidx.compose.ui:ui-tooling:1.0.1")
  // Foundation (Border, Background, Box, Image, Scroll, shapes, animations, etc.)
  implementation("androidx.compose.foundation:foundation:1.0.1")
  // Material Design
  implementation("androidx.compose.material:material:1.0.1")
  // Material design icons
  implementation("androidx.compose.material:material-icons-core:1.0.1")
  implementation("androidx.compose.material:material-icons-extended:1.0.1")
  // Integration with observables
  implementation("androidx.compose.runtime:runtime-livedata:1.0.1")
  implementation("androidx.compose.runtime:runtime-rxjava2:1.0.1")

  // UI Tests
  androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.0.1")
}

// test
dependencies {
  testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.2")
  testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.7.2")
  testImplementation("org.assertj:assertj-core:3.16.1")

  androidTestImplementation("androidx.test.ext:junit:1.1.3")
  androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
}
