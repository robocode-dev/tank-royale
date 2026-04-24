# 🏗️ Architecture as Code — Tank Royale

Comprehensive architecture documentation for Robocode Tank Royale following the **"Architecture as Code"** pattern with a three-pillar approach: **ADRs** (Why), **C4 Views** (What), and **Domain Models + Flows** (How).

---

## 🎯 Quick Navigation

### **Architecture Decision Records (ADRs)** — The "Why"
**[👉 adr/](./adr/)**

Why key architectural decisions were made:
- ✅ [**ADR-0001**](./adr/0001-monorepo-build-strategy.md) — Monorepo Build Strategy
- ✅ [**ADR-0002**](./adr/0002-standard-math-coordinate-system.md) — Standard Mathematical Coordinate System
- ✅ [**ADR-0003**](./adr/0003-cross-platform-bot-api-strategy.md) — Cross-Platform Bot APIs
- ✅ [**ADR-0004**](./adr/0004-java-reference-implementation.md) — Java as Authoritative Reference
- ✅ [**ADR-0005**](./adr/0005-independent-deployable-components.md) — Independent Deployable Components
- ✅ [**ADR-0006**](./adr/0006-schema-driven-protocol-contracts.md) — Schema-Driven Protocol Contracts
- ✅ [**ADR-0007**](./adr/0007-client-role-separation.md) — Client Role Separation (Bot/Observer/Controller)
- ✅ [**ADR-0008**](./adr/0008-server-authoritative-physics.md) — Server-Authoritative Deterministic Physics
- ✅ [**ADR-0009**](./adr/0009-websocket-communication-protocol.md) — WebSocket Protocol
- ✅ [**ADR-0010**](./adr/0010-declarative-bot-intent-model.md) — Declarative Bot Intent Model
- ✅ [**ADR-0011**](./adr/0011-realtime-game-loop-architecture.md) — Real-Time 30 TPS Game Loop
- ✅ [**ADR-0012**](./adr/0012-turn-timing-semantics.md) — Turn Timing Semantics
- ✅ [**ADR-0013**](./adr/0013-bot-configuration-env-vars.md) — Bot Configuration via Environment Variables
- ✅ [**ADR-0014**](./adr/0014-two-tier-authentication.md) — Two-Tier Shared-Secret Authentication
- ✅ [**ADR-0015**](./adr/0015-bot-id-team-id-namespace-separation.md) — Participant ID as Unified Team Identifier
- ✅ [**ADR-0016**](./adr/0016-session-id-bot-process-identification.md) — Session ID for Bot Process Identification
- ✅ [**ADR-0017**](./adr/0017-recording-format.md) — Recording Format (ND-JSON + Gzip)
- ✅ [**ADR-0018**](./adr/0018-custom-svg-rendering.md) — Custom SVG Rendering for Bot API Graphics
- ✅ [**ADR-0019**](./adr/0019-r8-code-shrinking.md) — R8 Code Shrinking
- ✅ [**ADR-0020**](./adr/0020-teams-support-observer-protocol.md) — Teams Support in Observer Protocol
- ✅ [**ADR-0021**](./adr/0021-java-swing-gui-reference-implementation.md) — Java Swing as GUI Reference Implementation
- ✅ [**ADR-0022**](./adr/0022-event-system-gui-decoupling.md) — Event System for GUI Decoupling
- ✅ [**ADR-0023**](./adr/0023-robocode-tank-royale-platform-scope.md) — Platform Scope and Boundaries
- ✅ [**ADR-0024**](./adr/0024-battle-runner-api.md) — Battle Runner API
- ✅ [**ADR-0025**](./adr/0025-game-type-presets-and-rule-configuration.md) — Game Type Presets and Rule Configuration
- ✅ [**ADR-0026**](./adr/0026-identity-based-bot-matching.md) — Identity-Based Bot Matching in Battle Runner
- ✅ [**ADR-0027**](./adr/0027-typescript-bot-api-architecture.md) — TypeScript Bot API for Web Platform Support
- ✅ [**ADR-0028**](./adr/0028-typescript-bot-api-threading-model.md) — TypeScript Bot API Threading Model
- ✅ [**ADR-0029**](./adr/0029-typescript-bot-api-runtime-targets.md) — TypeScript Bot API Runtime Targets
- ✅ [**ADR-0030**](./adr/0030-convention-over-configuration-bot-entry-points.md) — Template-Based Booting and Base Convention
- ✅ [**ADR-0031**](./adr/0031-optional-bot-config-and-runtime-validation.md) — Optional Bot Config and Runtime Validation
- ✅ [**ADR-0032**](./adr/0032-user-defined-visual-overrides-for-tanks.md) — Tank Color Display Mode
- ✅ [**ADR-0033**](./adr/0033-bot-debug-mode.md) — Server Debug Mode
- 📝 [**ADR-0034**](./adr/0034-breakpoint-mode.md) — Breakpoint Mode
- 📝 [**ADR-0035**](./adr/0035-bot-debugger-detection.md) — Bot API Debugger Detection
- 📝 [**ADR-0036**](./adr/0036-start-game-debug-options.md) — Start-Game Debug Options
- 📝 [**ADR-0037**](./adr/0037-functional-core-bot-api-testability.md) — Functional Core Extraction for Bot API Testability
- ✅ [**ADR-0038**](./adr/0038-shared-cross-platform-test-definitions.md) — Cross-Platform Test Parity and Shared Test Definitions
- 📝 [**ADR-0039**](./adr/0039-server-testability.md) — Server Testability — Physics Core and Test Framework
- ✅ [**ADR-0040**](./adr/0040-ready-timeout-default.md) — Raise Default readyTimeout from 1s to 10s
- 📝 [**ADR-0041**](./adr/0041-bot-api-library-version-management.md) — Bot API Library Version Management in the GUI

