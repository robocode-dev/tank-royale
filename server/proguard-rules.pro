-libraryjar <java.home>/jmods/java.base.jmod(!**.jar;!module-info.class)
-libraryjar <java.home>/jmods/java.sql.jmod(!**.jar;!module-info.class)
-libraryjar <java.home>/jmods/java.compiler.jmod(!**.jar;!module-info.class)

-dontoptimize
-dontobfuscate
-ignorewarnings

-keepattributes AnnotationDefault,*Annotation*

-keep public class dev.robocode.tankroyale.server.ServerKt { *; }
-keep class dev.robocode.tankroyale.** { *; }
-keep class com.github.ajalt.clikt.** { *; }
-keep class com.github.ajalt.mordant.** { *; }
-dontwarn com.github.ajalt.clikt.**
-dontwarn com.github.ajalt.mordant.**
-dontwarn com.sun.jna.**
-keep class org.slf4j.** { *; }
-keep class org.java_websocket.** { *; }
