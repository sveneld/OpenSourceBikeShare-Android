plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.bikeshare.app"
    compileSdk = 36

    val tagVersion = (project.findProperty("VERSION_NAME") as? String)?.takeIf { it.isNotBlank() } ?: "1.0.0"
    val commitCount = (project.findProperty("VERSION_CODE") as? String)?.toIntOrNull() ?: 1

    defaultConfig {
        applicationId = "com.bikeshare.app"
        minSdk = 26
        targetSdk = 35
        versionCode = commitCount
        versionName = tagVersion

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Build-time config: set in gradle.properties or env (e.g. API_BASE_URL, APP_NAME, LOGO_URL)
        val appName = (project.findProperty("APP_NAME") as? String)?.takeIf { it.isNotBlank() } ?: "BikeShare"
        val logoUrl = (project.findProperty("LOGO_URL") as? String)?.takeIf { it.isNotBlank() } ?: "https://whitebikes.info/images/logo_small.svg"
        val apiBaseUrl = (project.findProperty("API_BASE_URL") as? String)?.takeIf { it.isNotBlank() } ?: "https://whitebikes.info/api/v1/"

        val sentryDsn = (project.findProperty("SENTRY_DSN") as? String)?.takeIf { it.isNotBlank() } ?: ""

        buildConfigField("String", "API_BASE_URL", "\"$apiBaseUrl\"")
        buildConfigField("String", "APP_NAME", "\"$appName\"")
        buildConfigField("String", "LOGO_URL", "\"$logoUrl\"")
        buildConfigField("String", "SENTRY_DSN", "\"$sentryDsn\"")

        resValue("string", "app_name", appName)
    }

    signingConfigs {
        create("release") {
            val ksFile = file(System.getenv("KEYSTORE_FILE") ?: "release.keystore")
            if (ksFile.exists()) {
                storeFile = ksFile
                storePassword = System.getenv("KEYSTORE_PASSWORD") ?: ""
                keyAlias = System.getenv("KEY_ALIAS") ?: ""
                keyPassword = System.getenv("KEY_PASSWORD") ?: ""
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.findByName("release")
        }
        debug {
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    lint {
        abortOnError = true
        warningsAsErrors = false
        baseline = file("lint-baseline.xml")
    }
}

dependencies {
    // AndroidX Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)

    // Compose
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)
    debugImplementation(libs.compose.ui.tooling)

    // Navigation
    implementation(libs.navigation.compose)

    // Hilt DI
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // Networking
    implementation(libs.retrofit)
    implementation(libs.retrofit.moshi)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.moshi)
    ksp(libs.moshi.kotlin)

    // Maps (OpenStreetMap)
    implementation(libs.osmdroid)
    implementation(libs.play.services.location)

    // Camera & QR
    implementation(libs.camerax.core)
    implementation(libs.camerax.camera2)
    implementation(libs.camerax.lifecycle)
    implementation(libs.camerax.view)
    implementation(libs.mlkit.barcode)

    // Storage
    implementation(libs.datastore.preferences)
    implementation(libs.security.crypto)

    // Image Loading
    implementation(libs.coil.compose)
    implementation(libs.coil.svg)

    // Logging & Monitoring
    implementation(libs.timber)
    implementation(libs.sentry.android)
    implementation(libs.sentry.timber)
    implementation(libs.sentry.okhttp)
    implementation(libs.sentry.compose)

    // Coroutines
    implementation(libs.coroutines.android)

    // WorkManager
    implementation(libs.androidx.work.runtime.ktx)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.junit.ext)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.ui.test.junit4)
    debugImplementation(libs.compose.ui.test.manifest)
}
