## 1. Synchronize serverState

- [ ] 1.1 Add `@Volatile` to `serverState` field in `GameServer.kt`
- [ ] 1.2 Audit all read/write sites (lines 116, 150, 220, 479, 500, 508, 546, 788, 858, 866, 868, 875, 878, 884, 900) to confirm volatile semantics suffice (no compound check-then-act outside existing locks)
- [ ] 1.3 If any check-then-act patterns require atomicity beyond volatile, wrap them in a dedicated `stateLock`

## 2. Synchronize modelUpdater

- [ ] 2.1 Add `@Volatile` to `modelUpdater` field in `GameServer.kt`
- [ ] 2.2 Replace all `modelUpdater?.botsMap!!` patterns with `requireNotNull(modelUpdater) { "context message" }`
- [ ] 2.3 Verify that `sendTickToParticipants()` safely handles `modelUpdater` being null between `tickLock` release and method call

## 3. Fix botListUpdateMessage race

- [ ] 3.1 Change `updateBotListUpdateMessage()` to build a complete immutable `BotListUpdate` object and assign it atomically to a `@Volatile` field
- [ ] 3.2 Remove `cloneBotListUpdate()` — the field is now an immutable snapshot that can be sent directly
- [ ] 3.3 Verify `sendBotListUpdateToObserversAndControllers()` reads the volatile field once per invocation (local variable capture)

## 4. Encapsulate ModelUpdater.botsMap

- [ ] 4.1 Add `fun getBot(id: BotId): MutableBot?` accessor on `ModelUpdater`
- [ ] 4.2 Add `fun setDebugEnabled(id: BotId, enabled: Boolean)` accessor on `ModelUpdater`
- [ ] 4.3 Add `fun getBotInitialPositions(): Map<BotId, Point>` for `createGameStartedEventForBot()` use case
- [ ] 4.4 Replace all direct `modelUpdater?.botsMap` accesses in `GameServer.kt` with the new accessor methods
- [ ] 4.5 Change `botsMap` visibility from `internal` to `private` on `ModelUpdater`

## 5. Design document and verification

- [ ] 5.1 Write `design.md` documenting thread-safety contract: which threads call which methods, which fields are volatile, which locks guard what
- [ ] 5.2 Run existing server tests
- [ ] 5.3 Manual verification: run a multi-bot game with GUI to confirm no race conditions
