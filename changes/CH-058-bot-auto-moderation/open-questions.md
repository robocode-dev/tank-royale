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
