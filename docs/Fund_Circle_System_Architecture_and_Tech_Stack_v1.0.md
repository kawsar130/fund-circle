# Fund Circle — System Architecture & Tech Stack Document (v1.0)
**Document Version:** 1.0  
**Status:** Approved Engineering Blueprint  
**Target Platform:** Android 8.0+ (Min SDK 26, Target SDK 35, Compile SDK 35), Jetpack Compose (BOM 2024.10.01+), Kotlin 2.0.21+, Material 3  
**Authors:** Senior Principal Engineer & Android Architect  
**Traceability Reference:**
- Upstream Product Requirements (Archived): [Investment_Firm_App_PRD_v2.md](file:///Users/kawsar/Desktop/projects/personal/Android/fund-circle/docs/archive/Investment_Firm_App_PRD_v2.md) (v3.0)
- Upstream Functional Spec (Archived): [Investment_Firm_App_FSD_SRS_v1.md](file:///Users/kawsar/Desktop/projects/personal/Android/fund-circle/docs/archive/Investment_Firm_App_FSD_SRS_v1.md) (v2.1)
- Upstream User Stories (Archived): [Investment_Firm_App_User_Stories_Use_Cases_v1.md](file:///Users/kawsar/Desktop/projects/personal/Android/fund-circle/docs/archive/Investment_Firm_App_User_Stories_Use_Cases_v1.md) (v2.0)
- User Flowcharts: [Fund_Circle_User_Flowcharts_v1.0.md](file:///Users/kawsar/Desktop/projects/personal/Android/fund-circle/docs/Fund_Circle_User_Flowcharts_v1.0.md) (v1.1)
- Wireframes & UI Spec: [Fund_Circle_Wireframes_and_UI_Spec_v1.0.md](file:///Users/kawsar/Desktop/projects/personal/Android/fund-circle/docs/Fund_Circle_Wireframes_and_UI_Spec_v1.0.md) (v1.2)
- Design Tokens (Semantic Styling): [Design_Tokens.md](file:///Users/kawsar/Desktop/projects/personal/Android/fund-circle/docs/Design_Tokens.md)
- Branding & Style Guide (Archived): [Fund_Circle_Branding_and_Style_Guide_v1.0.md](file:///Users/kawsar/Desktop/projects/personal/Android/fund-circle/docs/archive/Fund_Circle_Branding_and_Style_Guide_v1.0.md) (v1.2)
- API Specification & Data Contracts: [Fund_Circle_API_Specification_v1.0.md](file:///Users/kawsar/Desktop/projects/personal/Android/fund-circle/docs/Fund_Circle_API_Specification_v1.0.md) (v1.0)
- Figma Interactive Prototype: [Fund Circle — Mobile App (Android M3)](https://www.figma.com/design/44Gqq8dDY62FfKf28yxXuJ?node-id=42-3&autoplay-code-preview=42-3)
- Visual Screen Gallery (2x Retina): [docs/design_preview/gallery.html](file:///Users/kawsar/Desktop/projects/personal/Android/fund-circle/docs/design_preview/gallery.html)
- Master Flowchart (FigJam): [Fund Circle — Master User Flow Architecture](https://www.figma.com/board/dxkTCpjsDBnEQp2zJRphAI)
- Local Design Prototype: [docs/design_preview/index.html](file:///Users/kawsar/Desktop/projects/personal/Android/fund-circle/docs/design_preview/index.html)

---

## Executive Summary & Architectural Invariants

This document establishes the definitive **System Architecture, Technical Stack, and Engineering Implementation Blueprint** for **Fund Circle** (Phase B). It defines how the Android client and Firebase cloud backend interact, enforces strict clean architecture boundaries, specifies the exact Gradle dependency catalog, provides production-grade security rules and database indexing, and details the CI/CD pipeline for the Google Play Store.

### The Six Core Architectural Invariants

1. **$O(1)$ Dashboard Performance:** The Home Dashboard loads in exactly **one document read** by reading atomic aggregates (`totalDeposits`, `totalExpenses`, `recordedBalance`) maintained on the parent `/funds/{fundId}` document via Firestore `FieldValue.increment()`.
2. **Whole-Integer Monetary Model:** Every financial figure is stored, validated, and computed as a 64-bit integer (`Long`, $\ge 1$). Decimals, poisha, and cents are strictly rejected (`.00` is illegal).
3. **Multi-Tenant Physical Isolation:** Data is organized in subcollections under `/funds/{fundId}/...`. Tenants cannot query or cross-read data outside their explicit circle memberships.
4. **Zero-Read Security Authorization:** Circle documents store a `memberEmails: List<String>` array, enabling security rule authorization in $O(1)$ without performing secondary document lookups (`get()`).
5. **Offline-First Resilience:** The application uses Firestore persistent offline cache with optimistic UI updates. Network state is continuously observed via Kotlin `Flow<NetworkStatus>`, providing seamless offline access with cached ledger banners.
6. **Immutable Audit Trail:** Financial records cannot be deleted (`allow delete: if false;`). Voiding a transaction preserves the original record (keeping its original positive integer amount $\ge 1$), marks `isVoid = true`, records mandatory `voidReason`, `voidedAt`, and `voidedBy`, and atomically reverses the parent aggregate balance via `FieldValue.increment(-originalAmount)`.

---

## 1. End-to-End System Architecture & Multi-Tenant Topology

### 1.1 High-Level System Topology

```
┌────────────────────────────────────────────────────────────────────────┐
│                        FUND CIRCLE SYSTEM TOPOLOGY                     │
├────────────────────────────────────────────────────────────────────────┤
│  Android Client (Kotlin 2.0.x / Jetpack Compose BOM 2024.10.x)         │
│  ├── Presentation Layer: ViewModels, StateFlow, Navigation Compose     │
│  ├── Domain Layer: Pure Kotlin Use Cases, Validation, Domain Models    │
│  └── Data Layer: Repositories, Offline Cache, Firebase Android SDK     │
├────────────────────────────────────────────────────────────────────────┤
│                                  ▲▼ TLS 1.3 (Encrypted in Transit)     │
├────────────────────────────────────────────────────────────────────────┤
│  Firebase Cloud Infrastructure (100% Serverless BaaS — Spark Free Tier)│
│  ├── Firebase Authentication (Google Credential Manager One-Tap)       │
│  ├── Cloud Firestore (Subcollection Tenancy + Atomic Aggregates)       │
│  └── Android Native Layer (On-Device CSV Ledger Engine + FileProvider) │
└────────────────────────────────────────────────────────────────────────┘
```

### 1.2 Cloud Services & Serverless Infrastructure (Spark Plan Compatible)

| Cloud / Core Service | Role in Fund Circle | SLA & Free-Tier Configuration |
| :--- | :--- | :--- |
| **Google Credential Manager** | Native One-Tap Google sign-in without legacy Play Services dependencies. | Cryptographic nonce verification, zero password management, 100% free. |
| **Cloud Firestore** | Primary distributed NoSQL document database. Subcollection multi-tenancy. | Spark plan (50k reads/day, 20k writes/day, 1GB storage). $O(1)$ atomic aggregates preserve quota. |
| **Android On-Device CSV Engine** | RFC 4180 compliant CSV ledger export generated in pure Kotlin (`LedgerCsvGenerator`). | Generates in-memory/cacheDir, shares via Android `FileProvider`. $0 hosting, works offline. |
| **Firebase Cloud Messaging** | Optional transaction alerts and administrative notices. | High-priority data payloads, Android 13+ runtime notification permissions. |
| **Firebase Crashlytics & Analytics** | Real-time crash reporting, ANR tracking, and user adoption funnel analytics. | ProGuard/R8 de-obfuscation mapping upload enabled on CI/CD, 100% free tier. |

---

### 1.3 Multi-Tenant Physical Data Architecture

Multi-tenancy is structured using **Firestore Subcollections**. Each Fund Circle is fully isolated within its own document subtree.

> [!IMPORTANT]
> **Single Source of Truth (SSOT) for Data Schemas:**  
> The exhaustive field definitions, TypeScript interfaces, Kotlin `@Serializable` DTOs, domain models, and boundary validators are maintained **exclusively** in [`Fund_Circle_API_Specification_v1.0.md`](Fund_Circle_API_Specification_v1.0.md) (Section 4 & 6). Do not duplicate or alter field definitions here.

#### High-Level Subcollection Topology
```
/funds/{fundId}                                <--- Circle Document (Root Tenant: Totals & memberEmails vector)
   ├── /members/{userId}                       <--- Member Directory Subcollection (Roles, Status & sequential memberId)
   │       └── /kyc/identity                   <--- Strictly Admin-Scoped PII Subcollection (NID & Nominee)
   ├── /deposits/{depositId}                   <--- Deposits Ledger Subcollection (Whole-integer Long, reversal audit metadata)
   ├── /expenses/{expenseId}                   <--- Expenses Ledger Subcollection (8 categories, whole-integer Long)
   └── /audit_logs/{logId}                     <--- Immutable Circle Audit Trail Subcollection

/users/{userId}                                <--- Top-Level User Profile & Session Index
   └── /invitations/{invitationId}             <--- Incoming Circle Invitations Gate
```

---

### 1.4 $O(1)$ Atomic Aggregate Data Pipeline

To eliminate client-side ledger recalculation, every transaction execution is executed as a **Firestore Atomic Batch / Transaction**:

```kotlin
// Android Client Architecture: Atomic Deposit Execution
suspend fun recordDeposit(fundId: String, deposit: DepositDto): Result<Unit> {
    val fundRef = firestore.collection("funds").document(fundId)
    val depositRef = fundRef.collection("deposits").document(deposit.depositId)

    return try {
        firestore.runBatch { batch ->
            // 1. Create the deposit entry in subcollection
            batch.set(depositRef, deposit)

            // 2. Atomically increment parent aggregate totals
            batch.update(
                fundRef,
                mapOf(
                    "totalDeposits" to FieldValue.increment(deposit.amount),
                    "recordedBalance" to FieldValue.increment(deposit.amount)
                )
            )
        }.await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```

```kotlin
// Android Client Architecture: Atomic Transaction Reversal Execution
suspend fun voidTransaction(
    fundId: String,
    collection: String, // "deposits" or "expenses"
    txId: String,
    originalAmount: Long,
    reason: String,
    adminEmail: String
): Result<Unit> {
    val fundRef = firestore.collection("funds").document(fundId)
    val txRef = fundRef.collection(collection).document(txId)

    return try {
        firestore.runTransaction { tx ->
            val snapshot = tx.get(txRef)
            val isVoid = snapshot.getBoolean("isVoid") ?: false
            if (isVoid) throw IllegalStateException("Transaction already voided.")

            // 1. Mark transaction as reversed with audit metadata
            tx.update(
                txRef,
                mapOf(
                    "isVoid" to true,
                    "voidReason" to reason,
                    "voidedBy" to adminEmail,
                    "voidedAt" to System.currentTimeMillis()
                )
            )

            // 2. Reverse parent aggregate balance atomically
            if (collection == "deposits") {
                tx.update(
                    fundRef,
                    mapOf(
                        "totalDeposits" to FieldValue.increment(-originalAmount),
                        "recordedBalance" to FieldValue.increment(-originalAmount)
                    )
                )
            } else {
                tx.update(
                    fundRef,
                    mapOf(
                        "totalExpenses" to FieldValue.increment(-originalAmount),
                        "recordedBalance" to FieldValue.increment(originalAmount)
                    )
                )
            }
        }.await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```

---

### 1.5 Offline-First Synchronization Architecture

1. **Persistent Cache Configuration:** The client enables Firestore offline persistence with a maximum cache size of **100MB** using modern `persistentDiskCacheSettings`:
```kotlin
// DatabaseModule: Firestore Persistent Disk Cache Configuration
val settings = firestoreSettings {
    setLocalCacheSettings(persistentDiskCacheSettings {
        setSizeBytes(100L * 1024 * 1024) // 100MB persistent disk cache
    })
}
firestore.firestoreSettings = settings
```
2. **Optimistic UI Updates:** UI state immediately reflects user writes. Pending writes are marked with a local syncing indicator until acknowledged by the Firebase cloud node.
3. **Connectivity Observer:** The Android client implements a real-time `NetworkMonitor` observing Android `ConnectivityManager` callbacks. When offline, an unobtrusive M3 warning banner (`"Viewing Offline Cache"`) is displayed.

---

## 2. Android Client Clean Architecture (MVI / Unidirectional Data Flow)

The Android application is structured strictly following **Clean Architecture** and **Unidirectional Data Flow (MVI)** principles.

```
com.fundcircle.app/
├── FundCircleApplication.kt                   <--- Application class, Hilt setup
├── MainActivity.kt                            <--- Single Activity entry point
│
├── core/                                      <--- Core Infrastructure & Utilities
│   ├── network/                               <--- NetworkMonitor, ConnectivityManager Flow
│   ├── security/                              <--- EncryptedSharedPreferences, Keystore
│   ├── database/                              <--- Firebase Firestore instances, converters
│   └── common/                                <--- Result, DispatchersModule, DateUtils
│
├── domain/                                    <--- Pure Kotlin Business Logic (Zero Android SDK)
│   ├── model/                                 <--- Immutable Domain Entities (Fund, Member, Transaction)
│   ├── repository/                            <--- Repository Interfaces (FundRepository, AuthRepository)
│   └── usecase/                               <--- Clean Use Cases (14 Use Cases)
│           ├── GetCircleDashboardUseCase.kt
│           ├── RecordDepositUseCase.kt
│           ├── RecordExpenseUseCase.kt
│           ├── VoidTransactionUseCase.kt
│           ├── InviteMemberUseCase.kt
│           └── SwitchCircleUseCase.kt
│
├── data/                                      <--- Data Layer Implementation
│   ├── datasource/
│   │   ├── remote/                            <--- FirestoreRemoteDataSource, AuthRemoteDataSource
│   │   └── local/                             <--- SessionLocalDataSource (EncryptedPrefs)
│   ├── mapper/                                <--- DTO <-> Domain Entity Mappers
│   └── repository/                            <--- Repository Implementations
│           ├── FundRepositoryImpl.kt
│           ├── TransactionRepositoryImpl.kt
│           └── MemberRepositoryImpl.kt
│
└── presentation/                              <--- UI Layer (Jetpack Compose & MVI)
    ├── navigation/                            <--- Type-Safe NavHost & Route Definitions
    ├── theme/                                 <--- Color, Type, Shape, Theme tokens
    ├── components/                            <--- Reusable Atoms & Molecules (Buttons, Chips)
    └── screens/                               <--- The 14 Verified Screens
            ├── auth/                          <--- AuthViewModel, AuthScreen
            ├── hub/                           <--- CircleHubViewModel, CircleHubScreen
            ├── dashboard/                     <--- DashboardViewModel, DashboardScreen
            ├── ledger/                        <--- LedgerViewModel, LedgerScreen
            ├── members/                       <--- MembersViewModel, MemberDetailSheet
            ├── profile/                       <--- ProfileViewModel, ProfileScreen
            ├── reports/                       <--- ReportsViewModel, ReportsScreen
            └── settings/                      <--- SettingsViewModel, SettingsScreen
```

---

### 2.1 The MVI Presentation Contract

Every screen in Fund Circle implements the `FundCircleViewModel<UiState, UiAction, UiEffect>` base architecture:

```kotlin
// Core MVI Interface
interface UiState
interface UiAction
interface UiEffect

abstract class BaseViewModel<S : UiState, A : UiAction, E : UiEffect>(
    initialState: S
) : ViewModel() {
    private val _uiState = MutableStateFlow(initialState)
    val uiState: StateFlow<S> = _uiState.asStateFlow()

    private val _uiEffect = Channel<E>(Channel.BUFFERED)
    val uiEffect: Flow<E> = _uiEffect.receiveAsFlow()

    abstract fun onAction(action: A)

    protected fun setState(reducer: S.() -> S) {
        _uiState.update(reducer)
    }

    protected fun sendEffect(effect: E) {
        viewModelScope.launch { _uiEffect.send(effect) }
    }
}
```

#### Example Implementation: Dashboard MVI Contract
```kotlin
// State
data class DashboardUiState(
    val isLoading: Boolean = true,
    val circleName: String = "",
    val currencyCode: String = "BDT",
    val recordedBalance: Long = 0L,
    val totalDeposits: Long = 0L,
    val totalExpenses: Long = 0L,
    val recentTransactions: List<TransactionItem> = emptyList(),
    val isOffline: Boolean = false,
    val errorMessage: String? = null
) : UiState

// Actions (User Intents)
sealed interface DashboardUiAction : UiAction {
    data object RefreshDashboard : DashboardUiAction
    data object OpenAddTransaction : DashboardUiAction
    data class ClickTransaction(val txId: String, val isExpense: Boolean) : DashboardUiAction
    data object OpenSettings : DashboardUiAction
}

// Side Effects (One-Shot Events)
sealed interface DashboardUiEffect : UiEffect {
    data class NavigateToDetail(val txId: String, val isExpense: Boolean) : DashboardUiEffect
    data object ShowAddTransactionModal : DashboardUiEffect
    data class ShowSnackbar(val message: String) : DashboardUiEffect
}
```

---

### 2.2 Type-Safe Navigation Compose 2.8+ DSL

Routing avoids fragile string paths. All navigation routes are defined as **Kotlin `@Serializable` classes/objects**:

```kotlin
package com.fundcircle.app.presentation.navigation

import kotlinx.serialization.Serializable

sealed interface ScreenRoute {
    @Serializable data object Welcome : ScreenRoute
    @Serializable data object CircleHub : ScreenRoute
    @Serializable data object CreateCircle : ScreenRoute
    @Serializable data class Dashboard(val fundId: String) : ScreenRoute
    @Serializable data class TransactionsLedger(val fundId: String) : ScreenRoute
    @Serializable data class TransactionDetail(val fundId: String, val txId: String, val isExpense: Boolean) : ScreenRoute
    @Serializable data class MembersRoster(val fundId: String) : ScreenRoute
    @Serializable data class MemberDetail(val fundId: String, val memberId: String) : ScreenRoute
    @Serializable data object ProfileEdit : ScreenRoute
    @Serializable data class Reports(val fundId: String) : ScreenRoute
    @Serializable data class Settings(val fundId: String) : ScreenRoute
}
```

---

## 3. Production Tech Stack & Version Catalog (`gradle/libs.versions.toml`)

The project uses the standard Gradle Version Catalog (`libs.versions.toml`) targeting Java 17/21, Android 15 (Compile SDK 35, Target SDK 35), Min SDK 26 (Android 8.0 Oreo — capturing 95.5%+ global device reach), Kotlin 2.0.21, and Compose BOM 2024.10.01:

```toml
[versions]
agp = "8.7.2"
kotlin = "2.0.21"
ksp = "2.0.21-1.0.28"
coreKtx = "1.15.0"
coreSplashScreen = "1.0.1"
lifecycleRuntimeKtx = "2.8.7"
activityCompose = "1.9.3"
composeBom = "2024.10.01"
navigationCompose = "2.8.3"
hilt = "2.52"
hiltNavigationCompose = "1.2.0"
coroutines = "1.9.0"
firebaseBom = "33.5.1"
credentialsManager = "1.3.0"
googleId = "1.1.1"
securityCrypto = "1.1.0-alpha06"
serialization = "1.7.3"
turbine = "1.2.0"
mockk = "1.13.13"
truth = "1.4.4"
junit5 = "5.11.3"
junit = "4.13.2"
androidxTestExtJunit = "1.2.1"
espresso = "3.6.1"
robolectric = "4.13"
roborazzi = "1.32.0"

[libraries]
androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "coreKtx" }
androidx-core-splashscreen = { group = "androidx.core", name = "core-splashscreen", version.ref = "coreSplashScreen" }
androidx-lifecycle-runtime-ktx = { group = "androidx.lifecycle", name = "lifecycle-runtime-ktx", version.ref = "lifecycleRuntimeKtx" }
androidx-lifecycle-runtime-compose = { group = "androidx.lifecycle", name = "lifecycle-runtime-compose", version.ref = "lifecycleRuntimeKtx" }
androidx-activity-compose = { group = "androidx.activity", name = "activity-compose", version.ref = "activityCompose" }

# Jetpack Compose BOM & UI
androidx-compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "composeBom" }
androidx-compose-ui = { group = "androidx.compose.ui", name = "ui" }
androidx-compose-ui-graphics = { group = "androidx.compose.ui", name = "ui-graphics" }
androidx-compose-ui-tooling = { group = "androidx.compose.ui", name = "ui-tooling" }
androidx-compose-ui-tooling-preview = { group = "androidx.compose.ui", name = "ui-tooling-preview" }
androidx-compose-material3 = { group = "androidx.compose.material3", name = "material3" }
androidx-compose-material-icons-extended = { group = "androidx.compose.material", name = "material-icons-extended" }
androidx-navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigationCompose" }

# Dependency Injection (Hilt)
hilt-android = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }
hilt-compiler = { group = "com.google.dagger", name = "hilt-compiler", version.ref = "hilt" }
androidx-hilt-navigation-compose = { group = "androidx.hilt", name = "hilt-navigation-compose", version.ref = "hiltNavigationCompose" }

# Coroutines & Serialization
kotlinx-coroutines-core = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-core", version.ref = "coroutines" }
kotlinx-coroutines-android = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-android", version.ref = "coroutines" }
kotlinx-coroutines-play-services = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-play-services", version.ref = "coroutines" }
kotlinx-serialization-json = { group = "org.jetbrains.kotlinx", name = "kotlinx-serialization-json", version.ref = "serialization" }

# Firebase BOM (100% $0 Free Tier Client BaaS)
firebase-bom = { group = "com.google.firebase", name = "firebase-bom", version.ref = "firebaseBom" }
firebase-auth-ktx = { group = "com.google.firebase", name = "firebase-auth-ktx" }
firebase-firestore-ktx = { group = "com.google.firebase", name = "firebase-firestore-ktx" }
firebase-messaging-ktx = { group = "com.google.firebase", name = "firebase-messaging-ktx" }
firebase-crashlytics-ktx = { group = "com.google.firebase", name = "firebase-crashlytics-ktx" }
firebase-analytics-ktx = { group = "com.google.firebase", name = "firebase-analytics-ktx" }

# Google Identity (Credential Manager)
androidx-credentials = { group = "androidx.credentials", name = "credentials", version.ref = "credentialsManager" }
androidx-credentials-play-services = { group = "androidx.credentials", name = "credentials-play-services-auth", version.ref = "credentialsManager" }
google-id = { group = "com.google.android.libraries.identity.googleid", name = "googleid", version.ref = "googleId" }

# Security
androidx-security-crypto = { group = "androidx.security", name = "security-crypto", version.ref = "securityCrypto" }

# Testing & Regression
junit = { group = "junit", name = "junit", version.ref = "junit" }
junit-jupiter-api = { group = "org.junit.jupiter", name = "junit-jupiter-api", version.ref = "junit5" }
junit-jupiter-engine = { group = "org.junit.jupiter", name = "junit-jupiter-engine", version.ref = "junit5" }
mockk = { group = "io.mockk", name = "mockk", version.ref = "mockk" }
turbine = { group = "app.cash.turbine", name = "turbine", version.ref = "turbine" }
google-truth = { group = "com.google.truth", name = "truth", version.ref = "truth" }
robolectric = { group = "org.robolectric", name = "robolectric", version.ref = "robolectric" }
roborazzi = { group = "io.github.takahirom.roborazzi", name = "roborazzi", version.ref = "roborazzi" }
roborazzi-compose = { group = "io.github.takahirom.roborazzi", name = "roborazzi-compose", version.ref = "roborazzi" }
roborazzi-junit-rule = { group = "io.github.takahirom.roborazzi", name = "roborazzi-junit-rule", version.ref = "roborazzi" }
androidx-compose-ui-test-junit4 = { group = "androidx.compose.ui", name = "ui-test-junit4" }
androidx-test-ext-junit = { group = "androidx.test.ext", name = "junit", version.ref = "androidxTestExtJunit" }
androidx-test-espresso-core = { group = "androidx.test.espresso", name = "espresso-core", version.ref = "espresso" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
hilt = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
google-services = { id = "com.google.gms.google-services", version = "4.4.2" }
crashlytics = { id = "com.google.firebase.crashlytics", version = "3.0.2" }
roborazzi = { id = "io.github.takahirom.roborazzi", version.ref = "roborazzi" }
```

### 3.1 App Module Platform Configuration Contract (`app/build.gradle.kts`)

```kotlin
plugins {
    alias(libs.plugins.android-application)
    alias(libs.plugins.kotlin-android)
    alias(libs.plugins.kotlin-compose)
    alias(libs.plugins.kotlin-serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.google-services)
    alias(libs.plugins.crashlytics)
    alias(libs.plugins.roborazzi) apply false
}

android {
    namespace = "com.fundcircle.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.fundcircle.app"
        minSdk = 26          // Android 8.0 Oreo (95.5%+ global device reach)
        targetSdk = 35       // Android 15 (Google Play Store compliance)
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Google One-Tap Credential Manager OAuth 2.0 Web Client ID
        // (Defaults to project resource or empty in local debug; inject via local.properties / CI)
        buildConfigField("String", "WEB_CLIENT_ID", "\"${project.findProperty("WEB_CLIENT_ID") ?: ""}\"")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
            freeCompilerArgs.addAll(
                "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
                "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api"
            )
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/LICENSE.md"
            excludes += "/META-INF/LICENSE-notice.md"
        }
    }
}

dependencies {
    // Core AndroidX & Splash Screen API (API 26-35)
    implementation(libs.androidx-core-ktx)
    implementation(libs.androidx-core-splashscreen)
    implementation(libs.androidx-lifecycle-runtime-ktx)
    implementation(libs.androidx-lifecycle-runtime-compose)
    implementation(libs.androidx-activity-compose)

    // Jetpack Compose BOM & Material 3 UI
    implementation(platform(libs.androidx-compose-bom))
    implementation(libs.androidx-compose-ui)
    implementation(libs.androidx-compose-ui-graphics)
    implementation(libs.androidx-compose-ui-tooling-preview)
    implementation(libs.androidx-compose-material3)
    implementation(libs.androidx-compose-material-icons-extended)
    implementation(libs.androidx-navigation-compose)
    debugImplementation(libs.androidx-compose-ui-tooling)

    // Dependency Injection (Hilt)
    implementation(libs.hilt-android)
    ksp(libs.hilt-compiler)
    implementation(libs.androidx-hilt-navigation-compose)

    // Coroutines & Serialization
    implementation(libs.kotlinx-coroutines-core)
    implementation(libs.kotlinx-coroutines-android)
    implementation(libs.kotlinx-coroutines-play-services)
    implementation(libs.kotlinx-serialization-json)

    // Firebase (100% Client BaaS — Spark Free Tier)
    implementation(platform(libs.firebase-bom))
    implementation(libs.firebase-auth-ktx)
    implementation(libs.firebase-firestore-ktx)
    implementation(libs.firebase-messaging-ktx)
    implementation(libs.firebase-crashlytics-ktx)
    implementation(libs.firebase-analytics-ktx)

    // Google Identity (Credential Manager with Play Services fallback for Android 8-13)
    implementation(libs.androidx-credentials)
    implementation(libs.androidx-credentials-play-services)
    implementation(libs.google-id)

    // Security & Encrypted SharedPreferences
    implementation(libs.androidx-security-crypto)

    // Unit & Integration Testing
    testImplementation(libs.junit)
    testImplementation(libs.junit-jupiter-api)
    testRuntimeOnly(libs.junit-jupiter-engine)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    testImplementation(libs.google-truth)
    testImplementation(libs.robolectric)
    testImplementation(libs.roborazzi)
    testImplementation(libs.roborazzi-compose)
    testImplementation(libs.roborazzi-junit-rule)

    // UI Instrumentation Testing
    androidTestImplementation(platform(libs.androidx-compose-bom))
    androidTestImplementation(libs.androidx-compose-ui-test-junit4)
    androidTestImplementation(libs.androidx-test-ext-junit)
    androidTestImplementation(libs.androidx-test-espresso-core)
}
```

---

### 3.2 Production Manifest & FileProvider Contract (`AndroidManifest.xml`)

Ensures rock-solid permissions, back gesture compatibility, file sharing, and deep links across Android 8.0 to Android 15:

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <!-- Essential Network & Status Permissions -->
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

    <!-- Android 13+ (API 33+) Push Notifications (FCM) -->
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />

    <application
        android:name=".FundCircleApplication"
        android:allowBackup="false"
        android:dataExtractionRules="@xml/data_extraction_rules"
        android:fullBackupContent="@xml/backup_rules"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.App.Starting"
        android:networkSecurityConfig="@xml/network_security_config"
        android:enableOnBackInvokedCallback="true">

        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:windowSoftInputMode="adjustResize"
            android:theme="@style/Theme.App.Starting">
            
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>

            <!-- Verified Android App Links (Domain Verification) -->
            <intent-filter android:autoVerify="true">
                <action android:name="android.intent.action.VIEW" />
                <category android:name="android.intent.category.DEFAULT" />
                <category android:name="android.intent.category.BROWSABLE" />
                <data android:scheme="https" android:host="fundcircle.app" android:pathPrefix="/invite" />
            </intent-filter>

            <!-- Fallback Custom Scheme (No autoVerify) -->
            <intent-filter>
                <action android:name="android.intent.action.VIEW" />
                <category android:name="android.intent.category.DEFAULT" />
                <category android:name="android.intent.category.BROWSABLE" />
                <data android:scheme="fundcircle" android:host="invite" />
            </intent-filter>
        </activity>

        <!-- On-Device CSV Export FileProvider -->
        <provider
            android:name="androidx.core.content.FileProvider"
            android:authorities="${applicationId}.fileprovider"
            android:exported="false"
            android:grantUriPermissions="true">
            <meta-data
                android:name="android.support.FILE_PROVIDER_PATHS"
                android:resource="@xml/file_paths" />
        </provider>

        <!-- Firebase Cloud Messaging Receiver Service -->
        <service
            android:name=".core.notification.FundCircleMessagingService"
            android:exported="false">
            <intent-filter>
                <action android:name="com.google.firebase.MESSAGING_EVENT" />
            </intent-filter>
        </service>
    </application>
</manifest>
```

#### FileProvider Paths XML Contract (`res/xml/file_paths.xml`)
```xml
<?xml version="1.0" encoding="utf-8"?>
<paths>
    <!-- Scoped strictly to internal cacheDir/exports (RFC 4180 CSV exports) -->
    <cache-path name="csv_exports" path="exports/" />
</paths>
```

#### Keystore Auto-Backup Exclusion Rules (`res/xml/data_extraction_rules.xml` & `res/xml/backup_rules.xml`)

> Prevents Google Cloud Auto-Backup from syncing hardware-encrypted `EncryptedSharedPreferences` ciphertext to new devices where hardware decryption keys do not exist:

```xml
<!-- res/xml/data_extraction_rules.xml (Android 12+ / API 31+) -->
<?xml version="1.0" encoding="utf-8"?>
<data-extraction-rules>
    <cloud-backup>
        <exclude domain="sharedpref" path="." />
    </cloud-backup>
    <device-transfer>
        <exclude domain="sharedpref" path="." />
    </device-transfer>
</data-extraction-rules>
```

```xml
<!-- res/xml/backup_rules.xml (Android 8.0–11 / API 26–30) -->
<?xml version="1.0" encoding="utf-8"?>
<full-backup-content>
    <exclude domain="sharedpref" path="." />
</full-backup-content>
```

#### Universal Splash Screen Theme XML Contract (`res/values/themes.xml`)

> Required by `androidx.core:core-splashscreen` to guarantee fluid branded launch with zero visual popping across Android 8.0 through Android 15:

```xml
<!-- res/values/themes.xml -->
<resources>
    <!-- Starting splash theme using androidx.core:core-splashscreen -->
    <style name="Theme.App.Starting" parent="Theme.SplashScreen">
        <!-- Splash screen background matching semantic surface color -->
        <item name="windowSplashScreenBackground">@color/splash_background</item>
        <!-- Center brand emblem icon -->
        <item name="windowSplashScreenAnimatedIcon">@drawable/ic_launcher_foreground</item>
        <!-- Post-splash transition theme immediately handed off to Compose -->
        <item name="postSplashScreenTheme">@style/Theme.FundCircle</item>
    </style>

    <!-- Base Activity theme (NoActionBar, transparent window background) -->
    <style name="Theme.FundCircle" parent="android:Theme.Material.Light.NoActionBar">
        <item name="android:windowBackground">@color/splash_background</item>
    </style>
</resources>
```

```xml
<!-- res/values/colors.xml (Light Mode Fallback) -->
<resources>
    <color name="splash_background">#F8F9FA</color>
    <color name="splash_primary">#006D4D</color>
</resources>
```

```xml
<!-- res/values-night/colors.xml (Dark Mode Fallback) -->
<resources>
    <color name="splash_background">#111418</color>
    <color name="splash_primary">#006D4D</color>
</resources>
```

#### Canonical Application Lifecycle Contract (`FundCircleApplication.kt`)

```kotlin
package com.fundcircle.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestoreSettings
import com.google.firebase.firestore.persistentDiskCacheSettings
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class FundCircleApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        initNotificationChannels()
        initFirestorePersistence()
    }

    private fun initNotificationChannels() {
        // Android 8.0+ (API 26+) requires NotificationChannel before posting notifications
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID_ALERTS,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = getString(R.string.notification_channel_desc)
                enableLights(true)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun initFirestorePersistence() {
        val firestore = FirebaseFirestore.getInstance()
        val settings = firestoreSettings {
            setLocalCacheSettings(persistentDiskCacheSettings {
                setSizeBytes(100L * 1024 * 1024) // 100MB persistent disk cache
            })
        }
        firestore.firestoreSettings = settings
    }

    companion object {
        const val CHANNEL_ID_ALERTS = "fc_circle_updates"
    }
}
```

#### Canonical Single-Activity & Intent Contract (`MainActivity.kt`)

```kotlin
package com.fundcircle.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.fundcircle.app.presentation.navigation.FundCircleNavHost
import com.fundcircle.app.presentation.theme.FundCircleTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // 1. Initialize modern splash screen (API 26-35) before super.onCreate()
        installSplashScreen()
        
        // 2. Enable adaptive edge-to-edge system bars (API 26-35)
        enableEdgeToEdge()
        
        super.onCreate(savedInstanceState)

        // 3. Handle incoming deep links (App Links & custom schemes)
        val deepLinkUri = intent?.data

        setContent {
            FundCircleTheme {
                FundCircleNavHost(initialDeepLinkUri = deepLinkUri)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Ensure active intent reference is updated when resumed with deep links (singleTop)
        setIntent(intent)
    }
}
```

---

### 3.3 Universal Backwards Compatibility & OS Support Matrix (API 26 through API 35)

| Technical Dimension | Android 8.0–11 (API 26–30) | Android 12–13 (API 31–33) | Android 14–15 (API 34–35) | Architectural Solution |
| :--- | :--- | :--- | :--- | :--- |
| **Edge-to-Edge & System Bars** | Not default; black/tinted bars by default | Opt-in via WindowCompat | **Enforced by default on API 35** | Call `enableEdgeToEdge()` in `MainActivity.onCreate()` before `setContent`. Consume `innerPadding` in `Scaffold`. |
| **System Bar Icon Legibility** | Light icons default | AppearanceController available | AppearanceController mandatory | Set `isAppearanceLightStatusBars = !darkTheme` and `isNavigationBarContrastEnforced = false` in `FundCircleTheme`. |
| **IME Keyboard Insets** | `adjustResize` handles window resize | WindowInsets.ime available | Fluid animated IME insets | Add `Modifier.imePadding()` to bottom sheet text inputs. |
| **Push Notifications** | Granted at install; Channel mandatory | Granted at install; Channel mandatory | **`POST_NOTIFICATIONS` runtime permission mandatory** | Initialize `NotificationChannel` in `Application.onCreate()`. Request `POST_NOTIFICATIONS` at runtime on API 33+. |
| **Notification PendingIntent** | `FLAG_UPDATE_CURRENT` | **`FLAG_IMMUTABLE` mandatory (API 31+)** | **`FLAG_IMMUTABLE` mandatory** | Always pass `PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE` when building notification intents. |
| **Broadcast Receivers** | Standard context registration | Standard context registration | **`RECEIVER_NOT_EXPORTED` mandatory** | Pass `ContextCompat.RECEIVER_NOT_EXPORTED` on all dynamic broadcast registrations. |
| **Google Sign-In / Credential Manager** | Play Services One-Tap fallback | Play Services One-Tap fallback | Native system Credential Manager bottom sheet | Handled transparently by `androidx.credentials:credentials-play-services-auth`. Call `clearCredentialState` on sign-out. |
| **Keystore / Session Encryption** | Software/Hardware Keystore (API 26 OEM bugs) | Strongbox / Hardware Keystore | Strongbox / Hardware Keystore | In `SessionLocalDataSource`, catch `GeneralSecurityException`, wipe corrupted prefs and re-initialize. Exclude from cloud backups. |
| **Predictive Back Navigation** | Standard back stack pop | Standard back stack pop | Native fluid predictive back gesture animation | Declare `android:enableOnBackInvokedCallback="true"` in Manifest; use Compose Navigation 2.8+ back handling. |
| **App Startup & Splash** | Blank white screen without compat lib | Native System Splash Screen | Native System Splash Screen | Use `androidx.core:core-splashscreen` with `installSplashScreen()` in `MainActivity` for uniform branded launch. |
| **Material 3 Dynamic Color** | Not supported (Crashes if unchecked) | Supported (API 31+) | Supported (API 34+) | **Hardcode `dynamicColor = false`** in `FundCircleTheme` to protect Emerald `#006D4D` and Red `#BA1A1A` financial semantics. |

---

## 4. Firestore Security Rules & Composite Indexing

### 4.1 Production Security Rules (`firestore.rules`)

The deployed rules live in [`firestore.rules`](../firestore.rules) at the repository root. **That file is the source of truth**; this section summarises the model it enforces and is not a copy. The suite in [`firestore-tests/`](../firestore-tests/) (see §6.2) runs the app's real write paths and the main attacks against the Firestore emulator, and CI runs it on every pull request.

**Identity**
- A member's identity is their **lower-cased** Google account email (`request.auth.token.email.lower()`). Invited members are stored before they first sign in, so their member document ID is not their Auth UID. Every self-service check matches on email.
- Membership is an $O(1)$ zero-read check: the caller's email is in `/funds/{fundId}.memberEmails`.
- The admin matches by `adminId == request.auth.uid` (the circle creator) **or** by `adminEmail` (after an ownership transfer to an invited member whose UID was never known).
- Member emails must be stored lower-case. An invite with `Bob@x.com` is rejected.

**`/funds/{fundId}` (aggregate document)**
- **Create:** the fund ID matches, only known keys are present, the currency is `BDT | USD | EUR`, the caller is `adminId`/`adminEmail`, `memberEmails == [callerEmail]`, `memberCount == 1`, all totals are `0` and `isLocked == false`.
- **Update:** caller is a member, and `fundId`, `currency` and `createdAt` never change. The write must also be exactly one of:
  - *Deposit record:* only `totalDeposits`, `recordedBalance`, `updatedAt` and `lastTxId` change, both totals rise by the same delta ≥ 1, the circle is unlocked, and `lastTxId` names a deposit that does not exist before this write, exists after it, and has `amount == delta`.
  - *Expense record (admin):* the same shape on `totalExpenses`, with the balance falling by the delta.
  - *Deposit / expense reversal (admin):* totals move back by exactly the named ledger document's original `amount`, and that document flips from `isVoid == false` to `isVoid == true` in the same write.
  - *Governance (admin):* only `name`, `description`, `isLocked`, `memberCount`, `memberEmails`, `adminId`, `adminEmail` and `updatedAt` change, and the new `adminEmail` must already be in `memberEmails`.
- **Delete:** never.

**Ledger: `/funds/{fundId}/deposits/{id}` and `/expenses/{id}`**
- **Create:** caller is a member (an admin for expenses), the circle is unlocked, the ID field matches the document ID, `amount` is an `int ≥ 1`, the date is not in the future (5 minutes of clock skew allowed), the document is not pre-voided, and the parent's `lastTxId` and total move by exactly `amount` in the same batch.
- **Update:** the Transaction Reversal Protocol only. The admin may change `isVoid`, `voidReason` (≥ 5 chars), `voidedAt` and `voidedBy` (must be the caller's email), only from `false` to `true`, and only while the parent total falls by the original `amount`. Reversals are one-way: a document cannot be un-reversed or reversed twice.
- **Delete:** never.

These two rules are inseparable by design. The ledger rule checks the parent with `getAfter`, and the parent rule checks the ledger document named by `lastTxId`. So a total cannot move without its ledger document, and a ledger document cannot be written without its total.

**Members: `/funds/{fundId}/members/{memberDocId}`**
- **Create:** either the creator's own `ADMIN` document, written in the same batch as a new fund, or an admin invite of a `MEMBER` whose email is added to `memberEmails` in the same batch. `totalContributed` must be `0` and the email must be lower-case.
- **Update:** the admin may change `role`, `status`, `name`, `phone`, `lastActiveAt` and `updatedAt`, but their own record must stay `ACTIVE` until ownership is transferred. A member may edit their own `name`/`phone` or set their own status to `LEFT`. The admin cannot leave.
- **Delete:** never.
- **KYC** (`.../kyc/identity`): readable and writable by the admin or the member themself. Only the admin may set `verifiedByAdmin = true`.

**Other paths**
- `/funds/{fundId}/audit_logs/{id}`: members may append entries under their own `actorEmail`. Updates and deletes are denied.
- `/users/{uid}` and `/users/{uid}/invitations/{id}`: owner only.
- Every other path is denied by default.

---

### 4.2 Composite Indexes (`firestore.indexes.json`)

```json
{
  "indexes": [
    {
      "collectionGroup": "deposits",
      "queryScope": "COLLECTION",
      "fields": [
        { "fieldPath": "depositDate", "order": "DESCENDING" },
        { "fieldPath": "__name__", "order": "DESCENDING" }
      ]
    },
    {
      "collectionGroup": "deposits",
      "queryScope": "COLLECTION",
      "fields": [
        { "fieldPath": "memberId", "order": "ASCENDING" },
        { "fieldPath": "depositDate", "order": "DESCENDING" }
      ]
    },
    {
      "collectionGroup": "expenses",
      "queryScope": "COLLECTION",
      "fields": [
        { "fieldPath": "category", "order": "ASCENDING" },
        { "fieldPath": "expenseDate", "order": "DESCENDING" }
      ]
    },
    {
      "collectionGroup": "expenses",
      "queryScope": "COLLECTION",
      "fields": [
        { "fieldPath": "expenseDate", "order": "DESCENDING" },
        { "fieldPath": "__name__", "order": "DESCENDING" }
      ]
    },
    {
      "collectionGroup": "members",
      "queryScope": "COLLECTION",
      "fields": [
        { "fieldPath": "status", "order": "ASCENDING" },
        { "fieldPath": "joinedAt", "order": "DESCENDING" }
      ]
    }
  ],
  "fieldOverrides": []
}
```

---

## 5. Security Architecture & Google Play Store Hardening

### 5.1 Android Keystore & Session Encryption
- User session tokens and active circle IDs are stored in `EncryptedSharedPreferences` backed by the **Android Keystore System** using AES-256-GCM.
- PII and NID values are never written to disk in unencrypted local databases.

### 5.2 Network Security Configuration (`res/xml/network_security_config.xml`)
```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <!-- Production Base Config: Cleartext HTTP traffic is strictly prohibited across all domains -->
    <base-config cleartextTrafficPermitted="false">
        <trust-anchors>
            <certificates src="system" />
        </trust-anchors>
    </base-config>
</network-security-config>
```

The release config above allows no cleartext at all. Debug builds replace it with `app/src/debug/res/xml/network_security_config.xml`, which also trusts user-installed CAs (`debug-overrides`) and adds one block for the local Firebase Emulator Suite:

```xml
<domain-config cleartextTrafficPermitted="true">
    <domain includeSubdomains="false">10.0.2.2</domain>
    <domain includeSubdomains="false">localhost</domain>
    <domain includeSubdomains="false">127.0.0.1</domain>
</domain-config>
```

### 5.3 ProGuard / R8 Optimization Rules (`proguard-rules.pro`)
```proguard
# Kotlin Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.SerializationKt
-keepclassmembers class * {
    @kotlinx.serialization.Serializable <fields>;
}
-keepclassmembers class * {
    @kotlinx.serialization.SerialName <fields>;
}

# Firebase Coroutines & DTO Models
-keep class com.google.firebase.** { *; }
-keep class com.fundcircle.app.data.dto.** { *; }

# Navigation Compose 2.8+ Type-Safe Route Serialization
-keep class com.fundcircle.app.presentation.navigation.** { *; }

# Dagger Hilt
-keep class * extends dagger.hilt.internal.GeneratedComponent { *; }
-keep class * extends dagger.hilt.internal.GeneratedComponentManager { *; }
```

### 5.4 Google Play Store Data Safety Declarations

| Data Category | Data Type | Collected / Shared | Purpose | Encrypted in Transit |
| :--- | :--- | :--- | :--- | :---: |
| **Personal Info** | Name, Email address | Collected (Account management) | App functionality | **Yes (TLS 1.3)** |
| **Financial Info** | User deposits, expenses, pool balances | Collected (User-generated ledger) | App functionality | **Yes (TLS 1.3)** |
| **Device / IDs** | User UID, Firebase Installation ID | Collected (Analytics/Diagnostics) | Crashlytics & Auth | **Yes (TLS 1.3)** |

*Zero third-party data broker sharing. Zero data tracking across other apps.*

---

## 6. Automated Testing Strategy & Quality Gates

The engineering workflow enforces a **70 / 20 / 10** testing pyramid:

```
        ▲
       / \         10% UI & Screenshot Tests (Roborazzi / Compose Test)
      /───\
     /     \       20% Integration Tests (Robolectric + Firebase Emulator)
    /───────\
   /         \     70% Unit Tests (JUnit 5 + MockK + Turbine + Truth)
  ─────────────
```

### 6.1 Unit Testing Suite (Domain & Presentation)
- **ViewModel MVI State Tests:** Evaluated using CashApp `Turbine` to verify state emissions and one-shot effects.
- **Use Case Business Logic Tests:** Validating whole-integer constraints, future date rejections, and reversal reason enforcement.
- **Formatting Utilities Tests:** Validating `CurrencyText` whole-integer output (`৳ 1,450,000`, zero poisha) and `PrivacyMaskUtils` NID masking (`•••• •••• 9821`).

### 6.2 Firestore Security Rules Suite (Firebase Emulator)
- Lives in [`firestore-tests/`](../firestore-tests/): a Node package (`@firebase/rules-unit-testing`, `node:test`) that loads the root `firestore.rules` into the Firestore emulator (`127.0.0.1:8089`, configured in the root `firebase.json`).
- **ALLOW cases** replay the real client write paths from `data/datasource/remote/*Impl.kt`: create circle batch, invite, member deposit, admin expense, deposit and expense reversal in a transaction, lock toggle, ownership transfer, leave, deactivate, and own KYC read/write.
- **DENY cases** cover the attacks: balance, admin or `memberEmails` edits, totals moved without a ledger document or with a mismatched amount, decimal or future-dated amounts, writes to a locked circle, double or undone reversals, edits and deletes of ledger, member or fund documents, currency changes, stranger reads and writes, self-verified KYC, and uppercase invite emails.
- Run locally (Node 20+ and Java 11+ on `PATH`):
  ```bash
  cd firestore-tests && npm ci && npm run test:emulator
  ```
- CI runs the same suite in the `firestore-rules-tests` job of `.github/workflows/ci.yml`. **If you change a client write path or `firestore.rules`, update the matching case in the same commit.**

### 6.3 Automated Screenshot Regression Tests (Roborazzi)
- Headless Compose screenshot tests for all 14 screens in both **Light** and **Dark** mode.
- Pull Requests fail automatically if visual diffs exceed a $0.1\%$ threshold.

---

## 7. CI/CD Pipeline & Play Store Release Engineering

Fund Circle employs **GitHub Actions** and **Fastlane** for continuous integration and automated Google Play deployment.

### 7.1 Pull Request Quality Gate (`.github/workflows/ci.yml`)

> The live workflow also runs the Firestore rules suite (`firestore-rules-tests` job, §6.2). The YAML below is the original sketch of the Gradle gate; the workflow file is authoritative.

```yaml
name: Fund Circle PR Quality Gate

on:
  pull_request:
    branches: [ main, develop ]

jobs:
  verify:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
          cache: gradle

      - name: Run Android Lint
        run: ./gradlew lintDebug

      - name: Run Unit Tests & Coverage
        run: ./gradlew testDebugUnitTest

      - name: Verify Screenshot Regression Tests
        run: ./gradlew verifyRoborazziDebug
```

### 7.2 Automated Release Pipeline (`.github/workflows/release.yml`)

```yaml
name: Google Play Store Release Pipeline

on:
  push:
    tags:
      - 'v*'

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Decode Keystore
        run: |
          echo "${{ secrets.RELEASE_KEYSTORE_BASE64 }}" | base64 --decode > app/release-keystore.jks

      - name: Build Android App Bundle (AAB)
        env:
          KEYSTORE_PASSWORD: ${{ secrets.KEYSTORE_PASSWORD }}
          KEY_ALIAS: ${{ secrets.KEY_ALIAS }}
          KEY_PASSWORD: ${{ secrets.KEY_PASSWORD }}
        run: ./gradlew bundleRelease

      - name: Deploy to Google Play Closed Testing Track
        uses: r0adkll/upload-google-play@v1
        with:
          serviceAccountJsonPlainText: ${{ secrets.PLAY_CONSOLE_SERVICE_ACCOUNT_JSON }}
          packageName: com.fundcircle.app
          releaseFiles: app/build/outputs/bundle/release/app-release.aab
          track: internal
          status: completed
```

---

## 8. Master Documentation & Architectural Sign-Off

With the completion of this document, the entire architectural and design specification suite is 100% complete and synchronized:

1. **Upstream Product Requirements (Archived):** [Investment_Firm_App_PRD_v2.md](file:///Users/kawsar/Desktop/projects/personal/Android/fund-circle/docs/archive/Investment_Firm_App_PRD_v2.md)
2. **Upstream Functional Spec (Archived):** [Investment_Firm_App_FSD_SRS_v1.md](file:///Users/kawsar/Desktop/projects/personal/Android/fund-circle/docs/archive/Investment_Firm_App_FSD_SRS_v1.md)
3. **Upstream User Stories (Archived):** [Investment_Firm_App_User_Stories_Use_Cases_v1.md](file:///Users/kawsar/Desktop/projects/personal/Android/fund-circle/docs/archive/Investment_Firm_App_User_Stories_Use_Cases_v1.md)
4. **User Flowcharts & Interaction Architecture (v1.1):** [Fund_Circle_User_Flowcharts_v1.0.md](file:///Users/kawsar/Desktop/projects/personal/Android/fund-circle/docs/Fund_Circle_User_Flowcharts_v1.0.md)
5. **Wireframes & Jetpack Compose UI Spec (v1.2):** [Fund_Circle_Wireframes_and_UI_Spec_v1.0.md](file:///Users/kawsar/Desktop/projects/personal/Android/fund-circle/docs/Fund_Circle_Wireframes_and_UI_Spec_v1.0.md)
6. **Design Tokens (Semantic Styling):** [Design_Tokens.md](file:///Users/kawsar/Desktop/projects/personal/Android/fund-circle/docs/Design_Tokens.md)
   *(Extended Brand Guide archived in [archive/Fund_Circle_Branding_and_Style_Guide_v1.0.md](file:///Users/kawsar/Desktop/projects/personal/Android/fund-circle/docs/archive/Fund_Circle_Branding_and_Style_Guide_v1.0.md))*
7. **System Architecture & Tech Stack (v1.0):** [Fund_Circle_System_Architecture_and_Tech_Stack_v1.0.md](file:///Users/kawsar/Desktop/projects/personal/Android/fund-circle/docs/Fund_Circle_System_Architecture_and_Tech_Stack_v1.0.md)
8. **API Specification & Data Contracts (v1.0):** [Fund_Circle_API_Specification_v1.0.md](file:///Users/kawsar/Desktop/projects/personal/Android/fund-circle/docs/Fund_Circle_API_Specification_v1.0.md)
9. **Interactive Design Prototype:** [docs/design_preview/index.html](file:///Users/kawsar/Desktop/projects/personal/Android/fund-circle/docs/design_preview/index.html)
10. **Figma Interactive Prototype:** [Fund Circle — Mobile App (Android M3)](https://www.figma.com/design/44Gqq8dDY62FfKf28yxXuJ?node-id=42-3&autoplay-code-preview=42-3)
11. **Live FigJam Flowchart Board:** [Fund Circle — Master User Flow Architecture](https://www.figma.com/board/dxkTCpjsDBnEQp2zJRphAI)

---

### Phase B Authorization

The project has achieved complete technical, architectural, and visual closure. We are completely prepared to begin **Phase B: Android Project Scaffolding & Engineering Setup** at your command.
