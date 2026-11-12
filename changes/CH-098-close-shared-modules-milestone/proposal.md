---
id: CH-098
type: change
status: open
links: [P-007]
title: Check the shared modules and test harness milestone against its exit criterion and close it, or record what is missing
---

# Check the shared modules and test harness milestone against its exit criterion and close it, or record what is missing

The plan item "Review GUI and deliver shared modules and test harness" (M-029) still says `todo`, and its progress notes end with a list of work still to do (the rest of the libraries and the headless controller, the single fixture command, the credential boundary check and the state migration). Later slices in the same notes built every one of those, so the list is stale and the status no longer says where the work stands.

This change serves that plan item and adds no product behavior.

## Why

The next milestones (unattended sessions, bot creation, downloading) depend on this one. A status that reads `todo` while the work is built hides what is really next, and a closed milestone that was never checked against its exit criterion hides a gap. The change checks first and only then closes.

## What changes

- **The exit criterion is walked item by item** with evidence from this checkout: the reviewed mockups and dependency diagram, `lib:rumble`, the `rumble` command line and battle worker, `lib:bot-project` with its inspection, staging and validation interfaces, the single fixture command with its machine-readable result and redacted report, structured interfaces, credential-free workers, the existing regressions and the non-destructive state migration.
- **If every item holds,** the plan item is marked `done` with the date and what was observed, and the stale "still to do" sentence is replaced by what is true.
- **If an item does not hold,** the plan item stays `todo`, the notes say plainly which item and why, and the follow-up work is recorded as the plan's next slice instead of being hidden.

## Challenge

- **Assumption most likely to be wrong:** that the fixture command and the module checks still pass on the current `main`; the notes record them as passing at the time each slice landed, not now.
- **Credible alternative:** skip the check and move straight to unattended sessions. Rejected: those sessions build on the headless controller and the fixture harness, so a hidden gap would surface later and cost more.
- **Cheapest test:** run `./gradlew rumbleFixtureCheck` and the module tests, and read the result and the report.
- **Result that revises the work:** any failing case, or an exit-criterion item with no evidence, keeps the milestone open and turns this change into a record of the gap.

## Out of scope

- Any screen of the Rumble menu beyond those the plan notes already list, and the contributor sessions (the next milestone).
- Retiring the old Rumble client repository, which waits until the Rumble can be run entirely from the desktop program.

## Acceptance criteria

None change. The change alters plan bookkeeping only.

## Identity allocation

The change was allocated with the repository's coordinated allocation (an allocator branch exists on the remote).

## Human evidence

None needed unless the check finds something only a person can observe.
