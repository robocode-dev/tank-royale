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

C-003's parity residual continues to name console output as an open divergence until M-018 closes it. Parity work that touches the TypeScript Bot API may cite P-005 rather than restating the gap.
