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

| ADR                                                              | Title                                                | Status   | Date       |
|------------------------------------------------------------------|------------------------------------------------------|----------|------------|
| [0001](./0001-monorepo-build-strategy.md)                        | Monorepo Build Strategy                              | Accepted | 2026-02-14 |
| [0002](./0002-standard-math-coordinate-system.md)                | Standard Mathematical Coordinate System              | Accepted | 2026-02-14 |
| [0003](./0003-cross-platform-bot-api-strategy.md)                | Cross-Platform Bot API Strategy                      | Accepted | 2026-02-11 |
| [0004](./0004-java-reference-implementation.md)                  | Java as Authoritative Reference Implementation       | Accepted | 2026-02-14 |
| [0005](./0005-independent-deployable-components.md)              | Independent Deployable Components                    | Accepted | 2026-02-14 |
| [0006](./0006-schema-driven-protocol-contracts.md)               | Schema-Driven Protocol Contracts                     | Accepted | 2026-02-14 |
| [0007](./0007-client-role-separation.md)                         | Client Role Separation (Bot / Observer / Controller) | Accepted | 2026-02-14 |
| [0008](./0008-server-authoritative-physics.md)                   | Server-Authoritative Deterministic Physics           | Accepted | 2026-02-14 |
| [0009](./0009-websocket-communication-protocol.md)               | WebSocket Communication Protocol                     | Accepted | 2026-02-11 |
| [0010](./0010-declarative-bot-intent-model.md)                   | Declarative Bot Intent Model                         | Accepted | 2026-02-14 |
| [0011](./0011-realtime-game-loop-architecture.md)                | Real-Time Game Loop Architecture                     | Accepted | 2026-02-11 |
| [0012](./0012-turn-timing-semantics.md)                          | Turn Timing Semantics                                | Accepted | 2026-02-13 |
| [0013](./0013-bot-configuration-env-vars.md)                     | Bot Configuration via Environment Variables          | Accepted | 2026-02-14 |
| [0014](./0014-two-tier-authentication.md)                        | Two-Tier Shared-Secret Authentication                | Accepted | 2026-02-14 |
| [0015](./0015-bot-id-team-id-namespace-separation.md)            | Participant ID as Unified Team Identifier            | Accepted | 2026-02-14 |
| [0016](./0016-session-id-bot-process-identification.md)          | Session ID for Bot Process Identification            | Accepted | 2026-02-14 |
| [0017](./0017-recording-format.md)                               | Recording Format (ND-JSON + Gzip)                    | Accepted | 2026-02-14 |
| [0018](./0018-custom-svg-rendering.md)                           | Custom SVG Rendering for Bot API Graphics            | Accepted | 2026-02-14 |
| [0019](./0019-r8-code-shrinking.md)                              | R8 Code Shrinking                                    | Accepted | 2026-02-14 |
| [0020](./0020-teams-support-observer-protocol.md)                | Teams Support in Observer Protocol                   | Accepted | 2026-02-14 |
| [0021](./0021-java-swing-gui-reference-implementation.md)        | Java Swing as GUI Reference Implementation           | Accepted | 2026-02-15 |
| [0022](./0022-event-system-gui-decoupling.md)                    | Event System for GUI Decoupling                      | Accepted | 2026-02-15 |
| [0023](./0023-robocode-tank-royale-platform-scope.md)            | Robocode Tank Royale Platform Scope and Boundaries   | Accepted | 2026-02-15 |
| [0024](./0024-battle-runner-api.md)                              | Battle Runner API                                    | Accepted | 2026-02-28 |
| [0025](./0025-game-type-presets-and-rule-configuration.md)       | Game Type Presets and Rule Configuration             | Accepted | 2026-02-28 |
| [0026](./0026-identity-based-bot-matching.md)                    | Identity-Based Bot Matching in Battle Runner         | Accepted | 2026-03-21 |
| [0027](./0027-typescript-bot-api-architecture.md)                | TypeScript Bot API for Web Platform Support          | Accepted | 2026-03-24 |
| [0028](./0028-typescript-bot-api-threading-model.md)             | TypeScript Bot API Threading Model                   | Accepted | 2026-03-24 |
| [0029](./0029-typescript-bot-api-runtime-targets.md)             | TypeScript Bot API Runtime Targets                   | Accepted | 2026-03-24 |
| [0030](./0030-convention-over-configuration-bot-entry-points.md) | Template-based Booting and Base Convention           | Accepted | 2026-04-05 |
| [0031](./0031-optional-bot-config-and-runtime-validation.md)    | Optional Bot Config and Runtime Validation           | Accepted | 2026-04-05 |
| [0032](./0032-user-defined-visual-overrides-for-tanks.md) | Tank Color Display Mode                              | Accepted | 2026-04-06 |
| [0033](./0033-bot-debug-mode.md)                                 | Server Debug Mode                                    | Accepted | 2026-04-07 |
| [0034](./0034-breakpoint-mode.md)                                | Breakpoint Mode                                      | Proposed | 2026-04-07 |
| [0035](./0035-bot-debugger-detection.md)                         | Bot API Debugger Detection                           | Proposed | 2026-04-07 |
| [0036](./0036-start-game-debug-options.md)                       | Start-Game Debug Options                             | Proposed | 2026-04-08 |
| [0037](./0037-functional-core-bot-api-testability.md)            | Functional Core Extraction for Bot API Testability   | Proposed | 2026-04-14 |
| [0038](./0038-shared-cross-platform-test-definitions.md)         | Cross-Platform Test Parity and Shared Test Definitions | Accepted | 2026-04-14 |
| [0039](./0039-server-testability.md)                             | Server Testability — Physics Core and Test Framework | Proposed | 2026-04-14 |
| [0040](./0040-ready-timeout-default.md)                          | Raise Default readyTimeout from 1s to 10s            | Accepted | 2026-04-15 |
| [0041](./0041-bot-api-library-version-management.md)             | Bot API Library Version Management in the GUI        | Proposed | 2026-04-16 |

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
6. **Update the Index of ADRs table** in this file and add the filename row to `INDEX.md`

## Guidelines

- **Be specific** — Include technical details, not just high-level concepts
- **Show your work** — Document alternatives considered and why they were rejected
- **Be honest** — Include negative consequences, not just benefits
- **Use diagrams** — Mermaid diagrams help explain complex decisions
- **Link extensively** — Connect to code, specs, and other documentation

---

**Related Documentation:**
- [C4 Views](../c4-views/README.md) — Visual architecture diagrams
- [Message Schema](../models/message-schema/README.md) — WebSocket message contracts
- [Business Flows](../models/flows/README.md) — Process documentation
- [OpenSpec Specs](/openspec/specs/) — Requirements and scenarios (references ADRs for design rationale)
- [AI Architecture Guide](/.ai/architecture.md) — AI agent routing to architecture docs
