plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.example.batteryalarm.domain.di"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(project(":domain-api"))
    implementation(project(":domain-impl"))
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    // Hilt 2.59.2 ships kotlin-metadata-jvm that only reads up to 2.3.0.
    ksp(libs.kotlin.metadata.jvm)
}
