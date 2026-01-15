import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.fh.anitrack"
    compileSdk {
        version = release(36)
    }

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.fh.anitrack"
        minSdk = 30
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        val properties = Properties()
        val propertiesFile = project.rootProject.file("local.properties")
        if (propertiesFile.exists()) {
            properties.load(propertiesFile.inputStream())
        }

        buildConfigField("String", "ANILIST_CLIENT_ID", "\"${properties.getProperty("anilist.client.id")}\"")
        buildConfigField("String", "ANILIST_REDIRECT_URI", "\"${properties.getProperty("anilist.redirect.uri")}\"")
        buildConfigField("String", "ANILIST_AUTH_URL", "\"${properties.getProperty("anilist.auth.uri")}\"")
        buildConfigField("String", "ANILIST_API_URL", "\"${properties.getProperty("anilist.api.uri")}\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.cardview)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation("com.github.bumptech.glide:glide:4.14.2")
    implementation("io.noties.markwon:core:4.6.2")
    implementation("io.noties.markwon:ext-strikethrough:4.6.2")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation(libs.security.crypto)
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)
    implementation(libs.retrofit)
    implementation(libs.converter.scalars)
    implementation(libs.converter.gson)
}