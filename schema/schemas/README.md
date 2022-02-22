# Schemas

This directory contains the schema of the protocol used by Robocode Tank Royale for network communication.

## Joining and leaving server

Handshakes are used between a client (bot, observer, controller) and the server to exchange metadata about the clients
and server and to notify the server when a client wants to join the server.

Basically, when a client opens a WebSocket connection with the server, the server will send a _server-handshake_
message to the client with information about the server. Then, if the client wants to join the server, it must send a
handshake to the server. The handshake from the client depends on the client type.

### Bot joining

The bot handshake must be sent by a bot to join the server.

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

### Bot leaving

A bot will be leaving a server when it closes its connection to the server. 

- [bot-list-update.yaml]()

```mermaid
sequenceDiagram
    Bot->>Server: <<event>> disconnected
    Note over Server: Produces: <<event>> Bot left
    Server->>Observer: bot-list-update
    Server->>Controller: bot-list-update
```

### Observer joining

The observer handshake must be sent by an observer to join the server.

- [server-handshake.yaml]()
- [observer-handshake.yaml]()

```mermaid
sequenceDiagram
    Observer->>Server: <<event>> connection established
    Server->>Observer: server-handshake
    Observer->>Server: observer-handshake
    Note over Server: Produces: <<event>> Observer joined
```

### Controller joining

The controller handshake must be sent by a controller to join the server.

- [server-handshake.yaml]()
- [controller-handshake.yaml]()

```mermaid
sequenceDiagram
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
        Note over Server: Start turn timeout timer
    else else Ready timer time-out
        Server->>Server: <<event>> Ready timer time-out
        Note over Server: Bot will not participate
        alt if number of ready participant >= min. required participants
            Note over Server: Server state = GAME_RUNNING
            Server->>Observer: game-started-event-for-observer
            Server->>Controller: game-started-event-for-observer
            Note over Server: Start turn timeout timer
        else else the game is not started
            Note over Server: Server state = WAIT_FOR_PARTICIPANTS_TO_JOIN            
        end
    end
```

## Running next turn

Running the next turn is the main loop in the game. The server sends _tick events_ for all clients which contains the
current game state for the observers, and the bot state for the bots.

This is the crucial part for the bots, and these need to sent their _bot intent_ before the turn timeout occurs.

- [round-started-event.yaml]()
- [round-ended-event.yaml]()
- [tick-event-for-bot.yaml]()
- [tick-event-for-observer.yaml]()
- [bot-intent.yaml]()
- [skipped-turn-event.yaml]()

```mermaid
sequenceDiagram
    Note over Server: Server state = GAME_RUNNING
    Server->>Server: <<event>> next turn
    Note over Server: Reset turn timer
    alt if first round
        Server->>Bot: round-started-event
        Server->>Observer: round-started-event
        Server->>Controller: round-started-event
    else if previous round has ended
        Server->>Bot: round-ended-event
        Server->>Observer: round-ended-event
        Server->>Controller: round-ended-event
    end
    Server->>Bot: tick-event-for-bot
    Server->>Observer: tick-event-for-observer
    Server->>Controller: tick-event-for-observer
    Bot->>Server: bot-intent
    Note over Server: Bot will not skip this turn
    Server->>Server: Turn timeout
    opt if bot did not send intent before turn timeout
        Server->>Bot: skipped-turn-event
    end
```

## Game is ending

The game is ended because a winner has been found, and results are available. An event is sends to the clients with the
results of the game.

- [game-ended-event-for-bot.yaml]()
- [game-ended-event-for-observer.yaml]()
- [won-round-event.yaml]()

```mermaid
sequenceDiagram
    Note over Server: Server state = GAME_RUNNING
    Server->>Server: <<event>> game ended
    opt if bot won round
        Server->>Bot: won-round-event
    end
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

## Changing the TPS

A controller can change the [TPS] (Turns Per Second) for a battle.

- [change-tps.yaml]()
- [tps-changed-event.yaml]()

```mermaid
sequenceDiagram
    Controller->>Server: change-tps
    Server->>Observer: tps-changed-event
    Server->>Controller: tps-changed-event
```

[TPS]: ../../docs/docs/articles/tps.md "TPS (Turns Per Second)"