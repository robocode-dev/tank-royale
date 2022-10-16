-libraryjar <java.home>/jmods/java.base.jmod(!**.jar;!module-info.class)

-dontshrink
-dontoptimize

-keepattributes AnnotationDefault,*Annotation*

-keep public class dev.robocode.tankroyale.booter.BooterKt { *; }
-keep class org.fusesource.jansi.** { *; }
-keepclassmembernames public class dev.robocode.tankroyale.booter.** { *; }
-keepclassmembernames public class picocli.** { *; }
