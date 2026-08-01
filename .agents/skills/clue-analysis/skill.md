---
cliewen-skill: true
version: 0.10.0
---

<!-- Generated from Cliewen's canonical skill sources; edit those sources, not this file. -->

# clue-analysis

Use when a change has unclear risks or unknowns — **before** planning or implementing. Spiral's core: retire the biggest risk first.

1. Name the risk or unknown in one sentence. If you cannot, that is the first finding.
2. Establish the evidence boundary before investigating: pin source revisions when possible; record the toolchain, runtime, operating system, shell, or other conditions relevant to reproduced results; and distinguish observed facts, inferences, and unverified intent. Repository activity is evidence of activity, not maintainer intent, unless explicit evidence says otherwise. Classify every verification result as either a clean disposable environment or a prepared environment. A clean result supports onboarding reproducibility only when it has no local prerequisites; any local prerequisite, documented or not, makes the result prepared. A prepared result names its prerequisites and establishes only what that prepared environment demonstrated.
3. Before treating a statistical or percentage claim as evidence, name the versioned corpus and population, eligibility rules, exclusions and their reasons, sampling or repetition method, uncertainty, and the deterministic-versus-quality boundary. Do not turn an environment-sensitive quality claim into a deterministic acceptance criterion. When assessing adoption, name the governance or process changes it introduces; do not describe scaffolding as neutral.
4. Run a **spike**: a throwaway investigation such as a prototype, measurement, or literature scan. Spikes are disposable; their findings are not.
5. End every spike with a findings document in `/docs/analysis/` (`AN-xxx-slug.md`, frontmatter: `id`, `type: analysis`, `status`, `links`, `title`). Include what was tried, what was rejected, and why; discarded options are half of why the system looks as it does.
   - If the finding is an incident where the corpus was green but reality proved a claim wrong, add `reality: contradicted` and link every failed capability or acceptance criterion as well as the decisions, constraints, or process carriers that failed to prevent it. This records the edge from reality; it does not ingest production telemetry or open the operations loop.
6. Route any outcome that constitutes a decision under **Decision records** below. A rejected alternative that is itself a decision gets a rejected decision record, not only a paragraph in the findings.
7. Feed findings to `clue-plan` or `clue-delta`. Analysis with no consumer is doc-slop; do not write it.

## Decision records

Route every decision by reversal cost. A cheap-and-local-to-reverse decision is a dated row in `docs/decisions/log.md` (columns `Date | Decision | Why | Change/PR`); otherwise write an ADR for software or corpus architecture, or a PDR for how the project works. A decision adopting a well-established practice cites it by name and records only the local why.

Agent-authored decisions start `status: inferred` and `author: agent`. Merging makes them binding without changing that status. Only explicit human approval promotes a decision to `verified`; record every approver in `accepted-by:`, use the first approval date, and cite the venue. An explicit objection keeps the decision `inferred` and becomes an open question.

`accepted-by:` records only approval given under Cliewen's merge boundary, never acceptance a source record already carried. A record converted from a format with its own acceptance history — names, roles, dates predating the corpus — preserves that history as body prose and keeps `accepted-by: []`, the same empty list any unsigned record carries.

Every decision record is timeless: state what is decided and only the enduring context and rationale needed to understand it. Keep triggering incidents, chronology, conversations, implementation details, and review history in findings, the change workspace, the PR, and Git history.

A decision that changes a methodology contract inventories every live carrier that states the affected contract and updates that complete inventory in the same change. Live carriers include current corpus truth, canonical and generated skills, templates, public or contributor guidance, implementation explanations, CLI text, and distribution metadata. Historical analyses, completed plans, and changelog entries remain pinned history. Add focused guards for stable repaired claims, but do not present those anchors as proof that an arbitrary future carrier inventory is complete; that general obligation remains agent-enforced until a mechanism can derive it.
