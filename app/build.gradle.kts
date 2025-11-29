plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.sim.darna"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.sim.darna"
        minSdk = 24
        targetSdk = 36
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.7.5"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Core + Lifecycle
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.activity:activity-compose:1.8.2")


    // Compose + Material 3
    //implementation("androidx.activity:activity-compose:1.11.0")
    // Compatible Activity + Compose libraries for AGP 8.7.1
    implementation("androidx.activity:activity:1.8.2")
    implementation("androidx.activity:activity-ktx:1.8.2")
    implementation("androidx.activity:activity-compose:1.8.2")


    // Compose + Material 3
    implementation(platform("androidx.compose:compose-bom:2024.10.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.activity:activity-compose:1.8.2")


    // Navigation Compose
    implementation("androidx.navigation:navigation-compose:2.8.2")
    
    // Pager for swipeable images
    implementation("androidx.compose.foundation:foundation:1.6.0")

    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.lifecycle)

    implementation ("androidx.camera:camera-camera2:1.3.0")
    implementation ("androidx.camera:camera-lifecycle:1.3.0")
    implementation ("androidx.camera:camera-view:1.3.0")


    // Tests
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.10.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.material:material:1.6.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

// Retrofit core
    implementation("com.squareup.retrofit2:retrofit:2.9.0")

// Retrofit with Gson converter
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

// OkHttp (for network logging and requests)
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")



    implementation("io.coil-kt:coil-compose:2.4.0")
    implementation("io.coil-kt:coil:2.4.0")
    implementation("io.coil-kt:coil-gif:2.4.0")
    
    // Lottie for high-quality vector animations (optional - best quality)
    implementation("com.airbnb.android:lottie-compose:6.1.0")

    implementation("org.osmdroid:osmdroid-android:6.1.18")
    implementation("androidx.preference:preference-ktx:1.2.1")


    // ✅ Retrofit + OkHttp + Kotlin Serialization
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:5.0.0-alpha.14")
    implementation("com.squareup.okhttp3:logging-interceptor:5.0.0-alpha.14")

// ✅ Kotlin Serialization (JSON)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

// ✅ Converter for Retrofit to use Kotlinx Serialization
    implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:1.0.0")

    // Firebase Cloud Messaging + Analytics
    implementation(platform("com.google.firebase:firebase-bom:33.4.0"))
    implementation("com.google.firebase:firebase-messaging")
    implementation("com.google.firebase:firebase-analytics-ktx")
}
