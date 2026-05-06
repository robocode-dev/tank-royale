# Backing up and resetting

This guide covers backup, migration, reset, and common troubleshooting tasks for Robocode Tank Royale user data.

## Backing up your configuration

1. Locate your user data directory
2. Copy the entire directory to a backup location
3. Store the backup somewhere safe

For the exact paths, see [User data locations](user-data-locations.md).

## Migrating to a new computer

1. Install Robocode Tank Royale on the new computer
2. Locate the user data directory on the new computer
3. Copy your backed-up configuration files into that directory
4. Replace existing files if prompted
5. Launch the GUI

Bot directories and bot files are not stored in the user data directory, so you must copy or download those separately
and then reconfigure **Bot Root Directories**.

## Resetting to defaults

1. Close the GUI completely
2. Locate your user data directory
3. Delete or rename the directory
4. Launch the GUI again

A new directory with default settings is created automatically.

## Troubleshooting

### Cannot find the user data directory

If you cannot locate the directory:

1. Launch the GUI
2. Open **Help → About**
3. Check the runtime details and logs for the path in use

Or verify the environment-based location directly:

**Windows**

```cmd
echo %LOCALAPPDATA%\Robocode Tank Royale
```

**macOS and Linux**

```bash
echo ~/Library/Application\ Support/Robocode\ Tank\ Royale
echo ~/.config/robocode-tank-royale
```

### Configuration is not being saved

Check that:

1. you have write permission to the user data directory
2. the directory is not read-only
3. the GUI is not running in a protected location

### Corrupted configuration files

If a `.properties` file becomes corrupted:

1. Close the GUI
2. Delete or rename the problematic file
3. Restart the GUI
4. Reconfigure the affected settings

### Migrating old recordings

If you upgraded from version 0.34.1 or earlier, you might still have recordings in the old project-level
`recordings` directory.

To migrate them:

1. Locate the old `recordings` directory in your Robocode installation folder
2. Copy all `.battle.gz` files
3. Paste them into the recordings directory under the user data directory
