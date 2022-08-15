# Booter

A **booter** is provided to boot up bots on a local machine. It comes built-in with the GUI for Robocode Tank Royale.

For the booter to be able to boot up bots written for any programming language, it is looking for script files used for
running the bots. Hence, the booter makes use of filename conventions to locate bot files on a local machine.

## Root directories

A bot **root directory** is top-level directory which is a collection of **bot directories**. For example, the sample
bots is a collection of bot directories containing directories like:

- Corners
- Crazy
- Fire
- MyFirstBot
- ...

Each of the names listed represents a **bot directory**.

Multiple root directories can be supplied to the booter. This could for example be bot root directories for separate
programming languages like e.g. Java and C#.

## Bot directories

A **bot directory** contains all files required to run a specific bot type and perhaps some metadata like a ReadMe file
etc. for the bot.

As minimum these files _must_ be available in a bot directory:

- Script for running the bot, i.e. a **sh** file (macOS and Linux) or **cmd** (Windows) file.
- [JSON config file] that describes the bot, and specify which game types it can handle.

### Base filename

All bot files in a bot directory must share the same common base filename, which _must_ match the filename of the
(parent) bot directory. Otherwise, the game will not be able to locate the bot file(s) as it is looking for filenames
matching the filename of the bot directory. All other files are ignored by the game.

### Example of bot files

Here is an example of files in a bot directory, in this case the Java version of MyFirstBot:

* `MyFirstBot.java` is the Java source file containing the bot program.
* `MyFirstBot.json` is the JSON config file.
* `MyFirstBot.cmd` used for running the bot on Windows.
* `MyFirstBot.sh` used for running the bot on macOS and Linux.

## Script files

The booter will look for script files and look for some that match the OS it is running on. So for macOS and Linux the
booter will try to locate a shell script file (.sh file) with the name _BotName_.sh and with Windows the booter will try
to locate a command script file (.cmd file) with the name _BotName_.cmd.

The script should contain the necessary command for running a bot. For Java-based bots, the *java* command can be used
for running a bot, and for a .Net-based bot the *dotnet* command can be used for running the bot.

The assumption here is the command(s) used within the scripts are available on the local machine running the bots.
Hence, it is a good idea to provide a ReadMe file that describes the required commands that must be installed to run the
script for a bot if other people should be able to run the bot on their system.

## JSON config file

All bot directories must contain a [JSON] file, which is basically a description of the bot.

For example, the bot MyFirstBot is accompanied by a _MyFirstBot.json_ file.

MyFirstBot.json for .Net:

```json
{
  "name": "My First Bot",
  "version": "1.0",
  "authors": "Mathew Nelson, Flemming N. Larsen",
  "description": "A sample bot that is probably the first bot you will learn about.",
  "homepage": "",
  "countryCodes": "us, dk",
  "platform": ".Net 6.0",
  "programmingLang": "C# 10.0",
  "gameTypes": "melee, classic, 1v1"
}
```

These fields are required:

* name
* version
* authors

The remaining fields are all optional, but recommended.

Meaning of each field in the JSON file:

* *name*: is the display name of the bot.
* *version*: is the version of the bot, where [SEMVER] is the recommended format, but not a requirement.
* *authors*: is a comma-separated list with the (full) name of the bot author(s). The name could be a nickname or
  handle.
* *description*: is a brief description of the bot.
* *homepage*: is a link to a web page for the bot.
* *countryCodes*: is a comma-separated list of [Alpha-2] country codes for the bot author(s).
* *platform*: is the platform required for running the bot, e.g. Java 17 or .Net 6.0.
* *programmingLang*: is the programming language used for programming the bot, e.g. C# or Kotlin.
* *gameTypes*: is a comma-separated list containing the [game types](game_types.md) that the bot is supporting, meaning
  that it should
  not play in battles with game types other than the listed ones. When this field is omitted, the bot will participate
  in any type of game.

### Escaping special characters

Note that some characters are reserved in [JSON] and _must_ be escaped within the JSON strings. Otherwise, the config
file for the bot cannot be read properly, and the bot might not boot.

- **Double quote** is replaced with `\"`
- **Backslash** to be replaced with `\\`
- **Newline** is replaced with `\n`
- **Carriage return** is replaced with `\r`
- **Tab** is replaced with `\t`
- **Form feed** is replaced with `\f`
- **Backspace** is replaced with `\b`

[JSON config file]: #json-config-file

[JSON]: https://fileinfo.com/extension/json

[SEMVER]: https://semver.org/

[Alpha-2]: https://www.iban.com/country-codes

