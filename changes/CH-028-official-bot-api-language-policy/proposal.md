---
id: CH-028
type: change
status: open
links: []
title: Close the official Bot API language set and define the community Bot API tier
---

# CH-028 — Close the official Bot API language set and define the community Bot API tier

## What

Record that the set of official Bot API languages is closed at Java, C#, Python, and TypeScript, publish the criteria that any future reopening must clear, and define the community Bot API tier as the supported home for Bot APIs in other languages. Carry that policy into the contributor-facing surfaces people actually read before asking: `CONTRIBUTING.md`, the README, and the published API documentation.

## Why

The corpus does not answer the question contributors are asking. ADR-0003 names the four supported languages but states no criterion for adding a fifth and says nothing about community-maintained APIs. C-003 governs parity *among* official APIs but is silent on which languages qualify to be official. `CONTRIBUTING.md` carries two sentences under "Alternative Bot APIs" and no adoption process.

The gap has been filled ad hoc twice. [Issue 198](https://github.com/robocode-dev/tank-royale/issues/198) asked for Lua and was answered in April 2026 with the community-tier position in all but name: an independent project maintained alongside Tank Royale is welcome, adoption into the main line is not offered. That issue was deliberately left open. [Discussion 239](https://github.com/robocode-dev/tank-royale/discussions/239) now offers a Nim Bot API for adoption and asks what the integration process is. Both get the same answer, given from memory rather than from a written rule, which makes a consistent position look improvised to the person receiving it and leaves the maintainer nothing to point at. This change records a position that already exists rather than inventing one.

Every official language is a permanent multiplier, not a one-time contribution: each acceptance criterion in `bot-api/tests/TEST-REGISTRY.md` must be green on every platform in both directions, each platform carries its own Tier-2 `MockedServer` and `AbstractBotTest` harness, each adds a publish gate to `verify-publish.yml`, an API reference, a tutorial, a sample-bot set, and a CI job, and each turns every future protocol or reference-API change into an N-way change that blocks the release until the slowest platform lands. For a solo-maintained project that cost, not the quality of any submission, is the deciding factor.

## Scope

- Add ADR-0045 recording that the official Bot API language set is closed, the criteria any reopening must clear, and the community Bot API tier.
- Expand `CONTRIBUTING.md` "Regarding Bot APIs" into a "Contributing a Bot API" section covering both tiers, the reopening criteria, and how a community Bot API gets listed.
- Split `web/docs/api/apis.md` into official and community sections, stating that official APIs are maintained by this project and 1:1 with the Java reference, and that community entries are maintained beside official Tank Royale rather than by it.
- State the same official/community split in the README's Bot API listing.
- Register ADR-0045 in the id ledger and add it to `docs/decisions/README.md`.
- Reconcile [issue 198](https://github.com/robocode-dev/tank-royale/issues/198) with the merged policy.

## Non-goals

- Changing C-003 parity semantics, ADR-0038 shared test definitions, or any acceptance criterion. This change records policy; it alters no runtime or test behavior.
- Adopting, rejecting, or reviewing any specific third-party Bot API implementation as an engineering artifact.
- Recording the Lua and Nim cases as instances in the corpus. A decision record is timeless, and a hand-maintained instance list goes stale; the dated public record lives on the issue and the discussion themselves.
- Restating the policy as a general rule for all community projects such as viewers, booters, GUIs, or the API bridge. ADR-0045 covers Bot APIs; the broader pattern stays informal.
- Any change to the Rumble. The Rumble's source-only catalog does not constrain which languages can be official, and naming it here would attach the policy to a rationale that does not hold: a bot in a compiled language can be submitted as source and built on the fly, so the Rumble excludes nothing.

## Compatibility

Documentation and decision records only. No code, schema, protocol, or test behavior changes, and no effect on existing bots. The `apis.md` restructure moves the four language sections from `##` to `###` under an "Official Bot APIs" heading; anchors are derived from heading text, so the existing `#java-jvm`, `#net`, and `#typescript--javascript` links continue to resolve.

## Plan

This change is plan-less. It serves no milestone in P-001, P-002, or P-003; it closes a governance gap in the corpus that surfaced through contributor questions.

## Decision content recorded in ADR-0045

**Decision.** The set of official Bot API languages is closed at Java, C#, Python, and TypeScript. Bot APIs for other languages are welcome as community Bot APIs: independent projects in their authors' own namespaces, maintained beside official Tank Royale rather than by it, linked from the project and adoptable later only by reopening this decision.

**Criteria any reopening must clear, all of them:**

1. **Audience reach.** The language sits in the top tier of general-purpose adoption, judged by the TIOBE index. Java's place is Robocode heritage, not rank. This is the criterion a niche language fails, however good its implementation.
2. **Runs from source.** A bot is runnable from its source without an author-built binary artifact, as with `java Bot.java`, `dotnet run bot.cs`, a Python module, or a TypeScript entry point.
3. **Demonstrated parity.** Every acceptance criterion in `bot-api/tests/TEST-REGISTRY.md` passing on the candidate platform in both directions, including its own Tier-2 harness, per C-003 and ADR-0038.
4. **A named second maintainer.** A committed maintainer besides the project maintainer, who accepts that the platform blocks releases when it lags.

**Community tier.** A community Bot API lives in its author's own namespace under their ownership, release cadence, and documentation. It is not hosted as a branch of this repository: a long-lived unmerged branch rots against `main` and implies an adoption this decision excludes. The project links it, labelled as community-maintained. Precedent already exists in the Tank Royale Viewer, linked from the README while remaining Jan Durovec's project.

**Explicitly not a criterion.** Implementation quality, 1:1 faithfulness, and author enthusiasm are necessary but never sufficient. A community Bot API may be excellent and still remain in the community tier, because what is being weighed is permanent maintenance and release coupling, not the merit of a contribution.
