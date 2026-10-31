---
type: index
title: Use cases
---

# Use cases

UC-xxx: one actor's end-to-end path across capabilities. `type: use-case`, filename `UC-<number>-<slug>.md`.

**This folder is optional and staying empty is a normal outcome.** Nothing requires a goal, a capability, or a criterion to have a use case, no report counts coverage over them, and absence is never a gap.

Write one when it changes what a reader understands: a journey crossing several capabilities, ordering or failure recovery that carries meaning, criteria that are each locally correct and still do not add up to the outcome, several actors collaborating, or brownfield behaviour no single capability explains.

Do not write one when a capability and its criteria already describe the behaviour clearly, when there is no meaningful actor interaction, when it would restate a goal, or when the subject is internal implementation behaviour that design, architecture, a constraint, or an IDR owns. A use case never restates an acceptance criterion and never becomes a design.

**Links point down.** A use case names the goal it serves and every capability it crosses; capabilities do not name their use cases, so the edge is written once. `clue context <id>` names the use cases that reach an artifact, by identity and title, without following them.

Each use case carries `## Actors`, `## Trigger`, `## Main flow`, and `## Outcome`, and adds preconditions, alternative and failure flows, and open questions when they carry meaning.

<!-- clue:index:start -->
- [UC-001 — Develop, test, submit and update a Rumble bot](UC-001-publish-a-rumble-bot.md) · `draft` — A bot author, the desktop application, GitHub, and catalog moderation.
- [UC-002 — Contribute computing time with control over bot priorities](UC-002-contribute-ranked-battles.md) · `draft` — A contributor who may own no bot, the GUI or CLI session controller, isolated battle workers, and result ingestion.
- [UC-003 — Withdraw an entry without erasing competition history](UC-003-withdraw-a-rumble-bot.md) · `draft` — The bot owner or a moderator, bot management, catalog publication, and ranking aggregation.
- [UC-004 — Repeat and reset development Rumble tests without affecting public participants](UC-004-repeat-rumble-development-tests.md) · `draft` — The maintainer or developer, local test tooling, the development catalog/result workflows, and an outside test account.
- [UC-005 — Download competitor bots to study and practise against](UC-005-download-competitor-bots.md) · `draft` — A Robocoder, the desktop application, the published bot catalog, and the bots the Robocoder already keeps.
<!-- clue:index:end -->
