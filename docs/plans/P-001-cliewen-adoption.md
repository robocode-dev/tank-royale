---
id: P-001
type: plan
status: active
links: [G-001]
title: Cliewen adoption
provenance: inferred
---

# P-001 — Cliewen adoption

Adopt the Cliewen corpus conventions as Tank Royale's system-of-record and retire the parallel truth carriers (OpenSpec, hand-enforced agent gates), so that every change runs one machine-judged loop. Serves G-001 indirectly: a single system-of-record is what keeps a solo-maintained, four-platform product coherent.

| ID | Milestone | Exit criterion | Status | Evidence |
|---|---|---|---|---|
| M-001 | Corpus adopted | `clue validate` green on `main`; `openspec/` gone; CI `validate` job armed with the vendored 0.3.0 release binary | done | CH-001 PR (this merge); AN-001 |
| M-002 | Tests declare purpose | Every test in every language carries exactly one purpose tag (AC ID or Unit/Sanity/Arch), enforced by an arch test per platform | todo | |
| M-003 | Public clue pin | cliewen repo is public; pin upgraded to v0.4.0; vendored binary replaced by direct release download in CI | todo | |

M-002 is the large named door this extraction leaves open: the Java/C#/Python/TypeScript suites predate AC IDs, and no scenario IDs existed in OpenSpec to inherit — tags are minted capability by capability, not in one big-bang change. Mechanism: extracted `criteria.md` files sit at `status: draft` (exempt from the AC↔test wall); wiring a capability's tests promotes its criteria to `active`, which arms enforcement for that prefix. M-003 is blocked on the upstream cliewen goes-public campaign (its P-003).
