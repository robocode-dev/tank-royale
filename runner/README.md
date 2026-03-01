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

#### Control Methods

| Method | Description |
|--------|-------------|
| `pause()` | Pauses the battle |
| `resume()` | Resumes a paused battle |
| `stop()` | Stops the battle |
| `nextTurn()` | Advances one turn while paused (single-step debugging) |

## Advanced Usage

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
