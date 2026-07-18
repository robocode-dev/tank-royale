---
version: 0.3.0
---

# clue-analysis

Use when a change has unclear risks or unknowns — **before** planning or implementing. Spiral's core: retire the biggest risk first.

1. Name the risk or unknown in one sentence. If you cannot, that is the first finding.
2. Run a **spike**: a throwaway investigation (prototype, measurement, literature scan). Spikes are disposable; their findings are not.
3. **Every spike ends in a findings document** in `/docs/analysis/` (`AN-xxx-slug.md`, frontmatter: `id`, `type: analysis`, `status`, `links`, `title`). Include what was tried, what was rejected, and why — discarded options are half of "why does the system look like this."
4. Rejected alternatives that constitute a decision get a rejected decision record (ADR if architectural, PDR if project/process) in `/docs/decisions/`, not just a paragraph in the findings.
5. Findings feed the plan (`clue-plan`) or the change (`clue-delta`). Analysis with no consumer is doc-slop; do not write it.
