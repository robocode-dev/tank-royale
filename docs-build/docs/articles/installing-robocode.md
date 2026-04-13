# Installing Robocode

This guide covers the system requirements and native installation methods for Robocode Tank Royale.

## Requirements

### Java 11 or newer

Robocode Tank Royale runs on the Java Runtime Environment and requires **Java 11** or newer.

What you need:

- **Java Runtime Environment (JRE)** for running the GUI, server, booter, and recorder
- **Java Development Kit (JDK)** if you plan to develop bots in Java

You do not need Java 11 specifically. Any newer Java release works.

**Where to get Java:**

- **Oracle JDK:** [Oracle Java Downloads](https://www.oracle.com/java/technologies/downloads/)
- **OpenJDK:** [Adoptium Eclipse Temurin](https://adoptium.net/)
- **Distribution help:** [whichjdk.com](https://whichjdk.com/)

### Verifying Java installation

Open a terminal and run:

```shell
java -version
```

You should see the installed Java version and vendor. If the command is not recognized, install Java first and make
sure it is available on your `PATH`.

## Native installers

Robocode Tank Royale provides native installers through the project's
[GitHub Releases](https://github.com/robocode-dev/tank-royale/releases).

| Platform       | Installer File                                                                                     |
|----------------|----------------------------------------------------------------------------------------------------|
| 🪟 **Windows** | `robocode-tank-royale-gui-{VERSION}.msi`                                                           |
| 🍎 **macOS**   | `robocode-tank-royale-gui-{VERSION}.pkg`                                                           |
| 🐧 **Linux**   | `robocode-tank-royale-gui-{VERSION}.rpm` or `robocode-tank-royale-gui-{VERSION}.deb`             |

### Unsigned installers

> ⚠️ **Security notice:** The installers are not code-signed, so your operating system may show warnings before
> installation. You can verify authenticity using the release checksums.

### Verifying installer authenticity

Each release includes `SHA256SUMS` and `SHA256SUMS.asc`.

```bash
# Verify the GPG signature
gpg --verify SHA256SUMS.asc SHA256SUMS

# Verify the installer checksum
sha256sum -c SHA256SUMS 2>/dev/null | grep robocode-tank-royale-gui
```

On Windows PowerShell:

```powershell
Get-FileHash robocode-tank-royale-gui-{VERSION}.msi -Algorithm SHA256
```

Compare the hash with the value from `SHA256SUMS`.

## Installation instructions

### Windows (MSI)

1. Ensure Java 11 or newer is installed and `JAVA_HOME` is set
2. Download the `.msi` file from GitHub Releases
3. Double-click the `.msi` file to start the installation

When Windows SmartScreen shows **"Windows protected your PC"**:

1. Click **More info**
2. Click **Run anyway**
3. Confirm any User Account Control prompt

You can also install from an elevated command prompt:

```powershell
msiexec /i robocode-tank-royale-gui-{VERSION}.msi
```

### macOS (PKG)

1. Ensure Java 11 or newer is installed and `JAVA_HOME` is set
2. Download the `.pkg` file from GitHub Releases
3. Double-click the `.pkg` file

macOS Gatekeeper blocks the installer because it is from an unidentified developer. To continue:

1. Open **System Settings**
2. Go to **Privacy & Security**
3. Find the blocked installer entry
4. Click **Open Anyway**
5. Confirm and enter your administrator password

You can also install from Terminal:

```bash
xattr -d com.apple.quarantine robocode-tank-royale-gui-{VERSION}.pkg
sudo installer -pkg robocode-tank-royale-gui-{VERSION}.pkg -target /
```

### Linux (RPM)

1. Ensure Java 11 or newer is installed and `JAVA_HOME` is set
2. Download the `.rpm` file from GitHub Releases
3. Install it with either:

```bash
sudo rpm -ivh robocode-tank-royale-gui-{VERSION}.rpm
```

or:

```bash
sudo dnf install ./robocode-tank-royale-gui-{VERSION}.rpm
```

### Linux (DEB)

1. Ensure Java 11 or newer is installed and `JAVA_HOME` is set
2. Download the `.deb` file from GitHub Releases
3. Install it with either:

```bash
sudo apt install ./robocode-tank-royale-gui-{VERSION}.deb
```

or:

```bash
sudo dpkg -i robocode-tank-royale-gui-{VERSION}.deb
sudo apt-get install -f
```

## Next step

After installation, either launch the GUI from your application menu or continue with
[Running the GUI](running-the-gui.md) if you want the portable JAR workflow.
