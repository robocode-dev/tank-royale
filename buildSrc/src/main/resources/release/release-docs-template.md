## 🚀 Quick Start

**New to Robocode Tank Royale?** Follow these steps to get started:

1. **Install Java 11 or newer** – Required to run Robocode
2. **Download and install the GUI** – Choose between native installer or JAR file (see below)
3. **Download sample bots** – Get pre-built bots to try out immediately
4. **Run your first battle** – Launch the GUI and start battling!

For detailed guidance, see the [Getting Started] guide and [My First Bot tutorial].

---

## 📄 Documentation

The complete Robocode Tank Royale documentation is available at:  
**https://robocode-dev.github.io/tank-royale/**

📖 **The Book of Robocode:** [book.robocode.dev](https://book.robocode.dev/) – Comprehensive guide with
advanced strategies for Tank Royale like wave surfing, guess-factor targeting, and movement techniques.

**Recommended reading order:**

- [Getting Started] – Learn the basics
- [Installation guide] – Detailed setup instructions
- [My First Bot tutorial] – Build your first bot
- [GUI documentation] – Master the user interface

---

## 🛠 Installing Robocode

### Requirements

- **Java 11 or newer** is required to run Robocode Tank Royale
- We recommend using the latest Java version for the best performance
- Download Java from the [official Oracle website](https://www.oracle.com/java/technologies/downloads/) or use [OpenJDK]

📖 See the [installation guide] for detailed instructions on installing Java and setting up Robocode.

### Choose Your Installation Method

You have two options to install and run the Robocode GUI:

1. **Native Installers (Recommended)** – Installs as a native application with desktop shortcuts
2. **Portable JAR File** – Run directly from the command line without installation

Both options require Java 11 or newer.

---

### Option 1: Native Installers (Recommended)

Native installers provide the easiest installation experience. The GUI will be registered with your operating system,
allowing you to launch it from your application menu or desktop shortcut.

**Download for your platform:**

| Platform       | Installer                                                                                       |
|----------------|-------------------------------------------------------------------------------------------------|
| 🪟 **Windows** | [robocode-tank-royale-gui-{VERSION}.msi]                                                        |
| 🍎 **macOS**   | [robocode-tank-royale-gui-{VERSION}.pkg]                                                        |
| 🐧 **Linux**   | [robocode-tank-royale-gui-{VERSION}.rpm] (RPM) / [robocode-tank-royale-gui-{VERSION}.deb] (DEB) |

**System Requirements:**

- Java 11 or newer must be installed
- The `JAVA_HOME` environment variable must be set to your Java installation directory
    - **Need help?** Read this article from Baeldung:
      [How to Set JAVA_HOME on Windows, macOS, and Linux](https://www.baeldung.com/java-home-on-windows-mac-os-x-linux)

> ⚠️ **Important: Unsigned Installers**
>
> The native installers are **not code-signed** with platform certificates. This is normal for open-source projects.
> Your operating system may display security warnings when installing — see the instructions below for your platform.
>
> You can verify installer authenticity using the [SHA256SUMS] and [SHA256SUMS.asc] files provided with this release.

**Installation Steps:**

<details>
<summary>🪟 <b>Windows (MSI)</b></summary>

1. Download the `.msi` file
2. Double-click to run the installer
3. **If Windows SmartScreen appears** ("Windows protected your PC"):
    - Click **"More info"**
    - Click **"Run anyway"**
4. If prompted by User Account Control (UAC), click **"Yes"**
5. Follow the installation wizard

</details>

<details>
<summary>🍎 <b>macOS (PKG)</b></summary>

1. Download the `.pkg` file
2. Double-click to run the installer
3. **If macOS Gatekeeper blocks it** ("cannot be opened because it is from an unidentified developer"):
    - Open **System Settings** → **Privacy & Security**
    - Scroll down and click **"Open Anyway"** next to the blocked installer
    - Or: Right-click the `.pkg` file and select **"Open"**, then click **"Open"** in the dialog
    - Or: Run in Terminal: `xattr -d com.apple.quarantine robocode-tank-royale-gui-{VERSION}.pkg`
4. Enter your administrator password when prompted
5. Follow the installation wizard

</details>

<details>
<summary>🐧 <b>Linux (RPM/DEB)</b></summary>

**RPM (Fedora/CentOS/RHEL):**

```bash
sudo dnf install ./robocode-tank-royale-gui-{VERSION}.x86_64.rpm
# or
sudo rpm -ivh robocode-tank-royale-gui-{VERSION}.x86_64.rpm
```

**DEB (Debian/Ubuntu):**

```bash
sudo apt install ./robocode-tank-royale-gui_{VERSION}_amd64.deb
# or
sudo dpkg -i robocode-tank-royale-gui_{VERSION}_amd64.deb
```

> **Note:** Linux packages may show warnings about being unsigned. This is normal for community-distributed software.

</details>

After installation, find **Robocode Tank Royale GUI** in your application menu/launcher and start battling!

---

### Option 2: Portable JAR File

For users who prefer a portable installation or want more control, you can download the GUI as a standalone JAR file.

**Download:**

| Platform | Download                                |
|----------|-----------------------------------------|
| All      | [robocode-tankroyale-gui-{VERSION}.jar] |

**System Requirements:**

- Java 11 or newer must be installed and available on your system `PATH`

**Running the GUI:**

Open a terminal (or command prompt on Windows) and run:

```bash
java -jar robocode-tankroyale-gui-{VERSION}.jar
```

**💡 Pro Tips for Better Organization:**

Create a dedicated directory for Robocode (e.g., `C:\Robocode` or `~/Robocode`) and place the JAR file there. Then
create a launcher script for easy access:

**Windows** (`run-robocode.bat`):

```batch
@echo off
java -jar robocode-tankroyale-gui-{VERSION}.jar
```

**Linux/macOS** (`run-robocode.sh`):

```bash
#!/bin/sh
java -jar robocode-tankroyale-gui-{VERSION}.jar
```

Make the script executable on Linux/macOS:

```bash
chmod +x run-robocode.sh
```

**Why use a dedicated directory?**

- Robocode automatically creates configuration files in the same directory as the JAR
- Makes it easy to manage settings, logs, and optional resources (like the `sounds/` folder)
- Keeps your system organized with all Robocode files in one place

---

For more details on using the GUI, see the [GUI documentation].

---

## 🤖 Sample Bots

To start battling immediately, download pre-built sample bots. These bots demonstrate different strategies and
programming styles.

**How to install sample bots:**

1. Download the sample bots archive for your preferred language(s)
2. Extract the archive to a directory on your system (e.g., `C:\Robocode\bots\python` or `~/robocode/bots/python`)
3. In the GUI, go to **Config → Bot Root Directories** and add the extracted directory
4. The bots will now appear in your bot list!

**Available sample bots:**

| Language      | Download                           | Requirements                    |
|---------------|------------------------------------|---------------------------------|
| 🐍 **Python** | [sample-bots-python-{VERSION}.zip] | [Python] 3.10 or newer          |
| 🔷 **C#**     | [sample-bots-csharp-{VERSION}.zip] | Microsoft [.NET SDK] 8 or newer |
| ☕ **Java**    | [sample-bots-java-{VERSION}.zip]   | Any [Java SDK] 11 or newer      |

📝 Each archive contains a `README.md` file with platform-specific instructions.

---

## 📦 Bot API

Ready to develop your own bots? Choose your preferred programming language and install the corresponding Bot API.

### 🐍 Python

Install via pip:

```bash
pip install robocode-tank-royale=={VERSION}
```

Or download from [Python Package Index (PyPI)](https://pypi.org/project/robocode-tank-royale/{VERSION})

### ☕ Java

**Option 1: Maven** (recommended)
Add to your `pom.xml`:

```xml

<dependency>
    <groupId>dev.robocode.tankroyale</groupId>
    <artifactId>robocode-tankroyale-bot-api</artifactId>
    <version>{VERSION}</version>
</dependency>
```

**Option 2: Gradle**
Add to your `build.gradle`:

```gradle
implementation 'dev.robocode.tankroyale:robocode-tankroyale-bot-api:{VERSION}'
```

**Option 3: Direct JAR download**

- Download: [robocode-tankroyale-bot-api-{VERSION}.jar]
- Or
  browse: [Maven Central Repository](https://central.sonatype.com/artifact/dev.robocode.tankroyale/robocode-tankroyale-bot-api/{VERSION})

### 🔷 .NET (C#, F#, VB.NET)

Install via NuGet Package Manager:

```bash
dotnet add package Robocode.TankRoyale.BotApi --version {VERSION}
```

Or browse: [NuGet repository](https://www.nuget.org/packages/Robocode.TankRoyale.BotApi/{VERSION})

---

## 🔊 Sound Effects

Enhance your Robocode experience with sound effects! Sound files are available separately from the main distribution.

**Download:** [Sounds Repository](https://github.com/robocode-dev/sounds)

**Installation:**

1. Visit the sounds repository linked above
2. Follow the installation instructions to place the `sounds/` directory in the correct location relative to your GUI
   installation
3. Restart the GUI to enable sound effects

Sounds include gunshots, explosions, collisions, and more!

---

## 📚 Additional Resources

- **GitHub Repository:** [robocode-dev/tank-royale](https://github.com/robocode-dev/tank-royale)
- **Issue Tracker:** [Report bugs or request features](https://github.com/robocode-dev/tank-royale/issues)
- **Documentation:** [Complete documentation site](https://robocode-dev.github.io/tank-royale/)
- **Community:** Join discussions and get help from other Robocode users

---

[sample-bots-python-{VERSION}.zip]: https://github.com/robocode-dev/tank-royale/releases/download/v{VERSION}/sample-bots-python-{VERSION}.zip "Sample bots for Python"

[sample-bots-csharp-{VERSION}.zip]: https://github.com/robocode-dev/tank-royale/releases/download/v{VERSION}/sample-bots-csharp-{VERSION}.zip "Sample bots for C#"

[sample-bots-java-{VERSION}.zip]: https://github.com/robocode-dev/tank-royale/releases/download/v{VERSION}/sample-bots-java-{VERSION}.zip "Sample bots for Java"

[robocode-tankroyale-bot-api-{VERSION}.jar]: https://s01.oss.sonatype.org/service/local/repositories/releases/content/dev/robocode/tankroyale/robocode-tankroyale-bot-api/{VERSION}/robocode-tankroyale-bot-api-{VERSION}.jar "Bot API Java archive file"

[robocode-tankroyale-gui-{VERSION}.jar]: https://github.com/robocode-dev/tank-royale/releases/download/v{VERSION}/robocode-tankroyale-gui-{VERSION}.jar "GUI Java archive file"

[robocode-tank-royale-gui-{VERSION}.msi]: https://github.com/robocode-dev/tank-royale/releases/download/v{VERSION}/robocode-tank-royale-gui-{VERSION}.msi "GUI for Windows (MSI)"

[robocode-tank-royale-gui-{VERSION}.pkg]: https://github.com/robocode-dev/tank-royale/releases/download/v{VERSION}/robocode-tank-royale-gui-{VERSION}.pkg "GUI for macOS (PKG)"

[robocode-tank-royale-gui-{VERSION}.rpm]: https://github.com/robocode-dev/tank-royale/releases/download/v{VERSION}/robocode-tank-royale-gui-{VERSION}.x86_64.rpm "GUI for Linux (RPM)"

[robocode-tank-royale-gui-{VERSION}.deb]: https://github.com/robocode-dev/tank-royale/releases/download/v{VERSION}/robocode-tank-royale-gui_{VERSION}_amd64.deb "GUI for Linux (DEB)"

[OpenJDK]: https://adoptium.net/ "Adoptium OpenJDK"

[GUI documentation]: https://robocode-dev.github.io/tank-royale/articles/gui.html "GUI Documentation"

[Python]: https://www.python.org/downloads/ "Python downloads"

[.NET SDK]: https://dotnet.microsoft.com/en-us/download/dotnet ".NET SDK"

[Java SDK]: https://robocode-dev.github.io/tank-royale/articles/installation.html#java-11-or-newer "Java SDK"

[My First Bot tutorial]: https://robocode-dev.github.io/tank-royale/tutorial/my-first-bot.html "My First Bot Tutorial"

[Getting Started]: https://robocode-dev.github.io/tank-royale/tutorial/getting-started.html "Getting Started"

[installation guide]: https://robocode-dev.github.io/tank-royale/articles/installation.html "Installing and running Robocode"

[GUI]: https://robocode-dev.github.io/tank-royale/articles/gui.html "The GUI"

[SHA256SUMS]: https://github.com/robocode-dev/tank-royale/releases/download/v{VERSION}/SHA256SUMS "SHA256 checksums for verifying installer integrity"

[SHA256SUMS.asc]: https://github.com/robocode-dev/tank-royale/releases/download/v{VERSION}/SHA256SUMS.asc "GPG signature for SHA256SUMS"

