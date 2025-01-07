import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    kotlin("plugin.serialization") version "1.9.0" // Use the latest version of Kotlin
}

android {
    namespace = "tv.projectivy.plugin.wallpaperprovider.fanart_wallpaper"
    compileSdk = 35

    defaultConfig {
        applicationId = "tv.projectivy.plugin.wallpaperprovider.fanart_wallpaper"
        minSdk = 23
        targetSdk = 35
        versionCode = 1
        versionName = "1.01"

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