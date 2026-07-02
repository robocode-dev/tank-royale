# Design Documents

This directory contains detailed design documents that complement the Architecture Decision Records (ADRs) in `../decisions/`.

- ADRs capture the decision, context, options, and rationale (MADR format).
- Design docs here capture specifications, flow details, diagrams, examples, and implementation-oriented material that would otherwise bloat an ADR.

Conventions:
- Keep ADRs focused; put detailed specs here and link from ADRs under “More Information”.
- Use descriptive filenames; cross-link with absolute project-root relative paths (starting with `/docs/…`).

Index:
- [WebSocket Protocol](./websocket-protocol.md)
- [Real-Time Game Loop](./game-loop-architecture.md)
- [Tank Royale Rumble (DRAFT)](./rumble/README.md): umbrella design for a serverless, community-driven tournament system, with sub-designs for [bot submission](./rumble/bot-submission.md), [client battles and result upload](./rumble/client-battles-and-results.md), [aggregation and dashboard](./rumble/aggregation-and-dashboard.md), and [user documentation and onboarding](./rumble/user-documentation.md)
