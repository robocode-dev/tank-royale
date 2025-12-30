# Packaging (jpackage)

This component is produced as a runnable JAR with bundled runtime dependencies and compacted via R8.

- App name: Robocode Tank Royale Recorder
- Main class: dev.robocode.tankroyale.recorder.RecorderKt
- Main JAR pattern (Gradle output): build/libs/robocode-tankroyale-recorder-<version>.jar
- Vendor: robocode.dev
- Version: inherited from root gradle.properties
- Required resources: none beyond bundled JAR contents

Suggested jpackage parameters:

- --name "Robocode Tank Royale Recorder"
- --main-class dev.robocode.tankroyale.recorder.RecorderKt
- --main-jar robocode-tankroyale-recorder-<version>.jar
- --app-version <version>
- --vendor robocode.dev
- --icon <platform-specific icon path> (to be provided)
