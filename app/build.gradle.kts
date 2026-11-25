import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.google.services)
    alias(libs.plugins.crashlytics)
}

// -----------------------------------------------------------------------------
// Build secrets: resolved from (1) Gradle -P properties, (2) environment
// variables (CI), (3) the git-ignored local.properties (developer machines).
// No secret value is ever committed to the repository.
// -----------------------------------------------------------------------------
val localProperties = Properties().apply {
    val localPropsFile = rootProject.file("local.properties")
    if (localPropsFile.exists()) {
        localPropsFile.inputStream().use { stream -> load(stream) }
    }
}

fun buildSecret(name: String): String? =
    (project.findProperty(name) as? String)?.takeIf { it.isNotBlank() }
        ?: System.getenv(name)?.takeIf { it.isNotBlank() }
        ?: localProperties.getProperty(name)?.takeIf { it.isNotBlank() }

val webClientId: String = buildSecret("WEB_CLIENT_ID").orEmpty()

// Release keystore: defaults to app/release-keystore.jks (decoded by release.yml),
// overridable with KEYSTORE_FILE (path relative to the app module or absolute).
val releaseKeystoreFile: File = file(buildSecret("KEYSTORE_FILE") ?: "release-keystore.jks")
val releaseKeystorePassword: String? = buildSecret("KEYSTORE_PASSWORD")
val releaseKeyAlias: String? = buildSecret("KEY_ALIAS")
val releaseKeyPassword: String? = buildSecret("KEY_PASSWORD")
val hasReleaseSigning: Boolean = releaseKeystoreFile.isFile &&
    releaseKeystorePassword != null &&
    releaseKeyAlias != null &&
    releaseKeyPassword != null

android {
    namespace = "com.fundcircle.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.fundcircle.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "WEB_CLIENT_ID", "\"$webClientId\"")
    }

    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile = releaseKeystoreFile
                storePassword = releaseKeystorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Real upload key when the keystore + credentials are present; otherwise the
            // debug key so secret-less CI (`assembleRelease` in ci.yml) can still verify R8.
            // A debug-signed build is never publishable: bundleRelease rejects it below.
            signingConfig = if (hasReleaseSigning) {
                signingConfigs.getByName("release")
            } else {
                logger.warn("Fund Circle: release signing secrets not found; signing release with the debug key.")
                signingConfigs.getByName("debug")
            }
        }
        debug {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += listOf(
            "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api"
        )
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    testOptions {
        unitTests {
            isReturnDefaultValues = true
        }
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/LICENSE.md"
            excludes += "/META-INF/LICENSE-notice.md"
        }
    }
}

// -----------------------------------------------------------------------------
// Publishable-build guard: `bundleRelease` produces the AAB uploaded to Google
// Play, so fail before any task runs if it would be debug-signed or would ship
// an empty WEB_CLIENT_ID (Google Credential Manager sign-in would be broken).
// Debug builds, unit tests and ci.yml's `assembleRelease` are unaffected.
// -----------------------------------------------------------------------------
gradle.taskGraph.whenReady {
    if (allTasks.any { it.project == project && it.name == "bundleRelease" }) {
        val problems = buildList {
            if (webClientId.isBlank()) {
                add("WEB_CLIENT_ID is empty (set the WEB_CLIENT_ID env var, -PWEB_CLIENT_ID, or local.properties).")
            }
            if (!hasReleaseSigning) {
                add(
                    "Release signing is not configured: need ${releaseKeystoreFile.path} plus " +
                        "KEYSTORE_PASSWORD, KEY_ALIAS and KEY_PASSWORD (env vars, -P, or local.properties)."
                )
            }
        }
        if (problems.isNotEmpty()) {
            throw GradleException(
                "bundleRelease refused to build a non-publishable bundle:\n - " + problems.joinToString("\n - ")
            )
        }
    }
}

dependencies {
    // Core AndroidX & Splash Screen API (API 26-35)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)

    // Jetpack Compose BOM & Material 3 UI
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // Dependency Injection (Hilt)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // Coroutines & Serialization
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.kotlinx.serialization.json)

    // Firebase (100% Client BaaS — Spark Free Tier)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.firebase.messaging.ktx)
    implementation(libs.firebase.crashlytics.ktx)
    implementation(libs.firebase.analytics.ktx)

    // Google Identity (Credential Manager)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services)
    implementation(libs.google.id)

    // Security & Encrypted SharedPreferences
    implementation(libs.androidx.security.crypto)

    // Unit & Integration Testing
    testImplementation(libs.junit)
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    testImplementation(libs.google.truth)
    testImplementation(libs.robolectric)
    testImplementation(libs.roborazzi)
    testImplementation(libs.roborazzi.compose)
    testImplementation(libs.roborazzi.junit.rule)

    // UI Instrumentation Testing
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
}
