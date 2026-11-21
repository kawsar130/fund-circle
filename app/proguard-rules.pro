# ==============================================================================
# Fund Circle — ProGuard / R8 Bytecode Optimization & Obfuscation Rules
# Target: Android 8.0+ (API 26-35) with R8 Full Mode & Resource Shrinking
# ==============================================================================

# 1. Attributes Preservation
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod, SourceFile, LineNumberTable

# 2. Domain & DTO Models (Preserve for Reflection, Deserialization & Serialization)
-keep class com.fundcircle.app.domain.model.** { *; }
-keep class com.fundcircle.app.data.dto.** { *; }

# 3. Navigation Compose 2.8+ Type-Safe Route Serialization
-keep class com.fundcircle.app.presentation.navigation.** { *; }

# 4. Kotlinx Serialization
-dontnote kotlinx.serialization.SerializationKt
-keepclassmembers class * {
    @kotlinx.serialization.Serializable <fields>;
}
-keepclassmembers class * {
    @kotlinx.serialization.SerialName <fields>;
}
-keepclassmembers class * {
    *** Companion;
}
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}
-keepclassmembers class **$$serializer {
    *;
}

# 5. Firebase Firestore, Auth, Crashlytics, Messaging & Play Services
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**
-keep @com.google.firebase.firestore.IgnoreExtraProperties class * { *; }
-keepclassmembers class * {
    @com.google.firebase.firestore.PropertyName <fields>;
    @com.google.firebase.firestore.PropertyName <methods>;
    @com.google.firebase.firestore.Exclude <fields>;
    @com.google.firebase.firestore.Exclude <methods>;
}

# 6. Google Credential Manager & Identity
-keep class androidx.credentials.** { *; }
-keep class com.google.android.libraries.identity.googleid.** { *; }
-dontwarn androidx.credentials.**

# 7. Dagger Hilt
-keep class * extends dagger.hilt.internal.GeneratedComponent { *; }
-keep class * extends dagger.hilt.internal.GeneratedComponentManager { *; }
-dontwarn dagger.hilt.**

# 8. Kotlin Coroutines
-keepclassmembernames class kotlinx.coroutines.** { *; }
-keep class kotlinx.coroutines.** { *; }

# 9. Jetpack Compose Runtime Inspection & UI
-keepclassmembers class * {
    @androidx.compose.runtime.Composable *;
}
