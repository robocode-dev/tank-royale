# Debugging and Bug Hunting

<!-- KEYWORDS: debug, bug, reproduce, Battle Runner, protocol, hanging test -->

## Three-Layer Debugging Strategy

Always start at the lowest layer that can reproduce the bug:

| Layer | Scope | Tools | When to use |
|-------|-------|-------|-------------|
| **1 — Server unit tests** | Physics, collision, scoring | Kotest, direct calls | Deterministic logic bugs |
| **2 — Bot API unit tests** | Validation, intent, events | MockedServer + JUnit/NUnit/pytest/Vitest | API contract bugs |
| **3 — Battle Runner** | Full game end-to-end | BattleRunner API (programmatic) | Timing, multi-bot, visual |

**Full guide:** `docs-internal/DEBUGGING-GUIDE.md`

---

## Layer 1: Server (pure functions, no mocking)

Key pure components to test directly:

| Component | What it does |
|-----------|-------------|
| `CollisionDetector` | Bullet-bot, bullet-bullet, bot-wall, bot-bot |
| `GunEngine` | Fire conditions, gun cooling, bullet creation |
| `Line` | Ray-segment intersection geometry |
| `ModelUpdater` | Turn processing pipeline |

```bash
.\gradlew :server:test --tests "*.CollisionDetectorTest"
```

---

## Layer 2: Bot API (MockedServer)

**Read first:** `bot-api/tests/TESTING-GUIDE.md` (intent-capture protocol)

### Hanging test checklist

1. Is `continueBotIntent()` called? (handler blocks forever without it)
2. Is `resetBotIntentLatch()` called before capture? (stale permits cause races)
3. Using `Bot` not `BaseBot`? Drain automatic intents before capturing.
4. Using `goAsync()` not `go()`? Direct `go()` triggers StopRogueThread/deadlock.
5. Take a thread dump — look for blocked `acquire()`/`Wait()`/`wait()`.

### Quick test commands

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

---

## Layer 3: Battle Runner (live game debugging)

Use **only** when Layers 1–2 cannot reproduce the issue.

- **Debug mode:** step through turns one-at-a-time (`handle.enableDebugMode()`, `handle.nextTurn()`)
- **Breakpoint mode:** server waits for a bot's intent instead of skipping (`handle.setBotPolicy(botId, breakpointEnabled = true)`)
- **Intent diagnostics:** capture raw bot intents per turn (`enableIntentDiagnostics()`)
- **Battle recording:** replay in GUI (`enableRecording(outputPath)`)

**Full API:** `runner/README.md`

### Battle Runner test gotchas

#### `onGameStarted` race condition
`startBattleAsync()` calls `waitForGameStarted()` internally and **unsubscribes before returning**.
Any subscription to `handle.getOnGameStarted()` in test code is too late — the event is already gone.

**Workaround:** collect bot state data for *all* IDs during the battle via `onTickEvent`, then call
`handle.awaitResults()` and use `BattleResults.getResults()` to identify bots by name post-battle.

```java
// Correct: subscribe before awaitResults, look up name after
handle.getOnTickEvent().on(owner, tick -> { /* record by id */ });
BattleResults results = handle.awaitResults();
int botId = results.getResults().stream()
    .filter(r -> "MyBot".equals(r.getName()))
    .mapToInt(BotResult::getId).findFirst().orElse(-1);
```

#### `BotState.name` is nullable — never match bots by name in tick events
`BotState.name` (`String? = null` in Kotlin) is absent from tick data. Always track bots by their
numeric `BotState.id`, which is stable for the lifetime of the game.

#### `BotResult.id` is the same as `BotState.id`
`BotResult.id` (from `BattleResults.getResults()`) is the same participant ID as `BotState.id` in
tick events. Use this to build a name→id mapping after the battle ends.

#### readyTimeout for JVM source-file bots
The default `readyTimeoutMicros = 1_000_000` (1 second) is too tight for bots launched via Java
source-file mode (`java -cp "../lib/*" MyBot.java`), which must compile before connecting.
**Always increase it in tests that boot external JVM bots:**

```java
var setup = BattleSetup.classic(s -> {
    s.setNumberOfRounds(NUM_ROUNDS);
    s.setReadyTimeoutMicros(10_000_000); // 10 s — JVM compile+start
});
```

#### JAR sync after bot-api changes
Two locations must be kept in sync with the freshly built `bot-api-*.jar`:
- `C:\Code\bots\java\lib\` — classpath for external test bots (e.g. TimingBugBot)
- `sample-bots/java/build/archive/lib/` — classpath for SpinBot et al.

Rebuild and copy with:
```powershell
.\gradlew :bot-api:java:jar :runner:copyRunnerJar
```

---

## Protocol Sequence Reference

For message ordering and timing guarantees, always consult:

```
docs-internal/architecture/models/flows/README.md   ← Index of all protocol flow diagrams
```

Key flows: `bot-connection`, `battle-lifecycle`, `turn-execution`, `event-handling`.

---

## Minimal test bots (`bot-api/tests/bots/`)

Minimal bots (Java, C#, Python) live in `bot-api/tests/bots/`. Connect any of them to a running
server to reproduce bot-API bugs in isolation without needing sample bots or the GUI.

---

## Known Bug Pattern: Continuous rate reset after turn 1

**Symptom:** A bot calls `setGunTurnRate(15)` (or `setRadarTurnRate`, `setTurnRate`,
`setTargetSpeed`) in `run()`, the effect is visible on turn 1, then silently stops working from
turn 2 onwards.

**Root cause:** `BaseBotInternals.resetMovement()` nullifies all intent fields once per round
(after the first `sendIntent()`). In continuous mode (`!overrideGunTurnRate`), the old
`update*Remaining()` returned early without re-calling `baseBotInternals.setGunTurnRate()`,
so turns 2+ sent `null` to the server, which the server treats as 0.

**Fix location:** `BotInternals` (all 4 platforms). Store the continuous value in a
`continuousGunTurnRate` field; re-apply it via `baseBotInternals.setGunTurnRate()` at the top
of every `updateGunTurnRemaining()` call when `!overrideGunTurnRate`.

**Diagnostic:** Use Battle Runner intent diagnostics to print `gunTurnRate` per turn.
Turn 1 shows `15.0`, turns 2+ show `null` → confirms this bug.

---

## Known Pitfall: Radar turn rate is cumulative

**Rule:** In Tank Royale, the radar's absolute turn = bodyTurnRate + gunTurnRate + radarTurnRate.
Setting `setRadarTurnRate(X)` adds X *on top of* the gun's rotation — it does **not** set an
absolute rate.

**Consequence:** Calling `setGunTurnRate(15)` + `setRadarTurnRate(15)` makes the radar spin at
30°/turn while the gun spins at 15°/turn. They drift apart; `fire()` in `onScannedBot` will miss
because the gun is no longer pointing where the radar scanned.

**Correct pattern:** To scan and fire in the same direction as the gun, set only
`setGunTurnRate(rate)` and leave `setRadarTurnRate` at 0 (default). This mirrors classic
Robocode's `RateControlRobot`, where the radar follows the gun when `setRadarRotationRate` is
not called.
