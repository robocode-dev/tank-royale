# Schemas

This directory contains the schema of the protocol used by Robocode Tank Royale for network communication.

## Handshakes

Handshakes are used between a client (bot, observer, controller) and the server to exchange metadata about the clients
and server and to notify the server when a client wants to join the server.

Basically, when a client opens a WebSocket connection with the server, the server will send a _server-handshake_
message to the client with information about the server. Then, if the client wants to join the server, it must send a
handshake to the server. The handshake from the client depends on the client type.

### Bot Handshake

- [server-handshake.yaml]()
- [bot-handshake.yaml]()
- [bot-list-update.yaml]()

```mermaid
sequenceDiagram
    Note over Bot: WebSocket connection is opened
    Bot->>Server: <<event>> connection established
    Server->>Bot: server-handshake
    Bot->>Server: bot-handshake
    Note over Server: Produces: <<event>> Bot joined
    Server->>Observer: bot-list-update
    Server->>Controller: bot-list-update
```

### Observer Handshake

- [server-handshake.yaml]()
- [observer-handshake.yaml]()

```mermaid
sequenceDiagram
    Note over Observer: WebSocket connection is opened
    Observer->>Server: <<event>> connection established
    Server->>Observer: server-handshake
    Observer->>Server: observer-handshake
    Note over Server: Produces: <<event>> Observer joined
```

### Controller Handshake

- [server-handshake.yaml]()
- [controller-handshake.yaml]()

```mermaid
sequenceDiagram
    Note over Controller: WebSocket connection is opened
    Controller->>Server: <<event>> connection established
    Server->>Controller: server-handshake
    Controller->>Server: controller-handshake
    Note over Server: Produces: <<event>> Controller joined
```

## Starting a game

The game is started from a controller, which sends a `start-game` message. The `start-game` message contains information
about which bots, selected by the controller, which should participate in the battle. The server sends
an `game-started-event-for-bot` message to all selected bots, and waits for a `bot-ready` message from each bot. If the
bot manage to respond with a `bot-ready` message, it will be a _participant_ of the battle.

Two things can happen. Either enough bots sends back a `bot-ready` event to reach the minimum number of required
participant for a battle (determined by the game rules), and the game will be started. Or the _Ready timer_ times out
and the game will check if there is enough participants to start the game.

When there is enough participants to start the battle, the server sends a `game-started-for-observer` message to all
observers and controllers, and the game will be in _running_ state.

If there is not enough participants for the battle, the _Ready timer_ will time out, and the server return to the state
where it waits for more bots to join the battle, and a controller will need to make a new attempt to start a game.

- [start-game.yaml]()
- [game-started-event-for-bot.yaml]()
- [bot-ready]()

```mermaid
sequenceDiagram
    Note over Server: Server state = WAIT_FOR_PARTICIPANTS_TO_JOIN
    Controller->>Server: start-game
    Note over Server: Server state = WAIT_FOR_READY_PARTICIPANTS
    Server->>Bot: game-started-event-for-bot
    alt if bot is ready
        Bot->>Server: bot-ready
        Note over Server: Bot is a participant
        alt if number of ready participant >= min. required participants
            Note over Server: Server state = GAME_RUNNING
            Server->>Observer: game-started-event-for-observer
            Server->>Controller: game-started-event-for-observer
        end
    else else Ready timer time-out
        Server->>Server: <<event>> Ready timer time-out
        Note over Server: Bot will not participate
        alt if number of ready participant >= min. required participants
            Note over Server: Server state = GAME_RUNNING
            Server->>Observer: game-started-event-for-observer
            Server->>Controller: game-started-event-for-observer
        else else the game is not started
            Note over Server: Server state = WAIT_FOR_PARTICIPANTS_TO_JOIN            
        end
    end
```

## Game is ending

The game is ended because a winner has been found, and results are available. An event is sends to the clients with the
results of the game.

- [game-ended-event-for-bot.yaml]()
- [game-ended-event-for-observer.yaml]()

```mermaid
sequenceDiagram
    Note over Server: Server state = GAME_RUNNING
    Server->>Server: <<event>> game ended
    Server->>Bot: game-ended-event-for-bot
    Server->>Observer: game-ended-event-for-observer
    Server->>Controller: game-ended-event-for-observer
    Note over Server: Server state = GAME_STOPPED
```

## Aborting a game

A controller is stopping the game while it is running. No results will be available when the game was aborted.

- [stop-game.yaml]()
- [game-aborted-event.yaml]()

```mermaid
sequenceDiagram
    Note over Server: Server state = GAME_RUNNING
    Controller->>Server: stop-game
    Server->>Bot: game-aborted-event
    Server->>Observer: game-aborted-event
    Server->>Controller: game-aborted-event
    Note over Server: Server state = GAME_STOPPED
```

## Pausing a game

A controller is pausing the game while it is running. The game will need to be resumed to continue. Note that the bots
are not being notified that the game is paused, but should see the game as running and the next turn to occur as usual.

- [pause-game.yaml]()
- [game-paused-event-for-observers.yaml]()

```mermaid
sequenceDiagram
    Note over Server: Server state = GAME_RUNNING
    Controller->>Server: pause-game
    Server->>Observer: game-paused-event-for-observers
    Server->>Controller: game-paused-event-for-observers
    Note over Server: Server state = GAME_PAUSED
```

## Resuming a paused game

A controller is resuming the game from being paused.

- [resume-game.yaml]()
- [game-resumed-event-for-observers.yaml]()

```mermaid
sequenceDiagram
    Note over Server: Server state = GAME_PAUSED
    Controller->>Server: resume-game
    Server->>Observer: game-resumed-event-for-observers
    Server->>Controller: game-resumed-event-for-observers
    Note over Server: Server state = GAME_RUNNING
```