# ADR-0009: WebSocket Communication Protocol

**Status:** Accepted  
**Date:** 2026-02-11

---

## Context

Tank Royale needs real-time bidirectional communication between server and clients (bots, observers) that supports:

- 30 TPS game loop with low latency
- Cross-platform compatibility (Java, .NET, Python, JavaScript)
- Multi-client support (multiple bots + observers)
- Network-first design (local and remote play identical)

**Problem:** Choose communication protocol for real-time game messaging.

---

## Decision

Use **WebSocket protocol with JSON messages** for all server-client communication.

**Specification:**

- Endpoint: `ws://localhost:7654`
- Message format: JSON (schema-validated)
- Full-duplex: Server pushes events, clients send intents

---

## Rationale

**Why WebSocket:**

- ✅ Real-time bidirectional communication (no polling)
- ✅ Native support in all target languages
- ✅ Browser compatibility (web-based clients possible)
- ✅ Network-first (same protocol for local/remote)

**Why JSON:**

- ✅ Human-readable (debugging)
- ✅ Language-agnostic
- ✅ Schema validation via YAML
- ✅ Easy to evolve (add fields without breaking clients)

**Alternatives rejected:**

- **HTTP REST + polling:** Too much latency for 30 TPS
- **gRPC:** No browser support, deployment complexity
- **Custom TCP/UDP:** Reinventing the wheel, no browser support
- **Message Queue:** Unnecessary broker complexity

---

## Implementation

**Connection flow:**

```
Bot → Server: WebSocket Connect
Server → Bot: server-handshake {sessionId}
Bot → Server: bot-handshake {sessionId, name}
Server → Bot: game-started-event
Loop: Server → Bot: tick-event, Bot → Server: bot-intent
```

**Example messages:**

```json
// Server → Bot
{
    "type": "tick-event-for-bot",
    "turnNumber": 42,
    "botState": {
        ...
    },
    "events": [
        ...
    ]
}

// Bot → Server  
{
    "type": "bot-intent",
    "turnRate": 5,
    "targetSpeed": 8,
    "firepower": 3
}
```

---

## Consequences

- ✅ Real-time communication at 30 TPS
- ✅ Cross-platform bot development
- ✅ Browser-based clients possible
- ✅ Simple deployment (no broker required)
- ❌ JSON parsing overhead vs binary protocols
- ❌ Schema evolution requires coordination

---

## References

- [WebSocket RFC 6455](https://tools.ietf.org/html/rfc6455)
- [Schema Definitions](/schema/schemas/README.md)
