# Design: Server Concurrency and Shared State Fixes

This document describes the thread-safety contract and synchronization mechanisms implemented in `GameServer.kt` and `ModelUpdater.kt`.

## Threading Model

The `GameServer` interacts with several types of threads:
1.  **WebSocket Threads**: Created by `java-websocket` to handle incoming messages from bots, observers, and controllers.
2.  **Timer Threads**: Created by `ResettableTimer` for 'ready' and 'turn' timeouts.
3.  **Main/Control Thread**: Used to start and stop the server.

## Synchronized Fields

### GameServer.kt

| Field | Synchronization | Description |
| :--- | :--- | :--- |
| `serverState` | `@Volatile` + `startGameLock` | Controls the high-level state of the server. Changes to `GAME_STOPPED` or `WAIT_FOR_PARTICIPANTS_TO_JOIN` are guarded by `startGameLock` to prevent races with `startGameIfParticipantsReady`. |
| `modelUpdater` | `@Volatile` | Holds the game logic state. Null when no game is running. Accesses are checked for nullability and captured in local variables to prevent NPEs during state transitions. |
| `botListUpdateMessage` | `@Volatile` | An immutable snapshot of the current bot list. Replaced atomically when the list changes. |
| `turnStartTimeNanos` | `@Volatile` | Used to calculate turn duration across threads. |

### Locks

| Lock | Description |
| :--- | :--- |
| `tickLock` | Guards the core game loop in `onNextTurn()` and prevents concurrent updates to `botIntents`. |
| `startGameLock` | Synchronizes game start/stop/abort transitions to ensure consistent state and timer management. |
| `participantsLock` | Guards access to the `participants` set and related structures (though many now use `ConcurrentHashMap`). |

## Key Patterns

1.  **Volatile for Visibility**: Simple fields like `serverState` and `modelUpdater` use `@Volatile` to ensure all threads see the latest value immediately.
2.  **Local Capture**: When accessing volatile nullable fields (like `modelUpdater`), we capture the value in a local variable first:
    ```kotlin
    val updater = modelUpdater ?: return
    updater.isAlive(botId) // Safe even if modelUpdater becomes null
    ```
3.  **Immutable Snapshots**: Instead of cloning objects under a lock, we replace volatile fields with entirely new immutable objects (e.g., `botListUpdateMessage`). This allows lock-free reads.
4.  **Encapsulation**: `ModelUpdater.botsMap` is now private. All accesses from `GameServer` go through accessor methods on `ModelUpdater`, ensuring a cleaner API and better control over how bot state is accessed.

## Verification Plan

-   **Unit Tests**: Run `server` module tests to ensure no regressions in game logic or connection handling.
-   **Concurrency Stress**: Manual verification by running multiple bots and observers to check for `ConcurrentModificationException` or `NullPointerException` during game transitions (start/stop/abort).
