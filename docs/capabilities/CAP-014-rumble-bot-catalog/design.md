---
id: CAP-014-design
type: design
status: draft
links: [CAP-014, ARCH-022, P-003]
title: Design notes for CAP-014 (rumble-bot-catalog)
provenance: inferred
reversal-cost: low
---

# CAP-014 design

The external `rumble-bots` repository is the authoritative source of reviewed bot source, while this corpus records its capability and contract. Its repository layout, validation policy, ownership model, license policy, governance, and source-run smoke checks are defined by [ARCH-022](../../design/rumble/bot-submission.md).

## Catalog contract

`bots/index.json` is generated after accepted changes and is never edited by contributors. Its top-level object contains a positive integer `schemaVersion`, an ISO-8601 `generatedAt` timestamp, the immutable generating Git `commit`, and a `bots` array.

Each bot entry contains `name`, `version`, `platform`, `path`, `sourceHash`, `owner`, `authors`, `addedAt`, and `status`. It also contains `teamMembers`, an immutable ordered list of catalog identities written as `name version`; the list is empty for an individual bot and contains exactly two active individual identities for a TwinDuel team. A member identity may occur twice because Battle Runner preserves repeated team slots. `sourceHash` uses the `sha256:<hex>` form and covers the entry's source tree, while the member identities let clients obtain and verify every source tree the team executes. Only `status: active` entries are eligible for matchmaking; `superseded`, `retired`, and `disqualified` entries remain historical metadata.

The catalog schema is additive within a schema version: readers ignore fields they do not understand, while a breaking structural change requires a new `schemaVersion`. Readers treat an absent `teamMembers` field as an empty list for compatibility. A client verifies every selected entry's source-tree hash after obtaining the cataloged source and must reject a mismatch.

## External evidence

The external `robocode-dev/rumble-bots` repository holds the implementation and focused integration tests for RBC-001 through RBC-003 at merged commit `c735e6ff4`. [rumble-bots#3](https://github.com/robocode-dev/rumble-bots/pull/3) at `d58e6b5` adds RBC-004 positive and negative evidence for TwinDuel membership, including rejection of malformed catalog entries. Run `python -m unittest discover -s tests -v` and `scripts/validate_bot.py --root . --owner flemming-n-larsen --smoke` in that repository to reproduce the evidence. During P-001/M-002, those tests will receive their RBC purpose tags and this criteria artifact can become active without changing the criterion meanings.
