# ADR-0007: Client Role Separation (Bot / Observer / Controller)

**Status:** Accepted  
**Date:** 2026-02-14 (Documenting historical decision)

---

## Context

Multiple client types connect to the Tank Royale server with fundamentally different needs: bots competing in battles,
spectators watching, and operators managing games.

**Problem:** How to handle different client capabilities and permissions over a single WebSocket server?

---

## Decision

Define three distinct **client roles** with separate handshakes, message permissions, and state views:

| Role | Purpose | Sends | Receives |
|------|---------|-------|----------|
| **Bot** | Competes in battles | `bot-intent` | `tick-event-for-bot` (own-bot view) |
| **Observer** | Watches battles | — | `tick-event-for-observer` (full view) |
| **Controller** | Manages battles | `start-game`, `stop-game`, `pause`, `resume`, `change-tps` | Full view + bot list |

**Implementation:**

- Each role has its own handshake schema (`bot-handshake`, `observer-handshake`, `controller-handshake`)
- Server maintains separate socket sets and handshake maps per role
- Observers and Controllers share the same secret pool
- Events split into role-specific variants (e.g., `TickEventForBot` vs `TickEventForObserver`)

---

## Rationale

- ✅ Principle of least privilege — bots can't control games, observers can't submit intents
- ✅ Information hiding — bots see only their own state (no omniscient view)
- ✅ Clean protocol — each role has a minimal, purpose-specific message set
- ✅ Security — role validated at handshake, enforced throughout session
- ❌ Dual event types (for-bot vs for-observer) add schema surface area

---

## References

- [Handshake schemas](/schema/schemas/) (`bot-handshake`, `observer-handshake`, `controller-handshake`)
- [Handshakes documentation](/docs-internal/architecture/models/message-schema/handshakes.md)
- [ADR-0009: WebSocket Communication Protocol](./0009-websocket-communication-protocol.md)
