# Booter

This module contains the *booter* used for booting up bots. The booter starts up one to multiple bots from bot
directories.

The booter is a command-line Java application that can be run independently of the Robocode GUI.

The booter is running on the [Java 11] platform and the [Kotlin] programming language (typically the newest
available version).

## Starting up bots

The booter does not know how to start up a specific bot for a specific platform and programming language, and hence
depends on the scripts for starting up a bot.

Read more details about the conventions for the bot directories [here](../docs/articles/booter.html).

When a bot is started up, it will be joining a server via WebSockets. If the server is not running, the boot procedure
will fail.

## Bot processes

Note that each bot will be run within a dedicated process, and the booter keeps track of all processes it is currently
running. When the booter is closed down, it will automatically stop the running processes.

## Running the booter

The booter is run using java from the command line:

    java -jar robocode-tankroyale-booter-x.y.z.jar

## Main options

The booter has some options to display help, but also version information:

- `-h` or `--help` to show the help message.
- `-V` or `--version` to show the version information.

The options and commands are provided after the `java -jar robocode-tankroyale-booter-x.y.z.jar` part like this:

    java -jar robocode-tankroyale-booter-x.y.z.jar --version

## The `dir` command

The `dir` command is used for listing the bot directories within bot root directories. It needs the absolute file paths
of the root directory containing bot directories.

Here is an example of how the `dir` command can be used:

    java -jar .\robocode-tankroyale-booter-x.y.z.jar dir c:/bots-java c:/bots-csharp

This will list the full file path of all the bot directories found in the two specified root directories `c:/bots-java`
and `c:/bots-csharp`, and list something like this:

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

Note, the `dir` command has a `-T` option available where used for only listing bot directories of bots supporting one
or more specific game types, and can be set to e.g. `-T melee,classic` to list only the game types for `melee`
and `classic`.

Example:

    java -jar .\robocode-tankroyale-booter-x.y.z.jar dir c:/bots-java c:/bots-csharp -T melee,classic

## The `info` command

The `info` command is used for retrieving information about each bot in bot directories. It is reading the JSON config
file for the individual bot and provides the `dir` and `info` for each bot. The JSON format is an array of JSON objects
similar to this:

```json
{
  "dir": "c:\\bots-java\\Corners",
  "info": {
    "name": "Corners",
    "version": "1.0",
    "gameTypes": "melee, classic, 1v1",
    "authors": "Mathew Nelson, Flemming N. Larsen",
    "description": "Moves to a corner, then swings the gun back and forth. If it dies, it tries a new corner in the next round.",
    "homepage": "",
    "countryCodes": "us, dk",
    "platform": "JVM",
    "programmingLang": "Java 11"
  }
}
```

Similar to the `dir` command, the `info` command provides a `-T` option for filtering on game types.

Example:

    java -jar .\robocode-tankroyale-booter-x.y.z.jar info c:/bots-java -T melee,classic

## The `run` command

The `run` command is used for running one or more bots. It works differently than the other commands as it does not
terminate when executed, but will read commands from the _standard input_ ([stdin]) when started, and will first
terminate when it receives an input line with `quit`.

The reason why the booter keeps running is that it starts up a process for each bot it boots. When the booter terminates
it automatically stops all running processes and hence stops running all booted bots.

The `run` command is used like this to run bots:

    java -jar .\robocode-tankroyale-booter-0.10.0.jar run c:\bots-java\Corners c:\bots-java\Target

This will run the two bots located in `c:\bots-java\Corners` and `c:\bots-java\Target`, and write something like this
to [stdout]:

    14808;c:\bots-java\Corners
    22224;c:\bots-java\Target

The booter writes out the process id for the bots is started in this format:

    {pid};{dir}

- `{pid}` is the process id
- `{dir}` is the bot directory

So in the example above, the Corners bots in `c:\bots-java\Corners` were started in a process with process id 14808.

It is possible to see all available [stdin] commands for `run` by writing:

    java -jar .\robocode-tankroyale-booter-x.y.z.jar run --help

### The `run` stdin command

It is possible to boot a bot while the booter is already running, by writing this to its [stdin]:

    run {dir}

Here the `{dir}` is the full file path of the bot directory containing the bot to run.

Example:

    run c:/bots-java/Corners

Executing the `run` stdin command will write out the `{pid};{dir}` information similar to the regular `run` command.

### The `stop` stdin command

It is possible to stop a bot that is run by the booter, by writing this to its [stdin]:

    stop {pid}

Here the `{pid}` is the process id (pid) of the bot to run.

Example:

    stop 12264

Executing the `stop` std command will write out `stopped {pid}`, e.g. `stopped 12264`, which is useful if several bots
are being stopped in parallel and terminated in a different time/order.

### The `quit` stdin command

The `quit` is used for quitting booter (obviously), which will terminate and automatically stop all running bot
processes.


[Java 11]: https://docs.oracle.com/en/java/javase/11/ "Java 11 documentation"

[Kotlin]: https://kotlinlang.org/ "Kotlin programming language"

[stdin]: https://en.wikipedia.org/wiki/Standard_streams#Standard_input_(stdin) "Standard input (stdin)"

[stdout]: https://en.wikipedia.org/wiki/Standard_streams#Standard_output_(stdout) "Standard output (stdout)"
