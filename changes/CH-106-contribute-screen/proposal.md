---
id: CH-106
type: change
status: open
links: [P-007, CAP-016, CAP-024, ADR-057, ADR-054, ADR-055, ADR-056]
title: Run a contribution session as a program of its own that the desktop program's Contribute battles screen and the command line start, follow and control, owned by the desktop program unless the person lets it run on
---

# Run a contribution session as a program of its own that the desktop program's Contribute battles screen and the command line start, follow and control, owned by the desktop program unless the person lets it run on

A contributor can run a whole contribution session with `rumble contribute`, but only Ctrl+C in its own terminal can pause or stop it, nothing else can see how it goes, and the desktop program cannot start one. The plan item for contributor-controlled unattended sessions (M-030) promises that the desktop program and the command line share session behaviour. The [decision that a contribution session runs as a program of its own](../../docs/decisions/ADR-057-contribution-sessions-run-as-their-own-process.md) has every session run as the command line's `contribute`, with one session folder per environment in the Tank Royale user folder through which the desktop program and the command line start, follow and control it; a session the desktop program starts has it as owner and stops after the running battle when it is gone, unless the person lets the session run on in the background.

## Why

Sessions run for hours. A contributor wants to start one from the desktop program, keep working or close the program, and find the session again with its progress and controls; a script or an agent wants the same. One program running every session keeps the desktop program and the command line from running a session differently.

## What changes

- **The decision is recorded**, and the participation design's line that the desktop program stops its managed session when it closes is refined.
- **The session folder.** `rumble contribute` keeps, in the environment's folder in the Tank Royale user folder:
  - a lock held while it runs, so a second session of the same environment is refused with a reason;
  - its process ID and start time, and its owner when it has one;
  - a status it keeps up to date (the battle running and its bots, kept and not kept, why it waits, whether Python is ready, waiting, sent and accepted results, and whether it is pausing or stopping);
  - a log of its events, only added to, and the summary of the last session after it ended;
  - control requests (pause, resume, stop after the running battle, stop now, release from the owner), read between and during battles as Ctrl+C is.
- **An owner the session watches.** `rumble contribute --owner-pid` gives the session an owning process; the session checks every few seconds that this very process, by its ID and start time, still runs, and when it is gone finishes the running battle, keeps and sends it, and ends. Without an owner it runs until its battles are done or it is stopped, and every session ends when its battle count is reached.
- **Commands to follow and control a running session**: `rumble session status`, `pause`, `resume`, `stop` and `stop-now`, with the command line's usual text and JSON answers and exit codes; a session that ended without closing is reported as ended unexpectedly.
- **The desktop program gets the command line program** the first time a session needs it: the screen says what it will fetch and how large it is and asks first, then fetches `rumble.jar` of its own version with the existing fetch, which checks it.
- **The Contribute battles screen**, behind the developer switch with the rest of the Rumble menu, following the menu mockup: before starting, the mode (All bots, Prefer selected bots, Only selected bots), the active bots of the catalog, the formats, and a battle count or "keep running"; while running, the status above and Pause, Resume, Stop (finish this battle, then stop) and Stop now (end this battle and keep nothing of it). It starts the session without a window and follows it through the session folder; the person can keep using the desktop program meanwhile. When the desktop program starts and a session runs, the screen shows it again; when one ended while the desktop program was closed, it shows that session's summary. When the battles are done, the screen shows the summary and the desktop program gives a short notice that does not have to be clicked away. A missing sign-in, registration, a pin this program cannot run under, or Python is shown plainly with the way to fix it where there is one, such as the GitHub account screen.
- **Closing the desktop program while a session runs** asks the first time whether to keep contributing in the background or to stop after the running battle, and keeps the answer as a setting the person can change. The desktop program starts its sessions with itself as owner, so a session stops after the running battle when the desktop program closes or crashes; to keep contributing, it releases the session from its owner before it closes, and with the setting at keep contributing it starts sessions without an owner.
- **The screen's behaviour lives in a class without a window**, like the other Rumble screens: work runs away from the screen's event thread, changes reach the screen through its event queue in order, and every text comes from the resource files with the same keys in every language file.

## Challenge

- **Assumption most likely to be wrong:** that a session started by the desktop program without a window behaves like one started in a terminal, including the worker's shield against Ctrl+C and the ending of the worker's process tree. A session started from the screen that leaves a worker running after Stop now, or that cannot be stopped, stops the slice.
- **Credible alternative:** run the session inside the desktop program and stop it when the program closes. Rejected with the decision: a session would end with a window closed by mistake, and the command line could not follow it.
- **Cheapest test:** the session folder, the lock, the owner and the control requests with the command line's fakes, with an owner that is a real process that is ended; `rumble session` against a fake running session and a dead one; the screen's controller with a fake session folder for every state; the fetch of the command line program with a fake download; then one real session started from the screen in the test Rumble, paused, stopped and found again after the desktop program was restarted, by the maintainer.
- **Result that revises the work:** a session that the screen cannot find again after a restart, a session that runs on after its owner is gone, or two sessions of one environment running at once, stops the slice.
- **How it could pass every criterion and still fail a contributor:** a session keeps running in the background without the person noticing. The screen and the command line therefore always show that one runs, and the first close asks.

## Out of scope

The tray icon, the session's own status window and its notice when the battles are done, which the decision lists for a later change; CPU and memory settings beyond the worker's limits, a switch that turns automatic sending off, the round shown while a battle runs, the Results and queue, My bots, Get started, Rankings and Help screens, starting a session when the computer starts, and removing the developer switch.

## Acceptance criteria

These will be written as criteria of the Rumble client capability and the command line's capability, each with its identity, when the work starts. Test class Unit unless stated.

- **A running session keeps its lock, status, log and process ID in the environment's session folder**, and a second session of the same environment is refused.
- **Control requests pause, resume, stop and stop now** a running session as Ctrl+C does, and release it from its owner.
- **A session with an owner stops after the running battle when the owner is gone**, and one without an owner runs until its battles are done or it is stopped.
- **`rumble session` shows and controls a running session**, and reports one that ended unexpectedly.
- **The desktop program fetches the command line program only after asking** and checks it.
- **The screen's choices start a session without a window, and the screen follows and controls it**, through the event queue in order, with every text from the resource files.
- **The screen finds a running session again** when the desktop program starts, and shows the summary of one that ended meanwhile; when the battles are done it shows the summary and a short notice.
- **Closing the desktop program while a session runs asks the first time and keeps the answer**, releasing the session to keep contributing or letting it stop after the running battle.
- **One real session started from the screen in the test Rumble is followed, paused, stopped and found again after a restart, and its result is accepted.** (Human evidence by the maintainer.)

## Identity allocation

The change and the decision were allocated with the repository's coordinated allocation. Criterion identities are allocated when the criteria are written.

## Human evidence

The maintainer starts one session from the Contribute battles screen in the test Rumble, keeps using the desktop program, pauses and resumes it, closes the desktop program choosing to keep contributing, reopens it and finds the session again, stops it, and sees its result accepted; and once more closes the desktop program choosing to stop, and sees the session end after its battle.
