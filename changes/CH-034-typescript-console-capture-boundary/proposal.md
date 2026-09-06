---
id: CH-034
type: change
status: open
links: [P-005, M-016]
title: Decide the TypeScript console-output capture boundary
---

# CH-034 — Decide the TypeScript console-output capture boundary

## What

Produce and accept the IDR that M-016 requires: where the TypeScript Bot API captures a bot's console output, how the captured text crosses the Worker boundary alongside the intent, and what the non-worker (legacy Node fallback) and browser paths do. No capture code is written in this change; M-017 implements the decision.

## Why

P-005 exists because TypeScript is the only Bot API where wiring `BotIntent.stdOut`/`stdErr` is not a mechanical port of the Java, .NET, and Python recording-stream approach: bot code can run inside a Worker, `process.stdout` does not exist in a browser, and only `console` is interceptable everywhere. PDR-013 deliberately carved the capture-boundary question into its own IDR before M-017 begins, so the Worker-crossing design is fixed once rather than discovered mid-implementation.

## Scope

- Read the current TypeScript Bot API worker/non-worker architecture (`BaseBotInternals.ts`, `WebSocketHandler.ts`) and the Java reference capture (`RecordingPrintStream`, `BaseBotInternals.transferStdOutToBotIntent`).
- Write IDR-005 stating the capture point, the Worker-crossing mechanism, and the non-worker/browser behavior.
- Update P-005's M-016 row (status, evidence) once the IDR is written.

## Non-goals

- Implementing the capture (M-017).
- Adding or changing tests for stdOut/stdErr parity (M-018).
- Changing the Java, .NET, or Python capture implementations.

## Plan

Serves [P-005/M-016](../../docs/plans/P-005-typescript-bot-console-output.md).
