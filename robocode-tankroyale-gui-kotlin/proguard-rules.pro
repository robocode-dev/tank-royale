-libraryjar <java.home>/jmods/java.base.jmod(!**.jar;!module-info.class)
-libraryjar <java.home>/jmods/java.desktop.jmod(!**.jar;!module-info.class)
-libraryjar <java.home>/jmods/java.net.http.jmod(!**.jar;!module-info.class)
-libraryjar <java.home>/jmods/java.sql.jmod(!**.jar;!module-info.class)

-dontwarn
-keepattributes

-keep public class dev.robocode.** { *; }
