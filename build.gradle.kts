// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false

    // Explicit versions (safe)
//    id("com.android.application") version "8.5.0" apply false
//    id("org.jetbrains.kotlin.android") version "1.9.0" apply false

    // ✅ Google Services plugin for Firebase
    id("com.google.gms.google-services") version "4.4.2" apply false
}