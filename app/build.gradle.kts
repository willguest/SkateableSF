plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    alias(libs.plugins.compose.compiler)
    kotlin("kapt")
}

android {
    namespace = "com.example.skateable_sf.WT901BLE"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.skateable_sf"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = false
        }
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
    sourceSets {
        getByName("main") {
            res.srcDir("src/main/resources")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
        viewBinding = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/DEPENDENCIES"
            excludes += "META-INF/LICENSE.md"
            excludes += "META-INF/NOTICE.md"
            excludes += "META-INF/versions/9/OSGI-INF/MANIFEST.MF"
        }
    }
    buildToolsVersion = "34.0.0"
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.ic4j.agent)
    implementation(libs.ic4j.candid)
    implementation(libs.ic4j.internetidentity)
    implementation(libs.slf4j.simple)
    implementation(libs.commons.codec)
    implementation(libs.commons.lang)
    implementation(libs.httpcomponents.client5)
    implementation(libs.jackson.databind)
    implementation(libs.jackson.datatype.jdk8)
    implementation(libs.jackson.dataformat.cbor)
    implementation(libs.bouncycastle.bcprov.jdk18on)
    implementation(libs.bouncycastle.bcpkix.jdk18on)
    implementation(libs.mpandroidchart)

    implementation(libs.support.compat)
    implementation(libs.design)
    implementation(libs.support.fragment)

    implementation(libs.androidx.drawerlayout)
    implementation(libs.androidx.appcompat)

    implementation(libs.guava)
    implementation(libs.androidx.runtime)
    implementation(libs.androidx.foundation.android)
    implementation(libs.androidx.material3.android)
    testImplementation(libs.junit)
    testImplementation(libs.testng)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation(libs.testng)
    androidTestImplementation(libs.junit)
}

