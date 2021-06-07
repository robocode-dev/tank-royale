-libraryjar <java.home>/jmods/java.base.jmod(!**.jar;!module-info.class)

-dontshrink
-dontoptimize

-keepattributes AnnotationDefault,*Annotation*

-keep public class dev.robocode.tankroyale.bootstrap.BootstrapKt { *; }
-keepclassmembernames public class dev.robocode.tankroyale.bootstrap.** { *; }
-keepclassmembernames public class picocli.** { *; }
