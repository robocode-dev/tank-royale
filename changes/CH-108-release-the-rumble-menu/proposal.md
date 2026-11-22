---
id: CH-108
type: change
status: open
links: [P-007, M-030, M-038, M-035, ADR-057, G-004]
title: Revise the Rumble participation plan so that the complete Rumble menu is finished and released first, and the remaining Rumble work follows the feedback from it
---

# Revise the Rumble participation plan so that the complete Rumble menu is finished and released first, and the remaining Rumble work follows the feedback from it

This is a plan revision of the [Rumble participation plan](../../docs/plans/P-007-rumble-participation.md). It proceeds under the [simple-participation goal](../../docs/goals/G-004-simple-rumble-participation.md); the corpus states no vision document.

## Why

The maintainer wants the Rumble in the desktop program finished completely and released as the new headline feature of Robocode Tank Royale, inviting everyone to try it and give feedback in issues, before any work outside the Rumble. The plan today has no milestone for that: its only release milestone is the final Windows acceptance with a public smoke test, which depends on withdrawal, readable dashboard details, the complete fixture suite and the organization-wide documentation. Four of the nine menu entries are built, all behind a developer switch; Get started, My bots, Results and queue, Rankings and Help are not.

The maintainer chose on 2026-11-22 that the release comes when the menu is complete, with the remaining Rumble milestones after it and guided by the feedback, and that the session's tray icon or its own window, which the [decision that a contribution session runs as a program of its own](../../docs/decisions/ADR-057-contribution-sessions-run-as-their-own-process.md) left for a later change, is part of the menu before the release. Work outside the Rumble comes after the release.

## What changes

A new milestone, M-039, "Release the complete Rumble menu", depends on the unattended sessions (M-030) and the downloads (M-038). Its exit criterion:

- Every entry of the Rumble menu mockups works in the desktop program without the developer switch: Get started, Submit or update bot, My bots, Download bots, Contribute battles, Results and queue, Rankings, GitHub account and Help.
- A contribution session can show its progress and controls in the operating system's tray where one is offered, or in a small window of its own, following the desktop program's theme, as the person chooses.
- The public Rumble accepts sign-in, registration and results from this version, so the menu works for an ordinary participant and not only against the test Rumble.
- The installed desktop program on Windows, and the platform packages it ships, can start a contribution session.
- The changelog presents the Rumble as the new headline feature and invites everyone to try it and report feedback through the repository's issue forms, which include a form for Rumble feedback that asks for the environment, the screen, the program version and where the session's log is, and never for a secret.
- A release of Tank Royale carries it.
- Each screen's behaviour runs through the fixture command, and each screen has a recorded trial by the maintainer.

M-030's remaining items that make sessions dependable for others (compiling a session's Java catalog bots once, the connect limit for a battle's bots, opt-in recording) are done within M-039 where they are needed for the release, and otherwise stay with M-030. The plan's verification section says that M-039's release is a public Beta release for feedback, and that the final Windows acceptance (M-035) still verifies the later complete journey. M-032, M-033, M-037, M-034 and M-035 keep their promises and follow after the release.

## Challenge

- **Assumption most likely to be wrong:** that the public Rumble can be made ready within this milestone. When it was last checked it had no sign-in App, did not accept results without labels and pinned an older engine behaviour version. That is setup in other repositories and needs the maintainer's hands; if it cannot be done, the release cannot invite ordinary participants and the milestone stops there.
- **Credible alternative:** release only when the whole Rumble plan is done. Rejected by the maintainer: feedback would come much later, and the remaining milestones are better shaped by it.
- **Cheapest test:** the screens one by one, each with its tests and the maintainer's trial against the test Rumble, then one end-to-end run against the public Rumble from an installed build before the release.
- **Result that revises the plan:** a public Rumble that cannot accept outside participants, or an installed build that cannot start a session, stops the release.
- **How it could meet every criterion and still fail participants:** they try it, meet a problem and have no easy way to say so. The issue form for Rumble feedback is therefore part of the milestone, and the changelog points to it.

## Out of scope

The implementation of the screens, which comes in their own tracked changes; withdrawal, readable dashboard details, the complete fixture suite, the organization-wide documentation and the final Windows acceptance, which keep their milestones; and all work outside the Rumble.

## Acceptance criteria

This revision changes the plan only and adds no acceptance criterion; each screen's change writes its own.

## Identity allocation

The change and the milestone were allocated with the repository's coordinated allocation.
