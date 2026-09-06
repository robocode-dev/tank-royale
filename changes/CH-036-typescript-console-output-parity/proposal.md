---
id: CH-036-proposal
type: proposal
status: draft
links: [P-005]
title: Evidence TypeScript console-output parity
provenance: inferred
reversal-cost: low
---

# CH-036 — Evidence TypeScript console-output parity

## What

Complete P-005's M-018 by extending the TypeScript console-capture tests to mirror the established Java, .NET, and Python recording-stream coverage, then close the plan's standing-output residual.

## Why

CH-035 made TypeScript console output reach `BotIntent.stdOut` and `stdErr`, but its tests currently prove the TypeScript-specific wiring rather than the recorder behavior already covered on the other Bot APIs. The parity claim needs evidence for preserved text, buffer draining, and pass-through before the P-005 residual can be closed.

## Scope

- Add focused TypeScript unit coverage for control characters, quotes, backslashes, buffer reset between drains, and forwarding to the original console methods.
- Run the focused recorder/capture tests on Java, .NET, Python, and TypeScript and retain the commands as milestone evidence.
- Mark P-005/M-018 done and revise the PDR-013 consequence so console output is no longer described as an open parity divergence.

## Out of scope

- Changes to the capture implementation or the accepted IDR-005 boundary.
- Capturing direct `process.stdout`/`process.stderr` writes, which remains the disclosed TypeScript boundary divergence.
- GUI rendering or protocol-schema changes.

## Dependency

This change is based on the unmerged CH-035 implementation (PR #259, base commit `913544170e3b8a3494442a07e424b64509b9a8af`). Its acceptance binds the M-018 evidence and digest on top of CH-035; it must not be integrated unless CH-035 is accepted first.