**Use When:** Understanding design rationale, trade-offs, alternatives

---

### **C4 Architecture Diagrams** — The "What" (Visual)
**[👉 c4-views/](./c4-views/)**

Hierarchical system visualization at multiple zoom levels:
- ✅ [**System Context**](./c4-views/system-context.md) — High-level 10,000-foot view
- ✅ [**Container**](./c4-views/container.md) — Major containers and deployment
- ✅ [**Server Components**](./c4-views/server-components.md) — Server internals
- ✅ [**Bot API Components**](./c4-views/bot-api-components.md) — Bot API structure
- ✅ [**GUI Components**](./c4-views/gui-components.md) — GUI internals
- ✅ [**Booter Components**](./c4-views/booter-components.md) — Booter internals
- ✅ [**Recorder Components**](./c4-views/recorder-components.md) — Recorder internals
- ✅ [**Runner Components**](./c4-views/runner-components.md) — Runner internals

**Use When:** Understanding system structure, data flow, components

---

### **Message Schema** — The "What" (Detailed)
**[👉 models/message-schema/](./models/message-schema/)**

WebSocket message contracts and data exchange objects:
- ✅ [**Handshakes**](./models/message-schema/handshakes.md) — Connection establishment (5 message types)
- ✅ [**Commands**](./models/message-schema/commands.md) — Controller operations (9 commands)
- ✅ [**Events**](./models/message-schema/events.md) — Server notifications (26 event types)
- ✅ [**Intents**](./models/message-schema/intents.md) — Bot actions (bot-intent, team-message)
- ✅ [**State Objects**](./models/message-schema/state.md) — Data transfer objects (13 schemas)

**Use When:** Understanding message formats, wire protocol, API contracts

---

### **Business Flows** — The "How" (Processes)
**[👉 models/flows/](./models/flows/)**

