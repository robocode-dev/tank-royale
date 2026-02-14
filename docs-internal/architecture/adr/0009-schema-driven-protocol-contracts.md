# ADR-0009: Schema-Driven Protocol Contracts

**Status:** Accepted  
**Date:** 2026-02-14 (Documenting historical decision)

---

## Context

Tank Royale's WebSocket protocol has 53+ message types exchanged between server and clients in multiple languages.

**Problem:** How to ensure all Bot APIs (Java, .NET, Python) use exactly the same message structures and prevent
protocol divergence?

---

## Decision

Define all protocol messages as **YAML/JSON Schema files** in `schema/schemas/`. These schemas are the single source of
truth for all message contracts.

**Mechanism:**

- Schemas defined in `schema/schemas/*.schema.yaml`
- Java classes auto-generated via `jsonschema2pojo` Gradle plugin
- Other Bot APIs manually conform to the same schemas
- Schema validation ensures wire-format consistency

---

## Rationale

- ✅ Single source of truth prevents protocol divergence across languages
- ✅ Machine-readable schemas enable tooling (validation, code generation, documentation)
- ✅ Easy to evolve (add optional fields without breaking clients)
- ✅ Human-readable YAML format
- ❌ Manual conformance required for non-Java Bot APIs
- ❌ Schema evolution requires coordination across all platforms

**Alternatives rejected:**

- **Protobuf/gRPC:** No browser support, overkill for JSON-based protocol
- **Hand-written classes only:** Inevitable divergence across 3+ languages
- **OpenAPI:** Designed for REST, not WebSocket messaging

---

## References

- [Schema definitions](/schema/schemas/)
- [ADR-0001: WebSocket Communication Protocol](./0001-websocket-communication-protocol.md)
- [Message Schema docs](/docs-internal/architecture/models/message-schema/)
