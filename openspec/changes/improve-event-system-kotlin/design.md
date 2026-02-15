# Design: Improve Event System with Modern Kotlin Patterns

## Executive Summary

This design modernizes the `Event<T>` system with three improvements that maintain 100% backward compatibility:

1. **Atomic copy-on-write** for better thread-safety and performance
2. **Inline value classes** for zero-overhead wrappers
3. **Operator overloads + property delegation** for cleaner syntax

**Key Constraint:** No Kotlin Coroutines/Flow dependency (deferred per YAGNI).

---

## Current Implementation Analysis

### Existing Code

```kotlin
open class Event<T> {
    private val eventHandlers = Collections.synchronizedMap(WeakHashMap<Any, Handler<T>>())

    fun subscribe(owner: Any, once: Boolean = false, eventHandler: (T) -> Unit) {
        eventHandlers[owner] = Handler(eventHandler, once)
    }

    fun unsubscribe(owner: Any) {
        eventHandlers.remove(owner)
    }

    fun fire(event: T) {
        eventHandlers.entries.toSet().forEach { (owner, handler) ->
            handler.apply {
                if (once) unsubscribe(owner)
                eventHandler.invoke(event)
            }
        }
    }

    class Handler<T>(
        val eventHandler: (T) -> Unit,
        val once: Boolean = false
    )
}
```

### Strengths

✅ Weak references prevent memory leaks  
✅ Thread-safe with `synchronizedMap()`  
✅ Type-safe generics  
✅ Simple API  

### Improvement Opportunities

⚠️ `synchronizedMap()` locks on every read and write  
⚠️ `Handler<T>` creates allocation overhead  
⚠️ Verbose syntax compared to C# delegates  
⚠️ No property delegation pattern  

---

## Improvement 1: Atomic Copy-on-Write

### Problem

`Collections.synchronizedMap()` uses a global lock for all operations, including reads during `fire()`. This creates contention when multiple threads fire events concurrently.

### Solution

Use `AtomicReference<WeakHashMap>` with copy-on-write semantics:

```kotlin
private val eventHandlers = AtomicReference(WeakHashMap<Any, Handler<T>>())

fun subscribe(owner: Any, once: Boolean = false, eventHandler: (T) -> Unit) {
    eventHandlers.updateAndGet { map ->
        WeakHashMap(map).apply { 
            put(owner, Handler(eventHandler, once))
        }
    }
}

fun fire(event: T) {
    // Lock-free read
    val snapshot = eventHandlers.get()
    snapshot.entries.forEach { (owner, handler) ->
        handler.apply {
            if (once) unsubscribe(owner)
            eventHandler.invoke(event)
        }
    }
}
```

### Trade-offs

**Pros:**
- ✅ Lock-free reads (better concurrency for `fire()`)
- ✅ No contention between multiple threads firing events
- ✅ Weak references still work (GC behavior unchanged)

**Cons:**
- ⚠️ Copy overhead on subscribe/unsubscribe (acceptable: infrequent operations)
- ⚠️ Potential ABA problem if handler is modified during iteration (mitigated: we work on snapshot)

### Performance Expectations

- **Subscribe/Unsubscribe:** Slightly slower (copy overhead)
- **Fire:** Significantly faster under concurrent load (no lock)
- **Memory:** Temporary extra allocation during subscription changes

**Verdict:** Net positive for event-heavy GUI scenarios where firing is more frequent than subscription changes.

---

## Improvement 2: Inline Value Class for Handler

### Problem

`Handler<T>` is a simple wrapper that creates allocation overhead:

```kotlin
class Handler<T>(
    val eventHandler: (T) -> Unit,
    val once: Boolean = false
)
```

### Solution

Convert to inline value class (Kotlin 1.5+):

```kotlin
@JvmInline
value class Handler<T>(private val data: Pair<(T) -> Unit, Boolean>) {
    constructor(eventHandler: (T) -> Unit, once: Boolean = false) : this(eventHandler to once)
    
    val eventHandler: (T) -> Unit get() = data.first
    val once: Boolean get() = data.second
}
```

### Trade-offs

**Pros:**
- ✅ Zero runtime allocation (JVM erases wrapper at compile time)
- ✅ Same API surface (backward compatible)
- ✅ Improved cache locality

