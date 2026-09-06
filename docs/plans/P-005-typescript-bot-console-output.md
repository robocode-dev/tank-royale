---
id: P-005
type: plan
status: completed
links: [G-001]
title: TypeScript bot console output reaches the GUI
provenance: inferred
reversal-cost: low
---

# P-005 — TypeScript bot console output reaches the GUI

P-005 closed the TypeScript standing-output gap: bot authors now see TypeScript console output in the GUI, matching the other three Bot APIs.

A bot's console output reaches the GUI only through `BotIntent.stdOut` and `BotIntent.stdErr`: the booter discards a bot process's stdout pipe outright (`BotBooter.kt`), so the pipe is not a second route. The Java, .NET and Python Bot APIs install recording streams over the process's standard streams, while TypeScript captures its four console methods, and all four APIs drain output into the intent on every send and again after a round ends.

The protocol side was already in place: `stdOut` and `stdErr` exist on `BotIntent` in the TypeScript schema. M-017 delivered the capture and plumbing, and M-018 completed the cross-platform evidence.

At the start of P-005, TypeScript was the only Bot API where this was not a mechanical port. Its bot code runs inside a Worker by default, and the intent is assembled there and handed to the main thread by `postMessage`. Capture therefore had to happen on the Worker side and travel with the intent, with the same code behaving sensibly in the non-worker path and in a browser, where `process.stdout` does not exist and only `console` can be intercepted. That boundary question made this a campaign rather than a single change.

Serves G-001: cross-platform play only counts when a bot author switching language does not lose a facility the other languages provide. Closes the standing-output portion of the C-003 parity residual.

| ID | Milestone | Exit criterion | Status | Evidence |
|---|---|---|---|---|
| M-016 | Capture boundary decided | An accepted IDR states where TypeScript captures console output, how it crosses the Worker boundary with the intent, and what the non-worker and browser paths do | done | [IDR-005](../decisions/IDR-005-typescript-console-capture-boundary.md) |
| M-017 | Console output populates the intent | `BotIntent.stdOut` and `stdErr` carry the bot's output on every intent send and after a round ends, in both worker and non-worker modes, matching where the Java Bot API drains its recording streams | done | CH-035, [CAP-011 TBA-132..TBA-136](../capabilities/CAP-011-typescript-bot-api/criteria.md) |
| M-018 | Parity evidenced and residual closed | Tests mirror the Java, .NET and Python coverage for standard-stream capture, and C-003's residual no longer names console output as an open divergence | done | CH-036; `:bot-api:java:test --tests ...RecordingPrintStreamTest`, `dotnet test ... --filter ...RecordingTextWriterTest`, `python -m pytest tests/test_stdout_capture.py`, and TypeScript `npm test`, `npm run lint`, and `npm run build` pass |

M-017 depends on M-016. M-018 depends on M-017.

This plan does not cover the GUI's rendering of bot output, which already works for the other three Bot APIs and is unchanged by it.
