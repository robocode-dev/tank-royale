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

GitHub rejected the repository ruleset with `github-actions[bot]` as a bypass actor: `Actor GitHub Actions integration must be part of the ruleset source or owner organization` (HTTP 422). The selected path is a generated catalog PR validated and merged by the existing moderator App under the same required check. The organization forbids `GITHUB_TOKEN` from creating PRs (HTTP 409 when attempting to enable that repository setting), so a second organization-owned App with only Contents read and Pull requests write must open the catalog PR. The moderator App pushes the generated branch; neither App bypasses direct-push protection on `main`. Register and install the Publisher App, then test the full path before enabling the branch rule.
