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
| [0001](./0001-monorepo-build-strategy.md) | Monorepo Build Strategy | Accepted | 2026-02-14 |
| [0002](./0002-standard-math-coordinate-system.md) | Standard Mathematical Coordinate System | Accepted | 2026-02-14 |
| [0003](./0003-cross-platform-bot-api-strategy.md) | Cross-Platform Bot API Strategy | Accepted | 2026-02-11 |
| [0004](./0004-java-reference-implementation.md) | Java as Authoritative Reference Implementation | Accepted | 2026-02-14 |
| [0005](./0005-independent-deployable-components.md) | Independent Deployable Components | Accepted | 2026-02-14 |
| [0006](./0006-schema-driven-protocol-contracts.md) | Schema-Driven Protocol Contracts | Accepted | 2026-02-14 |
| [0007](./0007-client-role-separation.md) | Client Role Separation (Bot / Observer / Controller) | Accepted | 2026-02-14 |
| [0008](./0008-server-authoritative-physics.md) | Server-Authoritative Deterministic Physics | Accepted | 2026-02-14 |
| [0009](./0009-websocket-communication-protocol.md) | WebSocket Communication Protocol | Accepted | 2026-02-11 |
| [0010](./0010-declarative-bot-intent-model.md) | Declarative Bot Intent Model | Accepted | 2026-02-14 |
| [0011](./0011-realtime-game-loop-architecture.md) | Real-Time Game Loop Architecture | Accepted | 2026-02-11 |
| [0012](./0012-turn-timing-semantics.md) | Turn Timing Semantics | Accepted | 2026-02-13 |
| [0013](./0013-bot-configuration-env-vars.md) | Bot Configuration via Environment Variables | Accepted | 2026-02-14 |
| [0014](./0014-two-tier-authentication.md) | Two-Tier Shared-Secret Authentication | Accepted | 2026-02-14 |
| [0015](./0015-bot-id-team-id-namespace-separation.md) | Participant ID as Unified Team Identifier | Accepted | 2026-02-14 |
| [0016](./0016-session-id-bot-process-identification.md) | Session ID for Bot Process Identification | Accepted | 2026-02-14 |
| [0017](./0017-recording-format.md) | Recording Format (ND-JSON + Gzip) | Accepted | 2026-02-14 |
| [0018](./0018-custom-svg-rendering.md) | Custom SVG Rendering for Bot API Graphics | Accepted | 2026-02-14 |
| [0019](./0019-r8-code-shrinking.md) | R8 Code Shrinking | Accepted | 2026-02-14 |
| [0020](./0020-teams-support-observer-protocol.md) | Teams Support in Observer Protocol | Proposed | 2026-02-14 |

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
