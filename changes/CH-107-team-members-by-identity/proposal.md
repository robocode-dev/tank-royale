---
id: CH-107
type: change
status: open
links: [P-007, CAP-001, CAP-002, CAP-004, CAP-016, CAP-017, ADR-058, ADR-047]
title: Find a team's members by their folder or by their name and version, the same way everywhere, so Rumble teams boot
---

# Find a team's members by their folder or by their name and version, the same way everywhere, so Rumble teams boot

A real contribution session in the test Rumble showed a TwinDuel battle that could not start: "Team member directory not found: Corners 1.0.0". The Rumble writes a team's members as `<name> <version>`, while the engine reads each member as the name of a folder beside the team. The same team downloaded with Download bots cannot be booted in New Battle either. The plan item for contributor-controlled unattended sessions (M-030) promises that sessions run all three ranked formats, and TwinDuel needs teams. The [decision that a team names each member by its folder or by its name and version](../../docs/decisions/ADR-058-team-members-found-by-folder-or-identity.md) has every part of the engine find a member the same way: a folder with the entry's exact name first, otherwise the one sibling folder whose bot file has that name and version, and otherwise a clear error.

## Why

Without it no Rumble team boots anywhere, so TwinDuel cannot be ranked by any contributor, and downloaded teams cannot be tried.

## What changes

- **The decision is recorded**, and the battle runner's, booter's and boot progress capabilities and the Rumble client's design say what it changes.
- **One shared function in the common library** finds a team member beside the team's folder by the three rules of the decision and says which folder it is, that none matches, or which folders all match. It is given how to read a folder's name and version, since each program reads bot files with its own parser.
- **The battle runner's check** of a team folder and **its list of expected identities** use it, and its error names the entry and the folders.
- **The booter** starts each member from the folder the function finds, and logs a member it cannot find with the same reason.
- **The desktop program's boot progress** reads the identities it waits for through it.
- **New Battle counts every bot as a participant**, also a team's members, as the server does. It counted each team as one participant, so two teams of two bots never filled the four TwinDuel places and Start Battle stayed off; this was found when the maintainer tried two Rumble teams in New Battle. The desktop program's tests now also run its JUnit tests: until now only its Kotest specs ran, so the existing TwinDuel preset tests had never run.
- **The desktop program's own server accepts TwinDuel.** It was started with the game types classic, melee and 1v1 only, so it silently refused a TwinDuel from New Battle; it now accepts every game type New Battle offers. This too was found in the maintainer's trial.
- **A contribution session** needs no change of its own: its team and members already lie side by side in the bot cache, each checked against the catalog's hash.

## Challenge

- **Assumption most likely to be wrong:** that the four places are all the places that find team members. Another one, such as the server or the desktop program's bot list, could still read members as folder names and disagree. The search for `teamMembers` in every module is part of the work, and a fifth place found means it uses the function too.
- **Credible alternative:** the Rumble writes members as folder names. Rejected with the decision: it changes the submission rules and the published teams, and the catalog's identities would no longer appear in the team's own file.
- **Cheapest test:** the function with folders on disk for every rule in both directions (exact folder, identity match, names with spaces, two copies, none, a bot without a bot file); the runner and booter with a Rumble-form team; then one real contribution session in the test Rumble with TwinDuel whose result is accepted.
- **Result that revises the work:** a real TwinDuel battle with a Rumble team that still does not boot, or two places that find different folders for the same entry, stops the slice.
- **How it could pass every criterion and still fail a contributor:** a team in a folder of downloaded bots next to the person's own copy of a member is refused as ambiguous. The error therefore names both folders, so the person can see what to remove.

## Out of scope

Changing the Rumble's submission rules or catalog, validating teams in the bot project checks, and the server, which never reads bot folders.

## Acceptance criteria

Test class Unit unless stated.

- **BR-050: The battle runner finds a team's members by their folder, else by their name and version, and refuses none or several matches**, in its check and its expected identities, by the one shared rule.
- **BR-051: The shared rule follows the decision exactly**: exact folder first, whole-text identity match without splitting, a bot without a bot file only by folder, and an error naming the entry and every matching folder.
- **BFD-008: The booter starts a member named by name and version** from the folder the rule finds, and logs one it cannot find with the reason.
- **GBP-010: The desktop program's boot progress waits for the identities of members named by name and version.**
- **GTD-002: Two teams of two bots fill the four TwinDuel places in New Battle**, and fewer or more bots cannot start it.
- **GTD-003: The desktop program's own server accepts every game type New Battle offers, TwinDuel among them.**
- **RCL-140: A contribution session's TwinDuel battle with Rumble teams runs and its result is accepted** in the test Rumble. (Integration, needs the saved sign-in; skipped without it.)

## Identity allocation

The change, the decision and the criteria were allocated with the repository's coordinated allocation, after the upgrade to Cliewen 0.33.1 brought the battle runner's, booter's and boot progress criteria into the identity ledger.

## Human evidence

The maintainer starts a Rumble team downloaded with Download bots in New Battle, and sees a contribution session in the test Rumble run a TwinDuel battle with Rumble teams whose result is accepted.
