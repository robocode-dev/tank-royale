## Decision records

Only a future-shaping choice earns a decision record. Route it by subject to exactly one type: an ADR for software or corpus architecture, a PDR for how the project or its methodology works, or an IDR for implementation. Reversal cost does not route a record; routine facts, chronology, and implementation history are not decisions. A decision adopting a well-established practice cites it by name and records only the local why.

A rejected future-shaping choice gets a rejected decision record rather than only a paragraph in findings, routed by the same subject test. A rejected option that does not constrain future work stays a paragraph.

Agent-authored decisions start `status: inferred` and `author: agent`. Merging makes them binding without changing that status. Only explicit human approval promotes a decision to `verified`; record every approver in `accepted-by:`, use the first approval date, and cite the venue. An explicit objection keeps the decision `inferred` and becomes an open question.

`accepted-by:` records only approval given under Cliewen's merge boundary, never acceptance a source record already carried. A record converted from a format with its own acceptance history — names, roles, dates predating the corpus — preserves that history as body prose and keeps `accepted-by: []`, the same empty list any unsigned record carries.

Every new or modified decision record is timeless and compact: keep enduring context and the decision; add considered alternatives only when they materially explain the choice, and consequences only when they help a future reader act on it. Keep triggering incidents, chronology, conversations, carrier inventories, implementation walkthroughs, and review history in findings, the change workspace, the PR, and Git history.

An ADR or IDR that changes system structure or cross-cutting design links in its body to the affected `docs/architecture/README.md` or `docs/design/README.md`; the decision states why the choice constrains future work and does not repeat the overview. A PDR links there only when it governs the documentation methodology.

A decision that changes a methodology contract inventories every live carrier that states the affected contract and updates that complete inventory in the same change. Live carriers include current corpus truth, canonical and generated skills, templates, public or contributor guidance, implementation explanations, CLI text, and distribution metadata. Historical analyses, completed plans, and changelog entries remain pinned history. Add focused guards for stable repaired claims, but do not present those anchors as proof that an arbitrary future carrier inventory is complete; that general obligation remains agent-enforced until a mechanism can derive it.
