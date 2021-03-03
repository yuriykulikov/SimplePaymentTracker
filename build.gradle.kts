// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    val kotlin = "1.4.21"

    allprojects {
        extra.apply {
            set("kotlin", kotlin)
        }
    }

    repositories {
        google()
        jcenter()
        maven { url = uri("https://plugins.gradle.org/m2/") }
    }

    dependencies {
        classpath("com.android.tools.build:gradle:7.0.0-alpha04")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin")
        classpath("com.google.gms:google-services:4.3.3")
        classpath("de.mannodermaus.gradle.plugins:android-junit5:1.7.0.0")
    }
}

allprojects {
    repositories {
        google()
        jcenter()
        maven { url = uri("https://plugins.gradle.org/m2/") }
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}