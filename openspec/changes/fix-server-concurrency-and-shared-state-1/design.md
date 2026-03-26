# Design: Server Thread-Safety Contract

## Context

`GameServer` is accessed from three concurrent thread sources with no documented contract:

1. **WebSocket handler threads** — `ClientWebSocketsHandler.executorService` (cached thread pool) dispatches incoming
   messages to `handleBotJoined`, `handleBotLeft`, `handleBotReady`, `handleBotIntent`, `handleStartGame`,
   `handleAbortGame`, `handlePauseGame`, `handleResumeGame`, `handleChangeTps`, `handleBotPolicyUpdate`
2. **Turn timer thread** — `ResettableTimer` callback fires `onNextTurn()` → `updateGameState()` →
   `sendTickToParticipants()`
3. **Ready timer thread** — `ResettableTimer` callback fires `onReadyTimeout()`

## Goals

- Eliminate data races on shared mutable fields without introducing contention on the hot path (turn processing)
- Make the threading contract explicit and auditable
- Encapsulate `ModelUpdater` internals so `GameServer` cannot accidentally alias mutable state

## Non-Goals

- Full decomposition of `GameServer` (that is proposal 2)
- Changing the threading model (e.g., moving to coroutines or single-threaded event loop)

## Decisions

### D1: Use `@Volatile` for `serverState` and `modelUpdater`

Both fields follow a publish-subscribe pattern: one thread writes, others read. Java's `volatile` guarantees
happens-before ordering, which is sufficient for these fields. An `AtomicReference` would also work but adds ceremony
for no benefit since compound check-then-act on `serverState` is already guarded by `startGameLock`.

### D2: Immutable-snapshot pattern for `botListUpdateMessage`

Current code mutates the `bots` list on the shared object, then clones it before sending. This is a TOCTOU race.
Fix: build a new `BotListUpdate` object each time and assign to a `@Volatile` field. Readers capture the reference
into a local variable and use it directly — no clone needed.

### D3: Accessor methods on `ModelUpdater` instead of exposing `botsMap`

`GameServer` currently reaches into `modelUpdater?.botsMap!!` for two purposes:
1. Read bot positions for `createGameStartedEventForBot()` (line 195)
2. Set `isDebuggingEnabled` flag (line 912)

Both can be served by dedicated methods on `ModelUpdater`. This prevents `GameServer` from holding a reference to the
mutable map and accidentally reading stale or partially-updated state.

## Risks / Trade-offs

- **Risk:** `@Volatile` on `serverState` does not protect compound operations (read state → decide → write state).
  Mitigation: all state-machine transitions that depend on current state are already inside `startGameLock` or
  `participantsLock`. The volatile ensures visibility for the reads that are not inside a lock (e.g., the guard in
  `onNextTurn`).

- **Risk:** Making `botsMap` private may surface hidden accesses not found during audit. Mitigation: compiler will
  catch them; fix each one with an appropriate accessor.

## Field-Level Threading Contract (Post-Fix)

| Field | Annotation/Lock | Writer Thread(s) | Reader Thread(s) |
|-------|----------------|-------------------|-------------------|
| `serverState` | `@Volatile` | WebSocket handlers, ready timer | Turn timer, WebSocket handlers |
| `modelUpdater` | `@Volatile` | WebSocket handlers (prepareGame, cleanup) | Turn timer, WebSocket handlers |
| `botListUpdateMessage` | `@Volatile` (immutable snapshot) | WebSocket handlers | WebSocket handlers (broadcast) |
| `gameSetup` | Written before game starts, read after | WebSocket handlers (handleStartGame) | Turn timer |
| `tps` | `@Volatile` (add) | WebSocket handlers (handleChangeTps) | Turn timer |
| `turnStartTimeNanos` | `@Volatile` (existing) | Turn timer | WebSocket handlers |
| `participants` | `ConcurrentHashMap.keySet` | WebSocket handlers | Turn timer, ready timer |
| `readyParticipants` | `ConcurrentHashMap.keySet` | WebSocket handlers | Ready timer |
| `participantIds` | `ConcurrentHashMap` | WebSocket handlers | Turn timer |
| `botIntents` | `ConcurrentHashMap` + `tickLock` for snapshot | WebSocket handlers | Turn timer (under tickLock) |
| `botsThatSentIntent` | `tickLock` | WebSocket handlers (under tickLock) | Turn timer (under tickLock) |
