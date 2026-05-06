# Running the GUI

This guide covers how to launch Robocode Tank Royale after installation and how to use the portable JAR distribution.

## After native installation

If you installed the native package, the GUI should be available from your operating system's application menu or
launcher as **Robocode Tank Royale GUI**.

If the application fails to start, verify that:

- Java 11 or newer is installed
- `JAVA_HOME` points to your Java installation
- `java -version` works from a terminal

## Running the portable JAR

If you prefer a portable installation, download the standalone JAR from the
[GitHub Releases](https://github.com/robocode-dev/tank-royale/releases).

The file name is `robocode-tankroyale-gui-x.y.z.jar`, where `x.y.z` is the release version.

You might be able to launch the JAR directly by double-clicking it. If that does not work, run it from a terminal:

```bash
java -jar robocode-tankroyale-gui-x.y.z.jar
```

## Using a dedicated directory

It is often useful to place the JAR in a dedicated directory such as `C:\Robocode` or `~/Robocode`.

Why:

- it keeps Robocode files together
- it makes launcher scripts easier to manage
- it gives you a predictable place for optional resources such as `sounds/`

### Example launcher script on Windows

```batch
@echo off
java -jar robocode-tankroyale-gui-x.y.z.jar
```

### Example launcher script on Linux or macOS

```bash
#!/bin/sh
java -jar robocode-tankroyale-gui-x.y.z.jar
```

Make the script executable on Linux or macOS:

```bash
chmod +x run-robocode.sh
```

## Next steps

- [Installing sample bots](installing-sample-bots.md)
- [Installing sounds](installing-sounds.md)
- [The GUI](gui.md)
