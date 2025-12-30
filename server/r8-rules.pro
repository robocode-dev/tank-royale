# R8 rules for Robocode Tank Royale Server

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
-dontwarn kotlinx.serialization.**

# Note: Serialization keep rules are handled by kotlinx-serialization-common.pro in META-INF
# Our broad -keep for dev.robocode.tankroyale.** ensures serialized classes are retained

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

# Gson (used for JSON schema classes)
-keep class com.google.gson.** { *; }
-dontwarn com.google.gson.**

# Note: Gson's META-INF/proguard/gson.pro contains conditional rules that may show warnings
# These are safe to ignore as the base Gson classes are kept above

# Keep all robocode classes
-keep class dev.robocode.tankroyale.** { *; }

# Server-specific keep rules
-keep public class dev.robocode.tankroyale.server.MainKt { *; }

# Keep enum methods that may be accessed reflectively
-keepclassmembers class * extends java.lang.Enum {
    <fields>;
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

