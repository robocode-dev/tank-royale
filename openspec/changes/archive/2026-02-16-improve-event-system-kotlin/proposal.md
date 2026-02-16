# Change: Improve Event System with Modern Kotlin Patterns

## Why

The current `Event<T>` implementation (documented in ADR-0022) works well but can benefit from modern Kotlin improvements:

1. **Thread-safety improvement** — `Collections.synchronizedMap(WeakHashMap)` can be replaced with atomic copy-on-write pattern for better performance
2. **Inline value classes** — Zero-overhead wrapper for `Handler<T>` using `@JvmInline`
3. **Operator overloads** — More ergonomic subscription syntax using `+=` operator (C#-style)
4. **Property delegation** — Cleaner event declaration syntax

These improvements maintain 100% backward compatibility while modernizing the implementation and API ergonomics.

**Explicitly excluded:** Kotlin Coroutines/Flow integration (deferred until concrete async use case emerges per YAGNI principle).

## What Changes

### Core Improvements

1. **Atomic copy-on-write for thread safety**
   - Replace `Collections.synchronizedMap()` with `AtomicReference<WeakHashMap>`
   - Lock-free reads improve performance for event-heavy scenarios
   - Copy-on-write for subscribe/unsubscribe operations

2. **Inline value class for Handler**
   - Zero runtime overhead for `Handler<T>` wrapper
   - Maintains type safety without allocation cost

3. **Operator overloads for ergonomics**
   - `event += owner to handler` — Subscribe with owner
   - `event -= owner` — Unsubscribe
   - Backward compatible: existing `subscribe()`/`unsubscribe()` remain

4. **Property delegation for declarations**
   - `val onStop by event<JButton>()` — Cleaner syntax
   - Optional: existing direct instantiation still works

### Implementation Strategy

- **Phase 1:** Improve internals (atomic operations, inline classes)
- **Phase 2:** Add operator overloads as convenience layer
- **Phase 3:** Add property delegation as optional pattern
- **Throughout:** Maintain 100% backward compatibility

## Impact

### Affected Components

- **Core:** `lib/common/src/main/kotlin/dev/robocode/tankroyale/common/Event.kt`
- **Tests:** `gui/src/test/kotlin/dev/robocode/tankroyale/gui/util/EventTest.kt`
- **Documentation:** ADR-0022 updated to reflect modern patterns

### Breaking Changes

**None.** All changes are additive or internal improvements.

### Performance Benefits

- Lock-free reads reduce contention in event-heavy scenarios
- Zero-allocation inline value classes
- Improved cache locality with atomic copy-on-write

### Distribution

Changes will be released via **GitHub releases** as part of regular artifacts. Users download and run the new version. If bugs are found, users report them through GitHub issues.

### Migration Path

No migration required. Existing code continues to work unchanged. Teams can adopt new patterns incrementally:

```kotlin
// Old style (still works)
val onStop = Event<JButton>()
onStop.subscribe(this) { button -> handleStop(button) }

// New style (optional)
val onStop by event<JButton>()
onStop += this to { button -> handleStop(button) }
```

## Success Criteria

- [ ] All existing tests pass without modification
- [ ] Performance benchmarks show improvement or parity
- [ ] New operator syntax works alongside existing API
- [ ] ADR-0022 updated with modern implementation details
- [ ] Zero allocations for inline value classes (verified via profiler)

## Related

- **ADR-0022:** Event System for GUI Decoupling
- **ADR-0021:** Java Swing as GUI Reference Implementation


