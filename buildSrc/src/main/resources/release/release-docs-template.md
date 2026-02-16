## 🚀 Quick Start

**New to Robocode Tank Royale?**

1. **Install Java 11+** → **Download GUI** → **Get sample bots** → **Start battling!**

📖 **Complete documentation:** [robocode.dev](https://robocode.dev/) | **Advanced strategies:** [book.robocode.dev](https://book.robocode.dev/)

## 🛠 Installing Robocode

**Requirements:** Java 11+ ([whichjdk.com](https://whichjdk.com/)) | **Detailed setup:** [robocode.dev/installation](https://robocode.dev/articles/installation.html)

### Choose Your Installation Method

You have two options to install and run the Robocode GUI:

1. **Native Installers (Recommended)** – Installs as a native application with desktop shortcuts
2. **Portable JAR File** – Run directly from the command line without installation

Both options require Java 11 or newer.

---

### Option 1: Native Installers (Recommended)

**Download for your platform:**

| Platform       | Installer                                                                                       |
|----------------|-------------------------------------------------------------------------------------------------|
| 🪟 **Windows** | [robocode-tank-royale-gui-{VERSION}.msi]                                                        |
| 🍎 **macOS**   | [robocode-tank-royale-gui-{VERSION}.pkg]                                                        |
| 🐧 **Linux**   | [robocode-tank-royale-gui-{VERSION}.rpm] (RPM) / [robocode-tank-royale-gui-{VERSION}.deb] (DEB) |

> ⚠️ **Note:** Installers are unsigned (normal for open-source projects). Your OS may show security warnings - choose "Run anyway" or similar. Verify using [SHA256SUMS] if needed.

### Option 2: Portable JAR File

**Download:** [robocode-tankroyale-gui-{VERSION}.jar]

**Running:** `java -jar robocode-tankroyale-gui-{VERSION}.jar`

📝 **Tip:** Create a dedicated folder for better organization. See [GUI documentation] for details.

## 🤖 Sample Bots

Download pre-built bots to start battling immediately:

1. Download the archive for your preferred language
2. Extract to a directory (e.g., `C:\Robocode\bots\python`)
3. In GUI: **Config → Bot Root Directories** → Add the extracted directory

| Language      | Download                           | Requirements                    |
|---------------|------------------------------------|---------------------------------|
| 🐍 **Python** | [sample-bots-python-{VERSION}.zip] | [Python] 3.10 or newer          |
| 🔷 **C#**     | [sample-bots-csharp-{VERSION}.zip] | Microsoft [.NET SDK] 8 or newer |
| ☕ **Java**    | [sample-bots-java-{VERSION}.zip]   | Any [Java SDK] 11 or newer      |


## 📦 Bot API

Ready to develop your own bots? Install the API for your preferred language:

### 🐍 Python
```bash
pip install robocode-tank-royale=={VERSION}
```

### ☕ Java
**Maven:**
```xml
<dependency>
    <groupId>dev.robocode.tankroyale</groupId>
    <artifactId>robocode-tankroyale-bot-api</artifactId>
    <version>{VERSION}</version>
</dependency>
```

**Gradle:**
```gradle
implementation 'dev.robocode.tankroyale:robocode-tankroyale-bot-api:{VERSION}'
```

**Direct JAR:** [robocode-tankroyale-bot-api-{VERSION}.jar]

### 🔷 .NET (C#, F#, VB.NET)
```bash
dotnet add package Robocode.TankRoyale.BotApi --version {VERSION}
```

**More info:** [robocode.dev/bot-api](https://robocode.dev/api/) | [PyPI](https://pypi.org/project/robocode-tank-royale/{VERSION}) | [Maven Central](https://central.sonatype.com/artifact/dev.robocode.tankroyale/robocode-tankroyale-bot-api/{VERSION}) | [NuGet](https://www.nuget.org/packages/Robocode.TankRoyale.BotApi/{VERSION})

## 🔊 Sound Effects

Enhance your Robocode experience with sound effects!

**Download & Installation:** [Sounds Repository](https://github.com/robocode-dev/sounds)

Follow the repository instructions to install sounds in the correct location.

## 📚 Additional Resources

- **Documentation:** [robocode.dev](https://robocode.dev/)
- **GitHub Repository:** [robocode-dev/tank-royale](https://github.com/robocode-dev/tank-royale)
- **Issue Tracker:** [Report bugs or request features](https://github.com/robocode-dev/tank-royale/issues)

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
[GUI documentation]: https://robocode-dev.github.io/tank-royale/articles/gui.html "GUI Documentation"
[Python]: https://www.python.org/downloads/ "Python downloads"
[.NET SDK]: https://dotnet.microsoft.com/en-us/download/dotnet ".NET SDK"
[Java SDK]: https://robocode-dev.github.io/tank-royale/articles/installation.html#java-11-or-newer "Java SDK"
[SHA256SUMS]: https://github.com/robocode-dev/tank-royale/releases/download/v{VERSION}/SHA256SUMS "SHA256 checksums for verifying installer integrity"


