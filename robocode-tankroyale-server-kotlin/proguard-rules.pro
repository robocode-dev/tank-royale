-libraryjars <java.home>/lib/rt.jar

-dontshrink
-dontoptimize
-dontobfuscate

-keepattributes AnnotationDefault,*Annotation*

-keep class dev.robocode.**
-keep class picocli.**
-keep class org.fusesource.jansi.*
-keep class org.slf4j.*
-keep class org.java_websocket.*