How entities interact through processes:
- ✅ [**Battle Lifecycle**](./models/flows/battle-lifecycle.md) — 4-phase state progression
- ✅ [**Bot Connection**](./models/flows/bot-connection.md) — WebSocket handshake & setup
- ✅ [**Turn Execution**](./models/flows/turn-execution.md) — 30 TPS game loop (15-step sequence)
- ✅ [**Event Handling**](./models/flows/event-handling.md) — Event generation, queuing, and dispatch

**Use When:** Understanding processes, sequences, error handling, timing

---

## 📚 Complete Index

### ADRs (Architecture Decision Records)

See **[adr/README.md](./adr/README.md)** for the full canonical ADR index with dates and statuses.

### Message Schema
| Category | Coverage | Status |
|----------|----------|--------|
| Handshakes | 5 message types | ✅ |
| Commands | 9 command types | ✅ |
| Events | 26 event types | ✅ |
| Intents | 2 intent types | ✅ |
| State Objects | 13 DTO schemas | ✅ |

### Business Flows
| Flow | Purpose | Status |
|------|---------|--------|
| Battle Lifecycle | State progression over battle | ✅ |
| Bot Connection | WebSocket handshake sequence | ✅ |
| Turn Execution | 30 TPS game loop mechanics | ✅ |
| Event Handling | Event generation, queuing, and dispatch | ✅ |

### C4 Views
| Level | Name | Status |
|-------|------|--------|
| 1 | System Context | ✅ |
| 2 | Container | ✅ |
| 3 | Server Components | ✅ |
| 3 | Bot API Components | ✅ |
| 3 | GUI Components | ✅ |
| 3 | Booter Components | ✅ |
| 3 | Recorder Components | ✅ |
| 3 | Runner Components | ✅ |

---

## 🎓 Learning Paths

### For **New Developers** (1-2 hours)
1. Read [System Context Diagram](./c4-views/system-context.md) (5 min)
2. Read [Message Schema Overview](./models/message-schema/README.md) (10 min)
3. Read [Events](./models/message-schema/events.md) (15 min)
4. Read [Battle Lifecycle Flow](./models/flows/battle-lifecycle.md) (15 min)
5. Review ADRs for design context (15 min each)

### For **Bot Developers**
1. [Handshakes](./models/message-schema/handshakes.md) — Connection setup
2. [Events](./models/message-schema/events.md) — Event types and payloads
3. [Intents](./models/message-schema/intents.md) — Bot action messages
4. [Bot Connection Flow](./models/flows/bot-connection.md) — Complete handshake sequence

### For **Server Developers**
1. All message schemas ([Handshakes](./models/message-schema/handshakes.md), [Commands](./models/message-schema/commands.md), [Events](./models/message-schema/events.md), [Intents](./models/message-schema/intents.md), [State](./models/message-schema/state.md))
2. [ADR-0011: Game Loop](./adr/0011-realtime-game-loop-architecture.md) — Timing & determinism
3. [Turn Execution Flow](./models/flows/turn-execution.md) — 15-step sequence
4. [Battle Lifecycle Flow](./models/flows/battle-lifecycle.md) — State machine

### For **System Designers**
1. [ADR index](./adr/) — All architectural decisions and their rationale
2. System Context and Container diagrams
3. All message schemas and state objects
4. Flow state machines

### For **AI Agents**
1. Message schemas for protocol understanding
2. Mermaid diagrams for structure
3. JSON examples for serialization
4. Schema constraints for validation
5. ADRs for decision context

---

## 📊 Statistics

```
Total ADRs:               41 (27 Accepted, 14 Proposed)
C4 Views:                 8 (L1: 1, L2: 1, L3: 6)
WebSocket Schema Files:   55 total
  Handshakes:             5
  Commands:               9
  Events:                 26
  Intents:                2
  State Objects:          13
Business Flows:           4
```

---

## 📖 About Robocode Tank Royale

Real-time programming game where players code virtual tank bots that battle in an arena:

