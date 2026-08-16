---
id: CH-028
type: change
status: open
links: []
title: Close the official Bot API language set and define the community Bot API tier
---

# CH-028 — Close the official Bot API language set and define the community Bot API tier

## What

Record that the set of official Bot API languages is closed at Java, C#, Python, and TypeScript, publish the criteria that any future reopening must clear, and define the community Bot API tier as the supported home for Bot APIs in other languages. Carry that policy into the contributor-facing surfaces that people actually read before asking: `CONTRIBUTING.md` and the README's community section.

## Why

The corpus does not answer the question contributors are asking. ADR-0003 names the four supported languages but states no criterion for adding a fifth and says nothing about community-maintained APIs. C-003 governs parity *among* official APIs but is silent on which languages qualify to be official. `CONTRIBUTING.md` carries two sentences under "Alternative Bot APIs" and no adoption process.

The gap has been filled ad hoc twice, with a declined Lua request and a Nim Bot API now offered for adoption. Deciding each case in conversation produces answers that look arbitrary to the person receiving them and gives the maintainer no written rule to point at. Writing the policy down converts a series of individual refusals into one consistent, checkable standard that also tells a contributor what the supported alternative is.

Every official language is a permanent multiplier, not a one-time contribution: each acceptance criterion in `bot-api/tests/TEST-REGISTRY.md` must be green on every platform in both directions, each platform carries its own Tier-2 `MockedServer` and `AbstractBotTest` harness, each adds a publish gate to `verify-publish.yml`, an API reference, a tutorial, a sample-bot set, and a CI job, and each turns every future protocol or reference-API change into an N-way change that blocks the release until the slowest platform lands. For a solo-maintained project that cost, not the quality of any submission, is the deciding factor.

## Scope

- Add ADR-0045 recording that the official Bot API language set is closed, the criteria any reopening must clear, and the community Bot API tier with adoption possible later at the maintainer's discretion.
- Record the Lua and Nim declines as dated rows in `docs/decisions/log.md` citing ADR-0045, so the audit trail shows both were decided by the same rule.
- Expand `CONTRIBUTING.md` "Regarding Bot APIs" with a "Contributing a Bot API" section covering the two tiers, the reopening criteria, and what a community Bot API author is expected to state.
- Extend the README community section so community Bot APIs are listed alongside the Tank Royale Viewer.
- Update `docs/decisions/README.md` and any affected corpus indexes.

## Non-goals

- Changing C-003 parity semantics, ADR-0038 shared test definitions, or any acceptance criterion. This change records policy; it alters no runtime or test behavior.
- Adopting, rejecting, or reviewing any specific third-party Bot API implementation as an engineering artifact.
- Adding a Community Bot APIs section to `robocode.dev/api/apis.html` (see open question 1).
- Restating the policy as a general rule for all community projects such as viewers, booters, GUIs, or the API bridge. ADR-0045 covers Bot APIs; the broader pattern stays informal.
- Any change to the Rumble. The Rumble's source-only catalog does not constrain which languages can be official, and naming it here would attach the policy to a rationale that does not hold.

## Compatibility

Documentation and decision records only. No code, schema, protocol, or test behavior changes, and no effect on existing bots.

## Plan

Plan-less. This change serves no milestone in P-001, P-002, or P-003; it closes a governance gap in the corpus that surfaced through contributor questions.

## Decision content proposed for ADR-0045

**Decision.** The set of official Bot API languages is closed at Java, C#, Python, and TypeScript. Bot APIs for other languages are welcome as community projects in their authors' own repositories, linked from this project and adoptable later only by reopening this decision.

**Criteria any reopening must clear, all of them:**

1. **Audience reach.** The language must be in the top tier of general-purpose adoption, judged by the TIOBE index. Java's place is heritage, not rank. This is the criterion that a niche language fails, however good its implementation.
2. **Runs from source.** A bot must be runnable from its source without an author-built binary artifact, in the manner of `java Bot.java`, `dotnet run bot.cs`, a Python module, or a TypeScript entry point.
3. **Demonstrated parity.** Every acceptance criterion in `bot-api/tests/TEST-REGISTRY.md` green on the candidate platform in both directions, including a platform Tier-2 harness, per C-003 and ADR-0038.
4. **Named second maintainer.** A committed maintainer other than the project maintainer, who accepts that the platform blocks releases when it lags.

**Community tier.** A community Bot API lives in its author's own namespace under their ownership, release cadence, and documentation. The project may link it, labelled as community-maintained and naming its maintainer and target schema version. `robocode.dev/api/apis.html` continues to list only official APIs, so that page keeps meaning "1:1 with the Java reference and maintained by this project". Precedent already exists in the Tank Royale Viewer, linked from the README while remaining Jan Durovec's project, adoptable later if its author ever wishes.

**Explicitly not a criterion.** Implementation quality, 1:1 faithfulness, and author commitment are not sufficient grounds for adoption. A community Bot API may be excellent and still stay in the community tier, because the cost being weighed is permanent maintenance and release coupling, not the merit of a contribution.
