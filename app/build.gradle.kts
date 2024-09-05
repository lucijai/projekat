plugins {
    id ("com.android.application")
    id ("org.jetbrains.kotlin.android")
    id ("com.google.gms.google-services")
    id ("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")

    id ("kotlin-kapt")
    id("dagger.hilt.android.plugin")
}

android {
    namespace = "com.example.rmas"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.rmas"
        minSdk = 30
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.firebase.auth.v2101)
    implementation(libs.firebase.crashlytics.buildtools)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.service)
    implementation(libs.firebase.storage.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)


    implementation(platform(libs.androidx.compose.bom.v202411))
    implementation(libs.ui)
    implementation(libs.androidx.material)
    implementation(libs.ui.tooling.preview)
    implementation(libs.androidx.navigation.compose)


    // Compose UI dependencies
    implementation(libs.androidx.compose.ui.ui)

    // LiveData integration with Compose
    implementation(libs.androidx.runtime.livedata)


    implementation(libs.androidx.lifecycle.viewmodel.compose)
     //mape
    implementation (libs.play.services.location)
    // Google maps for compose
    implementation(libs.maps.compose.v280)

    // KTX for the Maps SDK for Android
    implementation (libs.maps.ktx)
    // KTX for the Maps SDK for Android Utility Library
    implementation (libs.maps.utils.ktx)


    implementation(libs.hilt.android)
    kapt(libs.hilt.android.compiler)

    implementation(libs.kotlinx.coroutines.play.services.v164)
    implementation (libs.play.services.location.v2101)





    implementation (libs.play.services.maps.v1810)


    implementation(libs.com.google.firebase.firebase.firestore.ktx)

    implementation(libs.android.maps.utils)
    implementation(libs.androidx.work.runtime.ktx)



    implementation(libs.androidx.ui.v160) // ili najnovija verzija
    implementation(libs.androidx.compose.material.material) // ili najnovija verzija
    implementation(libs.androidx.compose.ui.ui.tooling.preview) // ili najnovija verzija
    implementation(libs.androidx.activity.compose.v180)
    implementation(libs.coil.compose)
    implementation(libs.androidx.ui.text)
    implementation(libs.material3)
    implementation(libs.google.firebase.storage.ktx)
    implementation(libs.androidx.activity.compose.v172)

    implementation (libs.androidx.activity.compose.v161)
    implementation (libs.androidx.ui.tooling.preview.v160)



    implementation (libs.com.google.firebase.firebase.storage.ktx)


    implementation (libs.androidx.camera.core.v100)
    implementation (libs.androidx.camera.camera2.v110)
    implementation (libs.androidx.camera.lifecycle.v110)
    implementation (libs.androidx.camera.view.v100alpha29)


    implementation (libs.androidx.work.work.runtime.ktx.v280)
    implementation( libs.play.services.location)
    implementation (libs.androidx.material3.v111)

    implementation(libs.androidx.ui.v140)
    implementation(libs.androidx.material.v140)
    implementation(libs.androidx.ui.tooling.preview.v140)
    implementation(libs.androidx.navigation.compose.v260)

}
