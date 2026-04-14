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
