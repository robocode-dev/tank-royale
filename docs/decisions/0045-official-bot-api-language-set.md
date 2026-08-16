---
id: ADR-0045
type: decision
status: verified
links: [ADR-0003, ADR-0038, C-003]
title: Official Bot API Language Set Is Closed
author: agent
accepted-by: Flemming N. Larsen (2026-08-16, Claude Code conversation)
---

# ADR-0045: Official Bot API Language Set Is Closed

## Context

ADR-0003 names the languages the project implements Bot APIs for, and C-003 requires every official Bot API to be 1:1 semantically equivalent to the Java reference. Neither states which languages qualify to become official, nor what happens to a Bot API written for a language outside the set.

Requests to add languages arrive periodically, and the implementations behind them can be complete and faithful. Without a written rule each request is answered from memory, which makes consistent answers look improvised to the person receiving them and gives a contributor nothing to evaluate their own proposal against before investing effort.

An official Bot API is not a one-time contribution but a permanent multiplier on every subsequent change:

- Every acceptance criterion in `bot-api/tests/TEST-REGISTRY.md` must be green on the platform in both directions, per ADR-0038.
- The platform needs its own Tier-2 test harness, because intent-capture is platform-specific.
- Each platform adds a package registry to the release verification gate, an API reference, a tutorial, a sample-bot set, and continuous integration jobs.
- Every protocol or reference-API change becomes an N-way change that cannot ship until the slowest platform lands.

That cost is borne by the project in perpetuity, while the contribution that triggers it is complete at merge. The asymmetry, not the merit of any implementation, is what the decision turns on.

## Decision

The set of official Bot API languages is closed at Java, C#, Python, and TypeScript.

Bot APIs for other languages are welcome as **community Bot APIs**: independent projects in their authors' own namespaces, maintained beside official Tank Royale rather than by it. The project lists them in its API documentation, in a section separate from the official APIs and labelled as community-maintained, so that a reader can tell at a glance which guarantees apply. Adoption into the official set requires reopening this decision.

Reopening requires all of:

1. **Audience reach.** The language sits in the top tier of general-purpose adoption, judged by the TIOBE index. Java's place in the set is Robocode heritage, not rank.
2. **Runs from source.** A bot is runnable from its source without an author-built binary artifact, as with `java Bot.java`, `dotnet run bot.cs`, a Python module, or a TypeScript entry point.
3. **Demonstrated parity.** Every acceptance criterion in `bot-api/tests/TEST-REGISTRY.md` passes on the candidate platform in both directions, including its own Tier-2 harness, satisfying C-003.

Once adopted, an official Bot API becomes the responsibility of the current and future maintainers of this repository, like every existing official Bot API. The contributing author has no continuing maintenance obligation, and adoption does not require a language-specific co-maintainer.

Implementation quality and 1:1 faithfulness are necessary but never sufficient. A community Bot API may be excellent and still remain in the community tier, because what is being weighed is permanent maintenance and release coupling rather than the merit of a contribution.

A community Bot API is not hosted as a branch of this repository. A long-lived unmerged branch rots against `main` and implies an adoption this decision excludes.

## Rationale

Audience reach is the criterion that decides nearly every case, and it is the honest one: the project supports the languages where bot authors actually are, so that a permanent cost buys a proportionate audience. A niche language fails this regardless of how good its Bot API is, and saying so plainly is fairer than implying a quality judgement.

Running from source keeps a bot distributable and reviewable as text, which is what the booter convention and source-first bot distribution already assume.

Demonstrated parity addresses silent semantic drift at adoption. After adoption, the repository maintainers own parity, packaging, documentation, tests, and release support for the platform in perpetuity; continued involvement from the contributing author is neither assumed nor required.

Publishing the criteria rather than only the outcome lets a contributor evaluate a proposal before building it, and lets the maintainer answer with a rule rather than a judgement of their work.

## Consequences

- Requests to add an official language have a single documented answer that does not depend on who asks or when.
- Contributors can assess a proposal's chances before investing effort.
- Bot APIs for other languages have a supported home rather than being turned away, and remain adoptable later if the criteria are met.
- Users retain the guarantee that every API the project presents as official is 1:1 with the Java reference and maintained by the project, because the documentation states which tier an entry belongs to instead of leaving readers to infer it.
- Some good implementations stay outside the official set. This is accepted deliberately.
- Reopening the set is a decision change requiring a new record, not a maintainer's discretionary call.

## References

- [ADR-0003 — Cross-Platform Bot API Strategy](0003-cross-platform-bot-api-strategy.md)
- [ADR-0038 — Shared Cross-Platform Test Definitions](0038-shared-cross-platform-test-definitions.md)
- [C-003 — Bot APIs are semantically identical across platforms](../constraints/C-003-cross-platform-bot-api-parity.md)
- [Bot API test registry](../../bot-api/tests/TEST-REGISTRY.md)
- [Contributing Guide](../../CONTRIBUTING.md)
