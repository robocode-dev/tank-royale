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

GitHub rejected the repository ruleset with `github-actions[bot]` as a bypass actor: `Actor GitHub Actions integration must be part of the ruleset source or owner organization` (HTTP 422). The selected path is for the trusted publisher to open a generated catalog PR, then let the existing moderator App validate, approve and merge that PR under the same required check. A separate publisher App and direct-push bypass are unnecessary. GitHub requires the repository's **Allow GitHub Actions to create and approve pull requests** setting for `GITHUB_TOKEN` to open the PR; this setting is not yet enabled. Do not enable the branch rule or claim end-to-end publication until this path passes a live test.
