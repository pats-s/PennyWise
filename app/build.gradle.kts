plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")
    kotlin("kapt")
}

android {
    namespace = "com.example.pennywise"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.pennywise"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    viewBinding {
        enable = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.room.common)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)



   /*
    implementation(libs.converter.gson.v290)

    implementation(libs.androidx.lifecycle.livedata.ktx)*/

    kapt ("androidx.room:room-compiler:2.6.1")
    implementation (libs.androidx.room.runtime)
    annotationProcessor (libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)

    implementation(libs.firebase.firestore)
    implementation(libs.firebase.auth) // If using Firebase Authentication
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)

    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.navigation.fragment.ktx.v273)
    implementation(libs.androidx.navigation.ui.ktx.v273)

    implementation(libs.material.v190)
    implementation(libs.retrofit)
    implementation(libs.converter.moshi)

    implementation(libs.androidx.lifecycle.livedata.ktx)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.play.services.auth)
    implementation (libs.androidx.lifecycle.viewmodel.ktx) // Replace version with the actual one in libs.androidx.lifecycle.viewmodel.ktx
    implementation (libs.glide) // Replace version with the actual one in libs.glide

    implementation (libs.mpandroidchart)

    kapt ("com.github.bumptech.glide:compiler:4.15.0")



}