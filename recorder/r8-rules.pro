# R8 rules for Robocode Tank Royale Recorder

# Don't optimize or obfuscate (keep code readable for debugging)
-dontoptimize
-dontobfuscate

# Suppress warnings for Java 11+ classes not present in Java 11
-dontwarn java.lang.foreign.**
-dontwarn java.lang.classfile.**

# Keep important attributes for reflection and debugging
-keepattributes AnnotationDefault,*Annotation*,Signature,InnerClasses,EnclosingMethod,RuntimeVisibleAnnotations,RuntimeInvisibleAnnotations,RuntimeVisibleParameterAnnotations,RuntimeInvisibleParameterAnnotations,RuntimeVisibleTypeAnnotations,RuntimeInvisibleTypeAnnotations,SourceFile,LineNumberTable

# Kotlin runtime
-keep class kotlin.** { *; }
-keep class kotlin.jvm.** { *; }
-keep class kotlin.jvm.internal.** { *; }
-dontwarn kotlin.**

# Kotlinx Serialization
-keep class kotlinx.serialization.** { *; }
-keep class **$$serializer { *; }
-keepclassmembers class ** {
    *** Companion;
}
-keepclasseswithmembers class ** {
    static ** serializer(...);
}
-keepclasseswithmembers class ** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keepclassmembers class kotlinx.serialization.** {
    *;
}
-dontwarn kotlinx.serialization.**

# Clikt CLI library
-keep class com.github.ajalt.clikt.** { *; }
-dontwarn com.github.ajalt.clikt.**

# Mordant terminal library (used by Clikt)
-keep class com.github.ajalt.mordant.** { *; }
-dontwarn com.github.ajalt.mordant.**

# SLF4J logging
-keep class org.slf4j.** { *; }
-dontwarn org.slf4j.**

# Java WebSocket
-keep class org.java_websocket.** { *; }
-dontwarn org.java_websocket.**

# Keep all robocode classes
-keep class dev.robocode.tankroyale.** { *; }

# Recorder-specific keep rules
-keep public class dev.robocode.tankroyale.recorder.cli.RecorderKt { *; }
-keep public class dev.robocode.tankroyale.recorder.MainKt { *; }

# Keep enum methods that may be accessed reflectively
-keepclassmembers class * extends java.lang.Enum {
    <fields>;
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

