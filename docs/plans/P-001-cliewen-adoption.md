---
id: P-001
type: plan
status: active
links: [G-001]
title: Cliewen adoption
provenance: inferred
reversal-cost: low
---

# P-001 — Cliewen adoption

Adopt the Cliewen corpus conventions as Tank Royale's system-of-record and retire the parallel truth carriers (OpenSpec, hand-enforced agent gates), so that every change runs one machine-judged loop. Serves G-001 indirectly: a single system-of-record is what keeps a solo-maintained, four-platform product coherent.

| ID | Milestone | Exit criterion | Status | Evidence |
|---|---|---|---|---|
| M-001 | Corpus adopted | `clue validate` green on `main`; `openspec/` gone; CI `validate` job armed with the vendored 0.3.0 release binary | done | CH-001 PR (this merge); AN-001 |
| M-002 | Tests declare purpose | Every test in every language carries exactly one purpose tag (AC ID or Unit/Sanity/Arch), enforced by an arch test per platform | done | [CH-040 PR](https://github.com/robocode-dev/tank-royale/pull/268); platform architecture guards |
| M-003 | Public clue pin | cliewen repo is public; pin upgraded off v0.3.0; vendored binary replaced by direct release download in CI | done | CH-004 PR |

M-002 completed the purpose-tagging door across the Java/Kotlin, .NET, Python, and TypeScript suites. Existing acceptance IDs remain the most specific purpose; generic `Unit`, `Sanity`, and `Arch` purposes cover tests without a wired criterion, and the platform guards enforce the effective one-purpose rule. Extracted `criteria.md` files remain at `status: draft` until their tests are wired and the corresponding capability is ready for promotion to `active`.
