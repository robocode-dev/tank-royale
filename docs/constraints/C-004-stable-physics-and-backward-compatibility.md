---
id: C-004
type: constraint
status: active
links: []
title: Game physics and bot backward compatibility are stable
source: openspec/project.md "Stability Rules (Non-negotiable)" (absorbed at CH-001), ADR-0008
enforcement: human
provenance: inferred
---

# C-004 — Game physics and bot backward compatibility are stable

- Core game rules and physics must remain stable: no breaking changes to how the game plays.
- Changes must not break existing bots — bots people wrote against released Bot APIs keep working.
- The server is authoritative (ADR-0008); no alternative drop-in server replacements.

**Promotion trigger:** a physics/behavior regression suite pinned against recorded battles — then `enforcement: machine` for the covered surface.
