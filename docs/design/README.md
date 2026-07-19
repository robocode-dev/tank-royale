# Design documents

Detailed design specifications that complement the decision records in [../decisions/](../decisions/README.md): ADRs stay focused on the decision, context, and rationale; the specs, flow details, diagrams, and examples that would bloat an ADR live here and are linked from the ADR's "More Information".

The `rumble/` folder is the umbrella design for **Tank Royale Rumble**, a serverless community-driven tournament system — draft, not yet built; its open questions are held as a door in the plans (see [P-003](../plans/README.md) once opened, or the rumble records below).

Capability-local design (how one capability works) belongs in that capability's `design.md` under [../capabilities/](../capabilities/README.md); records here cover cross-cutting design.

<!-- clue:index:start -->
- [ARCH-020 — Real-Time Game Loop — Design Specification](game-loop-architecture.md) · `draft`
- [ARCH-021 — WebSocket Protocol — Design Specification](websocket-protocol.md) · `draft`
- [ARCH-022 — Rumble Design: Bot Submission and Handling](rumble/bot-submission.md) · `draft`
- [ARCH-023 — Rumble Design: Client Battles and Result Upload](rumble/client-battles-and-results.md) · `draft`
- [ARCH-024 — Rumble Design: Result Aggregation and Dashboard](rumble/aggregation-and-dashboard.md) · `draft`
- [ARCH-025 — Rumble Design: User Documentation and Onboarding](rumble/user-documentation.md) · `draft`
<!-- clue:index:end -->
