---
id: PDR-013
type: decision
status: inferred
links: [P-005]
title: TypeScript console-output parity is a tracked plan, not a defect fix
author: agent
accepted-by: []
---

# PDR-013 — TypeScript console-output parity is a tracked plan, not a defect fix

## Context

The TypeScript Bot API never populates `BotIntent.stdOut` or `stdErr`, so a TypeScript bot's console output never reaches the GUI. The other three Bot APIs have carried this since before the corpus. It reads as a small omission, but TypeScript runs bot code inside a Worker and assembles the intent there, so the capture point and the Worker crossing have to be decided before anything is written, and the browser path has no `process.stdout` to record.

## Decision

The gap is represented by plan P-005 with milestones M-016 through M-018, rather than being folded into a parity change as a defect fix. The capture boundary earns its own IDR under M-016 before implementation begins.

## Consequences

P-005 tracked the TypeScript console-output gap through boundary decision, implementation, and parity evidence; M-018 closes the standing-output residual. Direct writes to `process.stdout`/`process.stderr` remain the disclosed TypeScript boundary in IDR-005, not an open console-output gap. Parity work that touches the TypeScript Bot API may cite P-005 rather than restating the former gap.
