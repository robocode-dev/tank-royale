---
id: CH-054
type: change
status: open
links: [C-002, PDR-016]
title: Switch maintainer acceptance to local
---

# CH-054 — Switch maintainer acceptance to local

## What

Move the maintainer's acceptance of tracked changes from a human-merged pull request to Cliewen's local acceptance: the agent prepares a verified, reviewed candidate on a branch, and the maintainer accepts that exact candidate with `clue accept` on a clean `main` checkout instead of merging a PR. The change removes `.clue/acceptance.yaml`, which makes local acceptance on `main` the effective policy, rewrites the repository rules that describe the PR boundary, and keeps pull requests as the way every other contributor proposes changes.

The maintainer has also authorized the agent to push `main` itself after the maintainer accepts a candidate with `clue accept`. The command never pushes, so this is a per-flow authorization that the rewritten constraint records, not a general permission to push `main`.

This change is plan-less: it serves the maintainer's review workflow rather than a milestone of an active plan.

## Why

The maintainer is the only person who accepts tracked changes, and most tracked work is authored by an agent working with them in a single session. A PR adds a hosted round trip and a ready/draft ceremony without adding an independent reviewer, while local acceptance binds the human decision to the exact candidate, base and acceptance brief and preserves them in the merge. Outside contributors are unaffected: they still open PRs, and the hosted validation workflow still judges every PR and every push to `main`.

## Scope

- Delete `.clue/acceptance.yaml` so the effective policy is local on `main`. This change is itself accepted under the current PR policy, because a candidate cannot switch the policy it is accepted under.
- Rewrite the review-boundary constraint so it states the new boundary: a tracked change reaches `main` only by the maintainer's explicit `clue accept` of the exact candidate; direct work keeps its per-change `Authorized-Push:` rule; outside contributors' PRs are merged by the maintainer; agents never decide acceptance of their own change. Revisit its enforcement and promotion text, which assumed a required PR check.
- Record the policy switch, and the rejected alternative of keeping PR acceptance for maintainer work, as a decision.
- Update `AGENTS.md` and `.github/copilot-instructions.md` where they say tracked work reaches `main` only through a human-merged PR or that the file selects PR acceptance.
- Keep `CONTRIBUTING.md` talking about pull requests, because it addresses outside contributors; adjust it only if it implies the maintainer's own flow.
- Keep `.github/pull_request_template.md` for outside contributors' PRs, trimming only wording that presumes an agent-initiated tracked change if it misleads them.
- Leave `.github/workflows/clue.yml` as it is: it already validates every PR and every push to `main`. Confirm that a tracked change accepted locally is validated by that push run.

## Non-goals

- Changing how outside contributors propose changes, the merge-commit requirement for their PRs, or branch protection.
- Giving agents authority to accept their own change, to fabricate approval, or to push `main` outside the accepted flow.
- Changing the direct-work rule that every direct push needs a per-change authorization and an `Authorized-Push:` trailer.

## Challenge

The assumption most likely to undermine this is that the hosted gate only mattered for the maintainer's flow. With local acceptance the first validation of an accepted change on the hosted runner happens after it lands on `main`, so a failure there is found late and in the shared branch. The credible alternative is to keep PR acceptance and accept the ceremony. The cheapest test is to run `clue accept --check` on the first change accepted this way and confirm that the local checks cover what CI would have; the push run on `main` then shows whether anything slipped through. If a hosted failure ever lands on `main` this way, revisit the policy or reintroduce a required check before push.

A change could meet every criterion and still fail the maintainer if the rewritten rules read as permission for agents to integrate unattended. The wording must keep acceptance as the human act on an exact candidate, and the push authorization must be limited to the push that follows it.

## Compatibility

No product behavior changes. Open PRs from outside contributors keep working. The next tracked change after this one follows the local handoff in the review-boundary reference.
