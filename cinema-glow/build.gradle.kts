import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    kotlin("plugin.serialization") version "1.9.0" // Use the latest version of Kotlin
}

android {
    namespace = "com.danielkorgel.projectivy.plugin.cinemaglow"
    compileSdk = 35

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.danielkorgel.projectivy.plugin.cinemaglow"
        minSdk = 23
        targetSdk = 35
        versionCode = 1
        versionName = "1.01"

        vectorDrawables.useSupportLibrary = true

        // Load the API key from the apikeys.properties file
        val apiKeysFile = file("apikeys.properties")
        if (apiKeysFile.exists()) {
            val properties = Properties()
            apiKeysFile.inputStream().use { properties.load(it) }

            // Add the API key as a BuildConfig field
            val tmdbApiKey = properties["TMDB_API_KEY"] as String
            buildConfigField("String", "TMDB_API_KEY", "\"$tmdbApiKey\"")
        } else {
            println("Warning: apikeys.properties file not found!")
        }

    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.leanback:leanback:1.2.0-alpha04")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.preference:preference-ktx:1.2.1")
    implementation("com.google.code.gson:gson:2.11.0")
    implementation(project(":api"))
    implementation("com.squareup.okhttp3:okhttp:4.11.0") // For HTTP requests
    implementation("com.google.code.gson:gson:2.11.0") // JSON Parsing
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0") // Use the latest version
}