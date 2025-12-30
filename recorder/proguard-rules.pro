# Include common ProGuard configuration
-include ../proguard-common.pro

# Recorder-specific library jars
-libraryjar <java.home>/jmods/java.base.jmod(!**.jar;!module-info.class)
-libraryjar <java.home>/jmods/java.compiler.jmod(!**.jar;!module-info.class)
-libraryjar <java.home>/jmods/java.net.http.jmod(!**.jar;!module-info.class)
-libraryjar <java.home>/jmods/java.sql.jmod(!**.jar;!module-info.class)

# Recorder-specific keep rules
-keep public class dev.robocode.tankroyale.recorder.cli.RecorderKt { *; }
-keep public class dev.robocode.tankroyale.recorder.MainKt { *; }
