---
id: CH-011
type: change
status: open
links: [P-003, M-007]
title: Create Rumble Data Repository
---

# CH-011 — Create Rumble Data Repository

## What

Create the community-owned `rumble-data` repository as Tank Royale Rumble's machine-written, auditable result ledger and static ranking service. The repository will provide the registered-client result inbox, portable Python validation and aggregation scripts, append-only raw facts, derived leaderboard and matchmaking projections, operational governance, a compact static dashboard, and thin GitHub Actions workflows that drain submissions and publish GitHub Pages.

The repository will preserve the Rumble's single-writer and event-sourcing invariants: clients submit batch envelopes through forge issues, while serialized CI is the only process that writes raw facts or projections. All displayed rankings and matchmaking advice will be reproducible from repository facts, the bot catalog, engine pin, and durable exclusion records.

## Why

Ranked battles need a trustworthy result destination before the Rumble client can contribute meaningful results. Separating the high-volume machine data from the review-gated bot catalog keeps both repositories forkable and reviewable while retaining zero-cost infrastructure, no persistent secrets, and an independently reproducible leaderboard.

## Scope

- Create `robocode-dev/rumble-data` under the community organization with fork-and-PR repository settings and GitHub Pages publishing.
- Define the repository contracts for engine pins, registered clients, submitted result batches, immutable raw facts, quarantine records, and generated leaderboard and matchmaking projections.
- Implement locally runnable Python validation and pure aggregation, including focused positive and negative evidence for valid results, rejection paths, reproducible projections, and matchmaking priority.
- Add serialized issue-ops ingestion and projection publishing workflows that use only the built-in CI token; CI alone writes accepted facts and projections.
- Add a small static dashboard driven by generated JSON, plus contributor, moderator, governance, onboarding, retention, compaction, ban, and fork-drill guidance.
- Define capability ownership, acceptance criteria, and proportionate durable decisions in this repository's corpus before implementing the external repository.
- Record M-007 evidence in P-003 after the external repository scaffold and its focused checks are complete.

## Non-goals

- Building the Rumble client or giving clients Git-history write access.
- Changing Tank Royale's engine, `behaviorVersion` model, Battle Runner, bot catalog, or Rumble game presets.
- Storing replays centrally, operating a live backend, or introducing paid infrastructure or external secrets.
- Adding GUI `TwinDuel` selection, user-facing Rumble guides in this repository, or changes to `rumble-bots` beyond consuming its published catalog contract.

## Compatibility

The repository consumes the generated `rumble-bots` catalog and the Tank Royale `behaviorVersion` contract delivered by CH-009. Its result and projection schemas are new, versioned external contracts; the later Rumble client must consume them by schema version rather than infer repository layout or rely on mutable issue bodies.

## Plan

Serves [P-003/M-007](../../docs/plans/P-003-rumble.md) and implements the `create-rumble-data-repo` step in the Rumble roadmap.
