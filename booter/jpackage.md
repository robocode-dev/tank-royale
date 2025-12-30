# Packaging (jpackage)

This component is packaged as a runnable JAR with all runtime dependencies included and then compacted via R8.

- App name: Robocode Tank Royale Booter
- Main class: dev.robocode.tankroyale.booter.BooterKt
- Main JAR pattern (Gradle output): build/libs/robocode-tankroyale-booter-<version>.jar
- Vendor: robocode.dev
- Version: inherited from root gradle.properties
- Required resources: none beyond bundled JAR contents

When creating a native installer with jpackage, use parameters equivalent to:

- --name "Robocode Tank Royale Booter"
- --main-class dev.robocode.tankroyale.booter.BooterKt
- --main-jar robocode-tankroyale-booter-<version>.jar
- --app-version <version>
- --vendor robocode.dev
- --icon <platform-specific icon path> (to be provided)

Note: The Gradle build produces a fat/shrinked JAR; you can point jpackage at that output.
