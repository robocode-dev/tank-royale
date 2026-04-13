# Booter

## Introduction

A **booter** is provided to boot up bots on a local machine.

It comes built-in with the GUI for Robocode Tank Royale, but can also run as stand-alone as well, e.g. if no GUI is
being used.

The intention of the booter is to allow booting up bots for any ecosystem and programming language.
To make this possible, the booter uses script files that are responsible for starting up bots for specific programming
languages and systems. The booter needs to locate these script files for each bot, or use **boot templates** if metadata
is provided in a [JSON config file] or if the platform can be detected automatically.

By default, the booter assumes that the main class or script file has the same name as the bot directory.
If no script or JSON file is found, the booter uses **heuristic platform detection** to identify the bot type
(e.g., `.jar` for Java, `.py` for Python, `.csproj` for .NET) and boots it using a default template.

Diagram showing how the booter boots up a bot using either a script file or a boot template:

```mermaid
flowchart TD
    Booter -- locates and runs --> ScriptFile
    ScriptFile -- boots up --> Bot
    Booter -- selects --> Template
    Template -- boots up --> Bot

    ScriptFile[Script file]
    Template[Boot template]
```

## Root directories

A bot **root directory** is top-level directory which is a collection of **bot directories**. For example, the sample
bots is a collection of bot directories containing directories like:

```
[root directory]
├── Corners (a bot directory)
├── Crazy
├── Fire
├── MyFirstBot
...
```

Each of the directory names listed represents a **bot directory**.

Multiple root directories can be supplied to the booter. This could for example be bot root directories for separate
programming languages like for example Java and C#.

## Bot directories

A **bot directory** contains all files required to run a specific bot type and perhaps some metadata like a ReadMe file
etc. for the bot.

To be discoverable by the booter, a bot directory should contain one of the following:

