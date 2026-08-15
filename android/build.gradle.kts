plugins {
    // AGP 8.9+ is required for compileSdk 36 (Play target-API requirement); needs Gradle 8.11.1+.
    id("com.android.application") version "8.9.2" apply false
    id("com.android.library") version "8.9.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.24" apply false
    id("org.jetbrains.kotlin.multiplatform") version "1.9.24" apply false
    // Must match the Kotlin version exactly.
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.24" apply false
}