**Cons:**
- ⚠️ Requires Kotlin 1.5+ (already satisfied by project)
- ⚠️ Slightly more complex bytecode (negligible)

### Verification

Use `-Xjvm-default=all` and inspect bytecode to confirm no wrapper allocation.

---

## Improvement 3: Operator Overloads

### Problem

Current syntax is verbose compared to C# delegates:

```kotlin
// Current
val onStop = Event<JButton>()
onStop.subscribe(this) { button -> handleStop(button) }
onStop.unsubscribe(this)

// C# equivalent
public event EventHandler<Button> OnStop;
OnStop += HandleStop;
OnStop -= HandleStop;
```

### Solution A: Subscribe/Unsubscribe Operators

```kotlin
operator fun plusAssign(subscription: Pair<Any, (T) -> Unit>) {
    val (owner, handler) = subscription
    subscribe(owner, once = false, handler)
}

operator fun minusAssign(owner: Any) {
    unsubscribe(owner)
}

// Usage
onStop += this to { button -> handleStop(button) }
onStop -= this
```

### Solution B: Once-Flag Wrapper

```kotlin
data class Once<T>(val owner: Any, val handler: (T) -> Unit)

operator fun plusAssign(subscription: Once<T>) {
    subscribe(subscription.owner, once = true, subscription.handler)
}

// Usage
onStop += Once(this) { button -> handleOneShot(button) }
```

### Solution C: Invoke Operator (Optional)

```kotlin
operator fun invoke(event: T) = fire(event)

// Usage
onStop(button)  // Instead of onStop.fire(button)
```

### Trade-offs

**Pros:**
- ✅ More concise syntax
- ✅ Familiar to C# developers
- ✅ Backward compatible (operators are additive)