- A [JSON config file] (e.g., `MyBot.json`) that describes the bot.
- A script for running the bot: `MyBot.sh` (macOS/Linux) or `MyBot.cmd` (Windows).
- A platform-specific project or binary file: `MyBot.jar` (Java), `MyBot.py` (Python), or `MyBot.csproj` (C#).

If only a project or binary file is present, the booter will use **heuristic platform detection** to select a
**boot template** and start the bot with default metadata. In this case, the bot **must** set its required
properties (`name`, `version`, `authors`) programmatically in its code.

If the bot does not provide these properties at runtime and no JSON file is present, a `BotException`
will be thrown when the bot tries to connect to the server.

### Base filename

All bot files inside a bot directory must share the same common base filename, which _must_ match the filename of the
(parent) bot directory. Otherwise, the game will not be able to locate the bot file(s) as it is looking for filenames
matching the filename of the bot directory. All other files are ignored by the booter.

### Example of bot files

Here is an example of files contained in a bot directory for the Java version of MyFirstBot:

* `MyFirstBot.java` is the Java source file containing the bot program.
* `MyFirstBot.json` is the JSON config file.
* `MyFirstBot.cmd` used for running the bot on Windows.
* `MyFirstBot.sh` used for running the bot on macOS and Linux.
* `ReadMe.md` is a ReadMe file used for instructions for how to run the bot.

For a C# (.NET) bot, the files might look like this:

* `MyFirstBot.cs` is the C# source file.
* `MyFirstBot.csproj` is the project file, which is required if the bot is distributed as source-only.
* `MyFirstBot.json` is the JSON config file.
* `NuGet.Config` is used to specify where to find dependencies (like the Bot API).

For a TypeScript bot, the files might look like this:

* `MyFirstBot.ts` is the TypeScript source file containing the bot program.
* `MyFirstBot.json` is the JSON config file.
* `MyFirstBot.cmd` used for running the bot on Windows.
* `MyFirstBot.sh` used for running the bot on macOS and Linux.

> **Note for TypeScript bots:** Unlike Java, Python, and .NET, the booter has no automatic platform detection
> for TypeScript (`.ts`) files. TypeScript bots must always provide a JSON config file **or** `.cmd`/`.sh` script
> files so the booter can locate and start them.

## Script files

The booter will look for script files and look for some that match the OS it is running on. So for macOS and Linux the
booter will try to locate a shell script file (.sh file) with the name _BotName_.sh and with Windows the booter will try
to locate a command script file (.cmd file) with the name _BotName_.cmd.

If no such script file exists, the booter will attempt to use a built-in **boot template** for the platform and
programming language specified in the [JSON config file].

By default, the booter assumes that the `base` property (the entry point) matches the name of the bot directory.
If the entry point is different, it can be explicitly specified using the `base` property in the JSON file.

The script should contain the necessary command for running a bot. For Java-based bots, the `java` command can be used
for running a bot, and for a .NET-based bot the `dotnet` command can be used for running the bot.

The assumption here is the command(s) used within the scripts are available on the local machine running the bots.
Hence, it is a good idea to provide a ReadMe file that describes the required commands that must be installed to run the
script for a bot if other people should be able to run the bot on their system.

## JSON config file

All bot directories must contain a [JSON] file, which is basically a description of the bot (or team),
unless the bot sets all its properties programmatically in its code and provides enough files (like `.jar`, `.py`,
or OS-specific scripts) for the booter to identify the platform.

For example, the bot MyFirstBot is accompanied by a **MyFirstBot.json** file.

MyFirstBot.json for .NET:

```json{2-7}
{
  "name": "My First Bot",
  "version": "1.0",
  "authors": [
    "Mathew Nelson",
    "Flemming N. Larsen"
  ],
  "description": "A sample bot that is probably the first bot you will learn about.",
  "homepage": "",
  "countryCodes": [
    "us",
    "dk"
  ],
  "platform": ".NET 6.0",
  "programmingLang": "C# 10.0",
  "initialPosition": "50,50, 90"
}
```

These fields are required:

* name
* version
* authors

The remaining fields are all optional, but recommended.

Meaning of each field in the JSON file:

- `name`: is the display name of the bot.
- `version`: is the version of the bot, where [SEMVER] is the recommended format, but not a requirement.
- `authors`: is a list containing the (full) name of the bot author(s). The name could be a nickname or handle.
- `description`: is a brief description of the bot.
- `homepage`: is a link to a web page for the bot.
- `countryCodes`: is a list containing [Alpha-2] country codes, representing the country of each author and/or bot.
- `platform`: is the platform required for running the bot, e.g. Java 17 or .NET 8.
- `programmingLang`: is the programming language used for programming the bot, e.g. C# or Kotlin.
- `gameTypes`: is a comma-separated list containing the [game types](game_types.md) that the bot is supporting, meaning
  that it should
  not play in battles with game types other than the listed ones. When this field is omitted, the bot will participate
  in any type of game.
- `initialPosition`: is a comma-separated string containing the starting x and y coordinate, and direction
  (body, gun, and radar) when the game begins in the format: x, y, direction. [^initial-start-position]
- `base`: is the entry point for the bot (e.g. the main class name for Java or the script name for Python). This property
  is optional and defaults to the name of the bot's parent directory if omitted. Providing either an explicit `base`
  property or relying on the directory name convention allows you to use **boot templates** and omit the OS-specific
  script files (.cmd and .sh).
 
Note that `initialPosition` should only be used for debugging purposes where using the same starting position and
direction of the body, gun, and radar is convenient. You need to enable initial starting position using
the `--enable-initial-position` option with the [server].

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

## Team directories

With Robocode, bots can be grouped together into teams. Teams are defined in a similar way as bots. Teams use
directories as well, where the name of the team directory is the same as team name, e.g., MyFirstTeam. And a JSON file
is needed to define the team.

MyFirstTeam.json:

```json{14-20}
{
  "name": "MyFirstTeam",
  "version": "1.0",
  "authors": [
    "Mathew Nelson",
    "Flemming N. Larsen"
  ],
  "description": "A sample team.\nMyFirstLeader scans for enemies,\nand orders the droids to fire.",
  "homepage": "",
  "countryCodes": [
    "us",
    "dk"
  ],
  "teamMembers": [
    "MyFirstLeader",
    "MyFirstDroid",
    "MyFirstDroid",
    "MyFirstDroid",
    "MyFirstDroid"
  ]
}
```

Notice the `teamMembers` field, which contains the name of each member bot. Each member must reside in a bot directory
next to the team directory so that the booter is able to locate the bots.

With the MyFirstTeam, the first listed member is MyFirstLeader, and then we have 4 more bots named MyFirstDroid.
This means that the team contains 5 members in total.

Note that most fields are the same as used for defining bots. But these fields are not used for teams:

- `countryCodes`
- `platform`
- `programmingLang`
- `gameTypes`
- `initialPosition`

Also note that only the JSON file is needed for defining the team.

[JSON config file]: #json-config-file "JSON config file"

[JSON]: https://fileinfo.com/extension/json "JSON (JavaScript Object Notation File)"

[SEMVER]: https://semver.org/ "Semantic Versioning 2.0.0"

[Alpha-2]: https://www.iban.com/country-codes "Alpha-2 country codes"

[server]: https://github.com/robocode-dev/tank-royale/tree/master/server#readme "Server"
