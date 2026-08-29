---
id: CH-029
type: change
status: open
links: []
title: Upgrade Cliewen carriers and CI to clue 0.20.0
---

# CH-029 — Upgrade Cliewen carriers and CI to clue 0.20.0

## What

Upgrade the repository's managed Cliewen skills and thin CI caller from their 0.16.0 baseline to the 0.20.0 release, while reconciling the legacy decision-log carrier that blocks the supported migration.

## Why

The installed `clue` command was initially resolving to a development build, and the repository's generated skills and `.github/workflows/clue.yml` still carried 0.16.0. The 0.20.0 release separates release freshness from local carrier currency and requires repositories with a legacy `docs/decisions/log.md` to resolve each row in a reviewed change before migration can write.

## Route

Recommended route: full. The mechanical carrier upgrade is simple work under the 0.20.0 workflow, but this repository must make its own durable classification decisions for the fourteen legacy log rows; that semantic discovery escalates this change to the full loop. Discovery would change the recommendation only if the log were removed from scope or every row were proven to be routine history already represented by an existing typed record.

## Plan

This change is plan-less. It serves no milestone in P-001, P-002, or P-003; it keeps the repository's Cliewen adoption current.

## Scope

- Install and use the signed `clue` 0.20.0 Windows AMD64 release binary.
- Replace the managed `.agents/skills/clue-*` carriers with the 0.20.0 generated content; `.claude/skills` remains the repository's existing symlink and is not written through.
- Update the thin caller in `.github/workflows/clue.yml` to the 0.20.0 release and its pinned upstream workflow revision.
- Classify every legacy decision-log row, preserve future-shaping choices in typed records or their existing authoritative carriers, account for routine history and the CH-027 ledger repair, repair live references, and remove `docs/decisions/log.md` and its index/ledger entries.
- Run the relevant corpus, migration, formatting, and repository checks.

## Legacy row dispositions

The seven methodology and process choices about C-002, release verification, Rumble sequencing, criteria lifecycle, ADR identity retention, index ownership, design typing, and the empty BSB extraction will become PDR records. The two TypeScript publishing choices will become IDR records. The `npmPublishDryRun` retirement will become an IDR record. The CH-027 counter repair is repair narrative whose durable carrier is `.clue/id-ledger.yaml` and whose detailed history remains in Git; it does not create a future-shaping decision record. The plan, capability, and constraint files already carry the resulting accepted meaning and will be repaired to point at typed records where needed.

## Non-goals

This change does not alter Tank Royale runtime behavior, protocol schemas, Bot API behavior, acceptance criteria, release artifacts, or the human merge boundary. It does not write through the `.claude/skills` symlink and does not add a changelog entry because the repository-local changelog rules exclude internal tooling and CI changes with no bot-developer-visible effect.

## Compatibility

The change updates agent guidance, corpus decision-record routing, and CI validation tooling only. Existing product code and user-facing behavior remain unchanged.
