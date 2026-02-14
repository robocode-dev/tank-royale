# Architecture Decision Records (ADRs)

This directory contains Architecture Decision Records documenting significant architectural decisions for Robocode Tank Royale.

## What are ADRs?

Architecture Decision Records capture the context, alternatives, and rationale behind architectural choices. They answer the question: **"Why did we design it this way?"**

Each ADR includes:
- **Context** — The problem or situation that requires a decision
- **Decision** — What was chosen
- **Rationale** — Why this option was selected
- **Alternatives Considered** — Other options evaluated
- **Consequences** — Positive and negative outcomes

## Format

We use [MADR (Markdown ADR)](https://adr.github.io/madr/) format for consistency and AI-readability.

## Index of ADRs

| ADR | Title | Status | Date       |
|-----|-------|--------|------------|
| [0001](./0001-websocket-communication-protocol.md) | WebSocket Communication Protocol | Accepted | 2026-02-11 |
| [0002](./0002-cross-platform-bot-api-strategy.md) | Cross-Platform Bot API Strategy | Accepted | 2026-02-11 |
| [0003](./0003-realtime-game-loop-architecture.md) | Real-Time Game Loop Architecture | Accepted | 2026-02-11 |
| [0004](./0004-turn-timing-semantics.md) | Turn Timing Semantics | Accepted | 2026-02-13 |
| [0005](./0005-bot-id-team-id-namespace-separation.md) | Bot ID vs Team ID Namespace Separation | Accepted | 2026-02-14 |
| [0006](./0006-teams-support-observer-protocol.md) | Teams Support in Observer Protocol | Proposed | 2026-02-14 |

## Status Definitions

- **Proposed** — Under discussion
- **Accepted** — Approved and implemented
- **Superseded** — Replaced by a newer ADR
- **Deprecated** — No longer recommended but may still exist in codebase

## Creating New ADRs

When making a new architectural decision:

1. **Copy the template** from an existing ADR
2. **Number sequentially** (e.g., 0004, 0005)
3. **Use descriptive title** that summarizes the decision
4. **Include all sections**:
   - Context and problem statement
   - Options considered with trade-offs
   - Decision outcome
   - Rationale
   - Consequences (positive and negative)
5. **Link to related docs** (domain models, flows, requirements, code)
6. **Update this index** when adding new ADRs

## Guidelines

- **Be specific** — Include technical details, not just high-level concepts
- **Show your work** — Document alternatives considered and why they were rejected
- **Be honest** — Include negative consequences, not just benefits
- **Use diagrams** — Mermaid diagrams help explain complex decisions
- **Link extensively** — Connect to code, specs, and other documentation

---

**Related Documentation:**
- [C4 Views](../c4-views/) — Visual architecture diagrams
- [Message Schema](../models/message-schema/) — WebSocket message contracts
- [Business Flows](../models/flows/) — Process documentation
- [OpenSpec Specs](/openspec/specs/) — Requirements and scenarios (references ADRs for design rationale)
- [AI Architecture Guide](/.ai/architecture.md) — AI agent routing to architecture docs
