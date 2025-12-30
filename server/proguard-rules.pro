# Include common ProGuard configuration
-include ../proguard-common.pro

# Server-specific library jars
-libraryjar <java.home>/jmods/java.base.jmod(!**.jar;!module-info.class)
-libraryjar <java.home>/jmods/java.sql.jmod(!**.jar;!module-info.class)
-libraryjar <java.home>/jmods/java.compiler.jmod(!**.jar;!module-info.class)

# Server-specific keep rules
-keep public class dev.robocode.tankroyale.server.ServerKt { *; }
-keep public class dev.robocode.tankroyale.server.MainKt { *; }
