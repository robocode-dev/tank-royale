---
id: ADR-058
type: decision
status: verified
links: [ADR-047, CAP-001, CAP-002, CAP-004, CAP-016, P-007]
title: A team names each member by its folder or by its name and version, and every part of the engine finds the member the same way
author: agent
accepted-by: Flemming N. Larsen (2026-11-20, Claude Code conversation)
---

# ADR-058: A team names each member by its folder or by its name and version, and every part of the engine finds the member the same way

## Context

A team's bot file lists its members in `teamMembers`. The engine has always read each entry as the name of a folder beside the team's own folder: the battle runner checks that the folder exists and reads the member's identity from it, the booter starts the member from it, and the desktop program reads the identities it waits for when bots boot. The Rumble writes a team's members differently: the [decision that the catalog publishes team membership](ADR-047-rumble-catalog-publishes-team-membership.md) names them as catalog identities, and the submission rules require each member to be written as `<name> <version>`, such as `Corners 1.0.0`, while the member's folder is named `Corners`. So no Rumble team can be booted: a contribution session's TwinDuel battles fail with "Team member directory not found", and a Rumble team downloaded with Download bots fails the same way in New Battle. Three places in the engine and one in the desktop program each find members on their own.

## Decision

A team's `teamMembers` entry names a member in either of two forms, and every place that finds team members uses one shared rule:

1. **A folder with exactly the entry's name** beside the team's folder is the member, as it always was. Existing teams behave as before, also where a folder happens to be named like `Corners 1.0.0`.
2. **Otherwise**, the member is the one folder beside the team's folder whose bot file (`<folder>/<folder>.json`) has a name and version that, joined by one space, equal the entry exactly. The entry is never split, so names with spaces work; nothing is trimmed or normalized.
3. **No folder, or more than one, matches**: nothing is guessed between two copies of the same bot. The battle runner and the desktop program's boot progress refuse the team with an error that names the entry and, when several match, every matching folder; the booter logs that member with the same reason and still starts the team's other members.

A bot without a bot file has no name and version to compare, so it can only be named by its folder. Only the bot files of the team's sibling folders are read, and only when the first form found nothing. The team's own folder is never one of its members. Each program reads a sibling's name and version with its own bot file reader, so a bot file that one reader refuses, such as one without its authors, can be found by another; the Rumble's bot checks keep such files out of its catalog.

The rule lives in one function in the shared common library, given how to read a folder's name and version, and the battle runner's check, the battle runner's list of expected identities, the booter and the desktop program's boot progress all use it.

## Rationale

Teaching the engine the Rumble's form keeps one team format working everywhere, in contribution sessions, in New Battle with downloaded bots and in the battle runner, without changing the Rumble's submissions, its catalog or teams already published. Trying the folder name first keeps every existing team as it is. Matching the whole text against `name version` from the bot file avoids parsing names that contain spaces. Refusing an ambiguous match keeps a downloaded copy or an own copy of the same bot from being chosen by accident. One shared function keeps the four places from disagreeing about which folder a member is, which would let a battle wait for an identity the booter never starts.

## Consequences

- Rumble teams boot in contribution sessions, in New Battle and in the battle runner.
- A folder beside a team that holds two copies of the same bot and version can no longer be used for a team that names that member by identity; the error says so, and the person removes or renames one copy.
- Finding a member by identity reads the bot files of the folders beside the team once for each member named that way, and the battle runner does so both when it checks the team and when it lists the identities it waits for; with a folder name match nothing more is read.
- The Rumble keeps writing members as `<name> <version>`, and other tools that read a team's members must understand both forms.

## References

- [ADR-047: The catalog publishes team membership](ADR-047-rumble-catalog-publishes-team-membership.md)
