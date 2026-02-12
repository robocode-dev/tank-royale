# Issue #180: TPS=-1 Re-entrancy Fix (Version 0.35.5)

## Problem Summary

Version 0.35.4 introduced a critical bug when attempting to fix timing issues at TPS=-1 (unlimited). The fix caused the `ResettableTimer` to execute synchronously on the calling thread when delay was 0, creating re-entrancy issues.

## Root Cause

In 0.35.4, when `delayNanos <= 0`, the code called `executeIfValid(generationId)` directly:

```kotlin
private fun scheduleInternal(delayNanos: Long, generationId: Long) {
    cancelScheduled()
    if (delayNanos <= 0L) {
        executeIfValid(generationId)  // ⚠️ WRONG: Executes on calling thread!
    } else {
        scheduledFuture = executor.schedule({ executeIfValid(generationId) }, delayNanos, TimeUnit.NANOSECONDS)
    }
}
```

**Call sequence at TPS=-1:**
1. `onNextTurn()` (on executor thread) calls `resetTurnTimeout()`
2. `resetTurnTimeout()` calls `timer.schedule(minDelayNanos = 0, ...)`
3. `schedule()` calls `scheduleInternal(0, generation)`
4. `scheduleInternal()` **immediately** calls `executeIfValid()` on the **calling thread**
5. `executeIfValid()` runs `job.run()` which is `onNextTurn()` again
6. **Result: Immediate recursion/re-entrancy on the same thread**

## Symptoms

User reports for 0.35.4:
- Bot behavior completely different at TPS=-1 vs normal TPS
- Lagging/stuttering around turn 1000 in 10-bot battles
- OutOfMemoryErrors
- "max event queue size reached: 256" - bot event queues overflowing
- Server showing all bots disconnected while UI thinks game is running
- Inconsistent game state

## Solution

Use `executor.submit()` instead of direct execution when delay is 0:

```kotlin
private fun scheduleInternal(delayNanos: Long, generationId: Long) {
    cancelScheduled()
    if (delayNanos <= 0L) {
        // ✓ CORRECT: Execute on executor thread via submit()
        scheduledFuture = executor.submit { executeIfValid(generationId) }
    } else {
        scheduledFuture = executor.schedule({ executeIfValid(generationId) }, delayNanos, TimeUnit.NANOSECONDS)
    }
}
```

**Benefits:**
1. **Thread Separation**: Task executes on executor thread, not calling thread
2. **No Re-entrancy**: `onNextTurn()` completes before the next call starts
3. **Cancellable**: Returns a `Future` that can be cancelled by `cancelScheduled()`
4. **No Queue Buildup**: Cancelling the future prevents old tasks from executing
5. **Preserves Memory Leak Fix**: Still uses single-threaded executor from 0.35.3

## Technical Details

### Type Change
Changed `scheduledFuture` from `ScheduledFuture<*>?` to `Future<*>?` because:
- `executor.submit()` returns `Future<T>`
- `executor.schedule()` returns `ScheduledFuture<T>` (which extends `Future<T>`)
- We only use `cancel(false)` which is available on both

### Why Not Use execute()?
- `executor.execute()` doesn't return a `Future`, so we can't cancel it
- Multiple rapid calls would queue up tasks without ability to cancel
- At TPS=-1 with fast CPUs, this could cause queue buildup

### Why submit() Over schedule(0)?
- `executor.submit()` executes immediately without scheduling overhead
- `executor.schedule(task, 0, TimeUnit.NANOSECONDS)` adds unnecessary scheduling logic
- Both maintain thread separation, but `submit()` is more direct

## Testing

Added new test to verify thread separation:
```kotlin
test("executes on executor thread not calling thread when delay is 0 (prevents re-entrancy)") {
    // Verifies that execution happens on "TurnTimeoutTimer" thread, not calling thread
}
```

All existing tests pass, including:
- `executes immediately when min delay is 0 and notifyReady called (high TPS scenario)`
- `rapid schedule with 0 delay does not queue tasks (TPS=-1 simulation)`

## Version History

- **0.35.3**: Fixed memory leak by replacing `NanoTimer` with `ResettableTimer`
  - Issue: Introduced timing regression at TPS=-1 (tasks queued up)
- **0.35.4**: Attempted fix by direct execution when delay=0
  - Issue: Created re-entrancy bug, made problem worse
- **0.35.5**: Fixed re-entrancy by using `executor.submit()`
  - ✓ Maintains thread separation
  - ✓ Preserves memory leak fix
  - ✓ Restores correct timing at TPS=-1

## Files Changed

- `server/src/main/kotlin/dev/robocode/tankroyale/server/core/ResettableTimer.kt`
  - Changed `scheduleInternal()` to use `executor.submit()` when delay=0
  - Changed `scheduledFuture` type from `ScheduledFuture<*>?` to `Future<*>?`
  - Updated imports to use `Future` instead of `ScheduledFuture`
- `server/src/test/kotlin/core/ResettableTimerTest.kt`
  - Added thread separation test
- `VERSIONS.md`
  - Added 0.35.5 entry
  - Marked 0.35.4 as broken
- `gradle.properties`
  - Version already set to 0.35.5

