---
id: CH-009
type: change
status: open
links: [P-003, M-005, CAP-001, CAP-002, CAP-006]
title: Prepare Tank Royale for Rumble
---

# CH-009 — Prepare Tank Royale for Rumble

## What

Prepare the existing Tank Royale repository for the first Rumble client and its later
`rumble-bots` and `rumble-data` repositories. Add the server-owned `behaviorVersion` compatibility
axis to the server handshake and protocol schema, extend the general booter bot configuration with
an optional SPDX `license` field, add the `twinduel` game type and Battle Runner preset, and add a
small deterministic physics regression hook that can grow into the replay corpus later.

The existing Battle Runner `BattleResults` API already exposes the per-participant result data the
client needs; this change verifies and documents that seam rather than inventing a second result
model.

## Why

Rumble results must be comparable across clients and releases. Release versions cover the whole
product, while `behaviorVersion` identifies the game-observable compatibility epoch. The Rumble
design also requires every submitted bot to carry an explicit machine-readable license and needs
the existing runner to express the three ranked formats: `1v1`, `TwinDuel`, and `Melee`.

## Scope

- Add an initial `behaviorVersion` constant and advertise it from every new server handshake.
- Add the additive `behaviorVersion` field to the server-handshake schema and the JVM client model.
- Add optional `license` metadata to booter entries and preserve it in directory listings.
- Add the `twinduel` common game type and its 800×800, four-participant, 75-round preset.
- Add focused acceptance criteria and tests for the new protocol metadata and preset.
- Add a deterministic, fixed-input server regression hook; the full replay corpus and playback
  product remain future work.
- Record the behavior-version contract as an inferred ADR and align the relevant architecture and
  user-facing documentation.

## Non-goals

- Creating `rumble-bots`, `rumble-data`, or the Rumble client.
- Implementing result submission, matchmaking, ranking, or replay playback.
- Requiring a license for existing local bots at runtime; validation policy belongs to the future
  submission repository.

## Compatibility

The new handshake field is additive on the wire. New servers always send the current integer;
clients retain a nullable/default representation so older servers remain readable. The license
field is optional and ignored by gameplay. Existing game types and Battle Runner factories keep
their current defaults.

## Plan

Serves [P-003/M-005](../../docs/plans/P-003-rumble.md). The proposal follows the Rumble roadmap in
[the umbrella design](../../docs/design/rumble/README.md); no open design question is introduced.
