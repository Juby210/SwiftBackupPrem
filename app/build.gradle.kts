@file:Suppress("UnstableApiUsage")

plugins {
    id("com.android.application")
}

android {
    namespace = "io.github.juby210.swiftbackupprem"
    compileSdk = 33
    ndkVersion = "25.1.8937393"

    defaultConfig {
        applicationId = "io.github.juby210.swiftbackupprem"
        minSdk = 27
        targetSdk = 33
        versionCode = 2
        versionName = "1.1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
    externalNativeBuild {
        cmake {
            path("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        buildConfig = false
        resValues = false
    }
}

dependencies {
    compileOnly("de.robv.android.xposed:api:82")
}
