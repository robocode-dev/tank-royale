---
id: ADR-0028
type: decision
status: verified
links: []
title: TypeScript Bot API Threading Model
accepted-by: Flemming N. Larsen (pre-Cliewen MADR acceptance)
---

﻿---
status: accepted
date: 2026-03-24
---

# ADR-0028: TypeScript Bot API Threading Model

## Context

The Java Bot API uses a two-thread model:

1. **WebSocket thread** — Receives tick messages from the server, parses JSON, adds events to the queue, signals the bot thread via `notifyAll()`.
2. **Bot thread** — Runs `bot.run()`, calls `go()` which dispatches events and then blocks on `monitor.wait()` until the next tick arrives.

Blocking movement methods like `forward(distance)` internally loop: call `go()` → `waitForNextTurn()` → check if movement complete → repeat. This provides a simple, synchronous programming model where `forward(100)` blocks until the bot has moved 100 units.

**Problem:** JavaScript is single-threaded. There is no `Thread`, `monitor.wait()`, or `notifyAll()`. How should the TypeScript Bot API (ADR-0027) replicate Java's blocking movement semantics?

**Constraints:**

- Must maintain 1:1 semantic equivalence with Java (ADR-0004)
- ADR-0003 states: "Different blocking behavior (`forward()` must block on ALL platforms) is unacceptable"
- The developer-facing API must be familiar across languages — no `async`/`await` leaking into bot code
- Must work in Node.js and browsers (ADR-0029)
- Event dispatch order must match Java exactly

---

## Decision

Use **Web Workers** (`worker_threads` in Node.js, `Worker` in browsers) with **`SharedArrayBuffer` + `Atomics.wait()`** to provide true blocking. The bot developer's code is identical to Java — no `async`, no `await`.

**API surface:**

```typescript
class MyBot extends Bot {
  run() {
    while (this.isRunning()) {
      this.forward(100);        // blocks until 100 units moved
      this.turnGunRight(360);   // blocks until gun turned 360°
      this.back(100);
    }
  }

  // Event handlers — called during dispatch, same as Java
  onScannedBot(event: ScannedBotEvent) {
    this.fire(1);
  }
}
```

**Two-thread architecture (mirrors Java):**

```
Main thread (≈ Java's WebSocket thread)        Bot Worker thread (≈ Java's bot thread)
─────────────────────────────────────────       ─────────────────────────────────────────
WebSocket receives tick                         bot.run() executing
  → Parse JSON                                    → forward(100)
  → Add events to queue                             → setForward(100)
  → Dispatch events (handlers in priority order)     → go() → sendIntent()
  → Atomics.notify() ─────────────────────────────→  → waitForNextTurn()
                                                        [Atomics.wait() — truly blocked]
                                                      → wakes, checks distanceRemaining
                                                      → loops until movement complete
```

**Key implementation details:**

- `waitForNextTurn()` calls `Atomics.wait(sharedBuffer, ...)` — the bot worker thread truly blocks, just like Java's `monitor.wait()`
- When a tick arrives, the main thread processes events and calls `Atomics.notify()` — the bot worker wakes, just like Java's `notifyAll()`
- Event handlers (`onScannedBot`, `onTick`, etc.) are called on the bot worker thread during `dispatchEvents()`, same as Java
- Event dispatch order is preserved — events dispatched in priority order before the worker resumes
- `SharedArrayBuffer` is used for the synchronization signal; event data is passed via `postMessage()` or shared memory

**Stop/interrupt semantics:**

- Java uses `Thread.interrupt()` + `ThreadInterruptedException`
- TypeScript equivalent: main thread sets a flag in the `SharedArrayBuffer` and calls `Atomics.notify()`. The bot worker checks the flag after waking and throws `BotStoppedException`
- `dispatchFinalTurnEvents()` is called before stopping to ensure final-tick events (WonRoundEvent, DeathEvent) are delivered

**Runtime specifics:**

- **Node.js:** `worker_threads` module with `SharedArrayBuffer` (available since Node.js 12)
- **Browser:** `Worker` with `SharedArrayBuffer` (requires `Cross-Origin-Opener-Policy: same-origin` and `Cross-Origin-Embedder-Policy: require-corp` headers)
- The runtime abstraction layer (ADR-0029) handles the difference

---

## Rationale

**Why Web Workers + Atomics:**

- ✅ **API parity across languages**: Bot code looks identical to Java, C#, and Python — no `async`, no `await`, no language-specific keywords leaking into the API
- ✅ **True blocking**: `forward(100)` genuinely blocks the bot thread. No Promises, no callbacks, no generator yields
- ✅ **Same two-thread model as Java**: WebSocket on main thread, bot logic on worker thread, synchronized via Atomics (equivalent to Java's `monitor.wait()`/`notifyAll()`)
- ✅ **Event dispatch on bot thread**: Same as Java — handlers run on the bot worker, in priority order, during `go()`

---

## Alternatives Considered

### async/await

Make blocking methods return `Promise<void>`. Bot developers write `await forward(100)` and declare `async run()`.

**Rejected because:**

- Leaks implementation details into the developer-facing API (`async`, `await` keywords)
- Bot code looks different from Java/C#/Python — violates cross-language familiarity
- Forgetting `await` is a silent bug (method returns immediately without blocking)
- Same concern drove the `refactor-python-api-sync` effort — async in the API was removed for the same reasons

### Generator-based Cooperative Multitasking

Use generator functions (`function*`) with `yield`. A scheduler resumes the generator when the next tick arrives.

**Rejected because:**

- `yield` leaks into the API, same problem as `await`
- Unusual pattern unfamiliar to most developers
- Cannot easily compose with standard control flow

### Callback-based API

Replace blocking methods with callbacks (`bot.forward(100, () => { ... })`).

**Rejected because:**

- Fundamentally different programming model from Java
- Violates 1:1 semantic equivalence
- Callback hell

---

## Consequences

### Positive

- ✅ Bot developers write synchronous, sequential code — identical to Java/C#/Python
- ✅ True blocking behavior — no hidden async machinery in the API
- ✅ Two-thread model mirrors Java exactly (main thread + bot worker)
- ✅ Event dispatch order preserved — handlers run synchronously on bot thread

### Negative / Challenges

- ⚠️ `SharedArrayBuffer` requires two HTTP headers in browsers (`Cross-Origin-Opener-Policy: same-origin` and `Cross-Origin-Embedder-Policy: require-corp`) — a one-line web server configuration, supported in all modern browsers
- ⚠️ Worker thread adds internal complexity (message passing, shared memory coordination) — comparable to Java's threading model, which the project already handles

---

## References

- [ADR-0003: Cross-Platform Bot API Strategy](./0003-cross-platform-bot-api-strategy.md)
- [ADR-0004: Java as Authoritative Reference Implementation](./0004-java-reference-implementation.md)
- [ADR-0027: TypeScript Bot API for Web Platform Support](./0027-typescript-bot-api-architecture.md)
- [ADR-0029: TypeScript Bot API Runtime Targets](./0029-typescript-bot-api-runtime-targets.md)
- [Java BaseBotInternals](/bot-api/java/src/main/java/dev/robocode/tankroyale/botapi/internal/BaseBotInternals.java) — Reference threading implementation
- [Java BotInternals](/bot-api/java/src/main/java/dev/robocode/tankroyale/botapi/internal/BotInternals.java) — Reference blocking movement implementation
