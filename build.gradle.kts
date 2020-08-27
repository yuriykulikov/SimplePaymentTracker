// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    val kotlin = "1.4.0"
    val compose = "0.1.0-dev17"

    allprojects {
        extra.apply {
            set("kotlin", kotlin)
            set("compose", compose)
        }
    }

    repositories {
        google()
        jcenter()
    }

    dependencies {
        classpath("com.android.tools.build:gradle:4.2.0-alpha07")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin")
        classpath("com.google.gms:google-services:4.3.3")
    }
}

allprojects {
    repositories {
        google()
        jcenter()
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}