---
id: CH-028-open-questions
type: open-questions
status: open
links: [CH-028]
title: Open questions for CH-028
---

# CH-028 — Open questions

## 1. Does `robocode.dev/api/apis.html` list community Bot APIs?

Non-blocking; the proposal assumes "no" and lists them in the README only.

The API page currently lists Java, C#, Python, TypeScript, and Battle Runner in one uniform structure, and that uniformity is itself the promise that everything on it is 1:1 and maintained by this project. Keeping it official-only preserves that promise and matches how the Tank Royale Viewer is handled today, in the README rather than in the product's own documentation surfaces. The cost is that someone hunting for an API on the documentation site will not find the community ones.

The alternative is a visually distinct "Community Bot APIs" block below the official entries, each naming its maintainer and target schema version. More discoverable, but it puts unofficial entries on the page users treat as authoritative, and it becomes a surface the maintainer must keep current as community projects come and go.

**Assumed answer:** README only. Flip this and the ADR's community-tier paragraph and one digest task change; nothing else does.

## 2. Are Lua and Nim named in ADR-0045, or only in `docs/decisions/log.md`?

Non-blocking; the proposal assumes the ADR stays generic and the instances become dated log rows.

Naming both somewhere is what demonstrates the policy was applied consistently rather than invented for one request. The question is only where. A `log.md` row is dated, factual, and reads as bookkeeping. An "Applied instances" section inside a permanent ADR names a contributor's project inside a rejection record and will outlive the conversation that produced it.

**Assumed answer:** generic ADR, dated rows in `log.md` citing ADR-0045.

## 3. How is issue #198 (Lua) reconciled with the merged policy?

**Blocking for the digest, not for implementation.** The ADR can be written either way; the issue must be reconciled before this change is accepted, because leaving both as they are publishes a contradiction.

Issue #198 is open, labelled `help wanted` and `huge effort`. In April 2026 the maintainer answered it with the community-tier position: an independent Lua project maintained alongside Tank Royale is welcome, the main line is not offered, and the issue was deliberately left open for someone to pick up. A merged ADR-0045 states the official set is closed. An open `help wanted` request to add an official language contradicts that.

Two ways out:

- **Close #198**, citing ADR-0045 and pointing at the community tier. Consistent and unambiguous, but it withdraws something the requester was told was still possible, and lands as a late reversal.
- **Keep #198 open, reframed.** Drop `help wanted`, relabel toward "community Bot API welcome", and comment that the standing position is now written down. This keeps the April commitment intact, because that comment already *was* the community-tier answer; only the label implies official adoption.

**Recommended:** keep it open, reframed. The maintainer's April reply and ADR-0045 say the same thing; the label is the only part that conflicts.

## 4. Does a contributor-facing policy change warrant a CHANGELOG entry?

Non-blocking; resolved during the digest against `.agents/instructions/changelog.md`.

The changelog is user-facing and this change ships no user-visible behavior, which argues no entry. Repository-local digest conventions call for a changelog entry on a full change, which argues yes. The instruction file governs; this note records the tension so the digest resolves it deliberately rather than by omission.
