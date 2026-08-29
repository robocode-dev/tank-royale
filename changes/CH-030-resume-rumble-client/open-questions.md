---
id: CH-030-open-questions
type: open-questions
status: open
links: [CH-030]
title: Open questions for CH-030
---

# CH-030 — Open questions

## How does the client verify the running server's behavior version?

RCL-004 requires ranked execution to reject a server whose advertised `behaviorVersion` differs from `engine.json`. The published Battle Runner 1.1.0 implementation receives `ServerHandshake.behaviorVersion` internally but its public API exposes only server features; the client cannot observe the value through the approved dependency. ADR-042 explicitly forbids inferring the compatibility epoch from the release version.

Resolving this needs a scope decision: either expose the handshake behavior version through a new Battle Runner public API, which expands CH-030 beyond its current non-goals and requires a coordinated Tank Royale change, or revise the CAP-016 contract with another compatibility mechanism. The approved dependency decision in [IDR-004](../../docs/decisions/IDR-004-rumble-client-battle-runner-dependency.md) remains valid but is insufficient to implement RCL-004.
