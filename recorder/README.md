# Recorder

This module contains the **recorder** used for recording games from the running server. The recorder saves the game
events to a local file, which can be used for later playback.

The recorder is a command-line [Java] application that can be run independently of the Robocode GUI. The recorder is
coded with the [Kotlin] programming language.

## How games are recorded

When the recorder is started, it connects to the server via WebSocket and listens for game events. The recorder saves
the game events to a local file in [ND-JSON] format that can be used for later playback.

When the recorder is running, it will record all game events until it is stopped or the server is disconnected. The
individual game events (starting with `GameStartedEventForObserver` and ending with `GameResultsForObserver`) are
written to the file as they occur. Control events (like `GamePausedEventForObserver` and `GameResumedEventForObserver`)
are not recorded, as the features like pausing, resuming, changing TPS, rewind, etc., should be controlled by the
viewer.

## Running the recorder

The recorder is run using the `java` command from the command line:

```
java -jar robocode-tankroyale-recorder-x.y.z.jar
```

## Options

The server has these options:

- `-h` or `--help` to show the help message.
- `-v` or `--version` to show the version information.
- `-u` or `--url=<url>` to specify the server URL (default: ws://localhost:7654).
- `-s` or `--secret=<secret>` to provide a secret if connecting to a secured server.
- `-d` or `--dir=<dir>` to specify the directory where recordings should be saved (default: current dir).

The options and commands are provided after the `java -jar robocode-tankroyale-recorder-x.y.z.jar` part like this:

```
java -jar robocode-tankroyale-recorder-x.y.z.jar --help
```

### The `q` stdin command

The `q` is used for quitting recorder.

[Java]: https://www.oracle.com/java/ "Java platform"

[Kotlin]: https://kotlinlang.org/ "Kotlin programming language"

[ND-JSON]: https://en.wikipedia.org/wiki/JSON_streaming#Newline-delimited_JSON "Newline-delimited JSON"
