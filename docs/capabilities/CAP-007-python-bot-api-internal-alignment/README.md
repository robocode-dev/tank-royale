---
id: CAP-007
type: capability
status: active
links: [G-001]
title: Python Bot API internal alignment
provenance: inferred
---

# CAP-007 — Python Bot API internal alignment

Defines the internal structure requirements for the Python Bot API, aligning it with the Java and
C# implementations. Specifically, this governs how `BotInternals` is organised as a standalone
module, how `BaseBotInternals` holds all mutable state directly (without a `BaseBotInternalData`
indirection layer), and the consequent clean-up of production code and tests.

Extracted from `openspec/specs/python-bot-api-internal-alignment/spec.md` at CH-001; the source spec's requirement prose is preserved as comments in [criteria.md](criteria.md).
