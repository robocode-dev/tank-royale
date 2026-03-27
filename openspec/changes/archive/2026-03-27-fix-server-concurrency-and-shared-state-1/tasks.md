## 1. Synchronize serverState

- [x] 1.1 Add `@Volatile` to `serverState` field in `GameServer.kt`
- [x] 1.2 Audit all read/write sites (lines 116, 150, 220, 479, 500, 508, 546, 788, 858, 866, 868, 875, 878, 884, 900) to confirm volatile semantics suffice (no compound check-then-act outside existing locks)
- [x] 1.3 If any check-then-act patterns require atomicity beyond volatile, wrap them in a dedicated `stateLock`

## 2. Synchronize modelUpdater

- [x] 2.1 Add `@Volatile` to `modelUpdater` field in `GameServer.kt`
- [x] 2.2 Replace all `modelUpdater?.botsMap!!` patterns with `requireNotNull(modelUpdater) { "context message" }`
- [x] 2.3 Verify that `sendTickToParticipants()` safely handles `modelUpdater` being null between `tickLock` release and method call

## 3. Fix botListUpdateMessage race

- [x] 3.1 Change `updateBotListUpdateMessage()` to build a complete immutable `BotListUpdate` object and assign it atomically to a `@Volatile` field
- [x] 3.2 Remove `cloneBotListUpdate()` — the field is now an immutable snapshot that can be sent directly
- [x] 3.3 Verify `sendBotListUpdateToObserversAndControllers()` reads the volatile field once per invocation (local variable capture)

## 4. Encapsulate ModelUpdater.botsMap

- [x] 4.1 Add `fun getBot(id: BotId): MutableBot?` accessor on `ModelUpdater`
- [x] 4.2 Add `fun setDebugEnabled(id: BotId, enabled: Boolean)` accessor on `ModelUpdater`
- [x] 4.3 Add `fun getBotInitialPositions(): Map<BotId, Point>` for `createGameStartedEventForBot()` use case
- [x] 4.4 Replace all direct `modelUpdater?.botsMap` accesses in `GameServer.kt` with the new accessor methods
- [x] 4.5 Change `botsMap` visibility from `internal` to `private` on `ModelUpdater`

## 5. Design document and verification

- [x] 5.1 Write `design.md` documenting thread-safety contract: which threads call which methods, which fields are volatile, which locks guard what
- [x] 5.2 Run existing server tests
- [x] 5.3 Manual verification: run a multi-bot game with GUI to confirm no race conditions
