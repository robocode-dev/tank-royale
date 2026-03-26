## 1. Quick Fixes (no dependencies)

- [ ] 1.1 Remove `System.gc()` call at `GameServer.kt` line 928
- [ ] 1.2 Create a single `private val random = Random()` field on `ModelUpdater`; replace `Random().nextInt(cellCount)` in `randomBotPoint()` and `Math.random()` calls (lines 529-530, 1053-1054)
- [ ] 1.3 Rename `x`/`y` local variables in `randomBotPoint()` to `cellX`/`cellY`
- [ ] 1.4 Consolidate three `getDisplayName` overloads in `GameServerConnectionListener.kt` — either extract a common `Handshake` interface with `name`/`version` or use a single generic approach

## 2. Visibility Restriction (depends on P1: concurrency)

- [ ] 2.1 Change `ModelUpdater.botsMap` from `internal` to `private`
- [ ] 2.2 Change `ModelUpdater.turn` from `internal` to `private`

## 3. Documentation (depends on P1 + P2 for final structure)

- [ ] 3.1 Add KDoc to all `internal fun handle*` methods in `GameServer` — document caller, thread context, preconditions
- [ ] 3.2 Add class-level KDoc to `GameServer` documenting threading contract (which threads, which locks, which fields)
- [ ] 3.3 Add class-level KDoc to `ModelUpdater` documenting single-threaded-under-tickLock contract

## 4. Verification

- [ ] 4.1 Run existing server tests
- [ ] 4.2 Spot-check KDoc renders correctly
