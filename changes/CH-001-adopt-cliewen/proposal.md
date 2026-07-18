---
id: CH-001
type: change
status: open
links: [P-001, M-001]
title: Adopt Cliewen as the system-of-record methodology
---

# CH-001 — Adopt Cliewen

## What

Adopt the [Cliewen](https://github.com/cliewen/cliewen) corpus conventions as Tank Royale's system-of-record, executed as a brownfield extraction (`clue-extract` skill, clue 0.3.0):

- Materialize the corpus taxonomy under `docs/` (goals, plans, capabilities, constraints, quality, analysis) beside the existing `decisions/`, `architecture/` and `design/` folders.
- Extract the OpenSpec corpus (`openspec/specs/*`) into `docs/capabilities/CAP-xxx/` with namespaced acceptance criteria; the pending OpenSpec change becomes a draft capability plus a plan milestone. `openspec/` is deleted in this same change — two systems of record equals zero.
- Convert the 41 existing MADR ADRs and all architecture/design documents to clue frontmatter; identities (`ADR-0001`…`ADR-0041`) are preserved.
- Install the five clue skills (v0.3.0 pair), vendor the checksum-verified release binary under `.github/tools/`, and add the `validate` CI wall.
- Rewrite `AGENTS.md` as the corpus routing hub; retire the agent instructions Cliewen supersedes (OpenSpec workflow and its approval gate, the ADR Review Gate, the process rules of `core-principles.md`); keep product/build instructions and the `/dot-*` principles system as the repo-local layer.

## Why

The maintainer has run this methodology in the cliewen and model2diagram repositories and trusts it. Tank Royale currently splits its truth across OpenSpec, MADR ADRs, and a bespoke instruction system with hand-enforced approval gates; Cliewen replaces the process half with one machine-judged corpus (`clue validate` locally and in CI) while the product facts stay where they are.

This change serves plan item M-001 of P-001 (created in this change; the plan is seeded by the extraction, as the target contract prescribes).
