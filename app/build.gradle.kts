plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.songdesk.launcher"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.songdesk.launcher"
        minSdk = 28
        targetSdk = 35
        versionCode = 2
        versionName = "2.0"
    }

    signingConfigs {
        create("release") {
            storeFile = rootProject.file("songdesk-release.keystore")
            storePassword = "songdesk2026"
            keyAlias = "songdesk"
            keyPassword = "songdesk2026"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material.icons)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
}
