---
cliewen-skill: true
version: 0.16.0
---

<!-- Generated from Cliewen's canonical skill sources; edit those sources, not this file. -->

# clue-extract

Transform one brownfield specification corpus into Cliewen through a report-only rehearsal and a human-authorized mutation.

## Routing

Read each reference when its condition is reached, before taking action governed by it. The references are required instructions, not optional background.

- Before branching, publishing, updating a hosted PR, or handing work to a human, read [Review boundary](references/review-boundary.md).
- Before beginning an extraction, read [Boundaries](references/boundaries.md).
- After proposal and before changing the target corpus, tests, routing, or hosted state, read [Rehearsal before mutation](references/rehearsal-before-mutation.md).
- After the human authorizes mutation and while constructing the target corpus, read [Target contract](references/target-contract.md).
- When the source uses a supported format or needs a new mapping, read [Source mappings](references/source-mappings.md).
- When extraction classifies or records a consequential choice, read [Decision records](references/decision-records.md).
- Before reconciling source instructions with repository-specific rules, read [Repository-local conventions](references/repository-local-conventions.md).
- When extraction work starts or resumes, a suggestion arrives, or a merge is reported, read [Durable work state](references/durable-work-state.md).
