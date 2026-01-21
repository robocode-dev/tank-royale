# Design: WebSocket Protocol

The Tank Royale protocol is designed for high-performance, real-time competitive play while remaining accessible to many
programming languages.

## Context

The game requires an authoritative server to maintain fair physics simulation and state management. Bots are remote
clients that receive partial state and must submit intents within strict time limits.

## Decisions

### Decision: JSON over WebSockets

- **Rationale**: WebSockets provide the low-latency, full-duplex communication needed for real-time turns. JSON is
  chosen for its universal support across programming languages, facilitating the creation of diverse Bot APIs.

### Decision: Schema-Driven (YAML/JSON Schema)

- **Rationale**: By defining all messages in `schema/schemas/*.schema.yaml`, we ensure that all Bot APIs (Java, .NET,
  Python, etc.) use exactly the same data structures. This "Source of Truth" approach prevents protocol divergence.

### Decision: Tick-Based Advancement

- **Rationale**: Discrete ticks provide a deterministic environment for bots. The "Running next turn" sequence ensures
  that bots react only to the state they have actually seen, preventing race conditions in the game physics.

## Sequence Diagrams

The authoritative sequence diagrams for the protocol are maintained in `schema/schemas/README.md`. These diagrams MUST
be followed by all server and client implementations.

Key diagrams include:

- **Bot joining**: Handshake and session establishment.
- **Starting a game**: Readiness synchronization.
- **Running next turn**: The core tick/intent reactive loop.

## Cross-Language Consistency

All Bot APIs MUST maintain 1:1 semantic equivalence with the Java reference implementation regarding how they handle
these protocol events.
