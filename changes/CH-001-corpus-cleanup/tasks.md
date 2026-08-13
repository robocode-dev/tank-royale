---
id: CH-001-tasks
type: tasks
status: open
links: [CH-001]
title: Tasks for CH-001
---

# Tasks

- [x] Record the authorized dependency on the unmerged 0.16.0 upgrade and keep both changes visible at the merge boundary.
- [x] Reflow all existing hard-wrapped Markdown paragraphs and list items without changing prose meaning (C-001). The mechanical batch reduced the validator result from 537 to 303 findings.
- [x] Resolve decision authorship metadata with provenance or human confirmation (C-009). Existing `accepted-by` records and repository history identify Flemming N. Larsen for all 44 legacy decisions.
- [x] Resolve capability goal metadata with corpus links or human confirmation (C-009). Existing capability links identify G-001 for all 17 capabilities.
- [x] Resolve stale constraint sources and residual declarations (C-002, C-003, C-004). Sources now point to surviving goal and decision artifacts, and human-enforced constraints declare their residuals.
- [x] Re-run `clue validate` and confirm no unexplained findings remain. Validation reports 134 valid artifacts; its remaining inferred-decision, agent-enforced-constraint, and index-quality counts are informational notices rather than issues.
