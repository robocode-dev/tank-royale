---
id: CAP-011-design
type: design
status: draft
links: [CAP-011]
title: Design notes for CAP-011 (typescript-bot-api)
provenance: inferred
reversal-cost: low
---

# CAP-011 design

No capability-local design was extracted at CH-001 — the design lives in the implementation and the architecture corpus (`docs/architecture/`, `docs/decisions/`). Pull design close to the criteria when this capability next changes.

Standard-output capture (TBA-132..TBA-136) follows [IDR-005](../../decisions/IDR-005-typescript-console-capture-boundary.md): `console.log`/`info`/`warn`/`error` are overridden (never the standard-stream pipes), installed once in whichever context runs `bot.run()` (`BaseBotInternals.ConsoleCapture`), and drained into `BotIntent.stdOut`/`stdErr` at the existing intent-send point.
