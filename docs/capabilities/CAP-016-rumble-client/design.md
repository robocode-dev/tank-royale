---
id: CAP-016-design
type: design
status: draft
links: [CAP-016, CAP-001, CAP-014, CAP-015, ARCH-023, P-003]
title: Design notes for CAP-016 (rumble-client)
provenance: inferred
reversal-cost: low
---

# CAP-016 design

The external `robocode-dev/rumble-client` repository owns the executable client, while this corpus owns its durable capability and behavioral contract. [ARCH-023](../../design/rumble/client-battles-and-results.md) describes the broader design direction; this document narrows it to the contracts accepted for M-008.

## Configuration and synchronized snapshot

One versioned configuration selects `ranked` or `practice`, the initial bot and data repository locations, a stable client ID for ranked mode, optional own-bot scheduling hints, selected V1 game types, a positive battle count, and local cache, journal, and evidence locations. Configuration validation completes before network, battle, journal, or submission side effects.

A ranked session begins from the configured data repository's `wellknown/rumble.json` and follows its canonical location. From one data-repository revision it reads `engine.json`, `catalog.json`, `clients/<forge-account>.json`, and `matchmaking/matches_needed-<game-type>.json` for each selected game type. The client rejects unknown schema versions, an unregistered client ID, unsupported game types, inconsistent projections, or a catalog whose declared source commit cannot identify the reviewed source in [CAP-014](../CAP-014-rumble-bot-catalog/README.md).

The accepted snapshot pins all ranked work in the session. Cached bot source is addressed by the catalog's immutable `sourceCommit` and each entry's `sourceHash`; a hash disagreement rejects the bot before execution. Matchmaking files are advice rather than reservations, so seeded selection may safely fall back to other active catalog participants while preserving the engine pin's participant count.

## Execution and mode boundary

Battle Runner [CAP-001](../CAP-001-battle-runner/README.md) owns server lifecycle, bot processes, identity matching, full-round execution, and `BattleResults`. The client owns synchronization, source preparation, selection, result transcription, replay retention, journaling, and submission.

Ranked mode accepts only active pinned catalog sources and requires the running server's advertised `behaviorVersion` to equal `engine.json`. Practice mode may use local unpublished sources and a different engine, but it has no path to the ranked journal or submission transport. This separation is part of the trust boundary, not a presentation option.

## Result, evidence, and journal contracts

After a full ranked battle completes, the client creates the V1 envelope record consumed by [CAP-015](../CAP-015-rumble-result-data/README.md): a UUID battle ID, ISO-8601 completion time, matching client identity and version, pinned behavior version, game type and dimensions, and the complete Battle Runner participant result model. The client stores the replay as local evidence addressed by the battle ID and records its SHA-256 hash locally; replay files are not submitted to `rumble-data` in V1.

The append-only local journal is the durability boundary between execution and transport. A completed ranked record is durable before submission begins. Acknowledgement removes only records covered by a successful ingestion receipt; interruption, partial rejection, authentication failure, and rate limiting preserve the remaining queue for retry. A later engine-pin change quarantines queued records from another behavior-version epoch instead of submitting incompatible facts.

## Submission and credential boundary

V1 uses the issue-ops contract implemented by `robocode-dev/rumble-data`: one `[result]` issue carrying one fenced JSON envelope of one to sixty results and the `result-submission` label. The submitting forge account and client ID must be registered. The credential is limited to Issues access for that repository and must not grant repository-content or Git-history write access.

Fork-pull-request submission remains a portability option only after the result-data capability publishes and validates that transport. `rumble-data` explicitly does not support it in V1, so M-008 does not claim it as an available fallback.

## Runtime boundary

The primary distribution is a rebuildable container containing the pinned Tank Royale engine, Battle Runner, and the Java, .NET, Python, and Node.js runtimes required by the catalog. Client network egress is limited to repository synchronization and issue submission; bot processes receive only the local server connection. Documented bare-metal setup remains available with an explicit warning that it does not provide the container boundary.

## External evidence

The external repository records focused evidence for RCL-001 through RCL-009. The accepted configuration implementation at `robocode-dev/rumble-client` commit `d19a90e99649f87664785f1dc20b5aa2f42da7d6` is the initial RCL-001 evidence; later CH-012 implementation commits must add the remaining evidence before M-008 can complete. During P-001/M-002, those tests will receive their RCL purpose tags and this criteria artifact can become active without changing the criterion meanings.
