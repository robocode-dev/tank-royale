# Debugging Guide

How to investigate and reproduce bugs in Tank Royale. This guide covers the
tools and techniques available at each layer of the system.

---

## Test layers and what they cover

Tank Royale has three test layers. Most bugs can be isolated and fixed using
only Layers 1 and 2. The Battle Runner (Layer 3) is reserved for scenarios that
cannot be reproduced without a live game.

| Layer | What it tests | Tools | Speed |
|-------|--------------|-------|-------|
| **1 — Server unit tests** | Physics, collision, scoring, turn processing | Kotest, direct function calls | Milliseconds |
| **2 — Bot API unit tests** | Validation, intent building, event handling | JUnit/NUnit/pytest/Vitest + MockedServer | Seconds |
| **3 — Battle Runner** | Full game: server + bots + WebSocket + timing | BattleRunner API (programmatic) | Seconds–minutes |

**Rule of thumb:** always start at the lowest layer that can reproduce the bug.

---

## Layer 1 — Server debugging

Server physics logic is pure (no I/O). To debug a collision, scoring, or
movement bug:

1. Write a Kotest test in `server/src/test/kotlin/` that calls the function
   directly with the problematic inputs.
2. Run it: `.\gradlew :server:test --tests "*.CollisionDetectorTest"`
3. Use IntelliJ's debugger to step through the physics code.

**Key pure components (no mocking needed):**

| Component | File | What it does |
|-----------|------|-------------|
| `CollisionDetector` | `server/.../CollisionDetector.kt` | Bullet-bot, bullet-bullet, bot-wall, bot-bot collisions |
| `GunEngine` | `server/.../GunEngine.kt` | Fire conditions, gun cooling, bullet creation |
| `Line` | `server/.../Line.kt` | Ray-segment intersection geometry |
| `ModelUpdater` | `server/.../ModelUpdater.kt` | Turn processing pipeline |

---

## Layer 2 — Bot API debugging

Bot API tests use a MockedServer to simulate the server without a real
WebSocket connection. See [`bot-api/tests/TESTING-GUIDE.md`](../bot-api/tests/TESTING-GUIDE.md)
for the full intent-capture protocol.

### Quick commands

```bash
# Java
.\gradlew :bot-api:java:test --tests "*.CommandsFireTest"

# C#
.\gradlew :bot-api:dotnet:test --filter "FullyQualifiedName~CommandsFire"

# Python
cd bot-api/python && python -m pytest tests/ -k "test_fire" --timeout=30

# TypeScript
cd bot-api/typescript && npx vitest run --reporter=verbose
```

### Common hanging test diagnosis

When a Bot API test hangs, it is almost always a missed gate call. Checklist:

1. **Is `continueBotIntent()` called?** Without it, the MockedServer handler
   blocks forever.
2. **Is `resetBotIntentLatch()` called before the capture cycle?** Stale
   permits cause races.
3. **Are you using `Bot` (not `BaseBot`)?** `Bot` sends automatic intents
   after each tick — drain them before capturing.
4. **Are you using `goAsync()` (not `go()`)?** `go()` from the test thread
   triggers `StopRogueThread` or deadlocks.
5. **Thread dump.** Look for threads blocked in `acquire()`/`Wait()`/`wait()`
   inside MockedServer gate methods.

---

## Layer 3 — Battle Runner (live game debugging)

Use the Battle Runner when the bug requires real server + bot interaction —
timing-dependent issues, multi-bot coordination, or "it only happens in a real
game."

### When to use the Battle Runner

- Reproduce a user-reported game behavior that unit tests cannot isolate
- Verify that server + Bot API work together end-to-end after a refactor
- Investigate timing-sensitive issues (turn timeouts, skipped turns)
- Watch game replays to understand unexpected visual behavior

### Quick start — programmatic

```kotlin
import dev.robocode.tankroyale.runner.*

BattleRunner.create { embeddedServer() }.use { runner ->
    val results = runner.runBattle(
        setup = BattleSetup.classic { numberOfRounds = 3 },
        bots = listOf(
            BotEntry.of("sample-bots/java/TrackFire"),
            BotEntry.of("sample-bots/java/Walls")
        )
    )
    results.results.forEach { println("#${it.rank} ${it.name} — ${it.totalScore} pts") }
}
```

