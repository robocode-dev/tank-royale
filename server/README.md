# Server

This module contains the server used for running battles. The server controls the rules and maintains the game state,
and send updates to bots, observers, and controllers.

The server is a command-line [Java] application that can be run independently of the Robocode GUI. The server is coded
with the [Kotlin] programming language.

## Running the server

The server is run using the `java` command from the command line:

```
java -jar robocode-tankroyale-server-x.y.z.jar
```

## Options

The server has these options:

- `-h` or `--help` to show the help message.
- `-v` or `--version` to show the version information.
- `-p` or `--port=<port>` to specify the port number of the server or `inherit` to use an inherited socket.
- `-g` or `--games=<gameTypes>` to provide a comma-separated list of game types.
- `-c` or `--controller-secrets=<secrets>` to provide a comma-separated list of observer/controller secrets for simple.
  access control.
- `-b` or `--bot-secrets=<secrets>` to provide a comma-separated list of bot secrets for simple access control.
- `-i` or `--enable-initial-position` to enable bots to set an initial starting position (default: false).
- `-t` pr `--tps` to set the initial Turns Per Second (TPS) in the range [-1..999], where -1 means maximum TPS, and 0 means paused.

The options and commands are provided after the `java -jar robocode-tankroyale-server-x.y.z.jar` part like this:

```
java -jar robocode-tankroyale-server-x.y.z.jar --help
```

## Game types

The `--games=<gameTypes>` is used for defining which game types the server will support when running. The game types are
described [here](../docs/articles/game_types.html).

Example:

```
java -jar robocode-tankroyale-server-x.y.z.jar --game-types classic,melee
```

Here, the server supports the `classic` and `melee` game types.

If no game type is specified, the server will run with the `classic` game type per default.

## Port

The `--port=<port>` is used for defining which port number the server must use when running.

Example:

```
java -jar robocode-tankroyale-server-x.y.z.jar --port=1234
```

If no port number is specified, the server will run on port number 7654 per default.

You can also inherit the socket, and hence the port, from another application, e.g. when using Socket Activation by
setting the port like this: `--port=inherit`.

You can read more about how to set to socket activation with [this guide](docs/systemd-socket-activation.md).

## Secrets

### Controller Secrets

The `--controller-secrets=<secret>` is used restricting access for controllers and observers. When one or more
controller secrets is/are set, a controller or observer must provide the correct (controller) secret when joining the
server.

Example:

```
java -jar robocode-tankroyale-server-x.y.z.jar --controller-secrets=yFTFllMU8fX8kaxlgQnV1g
```

If no secret is specified (default) any controller and observer may join the server.

### Bot Secrets

The `--bot-secrets=<secret>` is used restricting access for bots. When one or more bot secrets is/are set, a bot must
provide the correct (controller) secret when joining the server.

Example:

```
java -jar robocode-tankroyale-server-x.y.z.jar --bot-secrets=yijjEugA0zLcgGCO382gCA,7VPOzQaOnQ8HV9d2URHXOw
```

If no secret is specified (default) any bot may join the server.

[Java]: https://www.oracle.com/java/ "Java platform"

[Kotlin]: https://kotlinlang.org/ "Kotlin programming language"