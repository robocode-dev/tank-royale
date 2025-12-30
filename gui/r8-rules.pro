# R8 rules for Robocode Tank Royale GUI

# Don't optimize or obfuscate (keep code readable for debugging)
-dontoptimize
-dontobfuscate

# Suppress warnings for Java 11+ classes not present in Java 11
-dontwarn java.lang.foreign.**
-dontwarn java.lang.classfile.**

# Suppress warnings for optional annotation classes
-dontwarn com.google.errorprone.annotations.**
-dontwarn org.jetbrains.annotations.ApiStatus$Internal
-dontwarn org.jetbrains.annotations.MustBeInvokedByOverriders

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

# SLF4J logging
-keep class org.slf4j.** { *; }
-dontwarn org.slf4j.**

# Keep all robocode classes
-keep class dev.robocode.tankroyale.** { *; }

# GUI-specific keep rules
-keep public class dev.robocode.tankroyale.gui.GuiAppKt { *; }

# JSVG (SVG rendering library)
-keep class com.github.weisj.jsvg.** { *; }

# MigLayout (Swing layout manager)
-keep class net.miginfocom.** { *; }

# Keep enum methods that may be accessed reflectively
-keepclassmembers class * extends java.lang.Enum {
    <fields>;
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

