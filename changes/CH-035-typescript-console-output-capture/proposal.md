---
id: CH-035-proposal
type: proposal
status: draft
links: [P-005]
title: TypeScript Bot API captures console output into BotIntent
provenance: inferred
reversal-cost: low
---

# CH-035 — TypeScript Bot API captures console output into BotIntent

## What

Implement P-005's M-017: the TypeScript Bot API populates `BotIntent.stdOut` and `BotIntent.stdErr`
from the bot's `console.log`/`info`/`warn`/`error` calls, in both worker and non-worker (legacy
Node / browser) modes, following the accepted [IDR-005](../../docs/decisions/IDR-005-typescript-console-capture-boundary.md).

## Why

TypeScript is currently the only Bot API where a bot author's console output never reaches the
GUI, because `BaseBotInternals` never populates `stdOut`/`stdErr` on the intent it sends. IDR-005
already settled where and how capture must happen; this change carries out that decision so
TypeScript bot authors stop losing a facility every other language provides (G-001, C-003's
standing-output residual).

## Scope

- A small `ConsoleCapture` helper that overrides the four `console` methods, buffers their
  formatted output by stream (`log`/`info` → stdout, `warn`/`error` → stderr), and still forwards
  every call to the real `console` method.
- Installation exactly where `bot.run()` executes: inside the Worker bootstrap (`startAsWorker`)
  for worker mode, and inside `startAsMain`'s no-Worker fallback for the legacy/browser path. A
  worker-spawning main thread never installs it.
- Draining into `this.intent.stdOut`/`stdErr` at the existing `renderGraphicsToIntent()` call site
  (shared by `sendIntentDirect` and `sendIntentToMain`), and again right after `processRoundEnded`'s
  final dispatch, so output from final-tick handlers rides on the next round's first intent instead
  of being dropped.
- Restoring the original `console` methods at the lifecycle points where the installing context
  tears down: `processGameEnded`, the non-worker/worker `gameAborted` handling, and the
  non-worker/worker `disconnected` handling.
- New CAP-011 acceptance criteria for this behavior (none existed), each with `Test-type: Unit`
  and focused vitest coverage.

## Out of scope

- Byte-for-byte capture of direct `process.stdout`/`process.stderr` writes that bypass `console` —
  IDR-005 explicitly disclaims this as a divergence.
- Full parity test coverage mirroring the Java/.NET/Python suites — that is P-005's M-018.
- Any GUI-side rendering change; the GUI already renders `stdOut`/`stdErr` for the other three APIs.
