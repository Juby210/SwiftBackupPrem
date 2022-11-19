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
        versionCode = 1
        versionName = "1.0.0"
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
}

dependencies {
    compileOnly("de.robv.android.xposed:api:82")
}
