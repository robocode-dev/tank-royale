---
id: ADR-046
type: decision
status: verified
links: [CAP-001, CAP-016, ADR-042]
title: Battle Runner enforces an expected behavior version before bot boot
author: agent
accepted-by: Flemming N. Larsen (2026-08-29, Codex conversation)
---

# ADR-046 — Battle Runner enforces an expected behavior version before bot boot

## Context

Ranked Rumble execution must compare the synchronized engine epoch with the running server's handshake before the Runner starts the requested untrusted bot code. Battle Runner receives the server handshake internally but previously exposed no way for a caller to require its `behaviorVersion`.

## Decision

Battle Runner adds an optional `requireBehaviorVersion(int)` builder precondition. When configured, the Runner requires both role handshakes to advertise the same positive value and requires that value to equal the expected version after connection but before the Runner starts the requested bot processes. A missing, inconsistent, or mismatched value fails the battle with `BattleException`.

The default remains unpinned for backward compatibility. The API does not expose unrelated handshake fields or infer behavior compatibility from the Tank Royale release version.

## Consequences

Rumble Client can delegate fail-closed compatibility enforcement to the component that owns the server connection. Existing Runner callers retain their current behavior, while callers that require an epoch must opt in explicitly. The merged API may be consumed from a local build during client development, but it must ship in a released Runner artifact before the external client is distributed for ranked use.
