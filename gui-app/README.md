# The GUI Application

This module contains the GUI application used for letting a user boot up bots on a local or remote server, and then
watch the battles.

The [booter] and [server] are built into the GUI application.

The GUI application is running on the [Java 11] platform and the [Kotlin] programming language (typically the newest
available version).

## Build commands

#### Clean build directory:

```shell
./gradlew :gui-app:clean
```

#### Build/compile:

```shell
./gradlew :gui-app:build
```

#### Publish artifact to the local Maven repository

```shell
./gradlew :gui-app:publishToMavenLocal
```

## Running the GUI application

The GUI is started by running the `dev.robocode.tankroyale.booter.BooterKt` as a Kotlin application on Java.

When you run the GUI application from [IntelliJ IDEA] (used for developing Robocode), you can use the Gradle
task `copyJars` when setting up a Run/Debug Configuration. The `copyJars` tasks will make sure to build and copy the jar
files for the [booter] and [server] into the classpath for the GUI application so it is possible to boot up bots and
start battles on a server running locally.


[booter]: ../booter/README.md

[server]: ../server/README.md

[Java 11]: https://docs.oracle.com/en/java/javase/11/ "Java 11 documentation"

[Kotlin]: https://kotlinlang.org/ "Kotlin programming language"

[IntelliJ IDEA]: https://www.jetbrains.com/idea/ "IntelliJ IDEA"
