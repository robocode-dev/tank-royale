# Tasks: Improve Event System with Modern Kotlin Patterns

## Phase 1: Core Implementation Improvements

### 1.1 Atomic Copy-on-Write Thread Safety

- [x] 1.1.1 Replace `Collections.synchronizedMap(WeakHashMap)` with `AtomicReference<WeakHashMap>`
- [x] 1.1.2 Implement atomic `updateAndGet()` for subscribe operations
- [x] 1.1.3 Implement atomic `get()` for fire operations (lock-free reads)
- [x] 1.1.4 Add concurrent access stress tests for atomic operations
- [x] 1.1.5 Add memory leak tests (verify weak references still work)

### 1.2 Inline Value Class for Handler

- [x] 1.2.1 Convert `Handler<T>` to `@JvmInline value class`
- [x] 1.2.2 Test that existing code compiles without changes
- [x] 1.2.3 Update KDoc to mention inline optimization

## Phase 2: Operator Overloads

### 2.1 Subscribe Operator

- [x] 2.1.1 Add `operator fun plusAssign(subscription: Pair<Any, (T) -> Unit>)` for `event += owner to handler`
- [x] 2.1.2 Add overload for once-flag: `event += Once(owner, handler)`
- [x] 2.1.3 Update KDoc with operator examples
- [x] 2.1.4 Add unit tests for operator syntax

### 2.2 Unsubscribe Operator

- [x] 2.2.1 Add `operator fun minusAssign(owner: Any)` for `event -= owner`
- [x] 2.2.2 Update KDoc with operator examples
- [x] 2.2.3 Add unit tests for operator syntax

### 2.3 Invoke Operator (Optional)

- [x] 2.3.1 Consider `operator fun invoke(event: T)` as alias for `fire(event)`
- [x] 2.3.2 Evaluate if `myEvent(data)` is clearer than `myEvent.fire(data)`
- [x] 2.3.3 Document decision if implemented

## Phase 3: Property Delegation

### 3.1 Event Delegate Implementation

- [x] 3.1.1 Create `EventDelegate<T>` class with `getValue()` operator
- [x] 3.1.2 Create `event()` factory function returning `EventDelegate<T>`
- [x] 3.1.3 Test property delegation in event object declarations
- [x] 3.1.4 Update KDoc with delegation examples

### 3.2 Documentation Updates

- [x] 3.2.1 Document that delegation is optional (not required)
- [x] 3.2.2 Update Event.kt KDoc header with all usage patterns

## Phase 4: Testing and Validation

### 4.1 Existing Behavior Verification

- [x] 4.1.1 Run existing `EventTest.kt` without modifications
- [x] 4.1.2 Manual GUI smoke test — verify all event handlers work
  - **🐛 CRITICAL BUG FOUND**: fire() was missing `.toSet()` from original impl
  - **Original**: `eventHandlers.entries.toSet().forEach`
  - **Broken**: `eventHandlers.get().entries.forEach` (no snapshot!)
  - **Fixed**: `eventHandlers.get().entries.toSet().forEach`
- [x] 4.1.3 Verify `WebSocketClientEvents` work unchanged
  - **🐛 BUG FOUND**: BotSelectionPanel lazy init timing issue
  - **Fixed**: Added componentShown listener to refresh on dialog open
- [x] 4.1.4 Test EDT.enqueue() extension function compatibility

### 4.2 New Pattern Tests

- [x] 4.2.1 Add tests for operator `+=` and `-=` syntax
- [x] 4.2.2 Add tests for property delegation syntax

## Phase 5: Documentation

### 5.1 Code Documentation

- [x] 5.1.1 Update Event.kt KDoc with comprehensive examples (old + new syntax)
- [x] 5.1.2 Add KDoc for operator overloads
- [x] 5.1.3 Add KDoc for property delegation
- [x] 5.1.4 Document thread-safety guarantees with atomic operations

### 5.2 ADR Updates

- [x] 5.2.1 Update ADR-0022 "Implementation Details" section
- [x] 5.2.2 Add "Modern Kotlin Improvements (2026-02-15)" subsection
- [x] 5.2.3 Document atomic copy-on-write pattern
- [x] 5.2.4 Document operator overload syntax
- [x] 5.2.5 Document property delegation syntax

### 5.3 User-Facing Documentation

- [x] 5.3.1 Check if VERSIONS.md needs updating (likely not — internal refactoring only)

## Phase 6: Rollout

### 6.1 Deployment

- [ ] 6.1.1 Merge Event.kt improvements (Phase 1-3)
- [ ] 6.1.2 Keep existing usage patterns unchanged (backward compatible)
- [ ] 6.1.3 Optionally update 1-2 event objects to demonstrate new syntax
- [ ] 6.1.4 Build and test GitHub artifacts

## Notes

- **Backward compatibility is critical** — All existing code must work unchanged
- **This is a refactoring/improvement** — No expected performance degradation
- **Concurrency testing is essential** — Atomic operations must be verified under concurrent load
- **Internal change only** — Users won't see behavioral differences, only developers benefit from cleaner syntax
- **Released as GitHub artifacts** — If bugs occur, users report them
- **No coroutines** — Flow/coroutine integration deferred to future change proposal
