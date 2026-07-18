# Architecture Documentation

<!-- KEYWORDS: architecture, ADR, design decision, C4, protocol design, component diagram -->

## When to Load

Load for: architecture decisions, design rationale, system structure, protocol design, C4 diagrams.

## Architecture Location

All architecture docs in [`docs/architecture/`](../../docs/architecture/README.md).

| Category | Location |
|----------|----------|
| ADRs | `docs/decisions/` |
| C4 Views | `docs/architecture/c4-views/` |
| Message Schema | `docs/architecture/models/message-schema/` |
| Flows | `docs/architecture/models/flows/` |
| Design specs | `docs/design/` |

## Creating New ADRs

See [`docs/decisions/README.md`](../../docs/decisions/README.md) for format, numbering, and status vocabulary. New ADRs are born inside a change (`/changes/CH-xxx/`, `clue-delta` skill) with `status: inferred`; the human-merged PR is the review gate (constraint [C-002](../../docs/constraints/C-002-review-boundary.md)), and a human promotes to `verified` by adding `accepted-by`. Keep ADRs product-centered — no tooling or process ADRs.

## Relationship to Capabilities

- Architecture docs = source of truth for system design (the "why" and "how")
- Capability criteria (`docs/capabilities/*/criteria.md`) = what must be true, as Gherkin acceptance criteria
- Criteria reference architecture docs — never duplicate content between them
- Update architecture docs when making structural changes, in the same change as the code
