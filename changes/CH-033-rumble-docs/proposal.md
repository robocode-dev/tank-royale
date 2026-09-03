---
id: CH-033
type: change
status: open
links: [P-003, M-009, CAP-014, CAP-015, CAP-016]
title: Publish Rumble user documentation
---

# CH-033 — Publish Rumble user documentation

## What

Publish one quickstart per Rumble audience — bot author, battle contributor, moderator — under `web/docs/rumble/`, wired into the Tank Royale docs site navigation.

## Why

P-003/M-009 is the last open milestone in the Rumble plan. M-005 through M-008 and M-010 are all done, and PDR-005 deliberately sequenced M-009 after M-010 (GUI TwinDuel), which has since shipped in 1.1.0. The three Rumble repositories (`rumble-bots`, `rumble-data`, `rumble-client`) and the GUI are functioning, but nothing tells a newcomer how to participate.

## Route

Recommended route: full. Publishing these guides fulfills a plan promise (M-009 moves from `todo` to `done`), which is an accepted-contract change under the routing rule even though no acceptance criterion or capability changes. Discovery would change the route only if the guides turn out to require no plan bookkeeping change at all, which is not the case here.

## Plan

Serves [P-003/M-009](../../docs/plans/P-003-rumble.md). No CAP-014/015/016 acceptance criterion changes; the guides describe existing, already-accepted behavior.

## Scope

- `web/docs/rumble/bot-author-guide.md` — quickstart from template to a merged, ranked-eligible bot in `rumble-bots`: official Bot API requirement, SPDX license field, local `validate_bot.py` run, PR flow, ownership/versioning basics, slots.
- `web/docs/rumble/client-guide.md` — quickstart for running the Rumble client: one-time client registration PR in `rumble-data`, build/Docker-dev-image instructions reflecting the client's actual current state (no published release or production image yet), configuration, practice vs. ranked mode, submitting results.
- `web/docs/rumble/moderator-guide.md` — quickstart for the moderator role as it exists today: reviewing bot and client-registration PRs, using `bans.json`/`exclusions.json`/`disqualifiedBots`, and links to each repository's own `GOVERNANCE.md` as the authoritative operations reference (no separate `rumble-data/docs/moderator-handbook.md` exists yet, so this guide does not claim one does).
- `web/docs/rumble/index.md` — short landing page linking the three guides and the live dashboard, so the docs site has one entry point.
- Wire the four pages into `web/docs/.vitepress/config.mts` (nav + sidebar section).
- Update `docs/plans/P-003-rumble.md` to mark M-009 done, with evidence, in the digest.

## Non-goals

- A full `rumble-data/docs/moderator-handbook.md`, a separate `onboarding.md`, or a `faq.md` — the design doc's fuller document set (`docs/design/rumble/user-documentation.md`) remains the aspirational target; this change ships only the plan's literal M-009 commitment (one quickstart per audience).
- Any change to `rumble-bots`, `rumble-data`, or `rumble-client` repository content.
- Claiming a published client container, native release, or production Docker image exists — the client guide describes the real current build/run path.
- New or changed acceptance criteria, capabilities, or ADRs.
