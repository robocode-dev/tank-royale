# Architecture Documentation

## When to Load This

Load when task involves:
- Architecture decisions or ADRs
- Design rationale or trade-offs
- System structure or component design
- Protocol design decisions
- C4 diagrams or system views
- Keywords: "architecture", "ADR", "design decision", "C4", "component diagram"

## Architecture Location

All architecture documentation lives in [`docs-internal/architecture/`](../docs-internal/architecture/README.md).

### What's There

| Category | Location | Content |
|----------|----------|---------|
| **ADRs** | `docs-internal/architecture/adr/` | Why decisions were made (20 ADRs) |
| **C4 Views** | `docs-internal/architecture/c4-views/` | Visual system diagrams (4 levels) |
| **Message Schema** | `docs-internal/architecture/models/message-schema/` | WebSocket message contracts (53 types) |
| **Flows** | `docs-internal/architecture/models/flows/` | Process documentation (3 flows) |

### Key ADRs

- **ADR-0001**: Monorepo Build Strategy
- **ADR-0002**: Standard Mathematical Coordinate System
- **ADR-0003**: Cross-Platform Bot API Strategy
- **ADR-0004**: Java as Authoritative Reference Implementation
- **ADR-0005**: Independent Deployable Components
- **ADR-0006**: Schema-Driven Protocol Contracts
- **ADR-0007**: Client Role Separation (Bot/Observer/Controller)
- **ADR-0008**: Server-Authoritative Deterministic Physics
- **ADR-0009**: WebSocket Communication Protocol
- **ADR-0010**: Declarative Bot Intent Model
- **ADR-0011**: Real-Time Game Loop Architecture
- **ADR-0012**: Turn Timing Semantics
- **ADR-0013**: Bot Configuration via Environment Variables
- **ADR-0014**: Two-Tier Shared-Secret Authentication
- **ADR-0015**: Participant ID as Unified Team Identifier
- **ADR-0016**: Session ID for Bot Process Identification
- **ADR-0017**: Recording Format (ND-JSON + Gzip)
- **ADR-0018**: Custom SVG Rendering for Bot API Graphics
- **ADR-0019**: R8 Code Shrinking
- **ADR-0020**: Teams Support in Observer Protocol (Proposed)

## Relationship to OpenSpec

- **Architecture docs** = source of truth for system design (the "why" and "how")
- **OpenSpec specs** = requirements and scenarios (the "what must be true")
- OpenSpec specs reference architecture docs for design rationale — never duplicate
- When creating OpenSpec change proposals that touch architecture, consult and update relevant ADRs

## Creating New ADRs

See [`docs-internal/architecture/adr/README.md`](../docs-internal/architecture/adr/README.md) for:
- ADR format (MADR)
- Numbering conventions
- Required sections
- Guidelines

## Important

- **DRY**: Architecture content belongs in `docs-internal/architecture/`, not in `/openspec`
- **OpenSpec references architecture**: OpenSpec specs and `project.md` link to architecture docs
- **Keep in sync**: Update architecture docs when making structural changes
