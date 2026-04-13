# Proposal: Refactor NanoTimer for Thread Reuse

## Summary

Refactor the `NanoTimer` class to reuse a single thread instead of creating a new thread for each game turn, fixing a critical memory leak that causes OutOfMemoryError during extended gameplay sessions.

## Problem Statement

The current `NanoTimer` implementation creates a new `Thread` instance every time `start()` is called. In `GameServer.kt`, the `resetTurnTimeout()` method creates a new `NanoTimer` for each game turn:

```kotlin
private fun resetTurnTimeout() {
    turnTimeoutTimer?.stop()
    turnTimeoutTimer = NanoTimer(
        minPeriodInNanos = calculateTurnTimeoutMinPeriod().inWholeNanoseconds,
        maxPeriodInNanos = calculateTurnTimeoutMaxPeriod().inWholeNanoseconds,
        job = { onNextTurn() }
    ).apply { start() }
}
```

**Impact at high TPS:**
- At 500 TPS (turns per second), this creates **500 threads per second**
- Over 21 hours: ~37 million threads created
- User report shows thread names reaching "Thread-3216473"
- Memory usage gradually increases until `java.lang.OutOfMemoryError: Java heap space`
- GUI becomes "jittery" as GC pressure increases near heap limit

**User-reported symptoms:**
1. Gradual memory increase observed via macOS Activity app
2. `jcmd <PID> GC.heap_info` showed "used" approaching 4GB limit
3. OOM error in server log after ~21 hours at 500 TPS with 2 bots
4. WebSocketSelector thread failure: `Exception in thread "WebSocketSelector-3" java.lang.OutOfMemoryError`

## Proposed Solution

Refactor `NanoTimer` to use a `ScheduledExecutorService` with a single-thread pool, allowing the timer to be reset without creating new threads.

**Key changes:**
1. Replace per-turn thread creation with a reusable scheduled executor
2. Use a single daemon thread with a meaningful name for debugging
3. Implement proper `reset()` method to restart timing without new thread allocation
4. Ensure clean shutdown in `cleanupAfterGameStopped()`

## Affected Components

| File | Change Type |
|------|-------------|
| `server/src/main/kotlin/.../NanoTimer.kt` | Major refactor |
| `server/src/main/kotlin/.../GameServer.kt` | Minor (call site updates) |

## Success Criteria

- Memory usage remains stable during extended gameplay (24+ hours)
- No thread count growth during continuous high-TPS battles
- Turn timing behavior remains identical (min/max period semantics preserved)
- All existing server tests pass
- No performance regression at high TPS

## Risks

| Risk | Mitigation |
|------|------------|
| Timing behavior change | Comprehensive testing with timing validation |
| Thread safety issues | Use thread-safe ScheduledExecutorService APIs |
| Shutdown race conditions | Proper executor shutdown with timeout |

## References

- User bug report with server log showing OOM after 21h at 500 TPS
- Thread ID "Thread-3216473" indicating 3M+ thread creations

