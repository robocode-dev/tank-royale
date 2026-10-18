---
cliewen-skill: true
version: 0.31.0
type: skill
title: clue-verify
name: clue-verify
description: Verify a chosen tracked Cliewen change and run its bounded adversarial review before handing an exact candidate to the human for acceptance. Use before a tracked change's PR readiness or local acceptance handoff.
---

<!-- Generated from Cliewen's canonical skill sources; edit those sources, not this file. -->

# clue-verify

Verify a chosen tracked Cliewen change and run its bounded adversarial review before handing an exact candidate to the human for acceptance.

## Routing

Read each reference when its condition is reached, before taking action governed by it. The references are required instructions, not optional background.

- Before writing reader-facing prose, a human report or a handoff, read [Readable references](references/readable-references.md).
- Before writing, migrating, exporting or verifying executable acceptance evidence, read [Evidence workflow](references/evidence-workflow.md).
- Before confirming that tracked-route verification applies, read [Change routing](references/change-scope-and-tiers.md).
- Before inspecting or updating hosted pull-request state and before the readiness handoff, read [Review boundary](references/review-boundary.md).
- Before running readiness verification, read [Verification checklist](references/verification-checklist.md).
- After the complete candidate is committed and its applicable local checks pass, read [Agentic review loop](references/agentic-review-loop.md).
- When verification encounters or evaluates a consequential choice, read [Decision records](references/decision-records.md).
- Before selecting and running repository-specific checks, read [Repository-local conventions](references/repository-local-conventions.md).
- When review work starts or resumes, a suggestion arrives, or a merge is reported, read [Durable work state](references/durable-work-state.md).
