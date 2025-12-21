# Installing and running Robocode

## Introduction

This guide provides detailed instructions on how to install and run Robocode Tank Royale. Whether you're new to Robocode
or setting up a new installation, this guide will walk you through the entire process.

**Quick overview:**

1. Install Java 11 or newer
2. Choose your installation method (Native installer or JAR file)
3. Download and set up sample bots
4. Configure bot directories and start battling!

## Requirements

### Java 11 or newer

Robocode Tank Royale runs on the Java Runtime Environment (JRE) and requires **Java 11 as a minimum**. We recommend
using the latest Java version for best performance and security.

**What you need:**

- **Java Runtime Environment (JRE)** – Required to run Robocode GUI (including the server, booter, and recorder)
- **Java Development Kit (JDK)** – Required if you want to develop bots in the Java programming language

> **Note:** You don't need to run Java 11 specifically. Any version 11 or newer will work, including the latest Java
> releases. We encourage you to use the newest Java version available.

**Where to get Java:**

- **Oracle JDK:** [Oracle Java Downloads](https://www.oracle.com/java/technologies/downloads/)
- **OpenJDK:** [Adoptium Eclipse Temurin](https://adoptium.net/) (recommended free distribution)
- **Need help choosing?** Visit [whichjdk.com](https://whichjdk.com/) for guidance on selecting the best Java
  distribution

### Verifying Java installation

To check if Java is already installed on your system, open a terminal (or command prompt) and run:

```shell
java -version
```

This command should display the Java version and vendor information, confirming that Java is installed correctly and
available from the command line. For example:

```
openjdk version "21.0.1" 2023-10-17 LTS
OpenJDK Runtime Environment Temurin-21.0.1+12 (build 21.0.1+12-LTS)
OpenJDK 64-Bit Server VM Temurin-21.0.1+12 (build 21.0.1+12-LTS, mixed mode, sharing)
```

If the command is not recognized, you need to install Java first and ensure it's added to your system PATH.

---

## Installing Robocode

### Choose Your Installation Method

You have two options to install and run the Robocode GUI:

1. **Native Installers (Recommended)** – Installs as a native application with desktop shortcuts
2. **Portable JAR File** – Run directly from the command line without installation

Both options require Java 11 or newer.

---

### Option 1: Installing with Native Installers (Recommended)

Native installers provide the easiest installation experience. The GUI will be registered with your operating system,
allowing you to launch it from your application menu or desktop shortcut.

We provide native installer packages for the GUI application that are produced by CI and attached to the
project's [GitHub Releases](https://github.com/robocode-dev/tank-royale/releases). These installers are recommended for
most end-users as they install the GUI in platform-native locations and register file associations where appropriate.

**System Requirements:**

- Java 11 or newer must be installed
- The `JAVA_HOME` environment variable must be set to your Java installation directory
    - **Need help?** Read this article from
      Baeldung: [How to Set JAVA_HOME on Windows, macOS, and Linux](https://www.baeldung.com/java-home-on-windows-mac-os-x-linux)

**Where to get the installers:**

Download the appropriate installer from the [GitHub Releases](https://github.com/robocode-dev/tank-royale/releases) for
the version you want:

| Platform       | Installer File                                                                                   |
|----------------|--------------------------------------------------------------------------------------------------|
| 🪟 **Windows** | `robocode-tank-royale-gui-{VERSION}.msi`                                                         |
| 🍎 **macOS**   | `robocode-tank-royale-gui-{VERSION}.pkg`                                                         |
| 🐧 **Linux**   | `robocode-tank-royale-gui-{VERSION}.rpm` (RPM) or `robocode-tank-royale-gui-{VERSION}.deb` (DEB) |

#### Installation Instructions

**Windows (MSI):**

1. Ensure Java 11+ is installed and `JAVA_HOME` is set
2. Download the `.msi` file from GitHub Releases
3. Double-click the `.msi` file, or run from an elevated command prompt:

```powershell
msiexec /i robocode-tank-royale-gui-{VERSION}.msi
```

**macOS (PKG):**

1. Ensure Java 11+ is installed and `JAVA_HOME` is set
2. Download the `.pkg` file from GitHub Releases
3. Double-click the `.pkg` to run the macOS Installer, or run from the terminal:

```bash
sudo installer -pkg robocode-tank-royale-gui-{VERSION}.pkg -target /
```

**Linux (RPM):**

1. Ensure Java 11+ is installed and `JAVA_HOME` is set
2. Download the `.rpm` file from GitHub Releases
3. Install the RPM (example for Fedora/CentOS/RHEL):

```bash
sudo rpm -ivh robocode-tank-royale-gui-{VERSION}.rpm
```

**Linux (DEB):**

1. Ensure Java 11+ is installed and `JAVA_HOME` is set
2. Download the `.deb` file from GitHub Releases
3. Install the DEB (example for Debian/Ubuntu):

```bash
sudo apt install ./robocode-tank-royale-gui-{VERSION}.deb
```

#### After Installation

- The GUI should be available in your system's application menu or launcher
- Find and launch **Robocode Tank Royale GUI** to start the application
- If the application fails to start, verify that:
    - Java 11+ is installed (`java -version`)
    - `JAVA_HOME` environment variable points to your Java installation

---

### Option 2: Running the Robocode GUI (Portable JAR File)

For users who prefer a portable installation or want more control, you can download the GUI as a standalone JAR file.

**System Requirements:**

- Java 11 or newer must be installed and available on your system `PATH`

**Where to get the JAR file:**

You can download the application from the [Robocode releases](https://github.com/robocode-dev/tank-royale/releases).

Download the file named `robocode-tankroyale-gui-x.y.z.jar`, where `x.y.z` is the specific version number of Robocode (
e.g., version 0.34.2).

**Running the GUI:**

You might be able to simply start the application by (double)clicking it, depending on your OS and Java version. If you
cannot start the Robocode application by clicking it, you should start it from the command line.

Open a terminal (or command prompt on Windows), navigate to the directory containing the JAR file, and run:

```bash
java -jar robocode-tankroyale-gui-x.y.z.jar
```

**💡 Pro Tips for Better Organization:**

Create a dedicated directory for Tank Royale (e.g., `C:\Robocode` or `~/Robocode`) and place the JAR file there. Then
create a launcher script for easy access:

**Windows** (`run-robocode.bat`):

```batch
@echo off
java -jar robocode-tankroyale-gui-x.y.z.jar
```

**Linux/macOS** (`run-robocode.sh`):

```bash
#!/bin/sh
java -jar robocode-tankroyale-gui-x.y.z.jar
```

Make the script executable on Linux/macOS:

```bash
chmod +x run-robocode.sh
```

**Why use a dedicated directory?**

- The GUI automatically creates and stores `.properties` configuration files in the same directory as the JAR file
- Makes it easy to manage settings, logs, and optional resources (like the `sounds/` folder)
- Keeps your system organized with all Robocode files in one place

---

## Sample Bots

To start battling immediately, download pre-built sample bots. These bots demonstrate different strategies and
programming styles, and are perfect for learning how the game works.

**How to install sample bots:**

1. Download the sample bots archive for your preferred language(s) from
   the [Robocode releases](https://github.com/robocode-dev/tank-royale/releases)
2. Extract the archive to a directory on your system (e.g., `C:\Robocode\bots\java` or `~/robocode/bots/java`)
3. Note the file path of the directory where all the sample bot directories are located
4. In the GUI, go to **Config → Bot Root Directories** and add the extracted directory
5. The bots will now appear in your bot list!

**Available sample bots:**

| Language      | Archive File                   | Requirements         |
|---------------|--------------------------------|----------------------|
| 🐍 **Python** | `sample-bots-python-x.y.z.zip` | Python 3.10 or newer |
| 🔷 **C#**     | `sample-bots-csharp-x.y.z.zip` | .NET SDK 8 or newer  |
| ☕ **Java**    | `sample-bots-java-x.y.z.zip`   | Java SDK 11 or newer |

📝 Each archive contains a `README.md` file with platform-specific instructions.

**Troubleshooting:**

The sample bots should show up under the Bot Directories when selecting **Battle → Start Battle** from the menu. If they
don't appear, you might have a misconfiguration with the root bot directory. Make sure you've added the correct parent
directory containing all the bot subdirectories.

---

## Installing Sound Files

> **Note:** Installing sound files is optional, but highly recommended if you want to enhance your gaming experience
> with audio effects! 🔊

Sound files for Robocode are provided separately and can add exciting audio feedback to battles, including gunshots,
explosions, and collisions.

**Download:**

Download the `sounds.zip` archive from the [sounds releases](https://github.com/robocode-dev/sounds/releases), for
example: [sounds.zip 1.0.0](https://github.com/robocode-dev/sounds/releases/download/v1.0.0/sounds.zip).

**Installation Instructions:**

1. Unpack the `sounds` directory from the zip archive
2. Copy the `sounds` directory into the directory containing your `robocode-tankroyale-gui-x.y.z.jar` file
3. The directory structure should look like this:

```
[your tank royale directory]
├── robocode-tankroyale-gui-x.y.z.jar
└── sounds/  <-- place the sounds directory here
    ├── bots_collision.wav
    ├── bullet_hit.wav
    ├── bullets_collision.wav
    ├── death.wav
    ├── gunshot.wav
    ├── wall_collision.wav
    └── ...
```

**After Installation:**

- Sounds are automatically enabled when the `sounds/` directory is detected
- You can enable/disable all sounds or individual sound effects from the **Sound Options** in the GUI menu
- The GUI will automatically detect and use the sound files

### Using Your Own Sound Files

You can customize the audio experience by replacing one or more sounds with your own audio files:

- Replace any sound file in the `sounds/` directory with your own [WAV] file
- Keep the original filenames (e.g., `gunshot.wav`, `bullet_hit.wav`)
- Only [WAV] format files are supported
- Make sure your custom files are in the correct WAV format for best compatibility

[WAV]: https://en.wikipedia.org/wiki/WAV "WAV file"
