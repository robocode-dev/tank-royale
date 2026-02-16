# ADR-0023: Robocode Tank Royale Platform Scope and Boundaries

**Status:** Accepted  
**Date:** 2026-02-15

---

## Context

Robocode Tank Royale is a complete platform for competitive tank battling. It consists of multiple interconnected
components, but the project maintainers have finite capacity. Questions arise about what constitutes the "core platform"
versus extensions, and where responsibility boundaries lie.

**Problem:** What is the scope of the Robocode Tank Royale project, and what falls outside its boundaries?

---

## Decision

**Robocode Tank Royale is a platform and reference implementation** consisting of five **core** parts:

1. **Server** — Battle orchestrator (local or containerized deployment)
2. **GUI** — Desktop client for viewing and controlling battles
3. **Bot APIs** — Language bindings for writing competitive bots:
   - **Current:** Java, Python, .NET
   - **Planned:** WebAssembly (WASM) for web-based languages (JavaScript, TypeScript, etc.)
4. **Booter** — Launcher for running bots locally or in containers (required for GUI usage)
5. **Recorder** — Battle recording utility for playback and analysis

**Core principle:** All components are **replaceable**. Other implementations of any component (alternative server,
viewer, Bot API, booter, recorder) are welcome and should be developed **outside this repository**, building upon the
core platform.

---

## Rationale

### What This Repository Provides

The Tank Royale repository provides:

- ✅ **Platform architecture** — How components interact (WebSocket protocol, schema contracts)
- ✅ **Reference implementations** — Proof that the platform works (Java/Kotlin server, Java Swing GUI, Python/Java/.NET
  Bot APIs)
- ✅ **Authoritative schema** — Message contracts and protocols all implementations must follow
- ✅ **Common libraries** — Shared utilities for building components
- ✅ **Sample bots** — Educational examples in multiple languages
- ✅ **API stability guarantees** — Backward compatibility commitments

### What This Repository Does NOT Provide

The Tank Royale repository intentionally **excludes**:

❌ **Tournament/Competition Systems** (like LiteRumble, RoboRumble, or RoboResearch)

- These are **out of scope** — organized competition sits atop the core platform
- Maintaining both core engine AND tournament infrastructure exceeds maintainer capacity
- Tournament organizers are empowered to build their own systems on top of Tank Royale
- The original Robocode (classic) successfully separates these concerns: core game ≠ rumbles

❌ **Alternative Implementations** (other viewers, APIs, servers, booters, recorders)

- These should live in **separate repositories**
- They remain welcome to build on Tank Royale's protocols and schemas
- Keeping them separate preserves focus and prevents feature bloat
- Different implementations may have different design philosophies — that's okay

❌ **Language-Specific Extensions** beyond the core Bot API pattern

- Java, Python, and .NET Bot APIs follow the same pattern
- Additional languages should be developed **outside this repository** if they follow the same API design
- The pattern is clear and well-documented for others to replicate

---

## Bot APIs: Designed for Accessibility, Not Locked In

The Bot APIs in this repository were specifically designed to be **familiar to classic Robocode users**:

- Similar method names and concepts
- Similar event model
- Similar scoring mechanics
- Cross-references to [The Book of Robocode](https://book.robocode.dev) and [RoboWiki](https://robowiki.net)

**This is intentional.** We want to make migration from classic Robocode seamless.

### Current and Planned APIs

Currently, we provide Bot API implementations for:

- **Java** (canonical implementation, part of the core platform)
- **Python** (stable, part of core platform)
- **.NET** (stable, part of core platform)

**Planned for future release:**

- **WebAssembly (WASM)** — Will enable writing bots in web-based languages (JavaScript, TypeScript, Go, Rust, etc.),
  opening Tank Royale to browser-based development and competitive environments

**But:** If someone develops a **better** Bot API with a different philosophy (more functional, different naming,
different abstractions), that's excellent! Those implementations should:

1. Live in a separate repository
2. Implement the same wire protocol (use our schema definitions)
3. Clearly document how they differ from the Tank Royale APIs

---

## Consequences

### Positive

- ✅ **Clear focus** — Core team concentrates on platform stability, not tournament infrastructure
- ✅ **Ecosystem growth** — Community can build specialized solutions (rumbles, alternate GUIs, better APIs)
- ✅ **Replaceable components** — Users aren't locked into Tank Royale's implementations
- ✅ **Maintainability** — Smaller, focused codebase prevents feature creep
- ✅ **Precedent alignment** — Mirrors how classic Robocode separated game engine from competition systems

### Negative / Challenges

- ⚠️ **Fragmentation risk** — Multiple implementations may have incompatibilities (mitigated by schema contracts)
- ⚠️ **Discoverability** — Users must find third-party components themselves (address with documentation and links)
- ⚠️ **Coordination burden** — Core team won't directly support or maintain extensions (but welcomes links in docs)

---

## Related Decisions

- **ADR-0001** (Monorepo Build Strategy) — Why all core components are in one repository
- **ADR-0003** (Cross-Platform Bot API Strategy) — Why we provide Java, Python, and .NET APIs
- **ADR-0004** (Java Reference Implementation) — Why Java/Kotlin is authoritative
- **ADR-0005** (Independent Deployable Components) — Each component can be replaced or redeployed independently
- **ADR-0006** (Schema-Driven Protocol Contracts) — All components communicate via published schemas

---

## Alternatives Considered

### Alternative 1: Include Tournament System in This Repository

**Rejected because:**

- Tournament infrastructure (pairing algorithms, rating systems, bracket management) is a different product
- Maintainers lack capacity to support both engine stability AND tournament features
- Classic Robocode successfully operates without rumbles in the core engine
- Communities should own their own tournament rules and mechanics

### Alternative 2: Restrict Implementations Outside This Repository

**Rejected because:**

- Would limit ecosystem innovation
- Creates a single point of failure
- Contradicts the "platform" philosophy
- The platform thrives when others build on it

### Alternative 3: Incorporate Many Alternative Bot APIs Here

**Rejected because:**

- Each API might target different design philosophies
- Maintenance burden grows with each language/variant
- Users should choose APIs based on their preferences, not what we maintain
- We set the pattern; others can implement it

---

## How Others Can Build on Tank Royale

If you want to create an alternative implementation:

1. **Read the architecture docs** in this repository
2. **Review the schema** (message contracts are canonical)
3. **Create a new repository** for your implementation
4. **Document compatibility** with Tank Royale's wire protocol
5. **Reference Tank Royale's specifications** in your project
6. **Open an issue here** with a link to your project — we'll consider featuring it in documentation

---

## Notes for New Contributors

This ADR clarifies the maintainer's vision so that:

- **Feature requests for tournament systems** are redirected: "This is out of scope, but you could build it as an
  extension!"
- **Pull requests for new Bot APIs** are encouraged to be developed separately
- **New component implementations** (GUIs, booters, recorders) are celebrated when they're in separate repositories
- **Core platform issues** (server bugs, API stability, protocol contracts) are in-scope and valued

---

**Related Documentation:**

- [System Architecture (C4 Views)](../c4-views/README.md) — How components interact
- [Message Schema](../models/message-schema/README.md) — Wire protocol specifications
- [Cross-Platform Bot API Strategy (ADR-0003)](./0003-cross-platform-bot-api-strategy.md)
- [The Book of Robocode](https://book.robocode.dev)
- [RoboWiki](https://robowiki.net)

