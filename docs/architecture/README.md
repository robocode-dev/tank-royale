# 🏗️ Architecture as Code — Tank Royale

Comprehensive architecture documentation for Robocode Tank Royale following the **"Architecture as Code"** pattern with a three-pillar approach: **ADRs** (Why), **C4 Views** (What), and **Domain Models + Flows** (How).

---

## 🎯 Quick Navigation

### **Architecture Decision Records (ADRs)** — The "Why"
**[👉 adr/](./adr/)**

Why key architectural decisions were made. See **[adr/README.md](./adr/README.md)** for the full index with statuses and dates.

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
Total ADRs:               41 (35 Accepted, 6 Proposed)
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

## 🔗 Relationship to Other Documentation

| Location | Purpose | Relationship |
|----------|---------|--------------|
| `/docs` | Public user docs | Domain models explain public APIs |
| `/docs-internal/architecture` | **Architecture (this)** | Source of truth for system design |
| `/openspec` | Spec-driven development | References architecture for design rationale (DRY) |
| `/.agents/instructions` | AI guidelines | Routes to architecture docs via `.agents/instructions/architecture.md` |
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
│       ├── component-Recorder.svg
│       └── component-Runner.svg _(pending — run regenerate-all-svgs.ps1)_
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

**Last Updated:** 2026-04-25 | **ADRs:** 41 (35 Accepted, 6 Proposed)
