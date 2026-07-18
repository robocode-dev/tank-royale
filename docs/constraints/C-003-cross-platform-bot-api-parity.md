---
id: C-003
type: constraint
status: active
links: []
title: Bot APIs are semantically identical across platforms; Java is the reference
source: openspec/project.md "Bot API Rules (Non-negotiable)" (absorbed at CH-001), ADR-0003, ADR-0004
enforcement: machine
provenance: inferred
---

# C-003 — Bot APIs are semantically identical across platforms; Java is the reference

All official Bot APIs (.NET, Python, TypeScript) must be 1:1 semantically equivalent to the Java Bot API — the reference implementation and, when in doubt, the authoritative behavior:

- Same defaults, validation rules, and error handling across platforms.
- Events fire in the same order on every platform.
- No extra and no missing public API methods; platform idioms (properties instead of getters/setters) are allowed, semantics are not negotiable.

Rationale: maintainability, testing, documentation, and learning — a bot author switching language must not relearn behavior.

**Enforcement:** the shared cross-platform test definitions (`bot-api/tests`, ADR-0038) hold the overlap they cover; API surface and semantics beyond them are held by maintainer review (so partly `human` until coverage closes the gap).
