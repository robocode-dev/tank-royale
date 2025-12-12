-libraryjar <java.home>/jmods/java.base.jmod(!**.jar;!module-info.class)

-dontoptimize
-dontobfuscate
-ignorewarnings

-keepattributes AnnotationDefault,*Annotation*,Signature,InnerClasses,EnclosingMethod,RuntimeVisibleAnnotations,RuntimeInvisibleAnnotations,RuntimeVisibleParameterAnnotations,RuntimeInvisibleParameterAnnotations,RuntimeVisibleTypeAnnotations,RuntimeInvisibleTypeAnnotations,SourceFile,LineNumberTable

-keep public class dev.robocode.tankroyale.booter.BooterKt { *; }
-keep class dev.robocode.tankroyale.booter.** { *; }
# Keep Clikt CLI classes and Mordant terminal used by Clikt
-keep class com.github.ajalt.clikt.** { *; }
-keep class com.github.ajalt.mordant.** { *; }
-dontwarn com.github.ajalt.clikt.**
-dontwarn com.github.ajalt.mordant.**

# Keep Kotlinx Serialization (used for JSON output of info)
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

# Keep Kotlin internals often referenced
-keep class kotlin.** { *; }
-keep class kotlin.jvm.** { *; }
-keep class kotlin.jvm.internal.** { *; }
-dontwarn kotlinx.serialization.**
-dontwarn kotlin.**
