# Battle Runner API

The **Battle Runner API** lets you run Robocode Tank Royale battles programmatically from any JVM application — no GUI
required. Use it for automated testing, benchmarking, tournament systems, or any scenario where you need headless battle
execution.

::: info Java/JVM only
The Battle Runner is currently available for **Java and Kotlin** (any JVM language) only. Python and C# ports do not
exist yet. If you need support for another platform, please open a
[feature request on GitHub](https://github.com/robocode-dev/tank-royale/issues).
:::

## Installation

::: code-group

```kotlin [Gradle (Kotlin DSL)]
dependencies {
    implementation("dev.robocode.tankroyale:robocode-tankroyale-runner:0.37.0")
}
```

```groovy [Gradle (Groovy)]
dependencies {
    implementation 'dev.robocode.tankroyale:robocode-tankroyale-runner:0.37.0'
}
```

```xml [Maven]
<dependency>
    <groupId>dev.robocode.tankroyale</groupId>
    <artifactId>robocode-tankroyale-runner</artifactId>
    <version>0.37.0</version>
</dependency>
```

:::

## Quick Start

::: code-group

```kotlin [Kotlin]
import dev.robocode.tankroyale.runner.*

BattleRunner.create { embeddedServer() }.use { runner ->
    val results = runner.runBattle(
        setup = BattleSetup.classic { numberOfRounds = 5 },
        bots  = listOf(BotEntry.of("/path/to/MyBot"), BotEntry.of("/path/to/EnemyBot"))
    )
    results.results.forEach { bot ->
        println("#${bot.rank} ${bot.name} - ${bot.totalScore} pts")
    }
}
```

```java [Java]
import dev.robocode.tankroyale.runner.*;
import java.util.List;

try (var runner = BattleRunner.create(b -> b.embeddedServer())) {
    var results = runner.runBattle(
        BattleSetup.classic(s -> s.setNumberOfRounds(5)),
        List.of(BotEntry.of("/path/to/MyBot"), BotEntry.of("/path/to/EnemyBot"))
    );
    for (var bot : results.getResults()) {
        System.out.printf("#%d %s - %d pts%n", bot.getRank(), bot.getName(), bot.getTotalScore());
    }
}
```

:::

## Features

- **Embedded or external server** — start a managed server automatically, or connect to an existing one
- **Game type presets** — `classic`, `melee`, `1v1`, `custom` with full parameter overrides
- **Synchronous and async APIs** — block until results, or stream real-time events
- **Battle recording** — write `.battle.gz` replay files (same format as the Recorder module)
- **Intent diagnostics** — capture raw `bot-intent` messages per bot per turn via an opt-in WebSocket proxy
- **Multi-battle reuse** — run thousands of battles on the same `BattleRunner` instance without server restarts

## Creating a BattleRunner

The `BattleRunner` is the main entry point. It manages the server lifecycle, WebSocket connections, and bot processes.
Always use it with `use {}` (Kotlin) or try-with-resources (Java) to ensure proper cleanup.

### Embedded Server (default)

The runner starts and manages its own server process:

::: code-group

```kotlin [Kotlin]
// Dynamic port (recommended)
val runner = BattleRunner.create { embeddedServer() }

// Specific port
val runner = BattleRunner.create { embeddedServer(port = 7654) }
```

```java [Java]
// Dynamic port (recommended)
var runner = BattleRunner.create(b -> b.embeddedServer());

// Specific port
var runner = BattleRunner.create(b -> b.embeddedServer(7654));
```

:::

### External Server

Connect to a pre-started server:

::: code-group

```kotlin [Kotlin]
val runner = BattleRunner.create { externalServer("ws://192.168.1.100:7654") }
```

```java [Java]
var runner = BattleRunner.create(b -> b.externalServer("ws://192.168.1.100:7654"));
```

:::

## Battle Setup

Battle configuration starts from a game type preset. Individual parameters can be overridden.

| Preset | Arena | Min Bots | Max Bots |
|--------|-------|----------|----------|
| `classic` | 800×600 | 2 | — |
| `melee` | 1000×1000 | 10 | — |
| `oneVsOne` | 800×600 | 2 | 2 |
| `custom` | 800×600 | 2 | — |

::: code-group

```kotlin [Kotlin]
val setup = BattleSetup.classic()
val setup = BattleSetup.classic { numberOfRounds = 10 }
val setup = BattleSetup.melee { arenaWidth = 1200; arenaHeight = 1200 }
val setup = BattleSetup.oneVsOne()
val setup = BattleSetup.custom {
    arenaWidth = 500
    arenaHeight = 500
    numberOfRounds = 3
    gunCoolingRate = 0.2
}
```

```java [Java]
var setup = BattleSetup.classic();
var setup = BattleSetup.classic(s -> s.setNumberOfRounds(10));
var setup = BattleSetup.melee(s -> { s.setArenaWidth(1200); s.setArenaHeight(1200); });
var setup = BattleSetup.oneVsOne();
var setup = BattleSetup.custom(s -> {
    s.setArenaWidth(500);
    s.setArenaHeight(500);
    s.setNumberOfRounds(3);
    s.setGunCoolingRate(0.2);
});
```

:::

### Configurable Parameters

| Parameter | Description |
|-----------|-------------|
| `arenaWidth` | Arena width in pixels |
| `arenaHeight` | Arena height in pixels |
| `minNumberOfParticipants` | Minimum bots required to start |
| `maxNumberOfParticipants` | Maximum bots allowed (null = unlimited) |
| `numberOfRounds` | Number of rounds to play |
| `gunCoolingRate` | Rate at which gun heat decreases per turn |
| `maxInactivityTurns` | Max consecutive turns without activity |
| `turnTimeoutMicros` | Per-turn deadline for bot intents (µs) |
| `readyTimeoutMicros` | Deadline for bots to signal ready (µs) |

## Bot Selection

Bots are identified by their directory path. Each directory must contain a bot configuration file named
`<directory-name>.json`.

::: code-group

```kotlin [Kotlin]
val bots = listOf(
    BotEntry.of("/home/user/bots/MyBot"),
    BotEntry.of("/home/user/bots/EnemyBot"),
    BotEntry.of(Path.of("/home/user/bots/AnotherBot")),
)
```

```java [Java]
var bots = List.of(
    BotEntry.of("/home/user/bots/MyBot"),
    BotEntry.of("/home/user/bots/EnemyBot"),
    BotEntry.of(Path.of("/home/user/bots/AnotherBot"))
);
```

:::

## Running Battles

### Synchronous (blocking)

The simplest approach — blocks until the battle completes and returns results:

::: code-group

```kotlin [Kotlin]
val results = runner.runBattle(setup, bots)
println("Winner: ${results.results.first().name}")
```

```java [Java]
var results = runner.runBattle(setup, bots);
System.out.println("Winner: " + results.getResults().get(0).getName());
```

:::

### Asynchronous (event-driven)

For real-time event streaming and battle control:

::: code-group

```kotlin [Kotlin]
val owner = Any()

runner.startBattleAsync(setup, bots).use { handle ->
    handle.onTickEvent.on(owner) { tick ->
        println("Turn ${tick.turnNumber}: ${tick.botStates.size} bots alive")
    }
    handle.onRoundEnded.on(owner) { round ->
        println("Round ${round.roundNumber} ended")
    }

    val results = handle.awaitResults()
}
```

```java [Java]
var owner = new Object();

try (var handle = runner.startBattleAsync(setup, bots)) {
    handle.getOnRoundStarted().on(owner, event ->
            System.out.printf("Round %d started%n", event.getRoundNumber()));
    handle.getOnRoundEnded().on(owner, event ->
            System.out.printf("Round %d ended (turn %d)%n",
                    event.getRoundNumber(), event.getTurnNumber()));

    var results = handle.awaitResults();
}
```

:::

### Battle Handle Events

| Event | Description |
|-------|-------------|
| `onTickEvent` | Fires each turn with full game state |
| `onRoundStarted` | Fires when a new round begins |
| `onRoundEnded` | Fires when a round ends |
| `onGameStarted` | Fires when the game starts (all bots ready) |
| `onGameEnded` | Fires when the game ends with final results |
| `onGameAborted` | Fires when the game is aborted |
| `onGamePaused` / `onGameResumed` | Fires on pause/resume |
| `onBotListUpdate` | Fires when the connected bot list changes |

### Battle Handle Controls

| Method | Description |
|--------|-------------|
| `pause()` | Pauses the battle |
| `resume()` | Resumes a paused battle |
| `stop()` | Stops the battle |
| `nextTurn()` | Advances one turn while paused (single-step debugging) |

Use the event-driven pattern to react to pause confirmation before stepping — this avoids sending
`nextTurn` before the server has processed the pause:

::: code-group

```kotlin [Kotlin]
val owner = Any()
val controlled = AtomicBoolean()

runner.startBattleAsync(setup, bots).use { handle ->
    // Pause at turn 5 (once)
    handle.onTickEvent.on(owner) { tick ->
        if (tick.turnNumber == 5 && controlled.compareAndSet(false, true)) {
            handle.pause()
        }
    }

    // When paused: step 3 turns, then resume
    handle.onGamePaused.on(owner) { _ ->
        println("  Battle paused - stepping 3 turns manually...")
        handle.nextTurn()
        handle.nextTurn()
        handle.nextTurn()
        println("  Resuming...")
        handle.resume()
    }

    val results = handle.awaitResults()
}
```

```java [Java]
var owner = new Object();
var controlled = new AtomicBoolean();

try (var handle = runner.startBattleAsync(setup, bots)) {
    // Pause at turn 5 (once)
    handle.getOnTickEvent().on(owner, tick -> {
        if (tick.getTurnNumber() == 5 && controlled.compareAndSet(false, true)) {
            handle.pause();
        }
    });

    // When paused: step 3 turns, then resume
    handle.getOnGamePaused().on(owner, event -> {
        System.out.println("  Battle paused - stepping 3 turns manually...");
        handle.nextTurn();
        handle.nextTurn();
        handle.nextTurn();
        System.out.println("  Resuming...");
        handle.resume();
    });

    var results = handle.awaitResults();
}
```

:::



## Results

`BattleResults` contains per-bot scores ordered by final ranking:

::: code-group

```kotlin [Kotlin]
val results = runner.runBattle(setup, bots)
println("Rounds played: ${results.numberOfRounds}")
for (bot in results.results) {
    println("#${bot.rank} ${bot.name} v${bot.version}")
    println("  Total: ${bot.totalScore}, Survival: ${bot.survival}")
    println("  Bullet damage: ${bot.bulletDamage}, Ram damage: ${bot.ramDamage}")
    println("  1st places: ${bot.firstPlaces}")
}
```

```java [Java]
var results = runner.runBattle(setup, bots);
System.out.println("Rounds played: " + results.getNumberOfRounds());
for (var bot : results.getResults()) {
    System.out.printf("#%d %s v%s%n", bot.getRank(), bot.getName(), bot.getVersion());
    System.out.printf("  Total: %d, Survival: %d%n", bot.getTotalScore(), bot.getSurvival());
    System.out.printf("  Bullet damage: %d, Ram damage: %d%n", bot.getBulletDamage(), bot.getRamDamage());
    System.out.printf("  1st places: %d%n", bot.getFirstPlaces());
}
```

:::

## Battle Recording

Record battles to `.battle.gz` files that can be replayed in the GUI:

::: code-group

```kotlin [Kotlin]
BattleRunner.create {
    embeddedServer()
    enableRecording(Path.of("recordings"))
}.use { runner ->
    runner.runBattle(BattleSetup.classic(), bots)
    // Recording saved to recordings/game-<timestamp>.battle.gz
}
```

```java [Java]
try (var runner = BattleRunner.create(b -> {
    b.embeddedServer();
    b.enableRecording(Path.of("recordings"));
})) {
    runner.runBattle(BattleSetup.classic(), bots);
}
```

:::

## Intent Diagnostics

Enable the transparent WebSocket proxy to capture raw `bot-intent` messages for debugging:

::: code-group

```kotlin [Kotlin]
BattleRunner.create {
    embeddedServer()
    enableIntentDiagnostics()
}.use { runner ->
    runner.runBattle(setup, bots)
    val store = runner.intentDiagnostics
    // Query captured intents per bot per turn
}
```

```java [Java]
try (var runner = BattleRunner.create(b -> {
    b.embeddedServer();
    b.enableIntentDiagnostics();
})) {
    runner.runBattle(setup, bots);
    var store = runner.getIntentDiagnostics();
}
```

:::

After the battle completes, query the captured intents from the store:

::: code-group

```kotlin [Kotlin]
val store = runner.intentDiagnostics ?: return
for (botName in store.botNames()) {
    val intents = store.getIntentsForBot(botName)
    println("$botName — ${intents.size} intents")
    for (ci in intents.take(5)) {
        println("  round=${ci.roundNumber} turn=${ci.turnNumber}" +
                " speed=${ci.intent.targetSpeed} fire=${ci.intent.firepower}")
    }
}
```

```java [Java]
var store = runner.getIntentDiagnostics();
if (store == null) return;
for (var botName : store.botNames()) {
    var intents = store.getIntentsForBot(botName);
    System.out.printf("%s — %d intents%n", botName, intents.size());
    for (var ci : intents.subList(0, Math.min(5, intents.size()))) {
        System.out.printf("  round=%d turn=%d speed=%s fire=%s%n",
                ci.getRoundNumber(), ci.getTurnNumber(),
                ci.getIntent().getTargetSpeed(), ci.getIntent().getFirepower());
    }
}
```

:::

::: warning
Intent diagnostics adds an extra network hop between bots and server. Only enable when needed for debugging.
:::

## Multi-Battle Usage

The runner reuses the server across battles — only bot processes are recycled:

::: code-group

```kotlin [Kotlin]
BattleRunner.create { embeddedServer() }.use { runner ->
    repeat(1000) { i ->
        val results = runner.runBattle(
            BattleSetup.classic { numberOfRounds = 10 },
            bots
        )
        println("Battle $i: winner = ${results.results.first().name}")
    }
}
```

```java [Java]
try (var runner = BattleRunner.create(b -> b.embeddedServer())) {
    for (int i = 0; i < 1000; i++) {
        var results = runner.runBattle(
            BattleSetup.classic(s -> s.setNumberOfRounds(10)),
            bots
        );
        System.out.printf("Battle %d: winner = %s%n", i, results.getResults().get(0).getName());
    }
}
```

:::

## Error Handling

The runner throws `BattleException` for all battle-related failures:

- Bot path does not contain a valid configuration file
- Not enough bots to meet the minimum participant count
- Server unreachable (external mode) or failed to start (embedded mode)
- Battle aborted (not enough bots ready)
- Timeout waiting for bots to connect or game to start
- Connection lost during battle

## Runnable Examples

Ready-to-run Java examples are in [`runner/examples/`](https://github.com/robocode-dev/tank-royale/tree/main/runner/examples).
See the [examples README](https://github.com/robocode-dev/tank-royale/tree/main/runner/examples/README.md) for setup instructions.

| Example | Description |
|---------|-------------|
| `RunBattle.java` | Synchronous battle — blocks until done, prints a results table |
| `AsyncBattle.java` | Asynchronous battle — streams round start/end events in real time |
| `RecordBattle.java` | Records a battle to a `.battle.gz` replay file |
| `IntentDiagnosticsBattle.java` | Captures per-turn bot intents and prints a turn-by-turn table |
| `ControlBattle.java` | Pauses at turn 5, steps 3 turns manually, then resumes |

## API Reference

- [Javadoc](https://robocode-dev.github.io/tank-royale/api/runner/index.html)
