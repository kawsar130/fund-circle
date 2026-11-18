# Fund Circle Proguard / R8 Keep Rules
# Preserve domain models for serialization
-keepclassmembers class com.fundcircle.app.domain.model.** { *; }

# Kotlinx Serialization
-keepattributes *Annotation*,InnerClasses
-dontnote kotlinx.serialization.SerializationKt

# Firebase
-keepattributes *Annotation*

# Coroutines
-keepclassmembernames class kotlinx.coroutines.** { *; }
