import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    kotlin("plugin.serialization") version "1.9.0" // Use the latest version of Kotlin
}

android {
    namespace = "com.danielkorgel.projectivy.plugin.cinemaglow"
    compileSdk = 36

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.danielkorgel.projectivy.plugin.cinemaglow"
        minSdk = 23
        targetSdk = 36
        versionCode = 2
        versionName = "1.02"

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

    signingConfigs {
        create("release") {
            storeFile = file("../cinemaglow.jks")
            storePassword = "cY2tMwY4M0W8GF839MD7"
            keyAlias = "cinemaglow"
            keyPassword = "cY2tMwY4M0W8GF839MD7"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isDebuggable = false
            signingConfig = signingConfigs.getByName("release")
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
    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.leanback:leanback:1.2.0")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("com.google.android.material:material:1.13.0")
    implementation("androidx.preference:preference-ktx:1.2.1")
    implementation("com.google.code.gson:gson:2.13.2")
    implementation(project(":api"))
    implementation("com.squareup.okhttp3:okhttp:5.3.2") // For HTTP requests
    implementation("com.google.code.gson:gson:2.13.2") // JSON Parsing
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
}