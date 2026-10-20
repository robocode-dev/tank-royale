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

# Blocking deployment question

GitHub rejected the repository ruleset with `github-actions[bot]` as a bypass actor: `Actor GitHub Actions integration must be part of the ruleset source or owner organization` (HTTP 422). The current catalog publisher directly pushes generated files with `GITHUB_TOKEN`, so the required pull-request rule would stop publication. The safe options are (1) have the trusted publisher open a generated catalog PR, then let the existing moderator App validate, approve and merge that PR under the same required check, or (2) register a second, publisher-only GitHub App with a narrowly scoped bypass for generated direct pushes. Do not enable the branch rule or claim end-to-end publication until a path is chosen and tested.
