---
id: CH-111
type: change
status: open
links: [P-007, M-039, CAP-016, ARCH-028, G-004]
title: Add the My bots screen to the Rumble menu, which lists the signed-in account's bots in the Rumble and its sent bots with where each stands
---

# Add the My bots screen to the Rumble menu, which lists the signed-in account's bots in the Rumble and its sent bots with where each stands

This is a slice of the milestone that releases the complete Rumble menu (M-039) in the [Rumble participation plan](../../docs/plans/P-007-rumble-participation.md). It proceeds under the [simple-participation goal](../../docs/goals/G-004-simple-rumble-participation.md).

## Why

An author who has sent bots can today see where one bot stands only inside the Submit wizard, right after sending it, or with the command line. The [Rumble menu mockups](../../docs/design/rumble/gui-mockups.md) give the author one screen that lists their bots with name, version, language and where each stands, and leads to opening a bot's pull request or page and to updating it.

## What changes

- **My bots** comes after Submit or update bot in the Rumble menu, as drawn. It needs a sign-in; without one the screen says so and offers the GitHub account screen.
- **The list is made of two readings, both through the saved sign-in**: the Rumble's catalog, where every entry names its owner by GitHub account, and the account's pull requests to the Rumble's bot repository that the program sent (branches named `submit/<name>-<version>`). One row per bot name: the newest version the account owns or sent.
- **Each row says where the bot stands**, with the same plain states and texts as the Submit wizard's last step: waiting for the checks, being checked, sent back with the reason, waiting for a moderator, accepted and waiting to be merged, merged but not yet in the rankings, in the Rumble with its version, or closed without being merged. A bot whose newest pull request is still open shows that pull request's state, so an update under review is not hidden behind the version already in the Rumble; the row then also says which version is in the Rumble. A bot that is only in the catalog shows its catalog state, in the Rumble or no longer active.
- **Retired bots and closed submissions are hidden** unless the person ticks a box that shows them with their number; the program remembers the choice. The maintainer asked for this on 2026-11-22 after seeing retired test bots and a closed smoke test in the list: they cannot be deleted from the Rumble, whose catalog keeps its history, but they need not be shown.
- **Each row has Open and Update**, with tooltips that say what each opens: the newest pull request or the bot's folder, and the Submit or update bot screen. Open opens the bot's newest pull request on GitHub, or the bot's folder in the bot repository when there is none; only https pages on github.com are opened. Update opens the Submit or update bot screen, where the person chooses the bot's folder. A button below the list opens the Submit screen for a new bot.
- **The list is read when the screen opens**, away from the screen's event thread, and a reading that arrives after the screen is closed is thrown away. There is no refresh button: opening the screen again reads it again.
- **The behaviour is a plain class in the shared library** that builds the rows from the two readings, proved without a window and without GitHub, and the screen only shows the rows. Every text is in the resource files in all four languages.

## Challenge

- **Assumption most likely to be wrong:** that the account's sent bots are all found by the branch names the program uses. A bot sent by hand with another branch name is still found when it is in the catalog, by its owner, but its pull request before the merge is not listed. The mockup's list is for bots sent with the program, and the command line uses the same names.
- **Credible alternative:** also scan the person's own bot folders to link each row to its local project, as the mockup's note suggests. Left out of this change: Update opens the Submit wizard, where the folder is chosen anyway, and scanning folders adds work and failure cases for little gain now.
- **Cheapest test:** the class that builds the rows with fake catalogs and pull requests for every case (only in the catalog, only sent, an open update of a bot in the Rumble, closed, merged but not yet in the catalog, a bot of another account, a team), and the maintainer's trial against the test Rumble with the bots already there.
- **Result that revises the work:** if one pull request list of a hundred entries does not reach an author's sent bots in practice, the reading must page, and the slice grows.
- **How it could pass every criterion and still fail an author:** a GitHub rate limit or a slow answer makes the screen look empty. The screen says when a reading failed and why, and never shows an empty list as if the account had no bots.

## Out of scope

Withdrawing a bot (a later milestone), linking rows to local project folders, Results and queue, and removing the developer switch.

## Acceptance criteria

Test class Unit unless stated.

- **RCL-147: My bots lists one row per bot of the signed-in account** from the catalog's entries it owns and the pull requests it sent, with the newest version, the language, and nothing of another account.
- **RCL-148: Each row says where the bot stands**: an open pull request's state before the version in the Rumble, the catalog state when nothing is open, merged and closed pull requests as such, and a reading that failed is said, never shown as an empty list.
- **RCL-149: My bots comes after Submit or update bot in the menu, needs a sign-in and leads on**: without a sign-in it offers the GitHub account screen, Open opens only an https page on github.com, Update and Submit a new bot open the Submit screen, and every text is in every language.
- **RCL-150: The maintainer opens My bots against the test Rumble and sees the account's bots with where each stands, and Open and Update lead where RCL-149 says.** (Human.)

## Identity allocation

The change and the criteria were allocated with the repository's coordinated allocation.

## Human evidence

On 2026-11-22 the maintainer opened Rumble, Mine bots in the desktop program set to the development Rumble, with the saved sign-in, in the dark theme. The first trial stayed on "Henter dine bots fra Rumble..." because the table's column rule was refused by the layout manager on the screen's event thread; the error was read from the program's output, the rule fixed, and the table is now built in a test so such a rule fails there. The screen then listed the account flemming-n-larsen's bots with version, language and where each stands (screenshots): the bots in the rankings in green, and CliSmokeBot and RumbleOnboardingScout, whose pull requests were closed, in red. Seeing retired test bots and closed submissions, the maintainer asked whether they could be removed or ignored; they cannot be deleted from the Rumble, so they are now hidden unless a box, which shows their number (4), is ticked, and the maintainer saw the list both with and without the box ticked. The catalog's word "retired" was shown untranslated and now has a text in every language. The maintainer asked what Open and Update do; they now have tooltips, first as plain tooltips, which the maintainer saw were black instead of the program's yellow ones, and then with the program's own tooltip and texts from the hint files, which the maintainer confirmed look right. The maintainer reported that Open and Update work ("Åbn og opdater knapper virker").
