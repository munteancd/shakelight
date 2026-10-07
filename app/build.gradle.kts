plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.cristi.shakelight"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.cristi.shakelight"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    testImplementation(libs.junit)
}
