-libraryjar <java.home>/jmods/java.base.jmod(!**.jar;!module-info.class)

-dontoptimize
-dontobfuscate

-keepattributes AnnotationDefault,*Annotation*

-keep public class dev.robocode.tankroyale.booter.BooterKt { *; }
-keep class dev.robocode.tankroyale.booter.** { *; }
-keep class org.fusesource.jansi.** { *; }
-keep class picocli.** { *; }
