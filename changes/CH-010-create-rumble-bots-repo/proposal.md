---
id: CH-010
type: change
status: open
links: [P-003, M-006]
title: Create Rumble Bots Repository
---

# CH-010 — Create Rumble Bots Repository

## What

Create the community-owned `rumble-bots` repository as the source-only catalog of ranked Tank Royale Rumble bots. The repository will adopt the existing booter folder convention and provide a portable Python validator, GitHub Actions validation and catalog-generation workflows, contributor and governance guidance, templates, ownership and ban records, and sample bots that prove the pull-request flow end to end.

The generated `bots/index.json` catalog will be the stable hand-off to the later Rumble client and data repository. It will expose each active bot's identity, platform, path, source-tree hash, ownership, authors, and version without allowing clients to depend on a hand-maintained index.

When this proposal is implemented, P-003 will also record the requested post-Rumble GUI follow-up: after M-006 through M-009 are complete, a separate milestone will let the GUI game-type dialog select and start the `TwinDuel` preset. That work is deliberately not part of this repository or this change.

## Why

Rumble participants need a reviewable, forkable source of bots before contributors can run ranked battles. Keeping submissions in their own repository separates human-paced pull-request review from the high-volume machine facts that will live in `rumble-data`, while preserving zero infrastructure cost and the single-writer rule for result data.

## Scope

- Create `robocode-dev/rumble-bots` under the community organization with its default branch and repository settings suitable for fork-and-PR contributions.
- Define the repository's source-only `bots/<platform>/<bot-name>/` layout using the Tank Royale booter convention for Java, C#, Python, and TypeScript bots.
- Implement one locally runnable Python validator and a thin CI wrapper that checks structure, source-only contents, SPDX license allowlist, official-API/dependency rules, ownership and slot limits, version immutability, banned owners and bots, confusable names, and source-run smoke boots.
- Generate and validate `bots/index.json` and ownership data after accepted submissions; prevent manual catalog edits.
- Add contributor, moderation, ownership, licensing, DCO, ban, and fork-drill guidance, plus issue and pull-request templates and CODEOWNERS.
- Add one or more sample bot submissions and focused automated evidence that a valid first-time submission passes and invalid submissions fail with useful diagnostics.
- Define the catalog contract and acceptance criteria in this repository's corpus before implementing the external repository.

## Non-goals

- Creating `rumble-data`, ingesting results, calculating rankings, publishing a dashboard, or building the Rumble client.
- Changing Tank Royale's server, Battle Runner, booter convention, Bot APIs, or game presets.
- Implementing the later GUI `TwinDuel` selection; it remains a post-M-009 follow-up.
- Creating a sandbox that makes untrusted bot code safe to execute; this repository makes it reviewable and reproducible, while client-side containment belongs to the Rumble client work.

## Compatibility

The repository consumes the booter convention and `license` field delivered by CH-009 without changing either. The catalog is a new, versioned external contract; later clients must consume its published schema version rather than infer repository layout.

## Plan

Serves [P-003/M-006](../../docs/plans/P-003-rumble.md) and implements the `create-rumble-bots-repo` step in the Rumble roadmap. The requested GUI follow-up is recorded here for inclusion as a post-M-009 plan milestone when this proposal is approved and digested.
