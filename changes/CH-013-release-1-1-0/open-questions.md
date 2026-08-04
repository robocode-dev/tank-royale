---
id: CH-013-open-questions
type: change
status: open
links: [CH-013, C-002]
title: Open questions for CH-013
---

# CH-013 — Open Questions

## Q1 — How should the minor-release documentation step cross the review boundary?

The repository's `/release` skill cannot currently execute the 1.1.0 release as written. Its preflight reads a `version=` property from `gradle.properties`, although `VERSION` is the authoritative source and `gradle.properties` contains no version. More importantly, phase 5 tells the agent to generate documentation, commit it on `main`, and push directly to `main`, while C-002 and AGENTS.md prohibit every direct commit and push to `main`.

Should CH-013 generate and review the 1.1.0 documentation before merge, then repair `/release` to read `VERSION` and require a clean documentation tree on `main`; or should C-002 gain an explicit, human-approved release-documentation exception and every methodology carrier be updated accordingly?
