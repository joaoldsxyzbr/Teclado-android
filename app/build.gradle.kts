plugins {
    id("com.android.application")
}

val releaseKeystorePath = System.getenv("ANDROID_KEYSTORE_PATH")
val releaseKeystorePassword = System.getenv("ANDROID_KEYSTORE_PASSWORD")
val releaseRequested = gradle.startParameter.taskNames.any { it.contains("release", ignoreCase = true) }

if (releaseRequested && (releaseKeystorePath.isNullOrBlank() || releaseKeystorePassword.isNullOrBlank())) {
    throw GradleException("Release signing credentials are required")
}

android {
    namespace = "br.com.teclado"
    compileSdk = 36

    defaultConfig {
        applicationId = "br.com.teclado"
        minSdk = 26
        targetSdk = 36
        versionCode = 6
        versionName = "1.2.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            releaseKeystorePath?.takeIf { it.isNotBlank() }?.let { path ->
                releaseKeystorePassword?.takeIf { it.isNotBlank() }?.let { password ->
                    storeFile = file(path)
                    storePassword = password
                    keyAlias = "teclado"
                    keyPassword = password
                }
            }
        }
    }

    buildTypes {
        getByName("release") {
            signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation("androidx.core:core:1.17.0")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.json:json:20250517")
    androidTestImplementation("androidx.test:core:1.7.0")
    androidTestImplementation("androidx.test:runner:1.7.0")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
}
