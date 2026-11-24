---
id: CAP-001-design
type: design
status: draft
links: [CAP-001]
title: Design notes for CAP-001 (battle-runner)
provenance: inferred
reversal-cost: low
---

# CAP-001 design

No capability-local design was extracted at CH-001 — the design lives in the implementation and the architecture corpus (`docs/architecture/`, `docs/decisions/`). Pull design close to the criteria when this capability next changes.

## Team members

A team's `teamMembers` entry names a member by its folder or by its bot's name and version, such as `Corners 1.0.0`, which is how the Rumble writes them. Under the [decision that a team names each member by its folder or by its name and version](../../decisions/ADR-058-team-members-found-by-folder-or-identity.md), one function in the common library, `TeamMembers.find`, finds the member: a folder beside the team with exactly the entry's name, otherwise the one folder beside it whose bot file has that name and version joined by one space, otherwise nothing, with every matching folder named when there are several; the team's own folder is never a member. The Runner's check of a team directory and its list of expected identities both use it, and its `BattleException` names the member and the folders; the booter and the desktop program's boot progress use the same function, so every part of the engine agrees which folder a member is.

## Boot progress

While a battle starts, the runner waits for its bots to connect, up to its connect limit (30 seconds unless a caller sets another), and fires a progress report on every change of the connected bots and about every half second. The handle of a battle carries this event, but it exists only after the bots have connected, so a caller of the synchronous run could never see it. A runner can therefore be given a listener when it is built, which is called with the same reports for every battle; a listener that throws is logged and never stops the battle. The Rumble's battle worker uses it to tell a contribution session how far a battle's bots are.

The booter reports a bot process that ends by itself with the line `exited <pid>`, besides `stopped <pid>` for one it stopped, and the runner ends the wait for its bots at once when a bot whose process ended has not connected, instead of waiting the rest of the limit. Either way the `BattleException` names the bots that had not connected in `notConnected`. A bot whose process runs without connecting is still waited for until the limit, since a process that is alive may yet connect.
