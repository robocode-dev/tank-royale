---
id: CH-037-tasks
type: tasks
status: open
links: [CH-037]
title: Task breakdown for CH-037
---

# CH-037 — Tasks

- [x] Reserve CH-037 and branch from the accepted tip of `main`
- [x] Confirm M-012's exit criterion, `rumble-client#10`, and that RCL-004/RCL-001 already accept the practice-mode behavior being restored
- [x] Capture the full-route proposal and selected scope
- [ ] Commit and push the proposal, then open the required draft PR before implementation
- [ ] Spec-first pause: report the proposal and implementation plan, wait for explicit direction to proceed
- [ ] Remove the ranked-only guard from `RumbleSynchronizer.synchronize()` (RCL-004)
- [ ] Remove the ranked-only guard from `BotCachePreparer.prepare()` (RCL-004)
- [ ] Remove the ranked-only guard from `RankedBattleSelector.select()` (RCL-004)
- [ ] Remove the ranked-only guard from `RankedBattleExecution.execute()` (RCL-004)
- [ ] Gate `--submit` in `RumbleClient.run()` on ranked mode with a clear early error (RCL-004)
- [ ] Gate ranked-journal append and quarantine in `RumbleClient.run()`'s `--run` handling on `ClientMode.permitsRankedJournal()` (RCL-004)
- [ ] Update `RumbleSynchronizerTest#testUnitNegative_rejectsSynchronizationInPracticeModeBeforeRepositoryAccess` and `RankedBattleExecutionTest#testRCL004_IntegrationNegative_practiceModeCannotCreateRankedResult` to match restored behavior
- [ ] Add or extend RCL-001/RCL-004 evidence for a practice-mode `--sync` and `--run` that use local bot sources without journaling or submitting, and for `--submit` refusing cleanly in practice mode
- [ ] Verify the full `rumble-client` test suite passes and open the pull request for human review and merge
- [ ] Reconcile M-012 bookkeeping in the permanent corpus after the `rumble-client` pull request merges
