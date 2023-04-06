plugins {
  id("com.android.application")
  id("org.jetbrains.kotlin.android")
  id("com.google.gms.google-services")
  id("de.mannodermaus.android-junit5")
  id("kotlinx-serialization")
}

val jetpackCompose = "1.3.1"

android {
  compileSdk = 33
  defaultConfig {
    applicationId = "simple.payment.tracker"
    minSdk = 28
    targetSdk = 33
    versionCode = 10700
    versionName = "1.7.0"
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    multiDexEnabled = true
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

  kotlinOptions { jvmTarget = "1.8" }

  buildFeatures { compose = true }

  composeOptions { kotlinCompilerExtensionVersion = "1.3.0" }

  packagingOptions { resources.excludes.add("META-INF/INDEX.LIST") }
}

repositories {
  google()
  mavenCentral()
  maven { url = uri("https://plugins.gradle.org/m2/") }
  maven { url = uri("https://oss.jfrog.org/libs-snapshot") }
}

dependencies {
  implementation("org.koin:koin-core:2.2.2")
  implementation("org.koin:koin-android:2.2.2")
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.0")

  implementation("androidx.datastore:datastore:1.0.0")
  implementation("androidx.appcompat:appcompat:1.5.1")

  api("org.slf4j:slf4j-api:2.0.6")
  // we need 1.3.x on android because of the Java version
  implementation("ch.qos.logback:logback-classic:1.3.4")

  implementation("dev.gitlive:firebase-auth:1.4.3")
  implementation("dev.gitlive:firebase-database:1.4.3")
  implementation(platform("com.google.firebase:firebase-bom:30.5.0"))
  implementation("com.google.firebase:firebase-database")
  implementation("com.google.firebase:firebase-database-ktx")
  implementation("com.google.firebase:firebase-auth")
  implementation("com.google.firebase:firebase-auth-ktx")
  implementation("com.google.firebase:firebase-common-ktx")

  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.6.4")
  implementation("com.google.android.gms:play-services-auth:20.4.0")
}

dependencies {
  implementation("androidx.compose.ui:ui:$jetpackCompose")
  // Tooling support (Previews, etc.)
  implementation("androidx.compose.ui:ui-tooling:$jetpackCompose")
  // Foundation (Border, Background, Box, Image, Scroll, shapes, animations, etc.)
  implementation("androidx.compose.foundation:foundation:$jetpackCompose")
  // Material Design
  implementation("androidx.compose.material:material:$jetpackCompose")
  // Material design icons
  implementation("androidx.compose.material:material-icons-core:$jetpackCompose")
  implementation("androidx.compose.material:material-icons-extended:$jetpackCompose")

  implementation("com.google.accompanist:accompanist-systemuicontroller:0.27.1")

  // Activity
  implementation("androidx.activity:activity-compose:1.6.1")
  // UI Tests
  androidTestImplementation("androidx.compose.ui:ui-test-junit4:$jetpackCompose")
}

// test
dependencies {
  testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.1")
  testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.1")
  testImplementation("org.assertj:assertj-core:3.23.1")
  testImplementation("io.strikt:strikt-core:0.34.1")

  testImplementation("com.google.auth:google-auth-library-oauth2-http:1.7.0")
  val ktor_version = "2.2.4"
  testImplementation("io.ktor:ktor-client-core:$ktor_version")
  testImplementation("io.ktor:ktor-client-cio:$ktor_version")
  testImplementation("io.ktor:ktor-serialization-kotlinx-json:$ktor_version")
  testImplementation("io.ktor:ktor-client-content-negotiation:$ktor_version")

  androidTestImplementation("androidx.test.ext:junit:1.1.5")
  androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
