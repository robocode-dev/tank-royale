# Common ProGuard configuration for all Tank Royale modules
# This file is included by all module-specific proguard-rules.pro files

# Suppress warnings but keep errors visible
-dontnote **
-ignorewarnings

# Don't optimize or obfuscate (keep code readable for debugging)
-dontoptimize
-dontobfuscate

# Keep important attributes for reflection and debugging
-keepattributes AnnotationDefault,*Annotation*,Signature,InnerClasses,EnclosingMethod,RuntimeVisibleAnnotations,RuntimeInvisibleAnnotations,RuntimeVisibleParameterAnnotations,RuntimeInvisibleParameterAnnotations,RuntimeVisibleTypeAnnotations,RuntimeInvisibleTypeAnnotations,SourceFile,LineNumberTable

# Common Kotlin keep rules
-keep class kotlin.** { *; }
-keep class kotlin.jvm.** { *; }
-keep class kotlin.jvm.internal.** { *; }
-dontwarn kotlin.**

# Common Kotlinx Serialization keep rules
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

# Common Clikt CLI keep rules
-keep class com.github.ajalt.clikt.** { *; }
-dontwarn com.github.ajalt.clikt.**

# Common Mordant terminal keep rules (used by Clikt)
-keep class com.github.ajalt.mordant.** { *; }
-dontwarn com.github.ajalt.mordant.**

# Common SLF4J keep rules
-keep class org.slf4j.** { *; }
-dontwarn org.slf4j.**

# Common Java WebSocket keep rules
-keep class org.java_websocket.** { *; }
-dontwarn org.java_websocket.**

# Common JNA keep rules
-dontwarn com.sun.jna.**

# Keep all robocode classes
-keep class dev.robocode.tankroyale.** { *; }

# Keep enum methods that may be accessed reflectively
-keepclassmembers class * extends java.lang.Enum {
    <fields>;
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

