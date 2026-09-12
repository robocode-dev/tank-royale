---
id: CH-044
type: change
status: open
links: [M-019]
title: Close P-004/M-019 with its already-merged rumble-client evidence
---

# CH-044: Close P-004/M-019 with its already-merged rumble-client evidence

## What

`docs/plans/P-004-rumble-hardening.md` still marks M-019 ("Container runtime lanes stay current") `todo`, even though the work CH-038 proposed is already merged and verified in `robocode-dev/rumble-client`. This change updates the plan row to `done` with verified evidence. No code changes; plan bookkeeping only.

## Why

CH-038's own digest (commit `95f2b0202`, merged via tank-royale#266) updated the plan's cross-references (renumbering the milestone from M-016 to M-019 during CH-039's ID-collision fix aside) but left the status cell `todo` with only the bare citation `CH-038` — the milestone's actual completion evidence was never recorded, even after the referenced work landed upstream. This is exactly the same class of stale bookkeeping CH-041, CH-042, and CH-043 each closed for M-011, M-015, and M-014.

Verified directly against `robocode-dev/rumble-client`'s current `main` (checked out locally under `.external/rumble-client`) before drafting this proposal:

- `rumble-client#13` ("Refresh Rumble container runtime lanes") merged as `40de345` on 2026-09-10, with review fixes `229a7af` and `ea0582f` in the same PR.
- `.github/workflows/update-runtimes.yml` is a monthly (`cron: '0 9 1 * *'`) scheduled workflow, also manually dispatchable, that: updates the runtime policy from official release feeds, detects whether anything changed, and — only if it did — checks out the pinned Tank Royale commit, builds the sample bots, and runs `scripts/verify-container.sh` before its final step opens a reviewable pull request (`peter-evans/create-pull-request`, no auto-merge). GitHub Actions skips a later step when an earlier one in the same job fails, so the pull request step structurally cannot run unless the build and verification steps before it succeeded.
- `scripts/verify-container.sh` exercises hardened non-root, read-only, network-restricted container checks and runs the four required smoke battles: Python-vs-Java, Java-vs-Python, C#-vs-Java, and TypeScript-vs-Java, each asserting on the real `/tmp/rumble-smoke-success` marker written by a completed one-round battle.

This satisfies M-019's exit criterion as written — "a scheduled refresh proposes a pull request only after all four containerized bot smoke battles and hardened checks pass" — the same way M-015 was accepted on CI-proven behavior rather than requiring a live occurrence of the monthly trigger to have already fired (`gh run list` on the workflow shows no historical runs yet, which is expected: the workflow has existed for two days and fires on the 1st of the month).

## Scope

In scope: the M-019 row (and any prose that lists which milestones remain) in `docs/plans/P-004-rumble-hardening.md`.

Out of scope: any code change in `tank-royale` or `rumble-client` — the underlying work is already merged and reviewed; this change only reconciles the record.
