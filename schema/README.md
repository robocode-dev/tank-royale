# Schema used for the network communication protocol

This module contains the schema of the protocol used by Robocode Tank Royale for network communication between:

- server
- bots
- observers
- controllers

The and sequence diagrams the game communication is provided [here](schemas/game/README.md)

## Intention and purpose of the protocol

### Loose coupling

The intention with the schema is to achieve [loose coupling] between different platforms and programming languages.
Hence, the server, bots, observers, and controllers can be written for any platform or language. So it should be
possible to change to another technology without breaking changes for the other components participating in the
communication.

### Bot API is using the protocol

But the primary focus has been to make it possible to write bots for any platform and in any programming language. And
nobody is forced to use the provided [Bot API]. The Bot API is provided so people are not forced to handle the communication between a bot and the server by themselves.
Robocode aims towards letting the bot developers and fun
and focus on developing a bot instead of being forced to figure out how to communicate with the server.

### Direct communication

So it is possible to communicate *directly* with the server without an API, and also develop specialized APIs directly on top of the protocol.

However, the [Bot API] suits well for figuring out how the communication between a bot and the server can be done. And it might be easier to create a specialized Bot API on top of an existing API anyhow.

## Server

A server is running the battles with bots as participants. The game state and firing game events are handled entirely by
the server.

Bots must _join_ the server (like a lobby) but are selected to _participate_ in a battle by the server. Hence, when the bot has joined a server, it must wait for the server to let it participate in a battle (like waiting in a lobby).

The server will provide a bot with an event when the bot must be _ready_ to start in a new battle.

## Definitions

### Bots

Bots are participants in battles. And a bot only sends _intents_ on its own behalf and cannot control the game in any way.
And it is not able to observe the entire game state, but only receives events that are relevant for itself. So a bot is
forced to perform _scans_ to receive information about other bots' locations on the battle arena.

### Observers

An _observer_ can be seen as having _read_ access to the state of the game.

Observers receive updates from the server about the current state of the game and events occurring for all bots on the
battle arena. A good example of an observer could be a _window_ used for visualizing battles or a _logger_ used for
recording the battle.

Note that a bot should _never_ be an observer or receive data from an observer as this is considering cheating. The bot
should only receive _bot events_ from the server.

### Controllers

A _controller_ can be seen as having _read_ and _control_ access to the state of the game.

Controllers are used for sending _control commands_ to the server e.g. for stopping, starting, pausing, and resuming the game. But the controller itself has no impact on the game itself, as this is still up to the server to handle.

## About the protocol

### Based on WebSocket protocol

The protocol of Robocode Tank Royale is used for communication over [WebSocket], which is a computer communications
protocol based on [RFC 6455], providing full-duplex communication channels over a single TCP connection.

The [WebSocket] is supported by lots of platforms like Java and .Net, but also JavaScript.  And WebSocket is also based on am RFC standard, which is the primary reason why this communication protocol has been chosen for Robocode Tank Royale.

### Using JSON as message format

The chosen message format is [JSON] ([RFC 7159]), which is supported by lots of platforms and is a human-readable format
making it ideal for debugging the communication between the server and clients.

### Using JSON Schema and YAML for schema definitions

The [JSON Schema] is used for defining the JSON messages. For example, JSON Schema is used for defining the type of each field in a message, but also to tell if a field is optional or required and if there are any constraints on the field. JSON schema also helps with generating
classes to represent each message for each platform.

Even though JSON is the default format for defining JSON schemas, [YAML] is being used for defining the schemas in
Robocode. YAML is easier to read and less verbose than [JSON].


[loose coupling]: https://en.wikipedia.org/wiki/Loose_coupling "Loose coupling on Wikipedia"

[WebSocket]: https://en.wikipedia.org/wiki/WebSocket "WebSocket on Wikipedia"

[RFC 6455]: https://datatracker.ietf.org/doc/html/rfc6455 "The WebSocket Protocol"

[JSON]: https://en.wikipedia.org/wiki/JSON "JSON on Wikipedia"

[RFC 7159]: https://tools.ietf.org/html/rfc7159 "The JavaScript Object Notation (JSON) Data Interchange Format"

[Bot API]: ../bot-api/README.md "Bot API"

[JSON Schema]: https://json-schema.org/ "JSON Schema home"

[YAML]: https://en.wikipedia.org/wiki/YAML "YAML on Wikipedia"
