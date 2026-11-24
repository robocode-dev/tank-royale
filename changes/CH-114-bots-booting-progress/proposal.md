---
id: CH-114
type: change
status: open
links: [P-007, M-039, CAP-001, CAP-016, CAP-021, G-004]
title: Show a contribution session's progress while a battle's bots start, and let a battle wait longer for slow bots
---

# Show a contribution session's progress while a battle's bots start, and let a battle wait longer for slow bots

This is a slice of the milestone that releases the complete Rumble menu (M-039) in the [Rumble participation plan](../../docs/plans/P-007-rumble-participation.md). It proceeds under the [simple-participation goal](../../docs/goals/G-004-simple-rumble-participation.md).

## Why

The Rumble's bots start from source, and a Java bot is compiled at every start. A TwinDuel battle of the test Rumble spends 10 to 30 seconds starting its four bots, and on 2026-11-21 one battle failed because a Java bot missed the Battle Runner's 30-second connect limit while the others had started. During those seconds a session says only that it runs, which looks like a frozen program, and a contributor cannot tell a slow start from a stuck one. The maintainer asked on 2026-11-24 for a progress bar, and maybe a spinner, so that the starting bots can be seen.

## What changes

- **A battle waits up to 90 seconds for its bots**, instead of the Battle Runner's default of 30, in the Rumble's battle worker. It is only the longest a battle waits: the battle starts the moment the last bot has connected, as it does today, so fast starts lose nothing. It stays far below the host's limit of ten minutes for a whole job. The Battle Runner's own default does not change.
- **The Battle Runner can report the starting bots**: a runner can be given a listener that is called, while a battle starts, with how many of the expected bots have connected, how many are missing, how long it has waited and the limit. Until now only the handle of a battle that had already started had this event, so a caller of the synchronous `runBattle` could never see it.
- **The worker passes it on** as a new kind of event, once for every change in the number of connected bots and about once a second otherwise, carrying only those counts and times. It says nothing about which bot is missing, so a bot's name or output never takes this way. The host reads the event and hands it on without keeping it, and the outcome of a battle is decided by the other events as before.
- **A session reports it** as a new event, and its status carries the progress while the bots of the running battle start and no longer after they all connected or the battle ended. The command line writes one line, or one JSON line, only when the count of connected bots changes, so the event log does not fill with a line per second; the status is updated on every report.
- **Where it is shown.** The Contribute battles screen shows a progress bar of the connected bots with the seconds waited and a text, "Starting the bots: 2 of 4 connected, 18 s of 90 s", and the bar moves on its own before the first bot connects, which is the spinner. The session's small window shows the same text and bar. The tray's tip adds the line to its text. A session or a status without the progress, as written by an older program, shows what it showed before.
- **Every text** is in the resource files of the desktop program and of the session program in English, Danish, Spanish and Catalan.

## Challenge

- **Assumption most likely to be wrong:** that the counts the Battle Runner reports during its start are the whole of what a person waits for. The start also spends time before the first bot can connect, when the booter starts the processes, and the runner has no event for that; the bar then shows no connected bot for a while and moves as an unfilled bar, which is the spinner. A later change may add what the booter does.
- **Credible alternative:** compile a session's Java bots once, so the starts are short. It changes the rule that ranked bots run from source with their own launcher, which the moderation checks, so it needs its own decision and is left out here; this change makes the wait visible and survivable first, and shows whether compiling once is still needed.
- **Cheapest test:** the runner's listener with a fake of the starting bots, the worker's event and its throttling on a fake engine, the host handing events on without keeping them, the status and its clearing in the session's tracker, and the screen's table of texts in every language; then the maintainer's trial of a real session on Windows with Java bots.
- **Result that revises the work:** if a real start shows no progress for most of the wait, because the bots connect all at once at the end, the bar says little and the text must say that the bots are starting, not a count.
- **How it could pass every criterion and still fail a contributor:** the bar could stay on screen after the battle began if the clearing is missed in one path, such as a battle that fails during its start; the status therefore clears with every ending of the battle.

## Out of scope

Compiling Java bots once per session, progress while the bots of a normal local battle start in the desktop program (the Boot progress dialog already shows that), a way to cancel a start from the bar other than the session's own stop controls, and a bot's name in the progress.

## Acceptance criteria

Test class Unit unless stated.

- **BR-052: A Battle Runner can be given a boot progress listener** that is called during every battle's start with the connected and missing bots, the time waited and the limit, also for the synchronous run.
- **RBW-023: The worker waits up to 90 seconds for a battle's bots to connect.**
- **RBW-024: The worker reports the starting bots** as events with counts and times only, when the count of connected bots changes and about once a second otherwise, and never anything about a bot but its number.
- **RBW-025: The host hands the worker's boot progress on as it arrives and does not keep it**, and the outcome of a battle is the same with or without it.
- **RCL-160: A session reports the starting bots in its status and its events**, clears them when all connected or the battle ends in any way, and writes a line of its log only when the count changes.
- **RCL-161: The Contribute battles screen, the session's window and the tray's tip show the progress of the starting bots** with the counts and seconds in every language, and without it a status shows what it showed before.
- **RCL-162: The maintainer runs a real session with Java bots on Windows and sees the progress while the bots start and no progress after.** (Human.)

## Identity allocation

The change and the criteria were allocated with the repository's local allocation. A clone that has not pushed is invisible to a check of the remote.

## Human evidence

**Seen by the agent on 2026-11-24**, on Windows with the development Rumble, because the maintainer's screen was locked afterwards: two real one-battle TwinDuel sessions ran the Rumble's own bots, and the command line wrote "Bots starting: 1 of 4 connected" up to "4 of 4 connected", one line for each change and no more, while the status file and the log held the same counts. The bots connected within 0.9 seconds in both, so a real start was too quick to be seen on the screen. A play-through of a slow start in the same session folder, with four bots connecting over 20 seconds, showed in the session's window, in the dark theme and in Danish, "Bots starter: 1 af 4 forbundet, 5 s af 90 s" and later "2 af 4 forbundet, 13 s af 90 s" with a bar that grew with the count, and the line and the bar were gone after all four had connected. **Not seen yet:** the Contribute battles screen and the tray's tip with the progress, and a real slow start with Java bots, which the maintainer's trial must show.
