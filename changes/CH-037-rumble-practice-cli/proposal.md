---
id: CH-037
type: change
status: open
links: [P-004, M-012, CAP-016]
title: Fix practice mode's missing CLI execution path
---

# CH-037 — Fix practice mode's missing CLI execution path

## What

Remove the four unconditional ranked-mode guards in `rumble-client` (`RumbleSynchronizer.synchronize()`, `BotCachePreparer.prepare()`, `RankedBattleSelector.select()`, `RankedBattleExecution.execute()`) so that `--sync` and `--run` execute in practice mode using local bot sources, while `--submit` and ranked-journal persistence stay ranked-only.

## Why

P-004/M-012 requires `sync`, `run`, and `submit` to execute in practice mode. [`rumble-client#10`](https://github.com/robocode-dev/rumble-client/issues/10) confirms a `mode: practice` configuration has no working CLI path at all today: every command throws `IllegalArgumentException` immediately because `RumbleSynchronizer.synchronize()` — which `--sync`, `--run`, and `--submit` all funnel through — unconditionally requires `ClientMode.RANKED`.

This is a defect against an already-accepted criterion, not a new promise: [RCL-004](../../docs/capabilities/CAP-016-rumble-client/criteria.md) and its design note already state that "practice mode may use local bot sources but never appends to the ranked journal or invokes a submission transport." Inspecting the four guarded methods shows none of their logic beneath the guard is actually ranked-specific — each operates on the resolved snapshot, catalog, and configuration alike. The guards simply block the practice path the criterion already promises. Fixing this restores RCL-004; it does not change its meaning.

## Route

Recommended route: full. P-004 (mirroring P-003) requires every milestone delivered outside this repository to carry its own change proposal and decision record here before implementation, regardless of whether the corpus's own accepted criteria change. Discovery would change the route only if `rumble-client`'s accepted criteria (RCL-004, RCL-001) turn out to need a meaning change rather than a defect fix to satisfy M-012 — nothing found so far suggests that.

## Plan

Serves [P-004/M-012](../../docs/plans/P-004-rumble-hardening.md) without changing the accepted RCL-001 or RCL-004 criterion meanings.

## Scope

- In `robocode-dev/rumble-client`, remove the `configuration.mode() != ClientMode.RANKED` guard from `RumbleSynchronizer.synchronize()`, `BotCachePreparer.prepare()`, `RankedBattleSelector.select()`, and `RankedBattleExecution.execute()`.
- Also make `RumbleSnapshotParser.parse()`'s client registration lookup ranked-only (found during implementation): it unconditionally required a registered `clientId`, which practice mode may omit since a practice result is never journaled or submitted under an identity.
- In `RumbleClient.run()`, gate `--submit` on ranked mode with a clear early error (before synchronizing), since practice mode must never invoke the submission transport (RCL-004).
- In `RumbleClient.run()`'s `--run` handling, gate `RankedJournal.append(...)` (and obsolete-record quarantine) on `configuration.mode().permitsRankedJournal()`, since practice mode must never append to the ranked journal (RCL-004).
- Update or replace the existing tests that currently assert the buggy behavior as correct: `RumbleSynchronizerTest#testUnitNegative_rejectsSynchronizationInPracticeModeBeforeRepositoryAccess` and `RankedBattleExecutionTest#testRCL004_IntegrationNegative_practiceModeCannotCreateRankedResult`.
- Add or extend RCL-001/RCL-004 evidence proving a practice-mode `--sync` and `--run` complete using local bot sources without touching the ranked journal or submission transport, and that `--submit` refuses cleanly in practice mode.
- Reviewed and merged in `robocode-dev/rumble-client` through its normal pull-request flow (that repository carries no Cliewen workspace of its own).

## Non-goals

- Changing RCL-001, RCL-004, or any other accepted `rumble-client` criterion meaning.
- Implementing a practice-mode GUI battle-setup path (out of scope for M-012; CLI only).
- Touching `rumble-bots` or `rumble-data`, or M-013/M-015's independent scope.
- A Tank Royale release.
