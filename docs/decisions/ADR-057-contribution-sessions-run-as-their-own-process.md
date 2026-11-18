---
id: ADR-057
type: decision
status: verified
links: [ADR-054, ADR-052, ADR-053, P-007, CAP-016, CAP-024]
title: A contribution session runs as a program of its own that the desktop program and the command line start, follow and control, owned by the desktop program unless the person lets it run on
author: agent
accepted-by: Flemming N. Larsen (2026-11-18, Claude Code conversation)
---

# ADR-057: A contribution session runs as a program of its own that the desktop program and the command line start, follow and control, owned by the desktop program unless the person lets it run on

## Context

The [decision that the desktop program uses the Rumble libraries in its own process](ADR-054-desktop-program-uses-rumble-libraries.md) has it call the libraries in its own process for everything except battles, and the participation design says that the desktop program manages a separate controller and stops its managed session when it closes, while independent operation in the background uses the command line. The command line can now run a whole session with `rumble contribute`, proved with real battles that the Rumble accepted, but only Ctrl+C in its own terminal can pause or stop it, and nothing else can see how it goes.

A contribution session runs for hours. A contributor wants to start it from the desktop program, keep using the desktop program or close it, and find the session again when they open it, with its progress and its controls; a script or an agent wants the same from the command line. A session must not run on unnoticed after the program that started it is gone, unless the person chose that. Two sessions of one Rumble environment must never run at once, because both would write the same journal and send the same results.

## Decision

A contribution session always runs as a program of its own: the Rumble command line's `contribute`, started from a terminal, a script, or by the desktop program without a console window. It owns one session folder per Rumble environment in the Rumble state, which lies in the Tank Royale user folder below the environment's own folder, beside the journal, the sent batches, the bot cache and the saved sign-in, and is found there with the same methods. The folder holds:

- a lock the running session holds for as long as it runs, so a second session of the same environment is refused;
- the session's process ID and start time, and its owner when it has one;
- a status the session keeps up to date: what it runs, what it kept and did not keep, why it waits, whether Python is ready, and what is waiting, sent and accepted;
- a log of its events, only ever added to, and the summary of the last session after it ended;
- a place for control requests (pause, resume, stop after the running battle, stop now, release from the owner), which the session reads between and during battles as it reads Ctrl+C.

**The owner.** A session the desktop program starts is given the desktop program's process ID and start time as its owner, and checks every few seconds that this very process still runs; the start time keeps a reused process ID from being taken for it. When the owner is gone, whether it closed or crashed, the session finishes the running battle, keeps and sends it, and ends, so no session is left running without the program that started it. A session without an owner, such as one started from a terminal or a script, runs until its battles are done or it is stopped. Every session ends when its battle count is reached.

**Closing the desktop program.** When the desktop program closes while its session runs, it asks the first time whether to keep contributing in the background or to stop after the running battle, and keeps the answer as a setting the person can change. To keep contributing, it sends the request that releases the session from its owner before it closes; with the setting at keep contributing, it starts sessions without an owner. Otherwise it simply closes, and the session stops after the running battle because its owner is gone.

**Following and controlling.** The desktop program's Contribute battles screen starts a session and follows and controls it through the session folder; when the desktop program starts, it looks for a running session and shows it again, or shows the summary of a session that ended while it was closed. The command line gains commands to follow and control a running session the same way, so a session the desktop program started can be followed from a script, and the other way round. A session whose process ended without closing, such as by a crash, is shown as ended unexpectedly; what it kept is in the journal.

**When the battles are done.** The screen shows that the session finished and its summary, and the desktop program gives a short notice that does not have to be clicked away. A session that has a screen to show on can also, when the person chose it, give its own notice: as an icon in the operating system's tray, where the operating system offers one, or else as a small window of its own; a session without a screen, such as on a server, shows nothing and leaves its summary in the session folder.

**The tray, where it is offered.** Where Java reports that the operating system has a tray, a session can, when the person chose it, show an icon there for as long as it runs, with its progress in the icon's tip, a short menu to show the status, pause or resume, stop, stop now and open Tank Royale, and its notice when the battles are done. Where there is no tray, as on some Linux desktops, the session uses its own small window instead or nothing, as the person chose. The status window, and any window the session shows, follows the theme of the desktop program, read from the same setting; the tray's own menu is the operating system's and cannot follow it. The tray and these windows are offered as a convenience: a session works the same without them, and they are an extra over the session folder, never the only way to see or control a session.

The desktop program still calls the libraries in its own process for everything else, and the session program uses the saved sign-in as the command line already does.

## Rationale

One program runs every session, so the desktop program and the command line cannot run a session differently; the desktop program only starts, follows and controls it. A session that is a program of its own does not end with a window, which is what a contributor who wants to keep contributing while doing other work expects, and an owner that the session watches means it never outlives the desktop program unless the person chose that, also when the desktop program crashes. Files in the Rumble state need no network port and no service, work alike on every operating system, and survive both programs ending; the lock is also what keeps two sessions from writing one journal. Asking once and keeping the answer leaves the choice with the contributor without asking at every close. The tray is where people look for a program working in the background, but Java offers it only where the operating system does, so it is a convenience over the session folder with a window of the session's own as the fallback.

## Consequences

- The participation design's line that the desktop program stops its managed session when it closes is refined: it stops it unless the person chose to keep contributing in the background, and also when the desktop program crashes.
- The command line's `contribute` gains the session folder, the lock, the owner and the control requests; Ctrl+C keeps working in a terminal.
- A session started by the desktop program is visible to, and can be stopped by, anything running as the same user that can write the session folder, as the saved sign-in already is.
- The desktop program needs the command line program to start a session; it fetches it as the participation design describes, after asking.
- The session program carries the desktop program's look and feel for its own windows, which makes it somewhat larger; a session without a screen never loads it.
- The tray and the session's own windows come in a later change than the session folder and the screen; until then the screen and the command line are the ways to see and control a session.

## References

- [ADR-054: The desktop program uses the Rumble libraries in its own process](ADR-054-desktop-program-uses-rumble-libraries.md)
- [ADR-052: Share desktop and headless Rumble logic while isolating development and untrusted execution](ADR-052-rumble-desktop-and-test-environments.md)
- [ADR-053: An agent uses the participant's own account](ADR-053-agent-uses-participants-account.md)
- [Participation design](../design/rumble/participation.md)
