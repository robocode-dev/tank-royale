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
- `-C` or `--controllersecrets=<secrets>` to provide a comma-separated list of observer/controller secrets for simple
  access control.
- `-B` or `--botsecrets=<secrets>` to provide a comma-separated list of bot secrets for simple access control.

The options and commands are provided after the `java -jar robocode-tankroyale-server-x.y.z.jar` part like this:

    java -jar robocode-tankroyale-server-x.y.z.jar --version

### Game types

The `--games=<gameTypes>` is used for defining which game types the server will support when running. The game types are
described [here](../docs/articles/game_types.html).

Example:

    java -jar robocode-tankroyale-server-x.y.z.jar --game-types classic,melee

Here, the server will support the `classic` and `melee` game types.

If no game type is specified, the server will run with the `classic` game type per default.

### Port

The `--port=<port>` is used for defining which port number the server must use when running.

Example:

    java -jar robocode-tankroyale-server-x.y.z.jar --port=7913

If no port number is specified, the server will run on port number 80 per default.

### Secrets

#### controllerSecrets

The `--controllerSecrets=<secret>` is used for only allowing controllers and observers to join the server if they
provide a secret that is listed with this option.

Example:

        java -jar robocode-tankroyale-server-x.y.z.jar --controllerSecret=yFTFllMU8fX8kaxlgQnV1g

If no secret is specified (default) any controller and observer may join the server.

#### botSecrets

The `--botSecrets=<secret>` is used for only allowing bots to join the server if they provide  if they
provide a secret that is listed with this option.

Example:

        java -jar robocode-tankroyale-server-x.y.z.jar --botSecrets=yijjEugA0zLcgGCO382gCA,7VPOzQaOnQ8HV9d2URHXOw

If no secret is specified (default) any bot may join the server.