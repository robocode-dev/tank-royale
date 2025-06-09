-libraryjar <java.home>/jmods/java.base.jmod(!**.jar;!module-info.class)
-libraryjar <java.home>/jmods/java.compiler.jmod(!**.jar;!module-info.class)
-libraryjar <java.home>/jmods/java.net.http.jmod(!**.jar;!module-info.class)
-libraryjar <java.home>/jmods/java.sql.jmod(!**.jar;!module-info.class)

-dontoptimize
-dontobfuscate

-keepattributes AnnotationDefault,*Annotation*

-keep public class dev.robocode.tankroyale.recorder.RecorderKt { *; }
-keep class dev.robocode.tankroyale.** { *; }
-keep class org.fusesource.jansi.** { *; }
-keep class picocli.** { *; }
-keep class org.slf4j.** { *; }
-keep class org.java_websocket.** { *; }
