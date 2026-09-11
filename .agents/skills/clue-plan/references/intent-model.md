## Intent model

A corpus carries two threads, and they meet only at the goal.

The **intent thread** says what the product means: `VIS-001` (the vision, at `docs/vision.md`) → goal → optional use case → capability → acceptance criterion → evidence. The **delivery thread** says how that meaning gets built: goal → plan → milestone → change → accepted merge commit. Plan, milestone, and change never join the semantic hierarchy, which is what keeps a change from editing the vision merely because it edited code.

Each artifact answers one question, and none of them answers another's. A **goal** says who wants something and why. A **use case** says what one actor does, end to end, across capabilities. A **capability** says what the system can do. An **acceptance criterion** says what would prove it. A vision that lists goals, a use case that restates criteria, or a capability that argues for its own existence has taken over a neighbour's job — link instead of repeating.

**Both intent artifacts are optional to have.** `clue validate` never requires a vision or a use case, never reports one missing as an issue, and computes no coverage figure over either; absence is not a gap. Run `clue validate --intent` to see what a corpus states.

**Links point down.** A use case names the goal it serves and every capability it crosses; a goal names the vision. A capability never names a use case back, so no edge is written twice and none can drift. Reading it the other way is `clue context <id>`, which names the use cases reaching an artifact by identity and title without following them.

### The vision

One per corpus, roughly one screen, at `docs/vision.md`. It answers what the product or system is, whom it serves, what problem it addresses and what value it intends to create, what is in scope and what is deliberately out, what principles constrain its direction, how success would be recognized, and which assumptions are still uncertain. It is not a roadmap, a backlog, an architecture document, a requirements list, or a marketing statement, and it needs no business case.

Edit it only when the direction itself changes. Restating a new feature there is how a vision becomes a changelog and stops being read.

### When a use case is worth creating

Write one when it changes what a reader understands:

- an actor's journey crosses several capabilities;
- ordering, interaction, alternatives, or failure recovery carries meaning;
- the individual criteria are each locally correct and still do not explain the outcome;
- several actors collaborate through the system;
- a brownfield system holds important behaviour no single capability explains.

Do not write one when a capability and its criteria already describe the behaviour clearly, when there is no meaningful actor interaction, when it would restate an existing goal or capability, or when the subject is internal implementation behaviour that design, architecture, a constraint, or an IDR owns. Never create one for trivial create-read-update-delete behaviour to satisfy a process, and never let one grow into a design or prescribe interface details that are not themselves accepted intent.

Recommend for or against one, say why, and let the human decide. A use case is created because it earns its place, never because a capability lacks one.

### Its shape

`docs/use-cases/UC-<number>-<slug>.md`, `type: use-case`, links naming the goal and the capabilities it crosses, and four sections: `## Actors`, `## Trigger`, `## Main flow`, `## Outcome`. Add preconditions, alternative and failure flows, and open questions when they carry meaning; leave them out when they do not.

### Marking what is not yet confirmed

Agent-drafted intent is `status: draft` with `provenance: inferred` and `reversal-cost: low|high`; low explicitly permits deferral, while high can block an active capability that directly depends on the artifact. Once a human verifies the meaning, remove `reversal-cost`; it is no longer used. Assumptions and open questions stay visible in the artifact. Promotion is a human act. Never present an inference as a fact, and never resolve a contradiction between sources by picking the convenient reading — record it and ask.

**A full change's acceptance brief states the vision it proceeds under**, or states that the repository has none and that the change proceeds without one. When unresolved meaning would materially change what the system is for, stop and ask rather than deciding it inside an implementation.
