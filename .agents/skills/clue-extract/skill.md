---
version: 0.3.0
---

# clue-extract

Brownfield adoption: transform an existing repository's spec corpus into a Cliewen `/docs` corpus. Use once per adopted repo; the run is that repo's first change loop (`clue-delta` applies — branch, proposal, digest, PR).

## Target contract (source-independent)

The extraction PR is complete only when all of these hold:

1. **The full taxonomy exists**: `/docs` with goals, plans, capabilities (README / criteria / design per folder), decisions, constraints, quality, analysis, architecture — every folder with an indexed README. Extract meaning, don't invent it: a folder with nothing real to hold stays empty-but-indexed.
2. **Everything extracted is born `provenance: inferred`**. Decisions instead use `status: inferred` with `author: agent`. Human review of the PR promotes to `verified` — file-by-file or in bulk; never pre-promote.
3. **Existing AC IDs survive**: declare each capability's namespace with `ac-prefix:` in its criteria.md and keep the source's IDs verbatim. Renumbering is forbidden — IDs are meaning-immutable and existing test tags must keep resolving.
4. **Every test keeps or gains exactly one purpose**: tests already tagged with an AC ID are done; tests without one get `Unit`, `Sanity` or `Arch` per their actual intent. Where a JVM test framework is present, install an ArchUnit (or equivalent) rule enforcing one purpose tag per test — clue only harvests at file level.
5. **`clue validate` is green** against the repo root before the PR opens. The extracted corpus is judged by exactly the same rules as a greenfield one.
6. **The source corpus dies in the same PR**: spec trees, parallel registries and source-format skills are deleted — git history is their archive. Two systems of record is zero systems of record.
7. **Routing is rewritten and reconciled**: the repo's AGENTS.md points agents at `/docs/README.md` and the `clue-*` skills; the Cliewen skills (`clue-analysis`, `clue-plan`, `clue-delta`, `clue-verify`, `clue-extract`) are installed under `.agents/skills/`. Pre-existing agent instructions are reconciled, not copied: rules compatible with the methodology are absorbed as repo-local conventions in the rewritten AGENTS.md; rules that conflict with a skill (e.g. a PR workflow that bypasses the change loop) go to `open-questions.md` — AGENTS.md extends the methodology, never overrides it, and a conflict must not survive into the rewritten file unanswered.
8. **An extraction report lands in `/docs/analysis`** (AN-xxx): what was found, what mapped where, what was dropped and why — analysis must leave corpses.
9. **Unsolved adoption items become named doors in the repo's plan** (e.g. clue binary distribution for CI), not silent gaps.

## Source mappings

Per-source mappings live in this skill's `mappings/` folder (first: [openspec.md](mappings/openspec.md)). The target contract above governs every extraction; a mapping file only says what maps where for one source format. A new source format is a new mapping file, never a new skill (a per-source skill would duplicate this contract and drift from it) — if no mapping exists for the source at hand, writing one is the first task of the extraction PR.

## What this skill never does

Invent requirements the source doesn't state; renumber or rename IDs; leave the source corpus alive "for reference"; promote its own output to `verified`; touch test code beyond adding missing purpose tags and the purpose-enforcement rule.
