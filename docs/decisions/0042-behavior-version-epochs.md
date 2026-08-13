---
id: ADR-0042
type: decision
status: inferred
links: [P-003, M-005, CAP-006]
title: Behavior Version as the Battle Compatibility Contract
author: agent
accepted-by: []
---

# ADR-0042: Behavior Version as the Battle Compatibility Contract

## Context

Tank Royale releases server, booter, GUI, recorder, runner, and Bot APIs in lockstep. The release version therefore says that the product artifacts belong together, but it does not say whether the game outcome can have changed: a GUI-only release and a physics change both advance that version. The Rumble needs one durable compatibility key for result epochs and client/server agreement.

## Decision

The server owns a positive integer `behaviorVersion`, initially `1`, and advertises it in every `server-handshake`. It is independent of the product release version and is bumped only when a game-observable change can alter a battle outcome, including physics, scoring, turn processing, randomness, or Bot API behavior that changes bot-visible outcomes.

The field is additive on the wire. Current servers always emit it; typed clients represent it with a nullable/default value while reading handshakes so an older server remains readable. A client or Rumble validator may use the value as the compatibility epoch once it is present; it must not infer the epoch from the release version.

The initial preparation change adds a fixed-input deterministic regression hook. The hook replays a small, explicit setup and input sequence through the physics core and compares stable snapshots; the complete replay corpus and playback product remain separate work.

## Rationale

An explicit behavior axis prevents harmless release changes from resetting Rumble results while making outcome-changing changes visible and enforceable. Keeping ownership in the server avoids four Bot API implementations independently deciding whether a release changes the game.

## Consequences

- Rumble clients can pin one compatibility epoch and reject mixed results later.
- A behavior change carries an explicit version bump instead of relying on semantic-versioning interpretation.
- Older clients can continue to connect because the new handshake field is additive and unknown fields are ignored by the protocol participants.
- The regression hook gives CI a deterministic guard without committing a large binary replay corpus or implementing playback in this change.
- The initial value and future bump discipline are inferred until a human accepts this change at the merge boundary.

## References

- [P-003 — Tank Royale Rumble](../plans/P-003-rumble.md)
- [Rumble umbrella design](../design/rumble/README.md)
- [ADR-0008 — Server-Authoritative Deterministic Physics](0008-server-authoritative-physics.md)
