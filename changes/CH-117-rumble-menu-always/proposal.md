---
id: CH-117
type: change
status: open
links: [P-007, M-039, CAP-016, G-004]
title: Show the Rumble menu in every desktop program, without the developer switch
---

# Show the Rumble menu in every desktop program, without the developer switch

This is a slice of the milestone that releases the complete Rumble menu (M-039) in the [Rumble participation plan](../../docs/plans/P-007-rumble-participation.md), whose exit criterion asks that every entry of the menu works without the developer switch. It proceeds under the [simple-participation goal](../../docs/goals/G-004-simple-rumble-participation.md).

## Why

The Rumble menu has been shown only when a developer set the system property `rumble.menu` or the variable `RUMBLE_MENU`, because a released program must not show a menu with items that do not work. All nine entries of the mockups are now built and tried. The participation design says that the switch goes when the menu is whole, and the maintainer asked on 2026-11-25 to remove it.

## What changes

- **The desktop program always shows the Rumble menu**, with all its entries. The property `rumble.menu` and the variable `RUMBLE_MENU` are no longer read.
- **The desktop program always looks for a contribution session when it starts**, which until now it did only with the switch on, so a session that runs, or one that ended while the program was closed, is found again by every desktop program.
- **The environment choice stays.** The variable `RUMBLE_ENVIRONMENT` still names the development Rumble for a developer, and the program uses the public Rumble when it names none, as the command line does.
- **The criteria that named the switch are retired**, and new ones state that the menu is always there.
- **The settings a developer uses are described** in the repository's development guide, as the maintainer asked: `RUMBLE_ENVIRONMENT`, the `rumble.jar` property, `ROBOCODE_DEBUG`, `ROBOCODE_TURN_TIMING_DIAGNOSTICS`, `rumble.live` and `winConsole`, the variables of the Rumble's bot launchers, and how to set them for a local build and for an installed program.

## Challenge

- **Assumption most likely to be wrong:** that showing the menu is harmless before the public Rumble is ready. Its sign-in App is still missing, so signing in to the public Rumble says that it is not available. That is honest, and no release is made before the maintainer has made the public Rumble ready, which the milestone also requires. Main shows the menu meanwhile, so any build from main shows it.
- **Credible alternative:** keep the switch until the release, and remove it then. The maintainer chose to remove it now, so the program is tried as people will get it.
- **Cheapest test:** the menu built without any switch holds the Rumble menu with its entries in order, the session lookup at start runs without a switch, and the environment rule is unchanged.
- **Result that revises the work:** if the session lookup at start slows or disturbs a desktop program that is never used for the Rumble, it must wait until the Rumble menu is first used.
- **How it could pass every criterion and still fail a person:** a person who never wants the Rumble now sees a menu more. It has no setting to hide it; one can be added if feedback asks for it.

## Out of scope

Making the public Rumble ready, which is the maintainer's setup; a setting to hide the menu; and the release.

## Acceptance criteria

Test class Unit unless stated.

- **RCL-168: The desktop program always shows the Rumble menu and looks for a contribution session at start**, with no switch read, and the environment follows the command line's rule: the one `RUMBLE_ENVIRONMENT` names, the public Rumble when it names none, and an unknown name reported, never replaced. This replaces RCL-055, which showed the menu only with the switch.
- **RCL-169: The Download bots item is in the Rumble menu only**, which the menu bar always holds, and the dialog's title shows the environment it talks to. This replaces RCL-081, which showed it only with the switch.

## Identity allocation

The change and the criteria were allocated with the repository's local allocation. A clone that has not pushed is invisible to a check of the remote.

## Human evidence

**Seen by the agent on 2026-11-25** on Windows: the desktop program built from this change, started with no `RUMBLE_MENU`, `rumble.menu` or `RUMBLE_ENVIRONMENT` set, showed the Rumble menu in its menu bar between Indstillinger and Hjælp.
