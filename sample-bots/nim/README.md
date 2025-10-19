# Sample bots for Nim

This module contains sample bots for Robocode Tank Royale developed for the Nim programming language.

## Build commands

#### Clean build directory:

```shell
./gradlew :sample-bots:nim:clean
```

#### Build:

```shell
./gradlew :sample-bots:nim:build
```

#### Create archive

```shell
./gradlew :sample-bots:nim:zip
```

The archive will be output to the `build` directory as a zip file named `sample-bots-nim-x.y.z.zip`.

## Running the bots

To run a bot, you need to have Nim installed and the Robocode Tank Royale server running. Use the provided shell scripts:

- On Linux/Mac: `./BotName.sh`
- On Windows: `BotName.cmd`

Make sure the bot API library is available in the `../lib` directory relative to the bot's location.