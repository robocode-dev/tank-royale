# ADR-0005: Independent Deployable Components

**Status:** Accepted  
**Date:** 2026-02-14 (Documenting historical decision)

---

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

- [Server](/server/), [GUI](/gui/), [Booter](/booter/), [Recorder](/recorder/)
- [Container diagram](/docs-internal/architecture/c4-views/container.md)
