# ADR-0022: Event System for GUI Decoupling

**Status:** Accepted  
**Date:** 2026-02-15 (Documenting historical decision)

---

## Context

Traditional Java/Swing event handling uses listener interfaces with explicit registration and unregistration. This creates several problems:

1. **Listener registration hell** — `addActionListener()`, `removeActionListener()`, etc. for every component
2. **Memory leaks** — Forgetting to remove listeners prevents garbage collection
3. **Tight coupling** — Event producers must know about listener interfaces
4. **Boilerplate** — Anonymous inner classes or lambda expressions for every event

C#/.NET's delegate pattern provides a cleaner alternative: lightweight event subscription with automatic cleanup.

**Problem:** How to decouple event producers from event subscribers throughout the GUI while preventing memory leaks?

---

## Decision

Implement a **custom event system** (`dev.robocode.tankroyale.common.Event<T>`) inspired by C#/.NET delegates, using weak references to prevent memory leaks.

**Core API:**

```kotlin
class Event<T> {
    fun subscribe(owner: Any, once: Boolean = false, eventHandler: (T) -> Unit)
    fun unsubscribe(owner: Any)
    fun fire(event: T)
}
```

**Key design choices:**

- **Weak references** — Uses `WeakHashMap` internally so event handlers are garbage collected with their owners
- **Owner-based registration** — Subscribers pass `this` as owner, enabling automatic cleanup
- **Type-safe** — Generic parameter `T` ensures type safety at compile time
- **Atomic reference** — Uses `AtomicReference<WeakHashMap>` for lock-free reads and atomic writes
- **Once flag** — Optional auto-unsubscribe after first event (for one-shot handlers)

**Location:** Implemented in `lib/common` (shared across GUI and WebSocket client), not GUI-specific.

---

## Usage Throughout Codebase

The event system is used pervasively across the entire application:

### GUI Event Objects

All GUI event declarations follow the pattern: singleton `object` with `Event<T>` properties.

| Event Object | Location | Purpose | Events |
|-------------|----------|---------|--------|
| **ControlEvents** | `gui.ui.control` | Battle control panel | `onStop`, `onRestart`, `onPauseResume`, `onNextTurn` |
| **ClientEvents** | `gui.client` | WebSocket client lifecycle | `onConnected`, `onBotListUpdate`, `onGameStarted`, `onGameEnded`, `onGameAborted`, `onGamePaused`, `onGameResumed`, `onRoundStarted`, `onRoundEnded`, `onTickEvent`, `onStdOutputUpdated`, `onBotPolicyChanged`, `onPlayerChanged`, `onSeekToTurn` |
| **ServerEvents** | `gui.ui.server` | Local server lifecycle | `onConnected`, `onStarted`, `onStopped` |
| **ServerEventTriggers** | `gui.ui.server` | Server action requests | `onStartLocalServer`, `onStopLocalServer`, `onRebootLocalServer` |
| **TpsEvents** | `gui.ui.tps` | Turns-per-second updates | `onTpsChanged` |
| **BotSelectionEvents** | `gui.ui.newbattle` | Bot selection dialog | `onBotDirectorySelected`, `onJoinedBotSelected`, `onBotSelected`, `onSelectedBotListUpdated` |
| **MenuEventTriggers** | `gui.ui.menu` | Menu item actions | `onStartBattle`, `onReplayFromFile`, `onSetupRules`, `onShowServerLog`, `onStartServer`, `onStopServer`, `onRebootServer`, `onServerConfig`, `onBotDirConfig`, `onDebugConfig`, `onSoundConfig`, `onGuiConfig`, `onHelp`, `onAbout` |

### Non-GUI Usage

| Event Object | Location | Purpose |
|-------------|----------|---------|
| **WebSocketClientEvents** | `lib.client` | WebSocket lifecycle | `onOpen`, `onClose`, `onMessage`, `onError` |

### Event Handler Objects

Handler objects subscribe to events during initialization:

- **ControlEventHandlers** — Subscribes to `ControlEvents`, invokes `Client` methods
- **MenuEventHandlers** — Subscribes to `MenuEventTriggers`, shows dialogs, manages server
- **ServerActions** — Subscribes to `ServerEventTriggers`, starts/stops/reboots local server

### Swing EDT Integration

The `EDT.enqueue()` extension function ensures Swing thread-safety:

```kotlin
fun <T> Event<T>.enqueue(owner: Any, callable: () -> Unit) {
    this.subscribe(owner) { EDT.enqueue { callable.invoke() } }
}
```

**Usage pattern:**

```kotlin
ControlEvents.onStop.enqueue(this) {
    Client.stopGame()
}
```

This automatically:
1. Subscribes to the event with weak reference
2. Enqueues event handling on Swing EDT
3. Activates/deactivates busy cursor during execution

---


## Rationale

**Why custom implementation over standard patterns:**

- ✅ **No memory leaks** — Weak references eliminate need for manual unsubscribe
- ✅ **Decoupled architecture** — Event producers don't know about subscribers
- ✅ **Consistent API** — Same pattern across GUI, client, and protocol layers
- ✅ **Type-safe** — Compile-time checking vs. raw listener interfaces
- ✅ **Minimal boilerplate** — Single `subscribe()` call vs. multiple listener registrations
- ✅ **C#-style ergonomics** — Familiar to developers from .NET background
- ❌ **Custom implementation** — Not a standard Java pattern (requires documentation)
- ❌ **Debugging complexity** — Weak references can make event flow less obvious

**Alternatives rejected:**

- **Standard Swing listeners** — Too much boilerplate, manual cleanup, tight coupling
- **RxJava/Reactive Streams** — Heavyweight dependency for simple event pub/sub
- **EventBus libraries (Guava, GreenRobot)** — Similar to our implementation but external dependency
- **Property change listeners** — Java Beans pattern is verbose and less type-safe

---

## Consequences

### Positive

- GUI components are loosely coupled through events
- No memory leaks from forgotten listener removals
- Consistent event pattern across entire codebase
- Easy to add new events without modifying existing code
- EDT integration ensures Swing thread-safety by default
- Similar to C#/.NET delegates (familiar to cross-platform developers)

### Negative

- Custom pattern requires onboarding for new contributors
- Debugging event flow requires understanding weak reference semantics
- No compile-time checking that events are subscribed (only type-safety)

### Neutral

- Event system is reusable beyond GUI (already used in WebSocket client)
- Foundation for potential future technical article

---

## References

- [Event.kt](/lib/common/src/main/kotlin/dev/robocode/tankroyale/common/Event.kt) — Core implementation
- [EDT.kt](/gui/src/main/kotlin/dev/robocode/tankroyale/gui/util/EDT.kt) — Swing EDT integration
- [ControlEvents.kt](/gui/src/main/kotlin/dev/robocode/tankroyale/gui/ui/control/ControlEvents.kt) — Example event declarations
- [ControlEventHandlers.kt](/gui/src/main/kotlin/dev/robocode/tankroyale/gui/ui/control/ControlEventHandlers.kt) — Example event subscriptions
- [MenuEventHandlers.kt](/gui/src/main/kotlin/dev/robocode/tankroyale/gui/ui/menu/MenuEventHandlers.kt) — Complex event handling example
- [ADR-0021: Java Swing GUI Reference Implementation](./0021-java-swing-gui-reference-implementation.md)

---

## Future Article

The implementation of C#/.NET-style delegates in Java/Kotlin using weak references and generics may warrant a technical article demonstrating:
- How to implement type-safe delegates in Java/Kotlin
- Weak reference memory management patterns
- Thread-safe event firing
- Swing EDT integration
- Comparison with C#/.NET event system

