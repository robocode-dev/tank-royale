---
id: ADR-0005
type: decision
status: verified
links: []
title: Independent Deployable Components
accepted-by: Flemming N. Larsen (2026-02-14, pre-Cliewen MADR acceptance)
---

# ADR-0005: Independent Deployable Components

## Context

Tank Royale needs a server, a GUI, a bot launcher, and a recorder. These could be monolithic or independently
deployable.

**Problem:** Should components be bundled together or independently runnable?

---

## Decision

Each major component is an **independently deployable fat JAR** with its own main class:

| Component | Artifact | Communication |
|-----------|----------|---------------|
| **Server** | `robocode-tankroyale-server` | WebSocket server (port 7654) |
| **GUI** | `robocode-tankroyale-gui` | WebSocket client (Observer + Controller) |
| **Booter** | `robocode-tankroyale-booter` | stdin/stdout (process manager) |
| **Recorder** | `robocode-tankroyale-recorder` | WebSocket client (Observer) |

**Deployment modes:**

- **Standalone:** Each component runs as its own JVM process
- **Embedded:** GUI bundles Server, Booter, and Recorder JARs and can load them in-process

**Key choices:** Booter uses stdin/stdout (not WebSocket) — it's a process manager. Bots are separate OS processes
spawned by Booter, each connecting independently to the Server. All components share the same build pipeline:
Shadow JAR → R8 shrinking → native installers (jpackage).

---

## Rationale

- ✅ Components deployable independently (headless server, remote GUI, standalone recorder)
- ✅ Same artifacts work embedded in GUI or standalone
- ✅ Booter isolation — process management separate from game protocol
- ✅ Recorder is just an Observer — no special server support needed
- ❌ Multiple JVM processes in standalone mode
- ❌ GUI must bundle JAR copies for embedded mode

---

## References

- [Server](/server/README.md), [GUI](/gui/README.md), [Booter](/booter/README.md), [Recorder](/recorder/README.md)
- [Container diagram](/docs/architecture/c4-views/container.md)
