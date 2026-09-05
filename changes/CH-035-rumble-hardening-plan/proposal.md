---
id: CH-035
type: change
status: open
links: [G-001, P-003]
title: Track Rumble's remaining follow-up work
---

# CH-035 — Track Rumble's remaining follow-up work

P-003 (Tank Royale Rumble) is `status: completed`, closing out its six milestones. Since that closure, real loose ends have surfaced that were never turned into tracked work: delivery work for P-003's own milestones is still unmerged, and two genuine bugs were found during hands-on testing but never filed anywhere.

This change creates a new plan, `docs/plans/P-004-rumble-hardening.md`, linking back to `P-003` and `G-001`. `P-003` stays `completed` as the historical record of the V1 launch; `P-004` tracks the follow-up work as its own campaign, per the plan skill's convention that a completed plan is immutable and new scope belongs in a successor plan rather than reopening it.

## What P-004 tracks

- **M-011 — Reconcile in-flight P-003 delivery work.** The M-009 Rumble documentation (originally [`tank-royale#250`](https://github.com/robocode-dev/tank-royale/pull/250)) is carried by this change's own branch, so it lands with this proposal and that pull request closes as superseded. [`rumble-client#9`](https://github.com/robocode-dev/rumble-client/pull/9) (Docker quickstart plus `run`/`submit` launchers) is open, green-checked, and mergeable; it needs a maintainer merge.
- **M-012 — Fix `rumble-client`'s practice-mode CLI gap.** `RumbleSynchronizer.synchronize()` unconditionally requires ranked mode, so `sync`/`run`/`submit` have no working CLI path in practice mode at all. Tracked in [`robocode-dev/rumble-client#10`](https://github.com/robocode-dev/rumble-client/issues/10).
- **M-013 — Fix `rumble-bots`' `Vector` bot stale source hash.** `bots/index.json`'s recorded `sourceHash` for `Vector` doesn't match its checked-in source, so a clean repo-wide `validate_bot.py` run fails on current `main`. Tracked in [`robocode-dev/rumble-bots#8`](https://github.com/robocode-dev/rumble-bots/issues/8).
- **M-014 — Documentation accuracy sweep**, gated on M-011 through M-013 landing: re-read every Rumble doc against the system's actual post-fix behavior and correct anything left over from before the fixes.

## Explicitly out of scope

The fuller aspirational doc set described in `docs/design/rumble/user-documentation.md` (a separate `rumble-data` `moderator-handbook.md`, `onboarding.md`, `faq.md`) stays deferred, not a plan promise. P-003's own closing note already says so, and nothing has changed that assessment.
