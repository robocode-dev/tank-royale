# Constraints

C-xxx: rules that bind every change — laws, licenses, non-negotiable product rules, and the **convention register**: every repo rule that would otherwise live only in prose.

External constraints come from outside the project: you do not decide them, you comply with them (the decision *how* to comply is an ADR or PDR in `decisions/`). The register holds the rest: a convention that binds every change but would otherwise live only in prose registers here as a constraint artifact, so the rules have an inventory a validator can count instead of prose that drifts silently. Rules the versioned skills carry need no registration.

Each constraint names its `source` and an `enforcement` class: `machine` (a lint or CI check holds it), `agent` (an agent must hold it until a machine check exists — each such constraint states its promotion trigger), or `human` (only review can hold it). Every change is assessed against the active constraints before its PR.

<!-- clue:index:start -->
- [C-001 — Markdown prose is never hard-wrapped](C-001-no-hard-wrapped-markdown.md) · `agent`
- [C-002 — Every mutation of main goes through a branch and a human-merged PR](C-002-review-boundary.md) · `human`
- [C-003 — Bot APIs are semantically identical across platforms; Java is the reference](C-003-cross-platform-bot-api-parity.md) · `machine`
- [C-004 — Game physics and bot backward compatibility are stable](C-004-stable-physics-and-backward-compatibility.md) · `human`
<!-- clue:index:end -->
