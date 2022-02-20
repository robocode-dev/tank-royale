# Server

This module contains the server used for running battles. The server controls the rules and maintains the game state,
and send updates to bots, observers, and controllers.

The server is a command-line Java application that can be run independently of the Robocode GUI.

The server is running on the [Java 11] platform and the [Kotlin] programming language (typically the newest available
version).

## Running the server

The server is run using java from the command line:

    java -jar robocode-tankroyale-server-x.y.z.jar

## Options

The server has these options:

- `-h` or `--help` to show the help message.
- `-V` or `--version` to show the version information.
- `-g` or `--games=<gameTypes>` to provide a comma-separated list of game types.
- `-p` or `--port=<port>` to specify the port number of the server.
- `-s` or `--secret=<secret>` to set a client secret for simple access control.

The options and commands are provided after the `java -jar robocode-tankroyale-server-x.y.z.jar` part like this:

    java -jar robocode-tankroyale-server-x.y.z.jar --version

### Game types

The `--games=<gameTypes>` is used for defining which game types the server will support when running. The game types are
described [here](../docs/docs/articles/game_types.md).

Example:

    java -jar robocode-tankroyale-server-x.y.z.jar --game-types classic,melee

Here, the server will support the `classic` and `melee` game types.

If no game type is specified, the server will run with the `classic` game type per default.

### Port

The `--port=<port>` is used for defining which port number the server must use when running.

Example:

    java -jar robocode-tankroyale-server-x.y.z.jar --port=7913

If no port number is specified, the server will run on port number 80 per default.

### Secret

The `--secret=<secret>` is used for only allowing clients (observers, controllers) to join the server if they provide
the same secret as specified with this option.

Example:

        java -jar robocode-tankroyale-server-x.y.z.jar --secret=pyptsRs2eako3G3xa8xBYE1SH0uMrsjI

If no secret is specified, no secret is being used, and any client can join the server.

Hence, if you specify a secret, make sure to provide the secret to the people that need the secret to join with their
clients.
