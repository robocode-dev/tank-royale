---
id: CAP-021
type: capability
goal: G-001
status: draft
links: [G-001, P-007, CAP-001, CAP-016]
title: Rumble battle worker
---

# CAP-021 — Rumble battle worker

Run one pinned Rumble battle in an isolated worker process that receives a job and answers only with versioned, machine-readable messages, so the program that holds a participant's GitHub credentials never runs other people's bot code in its own process.

The worker is the executable `rumble` in worker mode. It depends on the [Battle Runner](../CAP-001-battle-runner/README.md) and on the small unpublished module `lib:rumble-protocol` that defines the messages, and on nothing else of the Rumble code. The host side, which starts the worker with a scrubbed environment and resource limits, and the controller that chooses battles, are later slices of the same capability. The external [Rumble client](../CAP-016-rumble-client/README.md) repository keeps its own behavior until it is replaced.
