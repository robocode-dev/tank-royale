-libraryjar <java.home>/jmods/java.base.jmod(!**.jar;!module-info.class)
-libraryjar <java.home>/jmods/java.sql.jmod(!**.jar;!module-info.class)
-libraryjar <java.home>/jmods/java.compiler.jmod(!**.jar;!module-info.class)

-dontoptimize
-dontobfuscate
-dontwarn # remove when debugging

-keepattributes AnnotationDefault,*Annotation*

-keep public class dev.robocode.tankroyale.server.ServerKt { *; }
-keep class dev.robocode.tankroyale.server.** { *; }
-keep class picocli.** { *; }
-keep class org.fusesource.jansi.** { *; }
-keep class org.slf4j.** { *; }
-keep class org.java_websocket.** { *; }
