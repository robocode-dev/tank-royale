# User Data and Configuration Files

## Introduction

Robocode Tank Royale stores user preferences, settings, and recordings in platform-specific user data directories. This
ensures that your configuration is preserved across sessions and follows standard operating system conventions for
application data storage.

This guide explains:

- Where to find your user data directory
- What configuration files are stored there
- How to edit and manage these files

## User Data Directory Location

The user data directory location varies by operating system, following platform conventions:

### Windows

On Windows, the user data directory is located at:

```
%LOCALAPPDATA%\Robocode Tank Royale
```

This typically expands to:

```
C:\Users\{YourUsername}\AppData\Local\Robocode Tank Royale
```

**How to access:**

1. Press `Win + R` to open the Run dialog
2. Type `%LOCALAPPDATA%\Robocode Tank Royale` and press Enter
3. The folder will open in Windows Explorer

Alternatively, if `%LOCALAPPDATA%` is not set, the directory falls back to:

```
%APPDATA%\Robocode Tank Royale
```

### macOS

On macOS, the user data directory is located at:

```
~/Library/Application Support/Robocode Tank Royale
```

This is the standard location for application support files on macOS.

**How to access:**

1. Open Finder
2. Press `Cmd + Shift + G` to open "Go to Folder"
3. Enter `~/Library/Application Support/Robocode Tank Royale` and press Enter

### Linux

On Linux, the user data directory follows the XDG Base Directory specification:

```
~/.config/robocode-tank-royale
```

Or, if the `XDG_CONFIG_HOME` environment variable is set:

```
$XDG_CONFIG_HOME/robocode-tank-royale
```

Note that the directory name uses lowercase with hyphens, following Linux naming conventions.

**How to access:**

Open a terminal and run:

```bash
cd ~/.config/robocode-tank-royale
```

Or use your file manager to navigate to the hidden `.config` folder in your home directory.

## Directory Structure

The user data directory contains the following files and subdirectories:

```
Robocode Tank Royale/
├── gui.properties           # GUI settings and preferences
├── server.properties        # Server configuration and secrets
├── game-setups.properties   # Custom game setup configurations
└── recordings/              # Battle recordings (when auto-recording is enabled)
    ├── game-2026-01-23-10-30-45.battle.gz
    ├── game-2026-01-23-11-15-20.battle.gz
    └── ...
```

## Configuration Files

### gui.properties

The `gui.properties` file stores GUI settings and user preferences. This file is automatically created when you first
launch the GUI and is updated whenever you change settings through the GUI Options dialog.

**Location:**

```
{UserDataDirectory}/gui.properties
```

**Example content:**

```properties
# GUI Language
locale=en
# UI Scaling for high-DPI displays
ui-scale=1.0
# Console settings
console-max-characters=10000
# Last used window dimensions and position
window-width=1200
window-height=800
window-x=100
window-y=100
```

**Common settings:**

| Property                 | Description                                | Default |
|:-------------------------|:-------------------------------------------|:--------|
| `locale`                 | GUI language (e.g., `en`, `da`, `de`)      | `en`    |
| `ui-scale`               | UI scaling factor for high-DPI displays    | `1.0`   |
| `console-max-characters` | Maximum characters retained in bot console | `10000` |
| `window-width`           | Main window width in pixels                | `1200`  |
| `window-height`          | Main window height in pixels               | `800`   |

**Editing:**

You can edit this file manually with any text editor, but it's recommended to use the GUI Options dialog (
`Config → GUI Options`) to ensure valid values.

### server.properties

The `server.properties` file contains server configuration, including authentication secrets for bots and controllers.
This file is automatically created when you first start the local server or GUI.

**Location:**

```
{UserDataDirectory}/server.properties
```

**Example content:**

```properties
# Port number for the local server
port=7654
# Bot authentication secret
bots-secrets=/zWlsdEfhNX1YPggA9DJlw
# Controller authentication secret (for GUI and recorder)
controller-secrets=Xcrw0ydtiscD7L7xAT/K4g
# Enable server secrets authentication
enable-server-secrets=false
```

**Common settings:**

| Property                | Description                              | Default        |
|:------------------------|:-----------------------------------------|:---------------|
| `port`                  | WebSocket port for the local server      | `7654`         |
| `bots-secrets`          | Secret for bot authentication            | Auto-generated |
| `controller-secrets`    | Secret for controller/GUI authentication | Auto-generated |
| `enable-server-secrets` | Enable/disable secret authentication     | `false`        |

**Security considerations:**

- The secrets are randomly generated on first run
- Keep these secrets confidential if running a public server
- Bots can provide secrets via the Bot API or the `SERVER_SECRET` environment variable
- Change secrets by editing this file manually or regenerating them through the Server Options dialog

**Editing:**

Edit this file with any text editor. Restart the server for changes to take effect. You can also configure these
settings through the Server Options dialog (`Config → Server Options`).

### game-setups.properties

The `game-setups.properties` file stores custom game configurations created through the Setup Rules dialog. This allows
you to save and reuse battle configurations for different game types.

**Location:**

```
{UserDataDirectory}/game-setups.properties
```

**Example content:**