**Cons:**
- ⚠️ `to` infix creates `Pair` allocation (minor overhead)
- ⚠️ Operators may be less discoverable than named methods
- ⚠️ `invoke()` may be confusing (events aren't functions)

**Recommendation:** Implement `+=`/`-=` operators. Defer `invoke()` until team feedback.

---

## Improvement 4: Property Delegation

### Problem

Event declarations are repetitive:

```kotlin
object ControlEvents {
    val onStop = Event<JButton>()
    val onRestart = Event<JButton>()
    val onPauseResume = Event<JButton>()
    val onNextTurn = Event<JButton>()
}
```

### Solution

Delegate pattern for cleaner syntax:

```kotlin
class EventDelegate<T> {
    operator fun getValue(thisRef: Any?, property: KProperty<*>) = Event<T>()
}

fun <T> event() = EventDelegate<T>()

// Usage
object ControlEvents {
    val onStop by event<JButton>()
    val onRestart by event<JButton>()
    val onPauseResume by event<JButton>()
    val onNextTurn by event<JButton>()
}
```

### Trade-offs

**Pros:**
- ✅ Slightly cleaner syntax
- ✅ Kotlin-idiomatic pattern
- ✅ Optional (existing syntax still works)

**Cons:**
- ⚠️ Marginal benefit (saves typing `= Event<T>()`)
- ⚠️ Property delegation may be overkill for simple instantiation

**Recommendation:** Implement as optional pattern. Document but don't mandate.

---

## Implementation Plan

### Phase 1: Atomic Operations

1. Replace `Collections.synchronizedMap()` with `AtomicReference`
2. Update `subscribe()` to use `updateAndGet()`
3. Update `fire()` to use lock-free `get()`
4. Add concurrent access tests

**Validation:** Existing tests pass unchanged + new concurrency tests.

### Phase 2: Inline Value Class

1. Convert `Handler<T>` to `@JvmInline value class`
2. Update constructor to wrap `Pair<(T) -> Unit, Boolean>`
3. Add property accessors for `eventHandler` and `once`
4. Profile to confirm zero allocations

**Validation:** Bytecode inspection + profiler confirms no wrapper allocation.

### Phase 3: Operator Overloads

1. Add `operator fun plusAssign(Pair<Any, (T) -> Unit>)`
2. Add `operator fun minusAssign(Any)`
3. Add `Once<T>` wrapper class
4. Add `operator fun plusAssign(Once<T>)`
5. Update KDoc with operator examples

**Validation:** New tests for operator syntax + existing API still works.

### Phase 4: Property Delegation

1. Create `EventDelegate<T>` class
2. Create `event()` factory function
3. Update 1-2 event objects as examples
4. Document as optional pattern

**Validation:** Delegated properties work correctly + direct instantiation still works.

---

## Backward Compatibility Guarantees

### Existing Code Unchanged

```kotlin
// All existing code continues to work
val onStop = Event<JButton>()
onStop.subscribe(this) { button -> handleStop(button) }
onStop.unsubscribe(this)
onStop.fire(button)
```

### New Syntax Optional

```kotlin
// New patterns are opt-in
val onStop by event<JButton>()
onStop += this to { button -> handleStop(button) }
onStop -= this
```

### Binary Compatibility

- No changes to public method signatures
- Operators are new methods (additive)
- Inline value class is JVM-transparent

---

## Performance Benchmarks (Expected)

| Operation | Before | After | Change |
|-----------|--------|-------|--------|
| `subscribe()` | 100ns | 120ns | +20% (copy overhead) |
| `fire()` (single thread) | 50ns | 45ns | -10% (no lock) |
| `fire()` (10 threads) | 500ns | 100ns | -80% (lock contention eliminated) |
| `Handler` allocation | 32 bytes | 0 bytes | -100% (inline) |

**Note:** Actual benchmarks will be measured during implementation.

---

## Rejected Alternatives

### Option A: Kotlin Flow Integration

**Why Rejected:** Adds coroutines dependency for minimal benefit in synchronous GUI scenarios. Deferred to future change when async use case emerges.

### Option B: RxJava/Reactive Streams

**Why Rejected:** External dependency + heavyweight for simple pub/sub pattern.

### Option C: Full C# Event Syntax

```kotlin
// Not possible in Kotlin without language support
event EventHandler<T> OnMyEvent  // ❌ No `event` keyword in Kotlin
```

**Why Rejected:** Requires language-level support. Operator overloads are closest approximation.

---

## Testing Strategy

### Unit Tests

- ✅ Existing EventTest.kt passes unchanged
- ✅ New tests for operator syntax
- ✅ New tests for property delegation
- ✅ Concurrent access stress tests

### Integration Tests

- ✅ GUI event handlers work unchanged
- ✅ WebSocketClientEvents work unchanged
- ✅ EDT.enqueue() extension function works unchanged

### Manual Smoke Testing

- ✅ Run GUI and verify all event-driven features work (battle controls, menus, dialogs)
- ✅ Verify no regressions in event handling

---

## Distribution

Changes will be released via **GitHub releases** as part of the regular build artifacts. Users download and run the new version. If bugs occur, users report them via GitHub issues.

---

## Documentation Updates

### Code Documentation

- Update Event.kt KDoc header with all usage patterns
- Add KDoc for operator overloads
- Add KDoc for property delegation
- Document thread-safety guarantees

### ADR Updates

- Add "Modern Kotlin Improvements (2026-02-15)" section to ADR-0022
- Document atomic copy-on-write pattern
- Document operator overload syntax
- Document property delegation pattern
- Add performance comparison notes

---

## Success Criteria

- [ ] All existing tests pass without modification
- [ ] New operator syntax works alongside existing API
- [ ] Property delegation works correctly
- [ ] Performance benchmarks show improvement or parity
- [ ] Zero allocations for inline value classes (verified)
- [ ] ADR-0022 updated with modern implementation details
- [ ] Team can adopt new patterns incrementally

---

## Future Enhancements (Out of Scope)

### Kotlin Flow Integration

When async use case emerges:

```kotlin
class Event<T> {
    // Keep existing synchronous API
    
    // Add optional Flow support
    private val _flow by lazy { MutableSharedFlow<T>(replay = 0) }
    val asFlow: Flow<T> get() = _flow.asSharedFlow()
    
    suspend fun fireAsync(event: T) {
        fire(event)
        _flow.emit(event)
    }
}
```

**Trigger:** Server-side async operations or WebSocket client migration to coroutines.

### Multicast Event Combinators

```kotlin
val combinedEvents = onStop or onRestart  // Fires when either event fires
```

**Trigger:** Complex event choreography requirements.

---

## Conclusion

This design modernizes the Event system with three backward-compatible improvements:

1. **Better performance** via atomic copy-on-write
2. **Zero allocations** via inline value classes
3. **Cleaner syntax** via operator overloads and property delegation

All improvements are optional and incremental. Existing code continues working unchanged.


