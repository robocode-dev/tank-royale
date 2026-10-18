---
cliewen-skill: true
version: 0.31.0
type: skill
title: clue-upgrade
name: clue-upgrade
description: Check for a newer Cliewen release and, only with explicit human authorization, carry out its coordinated repository upgrade. Use when `clue latest --quiet` prints a newer release or reports `latest` as an unknown command, or when the user asks to upgrade Cliewen.
---

<!-- Generated from Cliewen's canonical skill sources; edit those sources, not this file. -->

# clue-upgrade

Check for a newer Cliewen release and, only with explicit human authorization, carry out its coordinated repository upgrade.

## Routing

Read each reference when its condition is reached, before taking action governed by it. The references are required instructions, not optional background.

- Before writing reader-facing prose, a human report or a handoff, read [Readable references](references/readable-references.md).
- Before checking or acting on an available release, and before recommending the upgrade's route, read [Upgrade workflow](references/upgrade-workflow.md).
- When the upgrade escalates a decision of this repository's own to the tracked route, read [Change routing](references/change-scope-and-tiers.md).
- If an upgrade change begins and before branching, publishing, or handing it off, read [Review boundary](references/review-boundary.md).
- When the upgrade requires a consequential local choice, read [Decision records](references/decision-records.md).
- Before applying repository-specific upgrade or verification rules, read [Repository-local conventions](references/repository-local-conventions.md).
- When an upgrade change starts or resumes, a suggestion arrives, or a merge is reported, read [Durable work state](references/durable-work-state.md).
