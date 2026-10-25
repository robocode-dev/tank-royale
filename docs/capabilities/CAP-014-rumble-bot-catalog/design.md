---
id: CAP-014-design
type: design
status: draft
links: [CAP-014, ARCH-022, P-003, ADR-051]
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

## Runnable submission starters

The bot repository's `templates/` directory sits outside the active `bots/` tree. Individual starters contain matching configuration and launcher names, an official API source entry point and pinned dependency declarations. Authors copy and rename those inputs; JSON-only TwinDuel starters contain two explicit active member identities and no source or launchers. Shell scripts are committed executable, including when prepared on Windows.

The [runtime-dependency decision](../../decisions/ADR-050-official-bot-api-runtime-dependencies.md) permits the API's declared runtime dependencies while preserving the exclusion of unrelated bot libraries. TypeScript's official API needs its declared `ws` peer. Its launcher restores the committed lockfile in an isolated temporary run directory and uses Node's native TypeScript support; Java caches its official JAR externally, C# restores through `dotnet run` with generated output outside the source tree, and Python uses the interpreter and API environment prepared by the author. C# disables the native app host so it can run through `dotnet` when the generated output resides on a `noexec` temporary filesystem.

The validator checks TypeScript dependency manifests, lockfile consistency, the approved peer names and official registry resolutions, and rejects generated dependency directories in submissions. This is a deterministic review gate, not a security sandbox or a general dependency resolver. Future changes to the API's runtime dependency set require reviewing and updating that gate. Local smoke checks load the entry point and API, emit the expected ready marker and exit. GitHub's separate moderation path runs the submitted bot against a trusted reference through an embedded server for a complete round, inside a restricted container without credentials; only a clean, bot-only PR can be auto-merged by the moderator App. The trusted publisher pushes a generated branch, a narrowly scoped Publisher App opens its PR, and a separate credential-free check regenerates the catalog from `main` and compares the exact files before the moderator App approves and merges that PR. Uncertain source, team metadata and non-bot changes go to the Tank Royale Rumble moderators. The App verifies active membership in the organization's moderator team before accepting a human decision on a pending head; it leaves the human merge and required review in place. The [moderation decision](../../decisions/ADR-051-automated-rumble-bot-moderation.md) owns the trust boundary. Criteria remain draft in this corpus until external executable evidence can be registered.

## External evidence

The [distinct-entry participation policy](../../decisions/PDR-017-distinct-rumble-entries.md) allows human moderators to reject cosmetic near-duplicates while admitting licensed derivatives with substantial strategic contributions. The [submission design](../../design/rumble/bot-submission.md#distinct-ranked-entries) owns the moderation procedure. This judgment is separate from the existing automatic checks and can be raised through a dispute after automatic acceptance. The Human-class criterion RBC-011 is proven by the acceptance brief rather than an automated similarity classifier.

The external `robocode-dev/rumble-bots` repository holds the implementation and focused integration tests for RBC-001 through RBC-003 at merged commit `c735e6ff4`. [rumble-bots#3](https://github.com/robocode-dev/rumble-bots/pull/3) at `d58e6b5` adds RBC-004 positive and negative evidence for TwinDuel membership. Merged [rumble-data#12](https://github.com/robocode-dev/rumble-data/pull/12) adds RBC-005 evidence that normalized unchanged polls are not rewritten or aggregated and that changed bot/version catalogs enter the serialized publication path. Run each external repository's Python test suite to reproduce the evidence; criteria remain draft here until the external evidence can be registered in this corpus.
