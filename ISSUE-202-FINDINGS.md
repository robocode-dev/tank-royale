# Issue #202 — First-Turn Skip Bug

**Last updated:** 2026-04-12
**GitHub issue:** https://github.com/robocode-dev/tank-royale/issues/202
**Status:** ✅ Fix applied to Java, Python, and TypeScript — pending commit and tests

> ⚠️ **Java is the reference implementation.**
> C#, Python, and TypeScript must mirror the Java implementation exactly.
> Any bug fixed in Java must be applied to all other languages in the same way.

---

## Symptoms

1. **`SkippedTurnEvent` raised on tick 1** — bot runs out of time before the first tick begins
2. **Bot does not act on tick 2** — skipped turn causes a cascade miss
3. **`ScannedBotEvent` dispatched one turn late** — radar sweep on tick N triggers event on tick N+2

---

## Root Cause

`BotInternals.onFirstTurn()` started the bot thread the moment tick 1 arrived. Thread startup took
~10–20 ms of the 30 ms budget, causing the server to time out and issue a `SkippedTurnEvent`.

---

## The Fix (Pre-Warm Pattern)

Start the bot thread at `RoundStarted` (before tick 1), then block it until tick 1 arrives.

1. **`BotInternals.onRoundStarted` at P90** — fires after base resets state (P100). Calls `stopThread()` + `startThread(bot)`.
2. **`BotInternals.onFirstTurn`** — only calls `clearRemaining()`. Thread is already running.
3. **`BaseBotInternals.createRunnable()`** — calls `waitUntilFirstTickArrived()` before `bot.run()`. Blocks until `currentTickOrNull != null`, woken by `notifyAll()` from `onNextTurn` (P100).

**Priority order (critical):**

| Priority | Handler | Role |
|----------|---------|------|
| P110 | `BotInternals.onNextTurn` | `clearRemaining()` — captures initial directions |
| P100 | `BaseBotInternals.onNextTurn` | `notifyAll()` — wakes pre-warmed thread |
| P100 | `BaseBotInternals.onRoundStarted` | Resets state |
| P90  | `BotInternals.onRoundStarted` | Pre-warms thread |

---

## Platform Status

| Platform | Fix | Tests | Committed |
|----------|-----|-------|-----------|
| **Java** | ✅ Applied | 282 pass / 0 fail | ❌ Pending |
| **C#** | ✅ Applied | ✅ | ✅ `70ac34bed` |
| **Python** | ✅ Applied | ❌ Pending | ❌ Pending |
| **TypeScript** | ✅ Applied | ❌ Pending | ❌ Pending |

---

## Bugs Fixed

### Bug 1 — `enableEventHandling(false)` called before first tick

`stopThread()` → `enableEventHandling(false)` → `getCurrentTickOrThrow()` → `BotException` when tick
is null at round-started. Tore down the WebSocket connection, blocking `waitUntilFirstTickArrived()` forever.

**Fix:** guard with `getCurrentTickOrNull()` in `BaseBotInternals.java`:
```java
var tick = getCurrentTickOrNull();
eventHandlingDisabledTurn = (tick != null) ? tick.getTurnNumber() : 0;
```
Python and TypeScript already had equivalent null guards.

### Bug 2 — Gson `RuntimeTypeAdapterFactory` conflict on `ScannedBotEvent`

`Event` subclasses inherit a `type` field from `Message`. `RuntimeTypeAdapterFactory.of(Event.class, "type")`
tried to inject a second `type` discriminator during polymorphic serialization, conflicting with the existing field.

**Fix:** `maintainType=true` (third arg to `.of()`) in `GsonFactory.java`.

### Bug 3 — NPE in `BotStateMapper.map` (nullable Boolean unboxing)

`BotState.getIsDroid()` and `getIsDebuggingEnabled()` return `Boolean` (nullable).
Auto-unboxing null to primitive `boolean` threw NPE.

**Fix:** `Boolean.TRUE.equals(source.getIsDroid())` in `BotStateMapper.java`.

### Bug 4 — `try/catch` in `WebSocketHandler.onText` swallowed `BotException`

A debug-era `catch(Throwable t)` in `onText()` swallowed the `BotException` thrown by `validateBotInfo()`
when `BOT_VERSION` env var is missing. `onError()` never fired `ConnectionErrorEvent`, causing test timeouts.

**Fix:** Removed the `try/catch` wrapper from `onText()` and `error.printStackTrace` from `onError()`.

---

## Files Changed

### Java (`bot-api/java/`)
- `internal/BaseBotInternals.java` — pre-warm `waitUntilFirstTickArrived()`; null-safe `enableEventHandling`
- `internal/BotInternals.java` — `onRoundStarted` P90 pre-warm; `onFirstTurn` → `clearRemaining()` only
- `internal/WebSocketHandler.java` — removed debug `try/catch` from `onText`; removed `printStackTrace` from `onError`
- `internal/json/GsonFactory.java` — `maintainType=true` for `RuntimeTypeAdapterFactory`
- `mapper/BotStateMapper.java` — null-safe Boolean unboxing for `isDroid` and `isDebuggingEnabled`
- `test/java/test_utils/MockedServer.java` — added `holdTick()` / `releaseTick()` for test synchronization
- `test/java/dev/robocode/tankroyale/botapi/BotRunFirstTurnTest.java` — new tests 004a and 004b

### Python (`bot-api/python/`)
- `bot_api/bot.py` — `_on_round_started_prewarm` at P90; `_on_first_turn` → `_clear_remaining()` only
- `bot_api/internal/base_bot_internals.py` — `_wait_until_first_tick_arrived()`; call before `bot.run()`

### TypeScript (`bot-api/typescript/`, worktree `C:\Code\tank-royale`)
- `src/internal/BotInternals.ts` — `onRoundStartedPrewarm` at P90; `onFirstTurn` → `clearRemaining()` only (legacy mode still starts thread at tick 1)
- `src/internal/BaseBotInternals.ts` — `isWorkerMode()`; `waitUntilFirstTickArrived()` via `Atomics.wait()`; call before `bot.run()`
