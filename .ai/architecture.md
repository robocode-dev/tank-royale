# Architecture Documentation

<!-- KEYWORDS: architecture, ADR, design decision, C4, protocol design, component diagram -->

## When to Load

Load for: architecture decisions, design rationale, system structure, protocol design, C4 diagrams.

## Architecture Location

All architecture docs in [`docs-internal/architecture/`](../docs-internal/architecture/README.md).

| Category | Location |
|----------|----------|
| ADRs (38) | `docs-internal/architecture/adr/` |
| C4 Views | `docs-internal/architecture/c4-views/` |
| Message Schema (53 types) | `docs-internal/architecture/models/message-schema/` |
| Flows | `docs-internal/architecture/models/flows/` |

## Creating New ADRs

See [`docs-internal/architecture/adr/README.md`](../docs-internal/architecture/adr/README.md) for format (MADR), numbering, and required sections.

## ⛔ ADR Review Gate

After drafting a new ADR, **STOP and present it to the user for review before proceeding with implementation.**

- Write the ADR file and show it (or summarise it) to the user.
- Wait for explicit approval ("looks good", "approved", etc.) before touching any code.
- This applies even when the task says "plan and implement" — ADR first, approval, then code.

## Relationship to OpenSpec

- Architecture docs = source of truth for system design (the "why" and "how")
- OpenSpec specs = requirements and scenarios (the "what must be true")
- OpenSpec specs reference architecture docs — never duplicate content between them
- Update architecture docs when making structural changes
