# structurizr-dsl/

Structurizr DSL source files for all C4 architecture diagrams. These are the authoritative sources; the SVG files in `../images/` are generated from these.

| File | Diagram |
|------|---------|
| [system-context.dsl](./system-context.dsl) | C4 Level 1 — System Context |
| [container.dsl](./container.dsl) | C4 Level 2 — Container |
| [server-components.dsl](./server-components.dsl) | C4 Level 3 — Server Components |
| [bot-api-components.dsl](./bot-api-components.dsl) | C4 Level 3 — Bot API Components |
| [gui-components.dsl](./gui-components.dsl) | C4 Level 3 — GUI Components |
| [booter-components.dsl](./booter-components.dsl) | C4 Level 3 — Booter Components |
| [recorder-components.dsl](./recorder-components.dsl) | C4 Level 3 — Recorder Components |

To regenerate SVGs from these sources, run `../images/regenerate-all-svgs.ps1`.
