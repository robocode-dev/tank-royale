# Tasks: NanoTimer Thread Reuse Refactoring

## Implementation Checklist

### Phase 1: Create ResettableTimer Class

- [x] Create new `ResettableTimer.kt` in `server/src/main/kotlin/.../core/`
- [x] Implement using `ScheduledExecutorService` with single daemon thread
- [x] Thread name: `"TurnTimeoutTimer"`
- [x] Implement `schedule(minDelayNanos: Long, maxDelayNanos: Long)` method
- [x] Implement `notifyReady()` method with min-period check
- [x] Implement `cancel()` method
- [x] Implement `pause()` and `resume()` methods for game pause support
- [x] Implement `shutdown()` method with graceful termination
- [x] Add comprehensive KDoc documentation

### Phase 2: Update GameServer Integration

- [x] Replace `turnTimeoutTimer: NanoTimer?` with `turnTimeoutTimer: ResettableTimer?`
- [x] Create timer once in `startGame()` instead of per-turn
- [x] Update `resetTurnTimeout()` to call `timer.schedule()` instead of creating new timer
- [x] Update `handlePauseGame()` to use `timer.pause()`
- [x] Update `handleResumeGame()` to use `timer.resume()`
- [x] Update `cleanupAfterGameStopped()` to call `timer.shutdown()`
- [x] Update `stop()` to ensure timer shutdown on server stop

### Phase 3: Deprecate/Remove Old NanoTimer

- [ ] Mark `NanoTimer` as `@Deprecated` initially
- [ ] Verify no other usages of `NanoTimer` exist (`readyTimeoutTimer` uses it too)
- [ ] Decide: refactor `readyTimeoutTimer` to use `ResettableTimer` or keep `NanoTimer` for one-shot use
- [ ] Remove or keep `NanoTimer` based on decision above

### Phase 4: Testing

- [ ] Add unit tests for `ResettableTimer`:
  - [ ] Test max delay execution
  - [ ] Test notifyReady() before min delay (should wait)
  - [ ] Test notifyReady() after min delay (should execute immediately)
  - [ ] Test cancel() prevents execution
  - [ ] Test pause/resume timing
  - [ ] Test shutdown cleanup
- [ ] Add integration test: run 10000 turns, verify single thread
- [ ] Add memory test: verify no thread count growth over time
- [ ] Update any existing timing-related tests

### Phase 5: Validation

- [ ] Run existing server test suite
- [ ] Manual testing with GUI at 30, 100, 500 TPS
- [ ] Profile memory usage during extended session (1+ hours)
- [ ] Verify no timing regressions with stopwatch measurements
- [ ] Test game pause/resume behavior

### Phase 6: Documentation

- [ ] Update any internal developer documentation
- [ ] Add code comments explaining the thread reuse pattern
- [ ] Document the `TurnTimeoutTimer` thread in debugging guides (if any exist)

## Notes

- `readyTimeoutTimer` also uses `NanoTimer` but is only created once per game start (not per turn), so it's lower priority but could benefit from same pattern.
- The `ResettableTimer` could be made generic enough to replace both use cases.

