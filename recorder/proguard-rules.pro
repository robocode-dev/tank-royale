-libraryjar <java.home>/jmods/java.base.jmod(!**.jar;!module-info.class)
-libraryjar <java.home>/jmods/java.compiler.jmod(!**.jar;!module-info.class)
-libraryjar <java.home>/jmods/java.net.http.jmod(!**.jar;!module-info.class)
-libraryjar <java.home>/jmods/java.sql.jmod(!**.jar;!module-info.class)

-dontoptimize
-dontobfuscate
-ignorewarnings

-keepattributes AnnotationDefault,*Annotation*,Signature,InnerClasses,EnclosingMethod,RuntimeVisibleAnnotations,RuntimeInvisibleAnnotations,RuntimeVisibleParameterAnnotations,RuntimeInvisibleParameterAnnotations,RuntimeVisibleTypeAnnotations,RuntimeInvisibleTypeAnnotations,SourceFile,LineNumberTable

-keep public class dev.robocode.tankroyale.recorder.RecorderKt { *; }
-keep class dev.robocode.tankroyale.** { *; }
-keep class org.slf4j.** { *; }
-keep class org.java_websocket.** { *; }

# Keep Clikt CLI classes
-keep class com.github.ajalt.clikt.** { *; }

# Keep Kotlinx Serialization runtime and generated serializers
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

# Keep Kotlin metadata and internal markers often referenced by serializers
-keep class kotlin.** { *; }
-keep class kotlin.jvm.** { *; }
-keep class kotlin.jvm.internal.** { *; }

# Suppress warnings for Kotlin/Serialization/Clikt reflective accesses
-dontwarn kotlinx.serialization.**
-dontwarn kotlin.**
-dontwarn com.github.ajalt.clikt.**
