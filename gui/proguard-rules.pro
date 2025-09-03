-libraryjar <java.home>/jmods/java.base.jmod(!**.jar;!module-info.class)
-libraryjar <java.home>/jmods/java.desktop.jmod(!**.jar;!module-info.class)
-libraryjar <java.home>/jmods/java.net.http.jmod(!**.jar;!module-info.class)
-libraryjar <java.home>/jmods/java.sql.jmod(!**.jar;!module-info.class)

-dontwarn
-dontoptimize

-keep class dev.robocode.** { *; }
-keep class com.github.weisj.jsvg.** { *; }
-keepattributes *Annotation*
-keepattributes RuntimeVisibleAnnotations
-keepattributes RuntimeInvisibleAnnotations
-keepclassmembers class * extends java.lang.Enum {
    <fields>;
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
