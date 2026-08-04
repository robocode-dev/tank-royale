---
id: CH-013-open-questions
type: change
status: open
links: [CH-013, C-002]
title: Open questions for CH-013
---

# CH-013 — Open Questions

## Q1 — Resolved: How should the minor-release documentation step cross the review boundary?

The repository's `/release` skill cannot currently execute the 1.1.0 release as written. Its preflight reads a `version=` property from `gradle.properties`, although `VERSION` is the authoritative source and `gradle.properties` contains no version. More importantly, phase 5 tells the agent to generate documentation, commit it on `main`, and push directly to `main`, while C-002 and AGENTS.md prohibit every direct commit and push to `main`.

The maintainer chose on 2026-08-04 to generate and review the 1.1.0 documentation in CH-013, then repair `/release` to read `VERSION` and require a clean documentation tree on `main`. C-002 retains its no-direct-push rule.
