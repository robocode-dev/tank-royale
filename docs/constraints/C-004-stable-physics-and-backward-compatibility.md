---
id: C-004
type: constraint
status: active
links: []
title: Game physics and bot backward compatibility are stable
source: docs/goals/G-001-programming-game-for-learning-and-competition.md, docs/decisions/0008-server-authoritative-physics.md
enforcement: human
provenance: inferred
reversal-cost: high
---

# C-004 — Game physics and bot backward compatibility are stable

- Core game rules and physics must remain stable: no breaking changes to how the game plays.
- Changes must not break existing bots — bots people wrote against released Bot APIs keep working.
- The server is authoritative (ADR-0008); no alternative drop-in server replacements.

**Promotion trigger:** a physics/behavior regression suite pinned against recorded battles — then `enforcement: machine` for the covered surface.

**Residual:** Compatibility judgments outside the regression suite's covered behavior remain maintainer-reviewed and human-enforced.
