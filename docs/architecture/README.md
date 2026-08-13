# Architecture

ARCH-xxx: how the system is structured — C4 views (What), domain models and flows (How), with the decision records in [decisions/](../decisions/README.md) carrying the Why.

Layout: [c4-views/](c4-views/README.md) holds the hierarchical diagrams (system context → containers → components) with Structurizr DSL sources and generated SVGs; [models/message-schema/](models/message-schema/README.md) documents the WebSocket message contracts (source of truth: `/schema/schemas/`); [models/flows/](models/flows/README.md) documents the processes (battle lifecycle, bot connection, turn execution, event handling); [report/](report/README.md) holds architectural health reports produced by the audit skill.

New readers: start with the [System Context](c4-views/system-context.md), then the [Container view](c4-views/container.md), then the flow or schema closest to what you are changing. When structure, protocol, or processes change, update the matching record in the same change that alters the code.

All records below were authored pre-Cliewen and absorbed at CH-001 (`status: draft`, `provenance: inferred`); they are promoted to `verified` as they are checked against the code.

<!-- clue:index:start -->
- [ARCH-001 — System Context Diagram](c4-views/system-context.md) · `draft`
- [ARCH-009 — Battle Lifecycle Flow](models/flows/battle-lifecycle.md) · `draft`
- [ARCH-018 — Architectural Health Report](report/architectural-health-report.md) · `draft`
<!-- clue:index:end -->
