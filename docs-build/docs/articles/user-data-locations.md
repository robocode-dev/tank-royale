# User data locations

Robocode Tank Royale stores preferences, settings, and recordings in platform-specific user data directories.

## Windows

On Windows, the user data directory is:

```
%LOCALAPPDATA%\Robocode Tank Royale
```

This typically expands to:

```
C:\Users\{YourUsername}\AppData\Local\Robocode Tank Royale
```

To open it:

1. Press `Win + R`
2. Enter `%LOCALAPPDATA%\Robocode Tank Royale`
3. Press Enter

If `%LOCALAPPDATA%` is not set, the directory falls back to:

```
%APPDATA%\Robocode Tank Royale
```

## macOS

On macOS, the user data directory is:

```
~/Library/Application Support/Robocode Tank Royale
```

To open it:

1. Open Finder
2. Press `Cmd + Shift + G`
3. Enter `~/Library/Application Support/Robocode Tank Royale`
4. Press Enter

## Linux

On Linux, the user data directory is:

```
~/.config/robocode-tank-royale
```

If `XDG_CONFIG_HOME` is set, the directory becomes:

```
$XDG_CONFIG_HOME/robocode-tank-royale
```

To open it from a terminal:

```bash
cd ~/.config/robocode-tank-royale
```

## Directory structure

The user data directory contains:

```
Robocode Tank Royale/
├── gui.properties
├── server.properties
├── game-setups.properties
└── recordings/
    ├── game-2026-01-23-10-30-45.battle.gz
    ├── game-2026-01-23-11-15-20.battle.gz
    └── ...
```

For the meaning of the configuration files, see [Configuration files](configuration-files.md).

## Recordings directory

The `recordings` subdirectory stores battle recordings created by the GUI.

Recording files follow this naming pattern:

```
game-{YYYY-MM-DD}-{HH-mm-ss}.battle.gz
```

The files:

- use gzip compression
- contain NDJSON data
- can be replayed from **Battle → Replay from File...**

Recordings can accumulate over time, so delete old files manually when you no longer need them.
