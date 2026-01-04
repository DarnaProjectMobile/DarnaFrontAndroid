plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.gms.google-services")
    id("com.google.dagger.hilt.android")
    id("kotlin-kapt")
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
        multiDexEnabled = true
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
        kotlinCompilerExtensionVersion = "1.5.16"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

kapt {
    correctErrorTypes = true
    useBuildCache = true
    // Helps avoid weird stub generation issues with some setups
    includeCompileClasspath = false
}

dependencies {

    // ---------------- CORE ---------------- 
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.preference:preference-ktx:1.2.1")
    
    // ---------------- DATASTORE ----------------
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // ---------------- COMPOSE (ONE SOURCE OF TRUTH) ---------------- 
    implementation(platform("androidx.compose:compose-bom:2024.10.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    // Material 2 for SwipeToDismiss and other Material 2 components
    implementation("androidx.compose.material:material")
    // Material Icons Extended (includes all icons like CalendarToday, People, etc.)
    implementation("androidx.compose.material:material-icons-extended")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // ---------------- NAVIGATION ----------------
    implementation("androidx.navigation:navigation-compose:2.8.2")

    // ---------------- IMAGES & UI ---------------- 
    implementation("io.coil-kt:coil-compose:2.6.0")
    implementation("io.coil-kt:coil-gif:2.6.0")
    implementation("com.airbnb.android:lottie-compose:6.1.0")

    // ---------------- CAMERA ---------------- 
    implementation("androidx.camera:camera-camera2:1.3.0")
    implementation("androidx.camera:camera-lifecycle:1.3.0")
    implementation("androidx.camera:camera-view:1.3.0")
    
    // ---------------- ML KIT (Barcode Scanning) ---------------- 
    implementation("com.google.mlkit:barcode-scanning:17.3.0")

    // ---------------- MAP ----------------
    implementation("org.osmdroid:osmdroid-android:6.1.18")

    // ---------------- NETWORK ----------------
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:5.0.0-alpha.14")
    implementation("com.squareup.okhttp3:logging-interceptor:5.0.0-alpha.14")

    // ✅ Gson for Retrofit converter and custom JsonDeserializer classes
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    
    // ---------------- SOCKET.IO (Real-time Chat) ----------------
    implementation("io.socket:socket.io-client:2.1.0")

    // ---------------- MOSHI ----------------
    implementation("com.squareup.moshi:moshi:1.15.1")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.1")
    kapt("com.squareup.moshi:moshi-kotlin-codegen:1.15.1")

    // ---------------- FIREBASE ----------------
    implementation(platform("com.google.firebase:firebase-bom:33.4.0"))
    implementation("com.google.firebase:firebase-messaging")
    implementation("com.google.firebase:firebase-analytics-ktx")

    // ---------------- ✅ STRIPE ----------------
    implementation("com.stripe:stripe-android:20.39.0")

    // ---------------- HILT ----------------
    implementation("com.google.dagger:hilt-android:2.52")
    kapt("com.google.dagger:hilt-compiler:2.52")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // ---------------- BIOMETRIC ----------------
    implementation("androidx.biometric:biometric:1.1.0")

    // ---------------- MULTIDEX ----------------
    implementation("androidx.multidex:multidex:2.0.1")

    // ---------------- TESTS ----------------
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}
