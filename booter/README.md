# Booter

This module contains the **booter** used for booting up bots that runs locally only. The booter starts up one to
multiple bots from bot directories (file paths).

The booter is a command-line [Java] application that can be run independently of the Robocode GUI. The booter is coded
with the [Kotlin] programming language.

## How bots are booted

When the booter needs to boot up a bot for a specific platform and programming language, it locates and runs a script
that is provided for the bot to boot it up under macOS, Linux, or Windows. That is a `.sh` file (shell script) for macOS
and Linux, and `.cmd` file (command) for Windows.

File name conventions are used for naming the scripts and the config file for a bootable bot. Read more about the
conventions for the bot directories [here](../docs/articles/booter.html).

When a bot is started up, it joins a server via WebSocket. If the server is not running, the boot procedure will fail.
The same is the case if there is a problem with the script or code for running the bot.

## Bot processes

Note that each bot will run within its own dedicated process. The booter keeps track of all running bot processes.

When the booter terminates, all running bot processes terminate automatically to prevent the bot processes from running
in the background, so they do not waste resources like CPU power and RAM, e.g., if the booter is crashing.

## Running the booter

The booter is run using the `java` command from the command line:

```
java -jar robocode-tankroyale-booter-x.y.z.jar
```

## Main options

The booter has some options to display help, but also version information:

- `-h` or `--help` to show the help message.
- `-V` or `--version` to show the version information.

The options and commands are provided after the `java -jar robocode-tankroyale-booter-x.y.z.jar` part like this:

```
java -jar robocode-tankroyale-booter-x.y.z.jar --version
```

## The `dir` command

The `dir` command lists the bot directories within one or more root directories. It needs the absolute file paths of the
root directory containing bot directories.

Here is an example of using the `dir` command:

```
java -jar .\robocode-tankroyale-booter-x.y.z.jar dir c:/bots-java c:/bots-csharp
```

This will list the full file path of all the bot directories found in the two root directories `c:/bots-java`
and `c:/bots-csharp`, and list something like this:

```
c:\bots-csharp\Corners
c:\bots-csharp\Crazy
c:\bots-csharp\Fire
c:\bots-csharp\MyFirstBot
...
c:\bots-java\Corners
c:\bots-java\Crazy
c:\bots-java\Fire
c:\bots-java\MyFirstBot
c:\bots-java\RamFire
...
```

The `dir` command has the following options:

- `-b` or `--bots-only` to include only bots (not teams) in the listing.
- `-t` or `--teams-only` to include only teams (not bots) in the listing.
- `-g` or `--game-types` to include only bots that support specific game types.

The `-g` and `--game-types` takes a comma-separated list of game types, like in this example, where we only want to list
bot directories for bot supporting the game types `melee` and `classic`.

Example:

```
java -jar .\robocode-tankroyale-booter-x.y.z.jar dir c:/bots-java c:/bots-csharp --game-types=melee,classic
```

Note that if a bot does not specify which game types it supports by leaving out the `gameTypes` field in its JSON file,
the booter automatically include the bot it in the listing as the filtering will not apply to that bot.

## The `info` command

The `info` command lists information about each bot in bot directories. Like the `dir` command, it takes one or more
directories as input, which needs to be the absolute file paths of the root directory containing bot directories.

Here is an example of using the `info` command:

```
java -jar .\robocode-tankroyale-booter-x.y.z.jar info c:/bots-java c:/bots-csharp
```

The `info` command reads the JSON config file for the individual bot and provides the bot directory path (`dir`) and bot
information (`info`) for each bot. The JSON format is an array of bot entries, where each bot entry has a JSON structure
like this:

```json
{
  "dir": "c:\\bots-java\\Corners",
  "info": {
    "name": "Corners",
    "version": "1.0",
    "gameTypes": [
      "melee",
      "classic",
      "1v1"
    ],
    "authors": [
      "Mathew Nelson",
      "Flemming N. Larsen"
    ],
    "description": "Moves to a corner, then swings the gun back and forth. If it dies, it tries a new corner in the next round.",
    "homepage": "",
    "countryCodes": [
      "us",
      "dk"
    ],
    "platform": "JVM",
    "programmingLang": "Java 11"
  }
}
```

Similar to the `dir` command, the `info` command provides these options:

- `-b` or `--bots-only` to include only bots (not teams) in the listing.
- `-t` or `--teams-only` to include only teams (not bots) in the listing.
- `-g` or `--game-types` to include only bots that support specific game types.

Example:

```
java -jar .\robocode-tankroyale-booter-x.y.z.jar info c:/bots-java --game-types=melee,classic
```

## The `run` command

The `run` command boots and runs one or more bots. The directory file path for each bot to boot must be provided as a
list of file path arguments to the command.

The `run` command works differently than the other commands as it does not terminate when executed. When the command
starts, it reads commands from the _standard input_ ([stdin]). And it only terminates when it receives an input line
with the command `quit`.

The `run` command is used like this to run bots:

```
java -jar .\robocode-tankroyale-booter-0.10.0.jar run c:\bots-java\Corners c:\bots-java\Target
```

This will run the two bots located in `c:\bots-java\Corners` and `c:\bots-java\Target`, and write something like this
to [stdout]:

```
8072;c:\bots-java\Corners
20336;c:\bots-java\Target
```

The booter writes out the pid (process id) for each bot that has booted in this format:

```
{pid};{dir}
```

- `{pid}` is the process id.
- `{dir}` is the bot directory.

So in the example above, the bot named Corners located in `c:\bots-java\Corners` booted in a process with pid 8072. And
another bot named Target located in `c:\bots-java\Target` was booted in a process with pid 20336.

It is possible to see all available [stdin] commands for the `run` command by writing:

```
java -jar .\robocode-tankroyale-booter-x.y.z.jar run --help
```

### The `run` stdin command

It is possible to boot a bot while the booter is already running, by writing this to its [stdin]:

```
run {dir}
```

Here the `{dir}` is the full file path of the bot directory containing the bot to run.

Example:

```
run c:/bots-java/Corners
```

Executing the `run` stdin command will write out the `{pid};{dir}` information similar to the regular `run` command.

### The `stop` stdin command

It is possible to stop a bot that is run by the booter, by writing this to its [stdin]:

```
stop {pid}
```

Here the `{pid}` is the process id (pid) of the bot to stop.

Example:

```
stop 8072
```

Executing the `stop` std command will write out `stopped {pid}`, e.g. `stopped 8072`, which is useful if several bots
are being stopped in parallel and terminated in a different time/order.

### The `quit` stdin command

The `quit` is used for quitting booter (obviously), which will terminate and automatically stop all running bot
processes.

[Java]: https://www.oracle.com/java/ "Java platform"

[Kotlin]: https://kotlinlang.org/ "Kotlin programming language"

[stdin]: https://en.wikipedia.org/wiki/Standard_streams#Standard_input_(stdin) "Standard input (stdin)"

[stdout]: https://en.wikipedia.org/wiki/Standard_streams#Standard_output_(stdout) "Standard output (stdout)"
