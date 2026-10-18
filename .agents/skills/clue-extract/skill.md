---
cliewen-skill: true
version: 0.31.0
type: skill
title: clue-extract
name: clue-extract
description: Transform one brownfield specification corpus into Cliewen through a report-only rehearsal and a human-authorized mutation. Use when adopting Cliewen in a repository that already holds specifications, decision records, or tagged tests.
---

<!-- Generated from Cliewen's canonical skill sources; edit those sources, not this file. -->

# clue-extract

Transform one brownfield specification corpus into Cliewen through a report-only rehearsal and a human-authorized mutation.

## Routing

Read each reference when its condition is reached, before taking action governed by it. The references are required instructions, not optional background.

- Before writing reader-facing prose, a human report or a handoff, read [Readable references](references/readable-references.md).
- Before writing, migrating, exporting or verifying executable acceptance evidence, read [Evidence workflow](references/evidence-workflow.md).
- Before branching, publishing, updating a hosted PR, or handing work to a human, read [Review boundary](references/review-boundary.md).
- Before beginning an extraction, read [Boundaries](references/boundaries.md).
- After proposal and before changing the target corpus, tests, routing, or hosted state, read [Rehearsal before mutation](references/rehearsal-before-mutation.md).
- After the human authorizes mutation and while constructing the target corpus, read [Target contract](references/target-contract.md).
- Before proposing a vision or any use case for the target corpus, read [Intent model](references/intent-model.md).
- When the source repository states no usable vision, read [Intent discovery](references/intent-discovery.md).
- When the source uses a supported format or needs a new mapping, read [Source mappings](references/source-mappings.md).
- When extraction classifies or records a consequential choice, read [Decision records](references/decision-records.md).
- Before reconciling source instructions with repository-specific rules, read [Repository-local conventions](references/repository-local-conventions.md).
- When extraction work starts or resumes, a suggestion arrives, or a merge is reported, read [Durable work state](references/durable-work-state.md).
