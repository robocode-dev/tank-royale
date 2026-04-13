# Change: Clean Up Server Code Quality

## Why

The server audit identified seven low-severity findings — antipatterns, naming issues, DRY violations, and missing
documentation. Each is a small independent fix that improves readability and maintainability with minimal risk.

## What Changes

- Remove explicit `System.gc()` call in `GameServer.cleanupAfterGameStopped()` — the JVM manages GC; explicit calls
  introduce unpredictable pauses
- Restrict `ModelUpdater.botsMap` and `ModelUpdater.turn` visibility from `internal` to `private` (after accessor
  methods are added by the concurrency proposal)
- Replace per-iteration `Random()` allocation in `ModelUpdater.randomBotPoint()` with a single class-level instance;
  replace `Math.random()` calls similarly
- Consolidate three identical `getDisplayName` overloads in `GameServerConnectionListener` into a single function
- Rename `x`/`y` variables in `randomBotPoint()` to `cellX`/`cellY` to distinguish grid indices from pixel coordinates
- Add KDoc to all `internal fun handle*` entry points in `GameServer`
- Add class-level KDoc to `GameServer` and `ModelUpdater` documenting the threading contract

## Impact

- Affected code: `GameServer.kt`, `ModelUpdater.kt`, `GameServerConnectionListener.kt`
- No changes to observable behavior
- Partially depends on proposals `fix-server-concurrency-and-shared-state` (for visibility restriction and thread docs)
  and `simplify-server-model-and-decompose` (thread docs should reflect final class structure)
