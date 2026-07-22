---
cliewen-skill: true
version: 0.5.1
---

<!-- Generated from Cliewen's canonical skill sources; edit those sources, not this file. -->

# clue-analysis

Use when a change has unclear risks or unknowns — **before** planning or implementing. Spiral's core: retire the biggest risk first.

1. Name the risk or unknown in one sentence. If you cannot, that is the first finding.
2. Establish the evidence boundary before investigating: pin source revisions when possible; record the toolchain, runtime, operating system, shell, or other conditions relevant to reproduced results; and distinguish observed facts, inferences, and unverified intent. Repository activity is evidence of activity, not maintainer intent, unless explicit evidence says otherwise.
3. Run a **spike**: a throwaway investigation such as a prototype, measurement, or literature scan. Spikes are disposable; their findings are not.
4. End every spike with a findings document in `/docs/analysis/` (`AN-xxx-slug.md`, frontmatter: `id`, `type: analysis`, `status`, `links`, `title`). Include what was tried, what was rejected, and why; discarded options are half of why the system looks as it does.
5. Route any outcome that constitutes a decision under **Decision records** below. A rejected alternative that is itself a decision gets a rejected decision record, not only a paragraph in the findings.
6. Feed findings to `clue-plan` or `clue-delta`. Analysis with no consumer is doc-slop; do not write it.

## Decision records

Route every decision by reversal cost. A cheap-and-local-to-reverse decision is a dated row in `docs/decisions/log.md` (columns `Date | Decision | Why | Change/PR`); otherwise write an ADR for software or corpus architecture, or a PDR for how the project works. A decision adopting a well-established practice cites it by name and records only the local why.

Agent-authored decisions start `status: inferred` and `author: agent`. Merging makes them binding without changing that status. Only explicit human approval promotes a decision to `verified`; record every approver in `accepted-by:`, use the first approval date, and cite the venue. An explicit objection keeps the decision `inferred` and becomes an open question.

Every decision record is timeless: state what is decided and only the enduring context and rationale needed to understand it. Keep triggering incidents, chronology, conversations, implementation details, and review history in findings, the change workspace, the PR, and Git history.
