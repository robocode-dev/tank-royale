# Packaging (jpackage)

This component is built as an all-in-one runnable JAR and then compacted with ProGuard.

- App name: Robocode Tank Royale Server
- Main class: dev.robocode.tankroyale.server.ServerKt
- Main JAR pattern (Gradle output): build/libs/robocode-tankroyale-server-<version>.jar
- Vendor: robocode.dev
- Version: inherited from root gradle.properties
- Required resources: none beyond bundled JAR contents

Suggested jpackage parameters:

- --name "Robocode Tank Royale Server"
- --main-class dev.robocode.tankroyale.server.ServerKt
- --main-jar robocode-tankroyale-server-<version>.jar
- --app-version <version>
- --vendor robocode.dev
- --icon <platform-specific icon path> (to be provided)
