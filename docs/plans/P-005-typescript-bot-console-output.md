---
id: P-005
type: plan
status: draft
links: [G-001]
title: TypeScript bot console output reaches the GUI
provenance: inferred
reversal-cost: low
---

# P-005 — TypeScript bot console output reaches the GUI

TypeScript bot authors see none of their console output in the GUI, unlike authors on the other three Bot APIs.

A bot's console output reaches the GUI only through `BotIntent.stdOut` and `BotIntent.stdErr`: the booter discards a bot process's stdout pipe outright (`BotBooter.kt`), so the pipe is not a second route. The Java, .NET and Python Bot APIs each install a recording stream over the process's standard streams and drain it into the intent on every send and again after a round ends. The TypeScript Bot API never populates either field.

The protocol side is already in place: `stdOut` and `stdErr` exist on `BotIntent` in the TypeScript schema. What is missing is the capture and the plumbing.

TypeScript is the only Bot API where this is not a mechanical port. Its bot code runs inside a Worker by default, and the intent is assembled there and handed to the main thread by `postMessage`. Capture must therefore happen on the Worker side and travel with the intent, and the same code has to behave sensibly in the non-worker path and in a browser, where `process.stdout` does not exist and only `console` can be intercepted. That boundary question is what makes this a campaign rather than a single change.

Serves G-001: cross-platform play only counts when a bot author switching language does not lose a facility the other languages provide. Closes the standing-output portion of the C-003 parity residual.

| ID | Milestone | Exit criterion | Status | Evidence |
|---|---|---|---|---|
| M-016 | Capture boundary decided | An accepted IDR states where TypeScript captures console output, how it crosses the Worker boundary with the intent, and what the non-worker and browser paths do | done | [IDR-005](../decisions/IDR-005-typescript-console-capture-boundary.md) |
| M-017 | Console output populates the intent | `BotIntent.stdOut` and `stdErr` carry the bot's output on every intent send and after a round ends, in both worker and non-worker modes, matching where the Java Bot API drains its recording streams | todo | |
| M-018 | Parity evidenced and residual closed | Tests mirror the Java, .NET and Python coverage for standard-stream capture, and C-003's residual no longer names console output as an open divergence | todo | |

M-017 depends on M-016. M-018 depends on M-017.

This plan does not cover the GUI's rendering of bot output, which already works for the other three Bot APIs and is unchanged by it.
