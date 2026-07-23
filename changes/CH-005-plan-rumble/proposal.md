---
id: CH-005
type: change
status: proposed
links: [G-001]
title: Create plan P-003 — Tank Royale Rumble
---

# CH-005 — Create plan P-003 — Tank Royale Rumble

## What

Convert the draft Rumble design (`docs/design/rumble/`, ARCH umbrella plus four sub-documents) into an active plan `docs/plans/P-003-rumble.md` with one milestone per roadmap change, exactly as the umbrella document's "Change Proposal Roadmap" orders them:

1. Prepare Tank Royale for the rumble (`behaviorVersion`, SPDX license field in the booter bot config, Battle Runner result support, rumble game presets, deterministic replay-regression hook)
2. Create the `rumble-bots` repository
3. Create the `rumble-data` repository
4. Create the rumble client
5. Publish the rumble user documentation

This change mutates the plan layer only: it adds P-003 and regenerates the plans index. It implements nothing from the roadmap; each roadmap item remains its own future CH proposal that stops for maintainer approval before implementation, as the design mandates.

## Why

The umbrella design states its own next step: "convert this design into an active plan (P-xxx) with ordered changes (CH-xxx) before implementation starts." Every future rumble change proposal needs a plan item to name; without P-003 those proposals would each have to declare themselves plan-less, which misstates reality. Serves G-001: the rumble is the competition half of "learning and competition" — a continuously running, community-driven league in the RoboRumble/LiteRumble tradition.

## Scope

- Add `docs/plans/P-003-rumble.md` (milestones M-005 through M-009, all `todo`)
- Regenerate the `clue:index` block in `docs/plans/README.md` (also correcting P-002's stale `active` entry to `completed`, pure bookkeeping)
- No product code, no capability, no criteria, no design-document changes
