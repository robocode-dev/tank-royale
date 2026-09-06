---
id: CH-035-tasks
type: tasks
status: draft
links: [CH-035-proposal]
title: Tasks for CH-035 (TypeScript console output capture)
provenance: inferred
reversal-cost: low
---

# CH-035 tasks

- [x] Add CAP-011 acceptance criteria for console-output capture (TBA-132..TBA-136), each declaring
      `Test-type: Unit`, since none existed for this behavior before this change
- [x] Implement `ConsoleCapture` (install/restore/drain, pass-through to real `console`,
      log/info→stdOut, warn/error→stdErr) — serves TBA-132, TBA-133
- [x] Install `ConsoleCapture` only where `bot.run()` executes (`startAsWorker`, and
      `startAsMain`'s no-Worker fallback) — serves TBA-134
- [x] Drain into `this.intent.stdOut`/`stdErr` at `renderGraphicsToIntent()` and clear both fields
      after each intent send (`sendIntentDirect`, `sendIntentToMain`) — serves TBA-132, TBA-133
- [x] Drain the round-end residual right after `processRoundEnded`'s final `dispatchEvents` call so
      it rides on the next round's first intent — serves TBA-135
- [x] Restore the original `console` methods at `processGameEnded`, the non-worker/worker
      `gameAborted` handling, and the non-worker/worker `disconnected` handling — serves TBA-136
- [x] Add focused vitest unit coverage (positive and negative direction) for TBA-132..TBA-136
- [x] Update CAP-011's design.md to link the new criteria to IDR-005 (design lives in the
      implementation/architecture corpus per its current note; this just closes that pointer for
      the new behavior)