### Debug mode — step through turns

Debug mode pauses the server after every turn, letting you inspect state before
advancing.

```kotlin
val owner = Any()
runner.startBattleAsync(setup, bots).use { handle ->
    if (handle.serverFeatures?.debugMode == true) {
        handle.enableDebugMode()
    }

    handle.onGamePaused.on(owner) { event ->
        println("Paused after turn (cause: ${event.pauseCause})")
        // Inspect game state here...
        handle.nextTurn()  // Advance one turn
    }

    handle.awaitResults()
}
```

### Breakpoint mode — wait for a specific bot

Enable breakpoint mode so the server waits for a bot's intent instead of
issuing a `SkippedTurnEvent` when the turn timeout expires:

```kotlin
handle.setBotPolicy(botId, breakpointEnabled = true)
```

Tip: set `ROBOCODE_DEBUG=true` env var on the bot process to simulate a
debugger being attached.

### Intent diagnostics — capture raw bot intents

Enable intent diagnostics to capture every bot intent for post-mortem analysis:

```kotlin
BattleRunner.create {
    embeddedServer()
    enableIntentDiagnostics()
}.use { runner ->
    runner.runBattle(setup, bots)
    val store = runner.intentDiagnostics
    // Query: store.getIntents(botId, turnNumber)
}
```

### Battle recording — replay in GUI

Record a battle for visual replay:

```kotlin
BattleRunner.create {
    embeddedServer()
    enableRecording(Path.of("recordings"))
}.use { runner ->
    runner.runBattle(setup, bots)
    // Replay: open recordings/game-<timestamp>.battle.gz in the GUI
}
```

---

## Debugging decision tree

```
Bug reported
  │
  ├─ Is it a physics/collision/scoring issue?
  │    → Layer 1: Write a server unit test with exact inputs
  │
  ├─ Is it a Bot API issue (wrong intent, bad validation, event not firing)?
  │    → Layer 2: Write a MockedServer test on the affected platform
  │
  ├─ Is it timing-dependent or multi-bot?
  │    → Layer 3: Use BattleRunner with debug mode or intent diagnostics
  │
  └─ Is it visual (wrong rendering, UI glitch)?
       → Layer 3: Record a battle, replay in GUI
```

---

## Platform-specific tips

### Java

- Use IntelliJ's debugger for server and Bot API code.
- `.\gradlew :bot-api:java:test --debug-jvm` starts a JDWP listener on port
  5005 for remote attach.

### C\#

- Use `dotnet test --filter "Name~TestName"` for targeted runs.
- Attach Visual Studio or Rider to the test process for breakpoints.

### Python

- `python -m pytest tests/ -k "test_name" -s --timeout=300` disables output
  capture and extends timeout for interactive debugging.
- Use `import pdb; pdb.set_trace()` or `breakpoint()` in test code.
- Beware: Python's asyncio event loop in tests runs on a daemon thread. If
  the test hangs, the `os._exit(0)` fixture in `conftest.py` will force-kill
  the process after all tests finish.

### Kotlin (Server)

- `.\gradlew :server:test --tests "*.TestClassName" --debug-jvm` for JDWP
  debugging.
- Kotest tests can be run directly from IntelliJ with the Kotest plugin.

---

## Related documentation

| Document | What it covers |
|----------|---------------|
| [`bot-api/tests/TESTING-GUIDE.md`](../bot-api/tests/TESTING-GUIDE.md) | Intent-capture protocol, MockedServer gates, Bot vs BaseBot |
| [`bot-api/tests/TEST-REGISTRY.md`](../bot-api/tests/TEST-REGISTRY.md) | Acceptance test IDs and cross-platform coverage |
| [`runner/README.md`](../runner/README.md) | Full Battle Runner API reference |
| [ADR-0037](architecture/adr/0037-functional-core-bot-api-testability.md) | Functional core extraction for Bot API |
| [ADR-0038](architecture/adr/0038-shared-cross-platform-test-definitions.md) | Shared test definitions and parity policy |
| [ADR-0039](architecture/adr/0039-server-testability.md) | Server physics test framework |
