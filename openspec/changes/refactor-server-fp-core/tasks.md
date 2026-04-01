## 1. Score Immutability
- [ ] 1.1 Change all `var` fields in `Score.kt` to `val`
- [ ] 1.2 Remove `Score.accumulate()` method
- [ ] 1.3 Update `AccumulatedScoreCalculator.addScores()` to use `map` + `plus` operator instead of in-place mutation
- [ ] 1.4 Verify `RankDecorator.updateRanks()` still works with immutable scores
- [ ] 1.5 Run existing score tests (`ScoreCalculatorTest`, `RankDecoratorTest`)

## 2. ScoreTracker Exception Removal
- [ ] 2.1 Change `ScoreTracker.getScoreAndDamage()` return type to `ScoreAndDamage?`
- [ ] 2.2 Update all call sites to handle nullable return

## 3. Deterministic Bot Placement
- [ ] 3.1 Replace `while(true)` in `BotInitializer.randomBotPoint()` with Fisher-Yates shuffle of available cells
- [ ] 3.2 Verify bot placement still respects grid constraints

## 4. CollisionDetector Pure/Apply Split
- [ ] 4.1 Extract `forEachUniquePair` higher-order utility function
- [ ] 4.2 Refactor bullet-bullet collision to use `forEachUniquePair`
- [ ] 4.3 Refactor bot-bot collision to use `forEachUniquePair`
- [ ] 4.4 Split `handleBotHitBot()` into pure outcome + apply
- [ ] 4.5 Split `handleBulletHittingBot()` — extend existing `BulletHitOutcome` pattern to cover events
- [ ] 4.6 Split `checkAndHandleBotWallCollisions()` into detect + apply
- [ ] 4.7 Split `checkAndHandleBulletWallCollisions()` into detect + apply
- [ ] 4.8 Return collision results from public methods instead of mutating argument collections

## 5. GunEngine Pure/Apply Split
- [ ] 5.1 Split `fireBullet()` into a pure `computeFire()` returning a `FireOutcome` data class + apply function
- [ ] 5.2 Keep fire-assist scan lookup pure (read-only from last turn)

## 6. ModelUpdater Compute/Apply Extension
- [ ] 6.1 Split `checkAndHandleInactivity()` into pure `isInactive()` check + apply
- [ ] 6.2 Split `checkForAndHandleDisabledBots()` into detect + apply
- [ ] 6.3 Split `checkAndHandleDefeatedBots()` into detect + apply
- [ ] 6.4 Collect all step outcomes in `nextTurn()` compute phase; apply all at end
- [ ] 6.5 Update `nextTurn()` to follow consistent compute→apply flow

## 7. Immutable GameState Snapshot
- [ ] 7.1 Create `GameStateSnapshot` (or make `GameState` immutable) with `List<Round>` and `val isGameEnded`
- [ ] 7.2 Update `ModelUpdater.update()` to return immutable snapshot
- [ ] 7.3 Update `GameServer` callers to use the immutable snapshot type
- [ ] 7.4 Verify all observers/broadcasters work with immutable snapshots

## 8. Verification
- [ ] 8.1 Run full server test suite
- [ ] 8.2 Run a manual battle to verify game behavior unchanged
- [ ] 8.3 Verify no regressions in score calculation across multi-round games
