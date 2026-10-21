---
id: OQ-008
type: questions
status: active
links: [CH-058]
title: Bot moderation decisions
---

# Resolved decisions

- A runnable bot completes one full round against a reference bot, not merely a source-start smoke check.
- Every clean bot-only submission, including a first bot, is eligible for automatic merge. Runtime failure requests changes; uncertainty and non-bot changes go to Flemming with the reason.
- A required moderation check prevents a failing or uncertain submission from being merged by ordinary repository permissions. GitHub Actions starts triage for every pull request, including first-time contributors.

# Resolved deployment question

GitHub rejected `github-actions[bot]` as a ruleset bypass actor, so the active repository ruleset grants pull-request-only bypass to Flemming and grants no bypass to either App. The organization forbids `GITHUB_TOKEN` from creating PRs, so the organization-owned Publisher App opens generated catalog PRs with Contents read and Pull requests write; the Moderator App verifies, approves and merges them after testing exact catalog content. Test bot [PR #22](https://github.com/robocode-dev/rumble-bots/pull/22) and catalog [PR #28](https://github.com/robocode-dev/rumble-bots/pull/28) completed the live path successfully. Ruleset `Rumble main moderation guard` (ID `25580362`) is active on `main`, requires the `bot-moderation` status from Moderator App ID `5402066`, requires a pull request and one approval, and enforces code-owner approval on owned paths; GitHub's effective rules endpoint confirmed it applies. See the [ruleset](https://github.com/robocode-dev/rumble-bots/rules/25580362), [catalog publication run](https://github.com/robocode-dev/rumble-bots/actions/runs/39659028827) and [catalog verification run](https://github.com/robocode-dev/rumble-bots/actions/runs/39659079038).
