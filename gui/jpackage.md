# Packaging (jpackage)

This component assembles a fat JAR that embeds the booter, server, and recorder JARs and then compacts the result with
R8.

- App name: Robocode Tank Royale
- Main class: dev.robocode.tankroyale.gui.GuiAppKt
- Main JAR pattern (Gradle output): build/libs/robocode-tankroyale-gui-<version>.jar
- Vendor: robocode.dev
- Version: inherited from root gradle.properties
- Required resources: icons and images in src/main/resources; embedded booter/server/recorder JARs are copied by the
  copyJars Gradle tasks

Suggested jpackage parameters:

- --name "Robocode Tank Royale"
- --main-class dev.robocode.tankroyale.gui.GuiAppKt
- --main-jar robocode-tankroyale-gui-<version>.jar
- --app-version <version>
- --vendor robocode.dev
- --icon <platform-specific icon path> (to be provided)
