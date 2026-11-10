---
id: CH-095
type: change
status: open
links: [P-007, CAP-016, G-004]
title: Bot Info shows a copy level beside every bot's license, and a team's members
---

# Bot Info shows a copy level beside every bot's license, and a team's members

New Battle's Bot Info panel shows the license a bot declares, with a Show button that opens the license's page on the SPDX list. It does not say what the license lets a reader do with the bot's code, and for a team it does not say which bots the team is made of. The plan item for downloading competitor bots promises both: "The Bot Info panel in New Battle shows the declared license with a copy-permission level and a team's members for every bot." The Rumble menu mockups draw them, and an earlier change left them for a change of their own.

This change finishes the plan item for downloading competitor bots. The rest of its exit criterion was checked against the code before this proposal: the screen says that downloaded bots are other people's code; a downloaded bot that is no longer active stays and is marked; a bot with the same name as one of the person's own is kept apart and never replaces it; each bot is unpacked in a hidden work folder and moved into place only when complete, so after an interruption the missing bots are listed as new and fetched by the next press of Download; and the download cases run through the fixture command.

## Why

Downloaded bots are there to practise against, and their code is there to read. Anyone who reads another person's code should know whether they may copy from it. A license identifier such as `GPL-3.0-or-later` or `CC-BY-NC-4.0` says that only to someone who knows the licenses. A plain level beside it says it to everyone. A team's members tell the person what they will be fighting before they start a battle.

## What changes

- **A copy level beside the license.** Bot Info shows one of three levels after the declared license, always as a small shape and words, never as colour alone:
  - **Free to copy** (a circle): a license on the Blue Oak Council's list of permissive licenses rated Model, Gold, Silver or Bronze, such as MIT, the BSD licenses, Apache-2.0, ISC, Zlib, BSL-1.0, the Unlicense, 0BSD and CC0-1.0.
  - **Copy with conditions** (a triangle): a license on the Blue Oak Council's list of copyleft licenses, such as the GPL, LGPL and AGPL licenses, MPL-2.0, EPL-2.0 and EUPL-1.2, which require a copy to be shared under the same terms; a permissive license the council rates Lead, because such a license lacks an essential element or adds an unusual requirement and must be read before copying; and the Creative Commons attribution licenses CC-BY and CC-BY-SA, which require the author to be credited.
  - **Do not copy** (a square): no license declared, a license on neither list, and the Creative Commons no-derivatives and non-commercial licenses such as CC-BY-NC-4.0 and CC-BY-ND-4.0. It is the law's default that nobody may copy a work without a license.
- **Where the lists come from.** The Blue Oak Council, a council of licensing lawyers, publishes its list of permissive licenses (version 16, 225 licenses) and its guide to copyleft licenses (version 11, 61 licenses) as data under SPDX identifiers. The program keeps the identifiers taken from those versions as a plain list in the shared Rumble library, beside the SPDX list it already keeps, and a newer list is taken by replacing the file. Creative Commons licenses are on neither list, as they are not software licenses, so their levels follow Creative Commons' own terms. An identifier the SPDX list keeps as deprecated, such as `GPL-3.0` or `GPL-2.0+`, gets the level of the current identifier it stands for.
- **License expressions.** A license written as `MIT OR Apache-2.0` is at the level of its most permissive choice, because the reader may choose; `A AND B` is at the level of its most restrictive part; `GPL-2.0-only WITH Classpath-exception-2.0` is at the level of the license before `WITH`. An expression with parentheses is at the do-not-copy level, as an unrecognised license.
- **A reading aid, not legal advice.** The level's tooltip says that it reads only what the bot declares, cannot check that the declaration is true, and is not legal advice, and that Show opens the license's own text.
- **No level when the license is not known.** A bot that joined the battle on its own and has no bot of the same name, version and authors in the bot directories still shows that its license is not known, without a level.
- **Team members.** Bot Info gains a Team members row. For a team it lists the members by name exactly as its configuration file names them, in order, a repeated member each time, such as `MyFirstLeader, MyFirstDroid, MyFirstDroid, MyFirstDroid, MyFirstDroid`. That is how the original Robocode keeps a team's members too, in its `team.members` setting. For a single bot it says that the bot is not a team; for a bot that joined on its own it says that the members are not known.
- **The Download bots screen** shows the same level beside each bot's license in its list and results, as the mockups draw. A bot accepted into the Rumble always carries one of the licenses the Rumble allows: MIT, Apache-2.0 and BSD-3-Clause are free to copy, and GPL-3.0-or-later is copy with conditions, so a downloaded bot is never at the do-not-copy level.
- **Shared rules.** The level is worked out in the shared Rumble library from the license text, with no Swing and no disk, so the command line can use it later. The texts are resources in every language file, English until translated.
- **Documents:** the participation design and the mockups say what was built; the plan records the slice.

Nothing changes how a bot is booted, what the booter reads, or what the game protocol carries: the desktop program already receives the license and the team members from the booter.

## Challenge the commitment

The riskiest assumption is that a level is safe to show at all. A level could be read as legal advice, and a license list kept in the program can be wrong or out of date. The change keeps the level a reading aid: it is computed only from what the bot declares, every license it does not know is at the most careful level, the tooltip says that it is not legal advice, and the license's own text is one click away. The credible alternative is to show only the license and the Show button, as today, which is exactly right but helps only readers who know the licenses; the plan and the mockups chose the level.

## Out of scope

- The Folder row and the link to the full description that the mockups also draw for Bot Info.
- A level for licenses written with parentheses, beyond treating them as unrecognised.
- New Battle's window not adapting when resized, which is left for the planned tests of the user interface.

## Acceptance criteria

The capability for the Rumble client gains these criteria:

- **The copy level is worked out from the declared license.** Every license on the two lists, in any letter case, gets its level, a Lead license and a Creative Commons attribution license are copy with conditions, and a deprecated identifier gets the level of the identifier it stands for; a license on neither list, a no-derivatives or non-commercial license, and no license get the do-not-copy level; OR, AND and WITH expressions follow the rules above; an expression with parentheses is at the do-not-copy level; and a license that is not known has no level. (Unit)
- **Bot Info shows the level and the team members.** The License row shows the level as a shape and words after the license, with the tooltip, and the Team members row lists a team's members with counts or says that the bot is not a team or that its members are not known; the Download bots screen shows the same level beside each bot's license; and every text is in every language file. (Unit)
- **The maintainer reads the levels and the team members.** In the desktop program with the menu switched on for the test environment, the maintainer sees the level for a downloaded bot and for bots of their own with different licenses, the members of a team such as MyFirstTeam, and the level on the Download bots screen. (Human)
