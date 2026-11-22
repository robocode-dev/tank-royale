---
id: CH-110
type: change
status: open
links: [P-007, M-039, CAP-016, ARCH-028, G-004]
title: Add Rankings and Help to the Rumble menu, which open the environment's rankings and the Rumble guide in the browser, and put the menu's entries in the mockup's order
---

# Add Rankings and Help to the Rumble menu, which open the environment's rankings and the Rumble guide in the browser, and put the menu's entries in the mockup's order

This is a slice of the milestone that releases the complete Rumble menu (M-039) in the [Rumble participation plan](../../docs/plans/P-007-rumble-participation.md). It proceeds under the [simple-participation goal](../../docs/goals/G-004-simple-rumble-participation.md).

## Why

The [Rumble menu mockups](../../docs/design/rumble/gui-mockups.md) end the menu with Rankings and Help, and neither is built. A participant who has submitted a bot or contributed battles wants to see where the bots stand, and a newcomer wants the guide. Both are links: Rankings opens the selected environment's dashboard and Help opens the central participant guide, without a screen of their own and without sign-in.

## What changes

- **Rankings** opens the dashboard of the Rumble the menu talks to in the browser: `https://robocode-dev.github.io/rumble-data/` for the public Beta and the test dashboard for the development Rumble, as named in the environment list the whole program reads.
- **Help** opens the Rumble guide at `https://robocode.dev/rumble/`.
- **The menu's entries follow the mockup's order**: Get started, Submit or update bot, Download bots, Contribute battles, Rankings, GitHub account and Help, with My bots and Results and queue to come in their places. Today GitHub account comes second.
- **Where each entry leads** is decided in the shared library without a window, and an address that is not an https page is never opened.
- **Every text** is in the menu resource files in all four languages.

## Challenge

- **Assumption most likely to be wrong:** that the dashboard address in the environment list is the page a participant wants. It is today the published rankings of each environment; if the dashboard moves, the list changes in one place.
- **Credible alternative:** a Rankings screen inside the program. Rejected by the mockup: the dashboard already shows rankings, and a second view would drift from it.
- **Cheapest test:** the addresses for both environments and a refused address, a test that the menu lists the entries in the mockup's order with their texts in every language, and the maintainer's trial of both entries.
- **Result that revises the work:** a dashboard or guide address that does not open in the browser stops the slice.
- **How it could pass every criterion and still fail a participant:** the public dashboard shows no rankings yet for a format, and the person thinks the link is broken. That is the dashboard's own page and is outside this change.

## Out of scope

My bots, Results and queue, the dashboard's content and the guide's content, and removing the developer switch.

## Acceptance criteria

Test class Unit unless stated.

- **RCL-144: Rankings opens the selected environment's dashboard and Help opens the Rumble guide**, both as https pages, and an address that is not an https page is refused.
- **RCL-145: The Rumble menu lists its entries in the mockup's order**, ending with Rankings, GitHub account and Help as drawn, and every entry has its text in every language.
- **RCL-146: The maintainer chooses Rankings and Help from the Rumble menu and each opens its page in the browser.** (Human.)

## Identity allocation

The change and the criteria were allocated with the repository's coordinated allocation.

## Human evidence

To be written after the maintainer's trial, before the digest.
