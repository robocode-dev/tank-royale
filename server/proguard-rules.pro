-libraryjar <java.home>/jmods/java.base.jmod(!**.jar;!module-info.class)
-libraryjar <java.home>/jmods/java.sql.jmod(!**.jar;!module-info.class)

-dontshrink
-dontoptimize
-dontobfuscate

-keepattributes AnnotationDefault,*Annotation*

-keep class dev.robocode.**
-keep class picocli.**
-keep class org.fusesource.jansi.*
-keep class org.slf4j.*
-keep class org.java_websocket.*