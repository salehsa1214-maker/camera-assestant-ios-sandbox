plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.library")
    id("org.jetbrains.kotlin.plugin.serialization")
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "17"
            }
        }
    }

    // Stage 4: iOS targets. On non-macOS hosts Kotlin disables these with a warning, so the
    // Android build stays green from Windows; the framework compiles on a macOS host.
    listOf(
        iosArm64(),
        iosSimulatorArm64(),
        iosX64(),
    ).forEach { target ->
        target.binaries.framework {
            baseName = "DyrectoShared"
            isStatic = true
            // Exported so Swift sees Flow/StateFlow types instead of opaque stubs.
            export("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
        }
    }

    sourceSets {
        commonMain.dependencies {
            // api (not implementation): the iOS framework must export coroutines types.
            api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
        // JVM tests moved verbatim from :app (JUnit4 style) run here.
        getByName("androidUnitTest").dependencies {
            implementation("junit:junit:4.13.2")
        }
    }
}

android {
    namespace = "app.dyrecto.shared"
    compileSdk = 36

    defaultConfig {
        minSdk = 26
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
