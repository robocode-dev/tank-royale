---
id: CH-028-open-questions
type: open-questions
status: open
links: [CH-028]
title: Open questions for CH-028
---

# CH-028 — Open questions

## 1. Does `robocode.dev/api/apis.html` list community Bot APIs?

Non-blocking. Answered by the maintainer during implementation.

The API page previously listed Java, C#, Python, TypeScript, and Battle Runner in one uniform structure, and that uniformity is itself the promise that everything on it is 1:1 and maintained by this project. Keeping it official-only preserves that promise and matches how the Tank Royale Viewer is handled today, in the README rather than in the product's own documentation surfaces. The cost is that someone hunting for an API on the documentation site will not find the community ones.

The alternative is a visually distinct "Community Bot APIs" block below the official entries, each naming its maintainer and target schema version. More discoverable, but it puts unofficial entries on the page users treat as authoritative, and it becomes a surface the maintainer must keep current as community projects come and go.

**Resolved:** list them, clearly separated. `web/docs/api/apis.md` gains an explicit split: the official APIs are stated to be maintained by this project and 1:1 with the Java reference, and a separate "Community Bot APIs" section states that its entries are maintained beside official Tank Royale, not by it.

The separation carries the promise that uniformity used to carry implicitly. Because no community Bot API has published a public repository yet, the section ships as a short explanation of the tier and how to get listed, rather than as an empty list; entries are added as they appear.

## 2. Are Lua and Nim named in ADR-0045, or only in `docs/decisions/log.md`?

Non-blocking. Recommended by the agent during implementation and confirmed by the maintainer.

Naming both somewhere is what demonstrates the policy was applied consistently rather than invented for one request. The question is only where. A `log.md` row is dated, factual, and reads as bookkeeping. An "Applied instances" section inside a permanent ADR names a contributor's project inside a rejection record and will outlive the conversation that produced it.

**Resolved:** generic ADR, and no `log.md` rows either.

The corpus convention decides the first half. A decision record is timeless: triggering incidents, chronology, and conversations belong in findings, the change workspace, the PR, and Git history. Lua and Nim are exactly the triggering incidents, so they stay out of ADR-0045.

The second half follows from what `log.md` is for. It records decisions that are cheap and local to reverse, but applying ADR-0045 to a request is not a new decision, it is execution of one. A hand-maintained instance list also goes stale the moment a third request arrives and nobody adds a row, which is worse than not keeping one. The dated public record already exists in the right place: the issue carries the `community project` label, and the discussion carries the answer citing the merged ADR.

## 3. How is [issue 198](https://github.com/robocode-dev/tank-royale/issues/198) (Lua) reconciled with the merged policy?

**Blocking for the digest, not for implementation.** The ADR can be written either way; the issue must be reconciled before this change is accepted, because leaving both as they are publishes a contradiction.

[Issue 198](https://github.com/robocode-dev/tank-royale/issues/198) is open, labelled `help wanted` and `huge effort`. In April 2026 the maintainer answered it with the community-tier position: an independent Lua project maintained alongside Tank Royale is welcome, the main line is not offered, and the issue was deliberately left open for someone to pick up. A merged ADR-0045 states the official set is closed. An open `help wanted` request to add an official language contradicts that.

Two ways out:

- **Close [issue 198](https://github.com/robocode-dev/tank-royale/issues/198)**, citing ADR-0045 and pointing at the community tier. Consistent and unambiguous, but it withdraws something the requester was told was still possible, and lands as a late reversal.
- **Keep [issue 198](https://github.com/robocode-dev/tank-royale/issues/198) open, reframed.** Drop `help wanted`, relabel toward "community Bot API welcome", and comment that the standing position is now written down. This keeps the April commitment intact, because that comment already *was* the community-tier answer; only the label implies official adoption.

**Resolved:** keep it open, reframed. The maintainer's April reply and ADR-0045 say the same thing; the labels were the only part that conflicted.

The relabelling is done: a new repository label `community project` ("Welcome as an independent project; not adopted into the official line") replaces `help wanted` and `huge effort` on [issue 198](https://github.com/robocode-dev/tank-royale/issues/198), which remains open. The label is reusable for any later request of the same shape, which puts the policy in the issue tracker rather than only in `docs/decisions/`.

The explanatory comment is deliberately not posted yet, because it cites ADR-0045 and that record does not exist until this change merges. It remains a task.

## 4. Does a contributor-facing policy change warrant a CHANGELOG entry?

Non-blocking; resolved during implementation against `.agents/instructions/changelog.md`.

**Resolved: no entry.** The instruction file is explicit that the changelog is written for bot developers and is "not an internal maintenance log for the maintainer", and it excludes tooling changes with no user-visible effect. This change ships a decision record and contributor documentation; no bot developer observes a behavior difference. The generic full-change convention yields to the repository-local instruction, which is the more specific rule.

## 5. Does the mandatory agentic review loop run, given the session's standing instruction not to spawn subagents?

**Blocking for marking the PR ready.** Needs a human decision; recorded here rather than resolved silently, per the repository-local-conventions rule that a skill/local-rule conflict stops for a human.

`clue-verify` requires an automatic agentic review pass on the verified committed candidate before a full change's PR is marked ready. The operating instruction for this session states that the Agent tool is not to be used unless the user requests it. These cannot both be honored.

Three ways out:

- **The maintainer authorizes the review loop for this change**, and it runs as `clue-verify` specifies.
- **The maintainer waives it for this change**, accepting that a documentation-and-decision change carries no code risk, and the PR is marked ready with the waiver disclosed in the acceptance brief.
- **The maintainer reviews the PR personally** in place of the agentic pass, which is what the review boundary ultimately requires anyway, since only a human merge accepts the change.

**Resolved:** the maintainer waived the agentic review loop for this change. The waiver is disclosed in the acceptance brief on the pull request. It applies to CH-028 only and sets no precedent for a change that touches code, tests, schema, or runtime behavior.
