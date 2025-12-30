# Include common ProGuard configuration
-include ../proguard-common.pro

# GUI-specific library jars
-libraryjar <java.home>/jmods/java.base.jmod(!**.jar;!module-info.class)
-libraryjar <java.home>/jmods/java.desktop.jmod(!**.jar;!module-info.class)
-libraryjar <java.home>/jmods/java.net.http.jmod(!**.jar;!module-info.class)
-libraryjar <java.home>/jmods/java.sql.jmod(!**.jar;!module-info.class)

# GUI-specific keep rules
-keep public class dev.robocode.tankroyale.gui.GuiAppKt { *; }
-keep class com.github.weisj.jsvg.** { *; }
