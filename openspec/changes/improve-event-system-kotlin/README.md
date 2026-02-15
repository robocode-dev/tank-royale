# Improve Event System with Modern Kotlin Patterns

**Status:** Proposed  
**Created:** 2026-02-15  
**Author:** AI Assistant + User  

## Quick Summary

Modernize the `Event<T>` system with three backward-compatible improvements:

1. **Atomic copy-on-write** — Better thread-safety and performance
2. **Inline value classes** — Zero-overhead wrappers
3. **Operator overloads** — Cleaner syntax (`+=` / `-=` like C#)

**No breaking changes.** All existing code continues working unchanged.

## Files

- [`proposal.md`](./proposal.md) — Why, what, and impact summary
- [`design.md`](./design.md) — Detailed technical design and trade-offs
- [`tasks.md`](./tasks.md) — Implementation checklist

## Before/After

### Current Implementation
```kotlin
val onStop = Event<JButton>()
onStop.subscribe(this) { button -> handleStop(button) }
onStop.unsubscribe(this)
onStop.fire(button)
```

### After Improvements (Optional)
```kotlin
val onStop by event<JButton>()  // Property delegation
onStop += this to { button -> handleStop(button) }  // Operator overload
onStop -= this
onStop(button)  // Invoke operator (optional)
```

## Key Constraints

- ✅ 100% backward compatible
- ✅ No Kotlin Coroutines dependency (deferred)
- ✅ Incremental adoption (opt-in)

## Next Steps

1. Review proposal, design, and tasks
2. Get team approval
3. Implement Phase 1 (atomic operations)
4. Implement Phase 2 (inline value classes)
5. Implement Phase 3 (operator overloads)
6. Update ADR-0022

