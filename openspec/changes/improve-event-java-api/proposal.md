# Change: Improve Event System Java Ergonomics + Single Subscription API

## Why

The `Event<T>` class currently has two parallel ways to subscribe: the Kotlin operator API
(`+= On(...)` / `-=`) and the new method API (`on()` / `once()` / `off()`). Having two mechanisms
violates DRY and creates inconsistency — callers must choose between idioms. The operator API
also requires Java callers to use Kotlin-specific types (`On<T>`, `Once<T>`, `Subscription<T>`,
`Unit.INSTANCE`). Eliminating the operators and their supporting wrapper classes leaves a single,
clean API that works identically from both languages.

## What Changes

**Phase 1 — already implemented:**
- **ADDED** `on(owner, Consumer<T>)` and `on(owner, Int, Consumer<T>)` to `Event<T>`
- **ADDED** `once(owner, Consumer<T>)` and `once(owner, Int, Consumer<T>)` to `Event<T>`
- **ADDED** `off(owner)` alias for `-=`
- **UPDATED** internal runner Kotlin code and `AsyncBattle.java` example to use new API

**Phase 2 — this extension:**
- **ADDED** `@JvmSynthetic on(owner, (T) -> Unit)` and `on(owner, Int, (T) -> Unit)` Kotlin overloads
- **ADDED** `@JvmSynthetic once(owner, (T) -> Unit)` and `once(owner, Int, (T) -> Unit)` Kotlin overloads
- **BREAKING** `plusAssign` operator removed from `Event<T>`
- **BREAKING** `minusAssign` operator removed from `Event<T>`
- **BREAKING** `Subscription.kt` deleted (`Subscription<T>`, `On<T>`, `Once<T>` classes removed)
- **UPDATED** all call sites (~145 `+= On`, ~5 `+= Once`, ~7 `-=`) across ~40 files
- **UPDATED** `EventDelegate.kt` KDoc (remove `On` references)
- **SPEC** MODIFIED requirement "Event Subscription" in `battle-runner` capability

## Impact

- Affected specs: `battle-runner` (MODIFIED — single subscription API requirement)
- Affected code:
  - `lib/common/.../event/Event.kt` — remove operators, add Kotlin overloads
  - `lib/common/.../event/Subscription.kt` — **DELETE**
  - `lib/common/.../event/EventDelegate.kt` — KDoc only
  - `lib/common/src/test/.../EventTest.kt` — update ~15 usages
  - `recorder/.../RecordingObserver.kt` — update ~6 usages
  - `gui/src/main/kotlin/.../` — **~40 files**, ~145 usages total
  - `gui/src/test/.../EventTest.kt` — update ~2 usages
  - `runner/.../internal/ServerManager.kt` — update 1 usage
- Breaking change: any external code importing `On`, `Once`, or `Subscription` will fail to compile

## Kotlin API After Change

```kotlin
// Continuous subscription
event.on(owner) { e -> handle(e) }
event.on(owner, priority = 50) { e -> handle(e) }

// One-shot subscription
event.once(owner) { e -> handle(e) }

// Fire
event(myEvent)

// Unsubscribe
event.off(owner)
```

## Java API After Change (unchanged from Phase 1)

```java
event.on(owner, e -> handle(e));
event.on(owner, 50, e -> handle(e));
event.once(owner, e -> handle(e));
event.off(owner);
event.invoke(myEvent);
```
