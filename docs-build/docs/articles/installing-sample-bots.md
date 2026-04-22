# Installing sample bots

To start battling immediately, download the pre-built sample bot packages from the
[GitHub Releases](https://github.com/robocode-dev/tank-royale/releases).

## How to install sample bots

1. Download the sample bot archive for your preferred language
2. Extract the archive to a directory on your system
3. Note the parent directory containing the bot folders
4. In the GUI, open **Config → Bot Root Directories**
5. Add the extracted parent directory

The bots will then appear in the **Bot Directories** list when you start a battle.

## Available sample bots

| Language           | Archive File                      | Requirements          |
|--------------------|-----------------------------------|-----------------------|
| 🐍 **Python**      | `sample-bots-python-x.y.z.zip`    | Python 3.10 or newer  |
| 🔷 **C#**          | `sample-bots-csharp-x.y.z.zip`    | .NET SDK 8 or newer   |
| ☕ **Java**        | `sample-bots-java-x.y.z.zip`      | Java SDK 11 or newer  |
| 🟦 **TypeScript**  | `sample-bots-typescript-x.y.z.zip`| Node.js 22 or newer   |

Each archive contains its own `README.md` with platform-specific instructions.

## Troubleshooting

If the sample bots do not appear in the GUI, make sure you added the correct parent directory containing all the bot
subdirectories, not one of the individual bot folders.

For the battle setup flow inside the GUI, see [Setting up and starting a battle](gui-battle-setup.md).
