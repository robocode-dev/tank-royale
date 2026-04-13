# Debugging and Bug Hunting

<!-- KEYWORDS: debug, bug, reproduce, Battle Runner, protocol -->

## Tools for Reproducing Bot-Side Bugs

### Battle Runner (`/runner`)

The Battle Runner module starts a full headless game (server + bots) from the command line — no GUI needed.
Use it to reproduce timing-sensitive bugs quickly and consistently.

Build & run:
```bash
./gradlew :runner:build
./gradlew :server:bootRun
```

### Minimal test bots (`bot-api/tests/bots/`)

Minimal bots (Java, C#, Python) live in `bot-api/tests/bots/`. Connect any of them to a running
server to reproduce bot-API bugs in isolation without needing sample bots or the GUI.

---

## Protocol Sequence Reference

For message ordering and timing guarantees, always consult:

```
docs-internal/architecture/models/flows/README.md   ← Index of all protocol flow diagrams
```

Key flows: `bot-connection`, `battle-lifecycle`, `turn-execution`, `event-handling`.