- **Server** — Orchestrates battles, manages game state, enforces rules
- **GUI** — Visualizes battles and provides configuration
- **Bot APIs** — Multi-language libraries (Java, .NET, Python, etc.)
- **WebSocket Protocol** — Real-time bidirectional communication

### Architecture Principles

1. **Real-Time Performance** — 30 TPS (turns per second) deterministic loop
2. **Cross-Platform** — Bots in multiple languages with symmetric APIs
3. **Network-First** — All communication over WebSocket
4. **Deterministic Physics** — Reproducible, fair mechanics
5. **Extensible** — Support for custom behaviors and game modes

---

## 🔗 Relationship to Other Documentation

| Location | Purpose | Relationship |
|----------|---------|--------------|
| `/docs` | Public user docs | Domain models explain public APIs |
| `/docs-internal/architecture` | **Architecture (this)** | Source of truth for system design |
| `/openspec` | Spec-driven development | References architecture for design rationale (DRY) |
| `/.ai` | AI guidelines | Routes to architecture docs via `.ai/architecture.md` |
| `/schema` | WebSocket schemas | Referenced by ADRs and domain models |

---

## 💡 Quick Answers

| Question | Answer |
|----------|--------|
| **New to Tank Royale?** | Start with [System Context](./c4-views/system-context.md) |
| **Why this architecture?** | Check the [ADRs](./adr/) |
| **What messages are exchanged?** | Browse [Message Schema](./models/message-schema/) |
| **How do processes work?** | See [Business Flows](./models/flows/) |
| **Building bot code?** | Reference [Handshakes](./models/message-schema/handshakes.md) + [Events](./models/message-schema/events.md) + [Intents](./models/message-schema/intents.md) |
| **Developing server?** | Study game loop ADR + [Turn Execution](./models/flows/turn-execution.md) |
| **Debugging an issue?** | Find state machines in flows and message schemas |

---

## 🔄 Contributing

When making architectural changes:
1. **Update relevant ADR** if decision rationale changes
2. **Update C4 diagrams** if structure changes
3. **Update message schemas** if protocol changes
4. **Update flows** if processes change
5. Keep documentation synchronized with code and schemas in `/schema/schemas/`

---

## 📁 File Structure

```
docs-internal/architecture/
├── README.md                              (This file)
├── adr/                                   (Why)
│   ├── README.md
│   ├── 0001-monorepo-build-strategy.md
│   ├── 0002-standard-math-coordinate-system.md
│   ├── ...
│   └── 0041-bot-api-library-version-management.md
├── c4-views/                              (What - Visual)
│   ├── README.md
│   ├── system-context.md
│   ├── container.md
│   ├── server-components.md
│   ├── bot-api-components.md
│   ├── gui-components.md
│   ├── booter-components.md
│   ├── recorder-components.md
│   ├── runner-components.md
│   ├── structurizr-dsl/
│   │   ├── system-context.dsl
│   │   ├── container.dsl
│   │   ├── server-components.dsl
│   │   ├── bot-api-components.dsl
│   │   ├── gui-components.dsl
│   │   ├── booter-components.dsl
│   │   └── recorder-components.dsl
│   └── images/
│       ├── system-context.svg
│       ├── container.svg
│       ├── component-Server.svg
│       ├── component-BotAPI.svg
│       ├── component-GUI.svg
│       ├── component-Booter.svg
│       └── component-Recorder.svg
└── models/                                (What/How - Detailed)
    ├── message-schema/                    (WebSocket Contracts)
    │   ├── README.md
    │   ├── handshakes.md
    │   ├── commands.md
    │   ├── events.md
    │   ├── intents.md
    │   └── state.md
    └── flows/                             (Processes)
        ├── README.md
        ├── battle-lifecycle.md
        ├── bot-connection.md
        ├── turn-execution.md
        └── event-handling.md
```

---

**Last Updated:** 2026-04-24 | **ADRs:** 41 (27 Accepted, 14 Proposed)
