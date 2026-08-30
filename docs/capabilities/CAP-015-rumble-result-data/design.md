---
id: CAP-015-design
type: design
status: draft
links: [CAP-015, ARCH-024, P-003]
title: Design notes for CAP-015 (rumble-result-data)
provenance: inferred
reversal-cost: low
---

# CAP-015 design

The external `robocode-dev/rumble-data` repository owns result storage and projections, while this corpus records the durable capability and contracts. Its ingestion, scoring, retention, moderation, dashboard, and operational rules are defined by [ARCH-024](../../design/rumble/aggregation-and-dashboard.md).

## Input and immutable-fact contracts

`engine.json` pins a positive `behaviorVersion`, Tank Royale release and image references, and ranked settings for `1v1`, `twinduel`, and `melee`. `catalog.json` is the synchronized published bot catalog; only its active name-and-version entries can occur in ranked results. A contributor is admitted by a reviewed `clients/<forge-account>.json` registration that declares stable client IDs.

For every result, validation resolves each reported entry against the catalog before accepting or aggregating it. `1v1` and `melee` require the engine pin's exact number of distinct active individual entries, identified by an empty `teamMembers` list. `twinduel` requires exactly two distinct active team entries; each must have exactly two active individual catalog members, the teams' expanded members must equal the pinned participant count, and their member-identity sets must be disjoint. A team is ineligible for an individual game type, and an individual is ineligible for TwinDuel. This catalog lookup binds eligibility and membership without changing the result-envelope wire shape.

An issue-ops submission is one fenced JSON batch envelope with `schemaVersion`, `clientId`, `clientVersion`, and one to sixty result records. Each record includes a UUID `battleId`, completion time, matching nested client identity, behavior version, game type, pinned battle dimensions, and the complete Battle Runner participant result model. The validator checks the contract independently for every record, then normalizes valid records with the submitting account and payload hash. A content-addressed JSON file under `results/raw/<year>/<month>/` is the authoritative fact; issue bodies are transport, never state. The workflow publishes successful per-result receipts only after accepted facts reach the canonical repository, and an identical retry of an already retained result receives the same successful outcome.

## Projection and moderation contracts

`scripts/aggregate.py` is a pure function of raw facts and rollups, `catalog.json`, `engine.json`, registrations, `bans.json`, and `exclusions.json`. It emits one versioned leaderboard, pairing-statistics file, matches-needed file, and bot detail shard per game type, plus contributor totals. The projection identifier hashes its relevant inputs. Rankings use only the current `behaviorVersion` epoch and active bot versions.

CI serializes ingestion and is the sole writer of facts and projections on `main`. Moderators never rewrite a fact: an exclusion, ban, disqualification, or removed registration only affects the next recomputation. Monthly rollups replace raw files older than three full months on the archive branch after an equivalent projection is verified, retaining the event-sourcing guarantee.

## External evidence

The external `robocode-dev/rumble-data` repository holds the implementation and focused suite for RDA-001 through RDA-004. [rumble-data#4](https://github.com/robocode-dev/rumble-data/pull/4) added every-ranked-type matchmaking advice and catalog-synchronization coverage and was accepted at merge commit `18e916e4a381c8108568d7ca77e3e14d88dd4583`; M-007 is complete. [rumble-data#7](https://github.com/robocode-dev/rumble-data/pull/7) at `83e71d4` adds RDA-005 positive and negative validation and advice-generation evidence, including TwinDuel member disjointness while preserving repeated member slots within a team. Run `python -m unittest discover -s tests -v` in that repository to reproduce the evidence. During P-001/M-002, the external-evidence reconciliation set must include RDA-001 through RDA-005 before this criteria artifact can become active without changing the criterion meanings.
