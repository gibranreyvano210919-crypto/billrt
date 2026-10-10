plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false

    // Gunakan versi 4.4.2 untuk Google Services
    id("com.google.gms.google-services") version "4.4.2" apply false

    // Sesuaikan versi Safe Args agar sama dengan library Navigation di App Level (2.7.7)
    id("androidx.navigation.safeargs.kotlin") version "2.9.6" apply false
}
