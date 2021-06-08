#-libraryjar <java.home>/jmods/java.base.jmod(!**.jar;!module-info.class)
-libraryjars <java.home>/lib/rt.jar

-dontshrink
-dontoptimize

-keepattributes AnnotationDefault,*Annotation*

-keep public class dev.robocode.tankroyale.server.ServerKt { *; }
-keepclassmembernames public class dev.robocode.tankroyale.server.** { *; }
-keepclassmembernames public class picocli.** { *; }
-keep public class org.fusesource.jansi.** { *; }
-keep public class org.slf4j.** { *; }
