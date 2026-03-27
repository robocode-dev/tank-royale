# Change: Fix Server Concurrency and Shared-State Races

## Why

The server's `GameServer` class has multiple mutable fields (`serverState`, `modelUpdater`, `botListUpdateMessage`) that
are read and written from different threads (WebSocket handler pool, turn timer, ready timer) without consistent
synchronization. These are real race conditions that can cause state corruption, null-pointer crashes, and dropped game
events under concurrent load.

## What Changes

- Add `@Volatile` annotation to `serverState` and `modelUpdater` fields to guarantee visibility across threads
- Replace `botListUpdateMessage` mutation pattern with an immutable-snapshot-and-swap approach using a `@Volatile` field;
  remove the `cloneBotListUpdate()` workaround
- Replace `modelUpdater?.botsMap!!` with `requireNotNull(modelUpdater) { "..." }` for meaningful error messages
- Add accessor methods on `ModelUpdater` (`getBot()`, `setDebugEnabled()`, `isAlive()`) to encapsulate `botsMap`; stop
  exposing the raw `MutableMap` reference to `GameServer`
- Document the thread-safety contract for `GameServer` fields

## Impact

- Affected code: `GameServer.kt`, `ModelUpdater.kt`
- No changes to observable protocol behavior — all fixes are internal synchronization
- No impact on Bot APIs, GUI, or booter
