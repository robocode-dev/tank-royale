# Include common ProGuard configuration
-include ../proguard-common.pro

# Booter-specific library jars
-libraryjar <java.home>/jmods/java.base.jmod(!**.jar;!module-info.class)

# Booter-specific keep rules
-keep public class dev.robocode.tankroyale.booter.BooterKt { *; }
-keep public class dev.robocode.tankroyale.booter.MainKt { *; }
-keep class dev.robocode.tankroyale.booter.** { *; }
