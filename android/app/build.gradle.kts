import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.serialization")
}

// Release signing: keystore.properties (git-ignored) next to this file provides the upload key.
// Missing file → release falls back to the debug key so local release builds stay installable.
val keystoreProps = Properties().apply {
    val f = rootProject.file("keystore.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}
val hasUploadKey = keystoreProps.getProperty("storeFile") != null

android {
    namespace = "app.dyrecto"
    compileSdk = 36

    defaultConfig {
        applicationId = "app.dyrecto"
        minSdk = 26          // Android 8.0 — CompanionDeviceManager available
        targetSdk = 36       // Play target-API requirement (Aug 2026: API 36)
        versionCode = 3
        versionName = "1.0.0"
    }

    signingConfigs {
        if (hasUploadKey) {
            create("upload") {
                storeFile = rootProject.file(keystoreProps.getProperty("storeFile"))
                storePassword = keystoreProps.getProperty("storePassword")
                keyAlias = keystoreProps.getProperty("keyAlias")
                keyPassword = keystoreProps.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            // Package native-lib symbol tables (MediaPipe/LiteRT/etc.) into the bundle so Play
            // can symbolicate native crash stacks.
            ndk {
                debugSymbolLevel = "SYMBOL_TABLE"
            }
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            signingConfig = if (hasUploadKey) {
                signingConfigs.getByName("upload")
            } else {
                signingConfigs.getByName("debug")
            }
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
        // BuildConfig.DEBUG gates the Developer screen out of release builds.
        buildConfig = true
    }

    // JVM unit tests touch BleLog (android.util.Log); return defaults instead of throwing.
    testOptions {
        unitTests.isReturnDefaultValues = true
    }

    // TFLite models are memory-mapped at load; APK compression would break that.
    androidResources {
        noCompress += "tflite"
    }
    composeOptions {
        // Compatible with Kotlin 1.9.24.
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    // SSHJ pulls in BouncyCastle + others; drop duplicate metadata files.
    packaging {
        resources {
            excludes += setOf(
                "META-INF/INDEX.LIST",
                "META-INF/DEPENDENCIES",
                "META-INF/LICENSE*",
                "META-INF/NOTICE*",
                "META-INF/*.kotlin_module",
                "META-INF/BCKEY.SF",
                "META-INF/BCKEY.DSA",
                "META-INF/versions/9/OSGI-INF/MANIFEST.MF",
            )
        }
    }
}

dependencies {
    // KMP shared core (Stage 1 of the iOS migration — code moves in incrementally).
    implementation(project(":shared"))

    implementation("androidx.core:core-ktx:1.13.1")
    // Provides the XML Theme.Material3.* parent used as the Activity host theme.
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.3")
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    // User-configurable alert settings, persisted across restarts.
    implementation("androidx.datastore:datastore-preferences:1.1.7")
    // Dyrecto Premium: one-time non-consumable unlock (dyrecto_unlock) via Play Billing.
    // Plain (non-ktx) artifact: billing-ktx 8.x ships Kotlin 2.1+ metadata unreadable by this
    // project's Kotlin 1.9.24; we use the callback APIs so ktx isn't needed.
    implementation("com.android.billingclient:billing:8.0.0")

    // Jetpack Compose (BOM-managed versions).
    val composeBom = platform("androidx.compose:compose-bom:2024.06.00")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    debugImplementation("androidx.compose.ui:ui-tooling")

    // ---- VERIFIED CONNECTION STACK (carried over unchanged) ----
    // SSH client with strong keyboard-interactive support + direct TCP channels.
    implementation("com.hierynomus:sshj:0.38.0")
    // SSHJ crypto backend — the FULL BouncyCastle (Android's "BC" lacks EC).
    implementation("org.bouncycastle:bcprov-jdk18on:1.78.1")
    implementation("org.bouncycastle:bcpkix-jdk18on:1.78.1")
    // SSHJ logs through SLF4J; route to Android Logcat.
    implementation("org.slf4j:slf4j-api:1.7.36")
    implementation("uk.uuid.slf4j:slf4j-android:1.7.36-0")

    // Phase 5B: on-device face and eye detection (bundled model — no Play Services required).
    implementation("com.google.mlkit:face-detection:16.1.7")
    // Coroutine-native Task<T>.await() extension for ML Kit async results.
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.1")

    // Phase 10: on-device AI perception (object detection / image embedding / segmentation).
    // Bundled .tflite models in assets/models/ — fully offline, no Play Services, CPU delegate.
    implementation("com.google.mediapipe:tasks-vision:0.10.14")
    // Phase 16.1: LiteRT interpreter for the MobileCLIP2-S0 semantic expert (import-only). Kept
    // separate from MediaPipe so we control CLIP preprocessing exactly. MediaPipe wraps its own
    // TFLite inside libmediapipe_tasks_*.so, so this standalone runtime coexists.
    implementation("com.google.ai.edge.litert:litert:1.0.1")
    // Reference session/profile persistence as JSON files (embedding vectors + subject lists
    // don't fit flat DataStore keys).
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    testImplementation("junit:junit:4.13.2")
    // Deterministic coroutine testing (runTest + StandardTestDispatcher) for the Vision pipeline.
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
    // Stub Android types (e.g. Bitmap) in pure-JVM unit tests.
    testImplementation("org.mockito:mockito-core:5.11.0")
}
