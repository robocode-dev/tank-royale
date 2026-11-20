---
id: CAP-004-design
type: design
status: draft
links: [CAP-004]
title: Design notes for CAP-004 (gui-boot-progress)
provenance: inferred
reversal-cost: low
---

# CAP-004 design

No capability-local design was extracted at CH-001 — the design lives in the implementation and the architecture corpus (`docs/architecture/`, `docs/decisions/`). Pull design close to the criteria when this capability next changes.

## Team members

A team's `teamMembers` entry names a member by its folder or by its bot's name and version, such as `Corners 1.0.0`, which is how the Rumble writes them. Under the [decision that a team names each member by its folder or by its name and version](../../decisions/ADR-058-team-members-found-by-folder-or-identity.md), one function in the common library, `TeamMembers.find`, finds the member: a folder beside the team with exactly the entry's name, otherwise the one folder beside it whose bot file has that name and version joined by one space, otherwise nothing, with every matching folder named when there are several. The boot progress reads the identities it waits for through it; the battle runner and the booter use the same function uses it, so every part of the engine agrees which folder a member is.
