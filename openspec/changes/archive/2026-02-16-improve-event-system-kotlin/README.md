# Improve Event System with Modern Kotlin Patterns

**Status:** ✅ Completed & Archived  
**Created:** 2026-02-15  
**Completed:** 2026-02-16  
**Archived:** 2026-02-16  
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

## Completion Summary

All phases completed as of 2026-02-16:

- ✅ Phase 1: Atomic copy-on-write thread safety
- ✅ Phase 2: Operator overloads (`On`, `Once`, `+=`, `-=`)
- ✅ Phase 3: Property delegation (`by event<T>()`)
- ✅ Phase 4: Testing and validation (critical bugs fixed)
- ✅ Phase 5: Documentation (KDoc and ADR-0022 updated)
- ✅ Phase 6: Rollout (implementation merged, backward compatible)

**Spec updated:** `/openspec/changes/improve-event-system-kotlin/specs/internal-implementation/spec.md`


