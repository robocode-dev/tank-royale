---
id: CH-113
type: change
status: open
links: [P-007, M-039, CAP-016, CAP-024, ADR-057, G-004]
title: Let a contribution session show itself in the system tray or in a small window of its own, following the desktop program's theme and language, with its controls and a way back to Tank Royale
---

# Let a contribution session show itself in the system tray or in a small window of its own, following the desktop program's theme and language, with its controls and a way back to Tank Royale

This is a slice of the milestone that releases the complete Rumble menu (M-039) in the [Rumble participation plan](../../docs/plans/P-007-rumble-participation.md), which the maintainer chose to include before the release. It carries out what the [decision that a contribution session runs as a program of its own](../../docs/decisions/ADR-057-contribution-sessions-run-as-their-own-process.md) left for a later change: the tray and the session's own windows. It proceeds under the [simple-participation goal](../../docs/goals/G-004-simple-rumble-participation.md).

## Why

A contribution session runs for hours as a program of its own, also after the desktop program is closed. Today it can be seen and controlled only on the Contribute battles screen or with the command line. A contributor who works on something else wants to see at a glance how it goes, pause or stop it, and get back to Tank Royale, without opening the program first.

## What changes

- **The person chooses how a session shows itself**, on the Contribute battles screen: in the system tray, in a small window of its own, or not at all. The choice is remembered; it is the tray unless the person chose otherwise, as the maintainer chose on 2026-11-22. The desktop program passes it to the session it starts, with a new option of the command line's `contribute`; a session started from a terminal shows nothing unless asked.
- **In the tray**, where Java reports that the operating system has one, the session shows an icon for as long as it runs. Its tip says where the session stands. Its menu shows the status window, pauses or resumes, stops after the running battle, stops now, and opens Tank Royale; these send the same requests as the command line's session commands. When the battles are done it gives a short notice, and the icon goes when the session ends.
- **Without a tray**, the session uses its small window instead; on a computer without a screen it shows nothing, and works the same.
- **The small window**, which the tray's menu also opens, shows where the session stands, the running battle, the battles kept and the result counts, with the same controls as buttons. It follows the desktop program's theme and language, read from the desktop program's own settings when it opens. The tray's menu is the operating system's own and cannot follow the theme.
- **Open Tank Royale** asks a desktop program that runs to come to the front and show the Contribute battles screen, through the session folder. When no desktop program answers in a few seconds, the session starts Tank Royale with the command the desktop program gave it when it started the session, and the desktop program, as it starts, shows the running session as it already does. A session started from a terminal without that command offers no Open Tank Royale.
- **The desktop program's themes move to a small shared module**, so the desktop program and the session program use the same look; the desktop program does not change how it looks.
- **Every text** of the tray and the window is in resource files in English, Danish, Spanish and Catalan.

## Challenge

- **Assumption most likely to be wrong:** that the session program can show Swing windows and a tray icon while it is started without a console window by the desktop program. Java's tray needs a desktop session; a session started as a service would not have one. The maintainer's trial on Windows is the proof, and a session that cannot show either goes on without them.
- **Credible alternative:** let the desktop program own the tray icon. Rejected by the decision: the session outlives the desktop program, and a tray icon that disappears with the desktop program would leave the running session unseen.
- **Cheapest test:** the choice and its option, the tray's menu and tip, the window's content and the open request with fakes of the tray, the session folder and the process starter, and the maintainer's trial on Windows in a light and a dark theme.
- **Result that revises the work:** a tray icon that Windows hides away by default is still a tray icon, but if it cannot be found by a person, the window becomes the default.
- **How it could pass every criterion and still fail a contributor:** two desktop programs could answer the same open request. The first one that takes the request removes it, so only one comes to the front.

## Out of scope

Pausing or stopping from the operating system's notification itself, a tray on other operating systems than Windows proved by a trial, and changing what a session does.

## Acceptance criteria

Test class Unit unless stated.

- **RCL-155: The person chooses on the Contribute battles screen how a session shows itself**, the tray unless chosen otherwise and remembered, and the desktop program passes the choice and the command that opens Tank Royale to the session it starts.
- **RCL-156: A session shown in the tray** has an icon only where the operating system has a tray, a tip that says where it stands, a menu whose items send the session's own requests, a notice when the battles are done, and no icon after it ends; without a tray it falls back to its window, and without a screen it shows nothing.
- **RCL-157: The session's small window** shows where it stands, the running battle, the battles kept and the result counts with the controls, in the desktop program's theme and language read from its settings, with every text in every language.
- **RCL-158: Open Tank Royale** brings a running desktop program to the front with the Contribute battles screen, only one when two run, and otherwise starts Tank Royale with the command it was given; without that command it is not offered.
- **RCL-159: The maintainer starts a session from the desktop program on Windows, sees its tray icon, tip, menu and notice, opens its window in a light and a dark theme, and uses Open Tank Royale with the desktop program open and closed.** (Human.)

## Identity allocation

The change and the criteria were allocated with the repository's coordinated allocation.

## Human evidence

To be written after the maintainer's trial, before the digest.
