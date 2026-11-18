# Fund Circle Proguard / R8 Keep Rules

# Preserve domain models & DTOs for serialization and reflection
-keep class com.fundcircle.app.domain.model.** { *; }
-keep class com.fundcircle.app.data.dto.** { *; }
-keep class com.fundcircle.app.presentation.navigation.** { *; }

# Kotlinx Serialization
-keepattributes *Annotation*,InnerClasses
-dontnote kotlinx.serialization.SerializationKt

# Firebase
-keepattributes *Annotation*

# Coroutines
-keepclassmembernames class kotlinx.coroutines.** { *; }
