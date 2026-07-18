# Architecture

ARCH-xxx: how the system is structured — C4 views (What), domain models and flows (How), with the decision records in [decisions/](../decisions/README.md) carrying the Why.

Layout: [c4-views/](c4-views/README.md) holds the hierarchical diagrams (system context → containers → components) with Structurizr DSL sources and generated SVGs; [models/message-schema/](models/message-schema/README.md) documents the WebSocket message contracts (source of truth: `/schema/schemas/`); [models/flows/](models/flows/README.md) documents the processes (battle lifecycle, bot connection, turn execution, event handling); [report/](report/README.md) holds architectural health reports produced by the audit skill.

New readers: start with the [System Context](c4-views/system-context.md), then the [Container view](c4-views/container.md), then the flow or schema closest to what you are changing. When structure, protocol, or processes change, update the matching record in the same change that alters the code.

All records below were authored pre-Cliewen and absorbed at CH-001 (`status: draft`, `provenance: inferred`); they are promoted to `verified` as they are checked against the code.

<!-- clue:index:start -->
- [ARCH-001 — System Context Diagram](c4-views/system-context.md) · `draft`
- [ARCH-002 — Container View](c4-views/container.md) · `draft`
- [ARCH-003 — Server Components View](c4-views/server-components.md) · `draft`
- [ARCH-004 — Booter Components View](c4-views/booter-components.md) · `draft`
- [ARCH-005 — Bot API Components View](c4-views/bot-api-components.md) · `draft`
- [ARCH-006 — GUI Components View](c4-views/gui-components.md) · `draft`
- [ARCH-007 — Recorder Components View](c4-views/recorder-components.md) · `draft`
- [ARCH-008 — Runner Components View](c4-views/runner-components.md) · `draft`
- [ARCH-009 — Battle Lifecycle Flow](models/flows/battle-lifecycle.md) · `draft`
- [ARCH-010 — Bot Connection Flow](models/flows/bot-connection.md) · `draft`
- [ARCH-011 — Turn Execution Flow](models/flows/turn-execution.md) · `draft`
- [ARCH-012 — Event Handling Flow](models/flows/event-handling.md) · `draft`
- [ARCH-013 — Handshake Messages](models/message-schema/handshakes.md) · `draft`
- [ARCH-014 — Command Messages](models/message-schema/commands.md) · `draft`
- [ARCH-015 — Event Messages](models/message-schema/events.md) · `draft`
- [ARCH-016 — Intent Messages](models/message-schema/intents.md) · `draft`
- [ARCH-017 — State Objects (DTOs)](models/message-schema/state.md) · `draft`
- [ARCH-018 — Architectural Health Report](report/architectural-health-report.md) · `draft`
- [ARCH-019 — Architectural Health Report Template](report/architectural-health-report-template.md) · `draft`
<!-- clue:index:end -->
