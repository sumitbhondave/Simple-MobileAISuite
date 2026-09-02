# Standard Android optimizations
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontpreverify
-verbose
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*

# Attributes required for reflection and MediaPipe/Protobuf
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# Hilt rules (usually bundled, but good to have)
-keep class dagger.hilt.** { *; }
-keep interface dagger.hilt.** { *; }

# MediaPipe & GenAI (Important for AI features)
-keep class com.google.mediapipe.** { *; }
-keep class com.google.protobuf.** { *; }
-dontwarn com.google.mediapipe.**
-dontwarn com.google.protobuf.**
-keep class com.google.ai.client.generativeai.** { *; }

# Keep native methods and their class members
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep the data sources that interface with MediaPipe to preserve stack frames
-keep class com.sumit.simplemobileaisuite.data.datasource.local.** { *; }

# Compose rules
-keep class androidx.compose.** { *; }

# Keep your own models for serialization
-keep class com.sumit.simplemobileaisuite.domain.model.** { *; }
