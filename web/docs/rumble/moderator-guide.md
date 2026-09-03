# Moderator quickstart

[Rumble](index.md) moderation happens entirely through pull-request review and a few reviewable JSON files — there is no separate moderation tool. This page covers the moderator role as it exists today; each repository's own `GOVERNANCE.md` is the authoritative reference.

## What you review

- **Bot submissions** in [`rumble-bots`](https://github.com/robocode-dev/rumble-bots) — validation CI must be green before you look; review focuses on structure, the declared SPDX license, and anything the forbidden-API scan flagged (a flag is advisory, not an auto-reject — false positives happen). First-time authors get a stricter look; established authors with clean history may qualify for auto-merge on version bumps of their own bots.
- **Client registrations** and other ordinary code or policy changes in [`rumble-data`](https://github.com/robocode-dev/rumble-data) — one moderator approval merges a `clients/<account>.json` addition or any other reviewable change; CI is the only writer of accepted results and generated projections on `main`.

Full submission and review rules: [`rumble-bots`'s `CONTRIBUTING.md`](https://github.com/robocode-dev/rumble-bots/blob/main/CONTRIBUTING.md) and [`GOVERNANCE.md`](https://github.com/robocode-dev/rumble-bots/blob/main/GOVERNANCE.md).

## Handling problems

Everything below is a pull request against the relevant repository, explained in that PR's description — there's no hidden admin surface.

- **Dispute a result** — add its `battleId` to `rumble-data`'s `exclusions.json`. Aggregation omits it from the leaderboard while keeping the fact itself, so the audit trail never disappears.
- **Ban an account** — add it to `rumble-data`'s `bans.json` (blocks future submissions and quarantines past ones on the next aggregation) and, if the account owns bots, flip them to `disqualified` via `rumble-bots`'s governance process. Bans can be temporary or permanent; nothing is deleted, so lifting a ban restores everything on the next recompute.
- **Confusable or suspicious bot names** — the validator flags near-duplicate skeletons (leetspeak and Unicode-confusable folding) automatically; use judgment on whether it's imitation or coincidence.

## Operations

`rumble-data`'s ingestion workflow is triggered by labelled issues and a schedule; if scheduled workflows go dormant after inactivity, re-enable them (an incoming labelled issue also wakes the system). Monthly, the first drain compacts facts older than three months into rollups and archives the individual records. Quarterly, moderators run the fork drill: fork the repository, enable its workflows and Pages, run aggregation locally, and record the result — this is what keeps the system's bus-factor promise honest. Details: [`rumble-data`'s `GOVERNANCE.md`](https://github.com/robocode-dev/rumble-data/blob/main/GOVERNANCE.md).

There is not yet a standalone moderator handbook beyond each repository's `GOVERNANCE.md` — if you're stepping into this role, those two files plus this page are the complete current reference.
