plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
}

android {
    compileSdkVersion(29)
    defaultConfig {
        applicationId = "simple.payment.tracker"
        minSdkVersion(28)
        targetSdkVersion(29)
        versionCode = 1
        versionName = "1.0"
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

    kotlinOptions {
        jvmTarget = "1.8"
        useIR = true
    }

    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = extra["compose"] as String
    }
}

repositories {
    google()
    maven { url = uri("https://plugins.gradle.org/m2/") }
    maven { url = uri("https://oss.jfrog.org/libs-snapshot") }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:${project.extra["kotlin"]}")
    implementation("io.reactivex.rxjava2:rxandroid:2.1.1")
    implementation("io.reactivex.rxjava2:rxjava:2.2.19")
    implementation("io.reactivex.rxjava2:rxkotlin:2.4.0")
    implementation("com.squareup.moshi:moshi:1.9.2")
    implementation("com.squareup.moshi:moshi-kotlin:1.9.2")
    implementation("org.koin:koin-core:2.1.5")
    implementation("org.koin:koin-android:2.1.5")

    implementation("androidx.appcompat:appcompat:1.2.0")

    implementation("com.google.firebase:firebase-database:19.3.1")
}

// compose
dependencies {
    implementation("androidx.compose.ui:ui:${project.extra["compose"]}")
    implementation("androidx.compose.foundation:foundation-layout:${project.extra["compose"]}")
    implementation("androidx.compose.material:material:${project.extra["compose"]}")
    implementation("androidx.ui:ui-tooling:${project.extra["compose"]}")
}

// test
dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.6.2")

    androidTestImplementation("androidx.test.ext:junit:1.1.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.2.0")
}