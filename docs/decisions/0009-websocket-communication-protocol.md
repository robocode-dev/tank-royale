---
status: accepted
date: 2026-02-11
---

# WebSocket Communication Protocol

---

## Context and Problem Statement

Tank Royale needs real-time bidirectional communication between server and clients (bots, observers) that supports:

- 30 TPS game loop with low latency
- Cross-platform compatibility (Java, .NET, Python, JavaScript)
- Multi-client support (multiple bots + observers)
- Network-first design (local and remote play identical)

**Problem:** Choose communication protocol for real-time game messaging.

---

## Decision Outcome

Use **WebSocket protocol with JSON messages** for all server-client communication.

**Specification:**

- Endpoint: `ws://localhost:7654`
- Message format: JSON (schema-validated)
- Full-duplex: Server pushes events, clients send intents

---

### Consequences

- ✅ Real-time communication at 30 TPS
- ✅ Cross-platform bot development
- ✅ Browser-based clients possible
- ✅ Simple deployment (no broker required)
- ❌ JSON parsing overhead vs binary protocols
- ❌ Schema evolution requires coordination

## Considered Options

- WebSocket + JSON (chosen)
- HTTP REST + polling
- gRPC
- Custom TCP/UDP protocol
- Message Queue (brokered)

## Pros and Cons of the Options

### WebSocket + JSON (chosen)

Good, because real-time bidirectional communication, browser support, language-agnostic JSON, schema validation.

Bad, because JSON overhead versus binary formats; schema evolution coordination required.

### HTTP REST + polling

Bad, because too much latency for 30 TPS; server push awkward.

### gRPC

Good, because strong contracts and streaming; Bad, because poor browser support and higher deployment complexity.

### Custom TCP/UDP

Good, because low overhead; Bad, because reinvents protocol features; no browser support; higher maintenance burden.

### Message Queue

Good, decoupling via broker; Bad, unnecessary operational complexity for this use case.

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

## More Information

- Detailed protocol design, flows, and examples: `/docs/design/websocket-protocol.md`
- Schema YAML Definitions: `/schema/schemas/README.md`
- Protocol Flow Diagrams: `/docs/architecture/models/flows/README.md`
