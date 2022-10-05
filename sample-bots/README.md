# Sample bots

This module contains sample bots for various programming languages and platforms, which have been put into separate
directories.

The purpose of the sample bots is to provide the bot developers with some examples of bots that have been developed for
Robocode, which demonstrates different parts of the Bot API.

## Conventions

#### All standard sample bots must be present

The bots are considered the standard sample bots for Robocode. And _all_ of the sample bots must be present for each
archive containing sample bots for a specific language and platform.

#### Bots must work the same way between language and platform

A specific sample bot like e.g. Corners or Walls must work the same way between different languages and platforms.

#### Prefer running the bots from source files

Sample bots for C#/.Net and Java/JVM are provided as source files instead of binary versions. The reasons are:

- Making it easy and quick to make modifications to a bot using a text editor without a compiler or IDE.
- Keeping the size of the sample archive as small using source files instead of pre-compiled binary files.

## Directory structure

Each directory containing sample bots should contain these directories:

Assets:

- assets

Sample bots:

- Corners
- Crazy
- Fire
- MyFirstBot
- RamFire
- SpinBot
- Target
- TrackFire
- Walls

## ReadMe.md file

Make sure to keep the `/assets/ReadMe.md` file up-to-date as this one is distributed with the zip archive containing the
sample bots providing instructions for how to run the sample bots.

## Build commands

#### Clean build all sample bot directories:

```shell
gradle clean
```

#### Build/compile all sample bot archives:

```shell
gradle zip
```
