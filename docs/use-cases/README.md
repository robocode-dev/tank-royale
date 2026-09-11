# Use cases

UC-xxx: one actor's end-to-end path across capabilities. `type: use-case`, filename `UC-<number>-<slug>.md`.

**This folder is optional and staying empty is a normal outcome.** Nothing requires a goal, a capability, or a criterion to have a use case, no report counts coverage over them, and absence is never a gap.

Write one when it changes what a reader understands: a journey crossing several capabilities, ordering or failure recovery that carries meaning, criteria that are each locally correct and still do not add up to the outcome, several actors collaborating, or brownfield behaviour no single capability explains.

Do not write one when a capability and its criteria already describe the behaviour clearly, when there is no meaningful actor interaction, when it would restate a goal, or when the subject is internal implementation behaviour that design, architecture, a constraint, or an IDR owns. A use case never restates an acceptance criterion and never becomes a design.

**Links point down.** A use case names the goal it serves and every capability it crosses; capabilities do not name their use cases, so the edge is written once. `clue context <id>` names the use cases that reach an artifact, by identity and title, without following them.

Each use case carries `## Actors`, `## Trigger`, `## Main flow`, and `## Outcome`, and adds preconditions, alternative and failure flows, and open questions when they carry meaning.

<!-- clue:index:start -->
<!-- clue:index:end -->