```properties
# Custom game setup for testing
test-setup.game-type=custom
test-setup.arena-width=800
test-setup.arena-height=600
test-setup.number-of-rounds=5
test-setup.gun-cooling-rate=0.1
test-setup.min-number-of-participants=2
test-setup.max-number-of-participants=4
test-setup.max-inactivity-turns=450
test-setup.turn-timeout=10000
test-setup.ready-timeout=1000000
# Classic melee battle
melee.game-type=melee
melee.number-of-rounds=10
melee.min-number-of-participants=4
melee.max-number-of-participants=10
```

**Common game setup properties:**

| Property                     | Description                        | Typical Values                      |
|:-----------------------------|:-----------------------------------|:------------------------------------|
| `game-type`                  | Type of game                       | `classic`, `melee`, `1v1`, `custom` |
| `arena-width`                | Arena width in pixels              | `800` to `5000`                     |
| `arena-height`               | Arena height in pixels             | `600` to `5000`                     |
| `number-of-rounds`           | Number of rounds                   | `1` to `100+`                       |
| `gun-cooling-rate`           | Gun cooling rate per turn          | `0.1`                               |
| `min-number-of-participants` | Minimum bots required              | `2`                                 |
| `max-number-of-participants` | Maximum bots allowed               | `4` to `16+`                        |
| `max-inactivity-turns`       | Turns before inactivity penalty    | `450`                               |
| `turn-timeout`               | Microseconds per turn before skip  | `10000`                             |
| `ready-timeout`              | Microseconds for bot ready message | `1000000`                           |

**Editing:**

You can create custom game setups through the Setup Rules dialog or by manually editing this file. The GUI will read and
display these configurations in the game type dropdown.

## Recordings Directory

The `recordings` subdirectory stores battle recordings when auto-recording is enabled in the GUI.

**Location:**

```
{UserDataDirectory}/recordings/
```

**File naming:**

Recording files follow this naming pattern:

```
game-{YYYY-MM-DD}-{HH-mm-ss}.battle.gz
```

For example:

```
game-2026-01-23-14-30-45.battle.gz
```

**File format:**

- Files are compressed using gzip (`.gz` extension)
- Contains NDJSON (newline-delimited JSON) format
- Each line represents a game event or state update
- Can be replayed using `Battle → Replay from File...` in the GUI

**Managing recordings:**

- Recordings can accumulate over time and consume disk space
- Delete old recordings manually by removing files from this directory
- Keep recordings you want to analyze or share
- The replay file dialog automatically opens to this directory for easy access

## Backing Up and Migrating

### Backing up your configuration

To back up your Robocode Tank Royale configuration:

1. Locate your user data directory (see platform-specific paths above)
2. Copy the entire directory to a backup location
3. Store the backup in a safe place (cloud storage, external drive, etc.)

### Migrating to a new computer

To migrate your configuration to a new computer:

1. Install Robocode Tank Royale on the new computer
2. Locate the user data directory on the new computer
3. Copy your backed-up configuration files to this directory
4. Replace any existing files when prompted
5. Launch the GUI – your settings should be preserved

**Note:** Bot directories and bot files are not stored in the user data directory. You'll need to:

- Re-download or copy your bot files separately
- Reconfigure bot root directories through `Config → Bot Root Directories`

### Resetting to defaults

To reset Robocode Tank Royale to default settings:

1. Close the GUI completely
2. Locate your user data directory
3. Delete or rename the directory (renaming allows you to restore later)
4. Launch the GUI – a new directory with default settings will be created

## Troubleshooting

### Cannot find user data directory

If you cannot locate the user data directory:

1. Launch the GUI
2. Open `Help → About`
3. Note the version number
4. Check the logs for the exact path being used

Or verify the location programmatically by checking environment variables:

**Windows:**

```cmd
echo %LOCALAPPDATA%\Robocode Tank Royale
```

**macOS/Linux:**

```bash
echo ~/Library/Application\ Support/Robocode\ Tank\ Royale  # macOS
echo ~/.config/robocode-tank-royale                          # Linux
```

### Configuration not being saved

If your configuration changes are not being saved:

1. Check that you have write permissions to the user data directory
2. Verify the directory is not read-only
3. On Windows, ensure the directory is not in a protected location
4. Try running the GUI as an administrator (Windows) or with appropriate permissions

### Corrupted configuration files

If a configuration file becomes corrupted:

1. Close the GUI
2. Delete or rename the problematic `.properties` file
3. Restart the GUI
4. A new file with default values will be created
5. Reconfigure your settings through the GUI

### Old recordings directory

If you upgraded from version 0.34.1 or earlier, you may have recordings in the old location (project's `recordings`
directory). To migrate them:

1. Locate the old recordings directory in your Robocode installation folder
2. Copy all `.battle.gz` files
3. Paste them into the new recordings directory in the user data directory
4. The files will now be accessible when replaying battles

## See Also

- [GUI Documentation](gui.md) – Learn about GUI features and settings
- [Installation Guide](installation.md) – How to install and set up Robocode
- [Server Options](gui.md#server-options) – Configuring server settings
- [Debug Options](gui.md#debug-options) – Development and testing features
