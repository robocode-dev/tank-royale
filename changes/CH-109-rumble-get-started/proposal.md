---
id: CH-109
type: change
status: open
links: [P-007, M-039, CAP-016, ARCH-028, G-004]
title: Add the Get started screen to the Rumble menu, which tells a newcomer what the Rumble is and leads to making a bot, submitting a bot and contributing battles
---

# Add the Get started screen to the Rumble menu, which tells a newcomer what the Rumble is and leads to making a bot, submitting a bot and contributing battles

This is the first screen of the milestone that releases the complete Rumble menu (M-039) in the [Rumble participation plan](../../docs/plans/P-007-rumble-participation.md). It proceeds under the [simple-participation goal](../../docs/goals/G-004-simple-rumble-participation.md).

## Why

A person who opens the Rumble menu for the first time meets eight entries and no explanation. The [Rumble menu mockups](../../docs/design/rumble/gui-mockups.md) put a Get started screen first, which says in two lines what the Rumble is and offers the three things a newcomer does: make a bot, submit it and lend computing time to run battles. The release invites everyone to try the Rumble and give feedback, so the screen also says that it is a Beta and opens the Rumble feedback form.

## What changes

- **Get started is the first entry of the Rumble menu.** It opens a screen whose title names the Rumble it talks to, like the other Rumble screens.
- **The screen has three cards**, as in the mockup: Make a bot (a starter in Java, C#, Python or TypeScript, no account needed), Submit a bot (check it, then send it, needs a GitHub account) and Contribute battles (run ranked battles on this computer, needs a GitHub account).
- **Make a bot opens the "My first bot" tutorial** on robocode.dev in the browser. The mockup's Create bot wizard does not exist in the program; the maintainer chose on 2026-11-22 that the button opens the tutorial in this release, and the wizard stays with the bot creation milestone (M-031). The mockup is updated to say so.
- **Submit a bot and Contribute battles** open the Submit or update bot screen and the Contribute battles screen. When this computer holds no saved sign-in for the Rumble, the button opens the GitHub account screen instead, so the person signs in first, as the mockup says.
- **A Beta line** says that the Rumble is a Beta and has a button that opens the repository's Rumble feedback form in the browser.
- **The cards have colour**, asked for by the maintainer on 2026-11-22: one shared card for the Rumble screens with a soft tint of its colour, a thin edge and a coloured stripe, worked out for the light or dark theme each time it is painted. Get started's cards are blue, green and amber; the count tiles of Contribute battles take green for accepted, red for rejected or not kept, amber for waiting and blue for kept and sent, and stay neutral at zero; the session's short notice in the main window is a blue card.
- **Every text** is in the resource files in all four languages, and the behaviour (which screen or page each button leads to) is a plain class without a window, so it is tested without one.

## Challenge

- **Assumption most likely to be wrong:** that "no saved sign-in" is a good enough sign that the person must sign in first. A saved sign-in can be unusable, and then the Submit and Contribute screens say so themselves and offer the account screen; the Get started screen does not repeat that check and never waits for the network.
- **Credible alternative:** build the Create bot wizard now. Rejected by the maintainer: it is a large piece of work that belongs to bot creation, and it would delay the release.
- **Cheapest test:** the plain class with a saved sign-in present and absent, a test that every text key exists in all four languages, and the maintainer's trial of the screen in light and dark themes.
- **Result that revises the work:** a screen that cannot fit three cards readably at the program's default size in Danish, the longest language, means the cards stack instead.
- **How it could pass every criterion and still fail a newcomer:** the tutorial explains bots but not the Rumble, so a person makes a bot and does not know how to submit it. The Submit a bot card sits next to it on the same screen, and the tutorial is not changed here.

## Out of scope

A Create bot wizard, changes to the tutorial, the other missing screens (My bots, Results and queue, Rankings and Help), and removing the developer switch.

## Acceptance criteria

Test class Unit unless stated.

- **RCL-141: Get started is the first entry of the Rumble menu and opens a screen** whose title names the Rumble environment and which shows the three cards and the Beta line, with all texts from the resource files in all four languages.
- **RCL-142: The Get started buttons lead to the right place**: Make a bot to the tutorial page, the feedback button to the Rumble feedback form, and Submit a bot and Contribute battles to their screens, or to the GitHub account screen when no saved sign-in exists for the environment.
- **RCL-143: The maintainer opens Get started from the Rumble menu, in a light and a dark theme, and every button leads where RCL-142 says.** (Human.)

## Identity allocation

The change and the criteria were allocated with the repository's coordinated allocation.

## Human evidence

On 2026-11-22 the maintainer opened Rumble, Get started in the desktop program against the test Rumble, in the light theme and in the dark theme, and sent a screenshot of each. Get started was the menu's first entry, the title read "Kom i gang (Development)", and the three cards showed in blue, green and amber with readable text on both themes; after the first light screenshot the window was made lower to remove empty space below the Beta line, and the dark screenshot shows the lower window. The maintainer pressed the buttons and reported that everything works ("Det virker rigtig fint"). The maintainer had asked for the colour on the cards after the first trial, saw it on both themes and said the cards look much better.
