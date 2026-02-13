# C4 Views — System Architecture Diagrams


This directory contains C4 Model architecture diagrams for Robocode Tank Royale at multiple levels of abstraction, including SVG visualizations generated from Structurizr DSL.

## What is C4?

The [C4 Model](https://c4model.com/) provides a hierarchical way to visualize software architecture at four levels:

1. **Level 1: System Context** — The big picture (10,000-foot view)
2. **Level 2: Container** — High-level structure (1,000-foot view)
3. **Level 3: Component** — Internal details (100-foot view)
4. **Level 4: Code** — Implementation details (10-foot view)

Think of it like Google Maps: zoom out to see the country, zoom in to see streets.

## Diagrams in This Directory

| Level | View | Purpose |
|-------|------|---------|
| **L1** | [System Context](./system-context.md) | Big picture: users, system, external systems |
| **L2** | [Container](./container.md) | Major containers: Server, GUI, Booter, Recorder, Bot APIs |
| **L3** | [Bot API Components](./bot-api-components.md) | Bot API structure (BaseBot, handlers, state management) |
| **L3** | [Server Components](./server-components.md) | Internal server architecture (game loop, physics, events) |
| **L3** | [GUI Components](./gui-components.md) | GUI internals (battle arena, controls, replay viewer) |
| **L3** | [Booter Components](./booter-components.md) | Booter internals (CLI, process manager, bot model) |
| **L3** | [Recorder Components](./recorder-components.md) | Recorder internals (observer, ND-JSON writer) |

---

## Related Documentation

- **[ADRs](../adr/)** — Why architectural decisions were made
- **[Message Schema](../models/message-schema/)** — WebSocket message contracts
- **[Business Flows](../models/flows/)** — Process documentation
- **[OpenSpec](../../../openspec/)** — Detailed specifications

---

## Updating Diagrams

To regenerate diagrams after modifying C4 DSL:

```bash
# Run the generation script with DSL content
bash .github/skills/structurizr/generate.sh 'workspace { ... }'

# Or on Windows:
.\github\structurizr\generate.ps1 'workspace { ... }'
```

The script will:
1. Process C4 DSL with Structurizr CLI
2. Convert to PlantUML
3. Generate SVG files to `images/`

See `.github/skills/structurizr/README.md` for detailed instructions.
