-libraryjars <java.home>/lib/rt.jar

-dontshrink
-dontoptimize
-dontobfuscate

-keepattributes AnnotationDefault,*Annotation*

# To avoid ClassCastException
-keep @dev.robocode.tankroyale.schema.BotAddress public class *

-keep class dev.robocode.** { *; }
-keep public class picocli.** { *; }
-keep public class org.fusesource.jansi.** { *; }
-keep public class org.slf4j.** { *; }
-keep public class org.java_websocket.** { *; }