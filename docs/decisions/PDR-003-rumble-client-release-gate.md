---
id: PDR-003
type: decision
status: inferred
links: [P-003, M-008]
title: The Rumble client waits for the official engine release
author: agent
accepted-by: []
---

# PDR-003 — The Rumble client waits for the official engine release

## Context

Ranked Rumble battles must target a published immutable engine distribution rather than unreleased source or a partial runtime contract.

## Decision

M-008 resumes only after the official Tank Royale 1.1.0 release is available.

## Consequences

The client work is gated by a stable engine artifact and cannot accidentally bind the Rumble to an unreleased implementation.
