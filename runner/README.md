# Battle Runner API

Run [Robocode Tank Royale](https://github.com/robocode-dev/tank-royale) battles programmatically from any JVM
application. The Battle Runner handles server lifecycle, bot process management, and battle orchestration — you just
provide bot paths and a game setup.

## Features

- **Embedded or external server** — start a managed server automatically, or connect to an existing one
- **Game type presets** — `classic`, `melee`, `1v1`, `custom` with full parameter overrides
- **Synchronous and async APIs** — block until results, or stream real-time events
- **Battle recording** — write `.battle.gz` replay files (same format as the Recorder module)
- **Intent diagnostics** — capture raw `bot-intent` messages per bot per turn via an opt-in WebSocket proxy
- **Multi-battle reuse** — run thousands of battles on the same `BattleRunner` instance without server restarts
- **Identity-based bot matching** — bots are matched by `name`+`version` from their config files, not by connection order; stray bots and duplicate instances are handled correctly
- **Team bot support** — team directories are expanded into one identity per member; member directories are validated at battle-start time
- **Configurable boot timeout** — set how long to wait for bots to connect via `botConnectTimeout(Duration)`
- **Boot progress events** — subscribe to `onBootProgress` on `BattleHandle` for real-time connection status

## Installation

### Gradle (Kotlin DSL)

```kotlin
dependencies {
    implementation("dev.robocode.tankroyale:robocode-tankroyale-runner:0.37.0")
}
```

### Gradle (Groovy)

```groovy
dependencies {
    implementation 'dev.robocode.tankroyale:robocode-tankroyale-runner:0.37.0'
}
```

### Maven

```xml
<dependency>
    <groupId>dev.robocode.tankroyale</groupId>
    <artifactId>robocode-tankroyale-runner</artifactId>
    <version>0.37.0</version>
</dependency>
```

## Quick Start

> **Runnable examples** — see [`examples/`](examples/) for complete Java programs you can run directly
> with `java -cp lib/* Example.java` (no compilation needed).

### Kotlin

```kotlin
import dev.robocode.tankroyale.runner.*

BattleRunner.create { embeddedServer() }.use { runner ->
    val results = runner.runBattle(
        setup = BattleSetup.classic { numberOfRounds = 5 },
        bots  = listOf(BotEntry.of("/path/to/MyBot"), BotEntry.of("/path/to/EnemyBot"))
    )
    results.results.forEach { bot ->
        println("#${bot.rank} ${bot.name} — ${bot.totalScore} pts")
    }
}
```

### Java

```java
import dev.robocode.tankroyale.runner.*;
import java.util.List;

try (var runner = BattleRunner.create(b -> b.embeddedServer())) {
    var results = runner.runBattle(
        BattleSetup.classic(s -> s.setNumberOfRounds(5)),
        List.of(BotEntry.of("/path/to/MyBot"), BotEntry.of("/path/to/EnemyBot"))
    );
    for (var bot : results.getResults()) {
        System.out.printf("#%d %s — %d pts%n", bot.getRank(), bot.getName(), bot.getTotalScore());
    }
}
```

## API Overview

### BattleRunner

The main entry point. Create via `BattleRunner.create()` with a builder configuring server mode and optional features.
Implements `AutoCloseable` — use with Kotlin `use {}` or Java try-with-resources.

| Method | Description |
|--------|-------------|
| `runBattle(setup, bots)` | Runs a battle synchronously, blocking until completion |
| `startBattleAsync(setup, bots)` | Starts a battle and returns a `BattleHandle` for event streaming |
| `intentDiagnostics` | Access captured bot intents (when diagnostics enabled) |
| `close()` | Shuts down server, bot processes, and WebSocket connections |

#### Builder Options

| Method | Description |
|--------|-------------|
| `embeddedServer(port)` | Start a managed server (default: dynamic port) |
| `externalServer(url)` | Connect to a pre-started server |
| `enableIntentDiagnostics()` | Capture bot intents via WebSocket proxy |
| `enableRecording(outputPath)` | Write `.battle.gz` replay files |
| `botConnectTimeout(Duration)` | How long to wait for bots to connect (default: 30 s) |

### BattleSetup

Immutable battle configuration created from game type presets with optional overrides.

```kotlin
// Kotlin
val setup = BattleSetup.classic { numberOfRounds = 10 }
val setup = BattleSetup.melee { arenaWidth = 1200; arenaHeight = 1200 }
val setup = BattleSetup.oneVsOne()
val setup = BattleSetup.custom { arenaWidth = 500; arenaHeight = 500; numberOfRounds = 3 }
```

```java
// Java
var setup = BattleSetup.classic(s -> s.setNumberOfRounds(10));
var setup = BattleSetup.melee(s -> { s.setArenaWidth(1200); s.setArenaHeight(1200); });
var setup = BattleSetup.oneVsOne();
var setup = BattleSetup.custom(s -> { s.setArenaWidth(500); s.setArenaHeight(500); });
```

### BotEntry

Identifies a bot by its directory path. The directory must contain a bot configuration file (`<dir-name>.json`).

```kotlin
val bot = BotEntry.of("/path/to/MyBot")
val bot = BotEntry.of(Path.of("/path/to/MyBot"))
```

### BattleResults / BotResult

Returned after a battle completes, containing per-bot scores ordered by rank.

| Field | Description |
|-------|-------------|
| `rank` | Final placement (1 = winner) |
| `totalScore` | Sum of all scoring components |
| `survival` | Score from surviving rounds |
| `bulletDamage` | Score from bullet damage dealt |
| `ramDamage` | Score from ramming damage dealt |
| `firstPlaces` | Number of rounds won |

### BattleHandle (Async API)

Returned by `startBattleAsync()` for real-time event streaming and battle control.

```kotlin
val owner = Any()
runner.startBattleAsync(setup, bots).use { handle ->
    handle.onTickEvent.on(owner) { tick -> println("Turn ${tick.turnNumber}") }
    handle.onRoundEnded.on(owner) { round -> println("Round ${round.roundNumber} ended") }
    val results = handle.awaitResults()
}
```

#### Events

| Event | Description |
|-------|-------------|
| `onTickEvent` | Fires each turn with full game state |
| `onRoundStarted` | Fires when a new round begins |
| `onRoundEnded` | Fires when a round ends |
| `onGameStarted` | Fires when the game starts |
| `onGameEnded` | Fires when the game ends with results |
| `onGameAborted` | Fires when the game is aborted |
| `onGamePaused` | Fires when the game is paused |
| `onGameResumed` | Fires when the game resumes |
| `onBootProgress` | Fires during bot connection phase with identity-aware progress |

#### Control Methods

| Method | Description |
|--------|-------------|
| `pause()` | Pauses the battle |
| `resume()` | Resumes a paused battle |
| `stop()` | Stops the battle |
| `nextTurn()` | Advances one turn while paused (single-step debugging) |
| `setBotPolicy(botId, breakpointEnabled, debuggingEnabled)` | Sets per-bot policy flags (breakpoint mode, debug graphics) |
| `enableDebugMode()` | Puts the server into debug mode — pauses after every turn |
| `disableDebugMode()` | Exits debug mode, returning to normal auto-advancing |

#### Properties

| Property | Description |
|----------|-------------|
| `serverFeatures` | Server capabilities advertised during handshake (e.g. `breakpointMode`) |

## Advanced Usage

### Debug Mode

Enable debug mode to step through a battle one turn at a time. The server pauses after each
turn completes — bots still deliver intents normally and the turn timeout is enforced — then
waits for `nextTurn()` before advancing.

```kotlin
val owner = Any()
runner.startBattleAsync(setup, bots).use { handle ->

    if (handle.serverFeatures?.debugMode == true) {
        handle.enableDebugMode()
    }

    handle.onGamePaused.on(owner) { event ->
        println("Paused after turn (cause: ${event.pauseCause})")
        // inspect state here, then step
        handle.nextTurn()
    }

    handle.awaitResults()
}
```

> **Note:** `resume()` implicitly disables debug mode and returns to normal auto-advancing.
> Use `disableDebugMode()` if you want to exit debug mode without resuming.

### Breakpoint Mode

Enable breakpoint mode for a bot so the server waits for its intent instead of issuing a
`SkippedTurnEvent` when the turn timeout expires. This mirrors what a developer's debugger
would trigger when a breakpoint freezes the bot thread.

```kotlin
val owner = Any()
runner.startBattleAsync(setup, bots).use { handle ->

    // Enable breakpoint mode on connection once we know bot IDs
    handle.onBotListUpdate.on(owner) { update ->
        if (handle.serverFeatures?.breakpointMode == true) {
            update.bots.forEach { bot ->
                handle.setBotPolicy(bot.id, breakpointEnabled = true)
            }
        }
    }

    // Observe breakpoint pauses — pauseCause == "breakpoint"
    handle.onGamePaused.on(owner) { event ->
        println("Game paused: ${event.pauseCause}")
        // The server is waiting for the bot's intent; call resume() to unblock manually
        // or disable breakpoint mode to trigger a skip+resume
    }

    handle.onGameResumed.on(owner) { _ -> println("Game resumed") }

    handle.awaitResults()
}
```

> **Tip:** To simulate a bot with a debugger attached without launching an actual debugger,
> set the `ROBOCODE_DEBUG=true` environment variable when starting the bot process.
> The bot will include `debuggerAttached: true` in its handshake, which the GUI uses to
> auto-enable breakpoint mode.

### External Server

Connect to a pre-started server instead of launching an embedded one:

```kotlin
BattleRunner.create { externalServer("ws://192.168.1.100:7654") }.use { runner ->
    val results = runner.runBattle(BattleSetup.classic(), bots)
}
```

### Battle Recording

Record battles to `.battle.gz` files for later replay in the GUI:

```kotlin
BattleRunner.create {
    embeddedServer()
    enableRecording(Path.of("recordings"))
}.use { runner ->
    runner.runBattle(BattleSetup.classic(), bots)
    // Recording saved to recordings/game-<timestamp>.battle.gz
}
```

### Intent Diagnostics

Capture raw bot intents for debugging and analysis:

```kotlin
BattleRunner.create {
    embeddedServer()
    enableIntentDiagnostics()
}.use { runner ->
    runner.runBattle(setup, bots)
    val store = runner.intentDiagnostics
    // Query captured intents per bot per turn
}
```

### Identity-Based Bot Matching

Bots are matched by `name` and `version` read from their `<dir>.json` config files. This means:
- Stray bots that connect but were not requested are ignored
- If the same bot directory is listed twice, two connections with that identity are required
- Team directories are expanded: each `teamMembers` entry becomes one expected identity
- Member directories are validated at battle-start time — a missing member directory throws `BattleException` immediately

### Configurable Boot Timeout

By default the runner waits 30 seconds for all bots to connect. Override this with `botConnectTimeout(Duration)`:

```kotlin
BattleRunner.create {
    embeddedServer()
    botConnectTimeout(Duration.ofSeconds(60))
}.use { runner ->
    runner.runBattle(BattleSetup.classic(), bots)
}
```

```java
try (var runner = BattleRunner.create(b -> {
    b.embeddedServer();
    b.botConnectTimeout(Duration.ofSeconds(60));
})) {
    runner.runBattle(BattleSetup.classic(), bots);
}
```

### Boot Progress Reporting

Subscribe to `onBootProgress` on the `BattleHandle` to receive real-time connection status while bots are starting up:

```kotlin
val owner = Any()
runner.startBattleAsync(setup, bots).use { handle ->
    handle.onBootProgress.on(owner) { progress ->
        println("Connected ${progress.totalConnected}/${progress.totalExpected}" +
                " (${progress.elapsedMs}ms elapsed)")
        if (progress.pending.isNotEmpty()) {
            println("  Pending: ${progress.pending}")
        }
    }
    handle.awaitResults()
}
```

```java
var owner = new Object();
try (var handle = runner.startBattleAsync(setup, bots)) {
    handle.getOnBootProgress().on(owner, progress -> {
        System.out.printf("Connected %d/%d (%dms elapsed)%n",
                progress.getTotalConnected(), progress.getTotalExpected(),
                progress.getElapsedMs());
    });
    handle.awaitResults();
}
```

### Team Bot Entries

A team directory contains a `<dir>.json` with a `teamMembers` array listing member bot names. Each member must have its own sibling directory with a matching config file. Add the team directory as a single `BotEntry` — the runner expands it automatically:

```kotlin
val bots = listOf(
    BotEntry.of("/path/to/MyTeam"),   // expands to one entry per team member
    BotEntry.of("/path/to/EnemyBot")
)
```

### Multi-Battle Benchmarking

Run many battles efficiently on the same server:

```kotlin
BattleRunner.create { embeddedServer() }.use { runner ->
    repeat(1000) { i ->
        val results = runner.runBattle(BattleSetup.classic { numberOfRounds = 10 }, bots)
        println("Battle $i: winner = ${results.results.first().name}")
    }
}
```

## Architecture

The Battle Runner composes existing Tank Royale components rather than embedding game logic:

- **ServerManager** — Starts/stops the embedded server process (or validates external server)
- **BooterManager** — Launches and manages bot processes via the Booter
- **ServerConnection** — Dual WebSocket client (Observer + Controller) for event streaming and battle control
- **IntentDiagnosticsProxy** — Optional transparent WebSocket proxy for capturing bot intents

All battles run at TPS = -1 (unlimited speed) since there is no GUI rendering.

## Requirements

- Java 11 or newer
- Bot directories with valid bot configuration files

## License

[Apache License 2.0](../LICENSE)
