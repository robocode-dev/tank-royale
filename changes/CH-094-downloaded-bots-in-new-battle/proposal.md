---
id: CH-094
type: change
status: open
links: [P-007, CAP-016, UC-005, G-004]
title: Downloaded bots are added to the bot directories with a label and shown as their own group in New Battle, and Bot Info shows the license
---

# Downloaded bots are added to the bot directories with a label and shown as their own group in New Battle, and Bot Info shows the license

Today the Download bots screen places bots in a folder and tells the person to add that folder under Config, Bot Root Directories by hand. In New Battle the downloaded bots are then mixed in among the person's own bots, sorted by name, with nothing saying which are downloaded. There is also a weakness in the default folder: the screen suggests a `rumble-bots` folder beside the person's first bot directory. When the person adds a download folder as their first bot directory, the suggestion moves next to the download folder, as the maintainer saw in the trial of the platform checks.

This change finishes the part of the plan item for downloading competitor bots that concerns the folder and New Battle.

## Why

Downloaded bots are there to practise against, so they should be ready in New Battle without a trip to the settings. They are other people's code, so the person should always see which bots are downloaded, and their own bots should come first.

## What changes

- **The folder is added by itself.** When the screen places the first bot in a folder, that folder is added to the bot directories, switched on, with the label "Rumble bots (downloaded)". The result screen says so in one line, and the person can switch it off or remove it under Config, Bot Root Directories, as with any other folder. A folder already in the bot directories is not added again. A folder the person removed is not added again silently: the screen adds it again only on the next download into it, and says so.
- **Labels are kept beside the existing setting.** The existing setting of the bot directories, a list of paths each switched on or off, is read and written exactly as today. The labels are a separate setting keyed by path, so an older version of the program reads the same bot directories as before, and no existing directory is lost or changed. The Bot Root Directories dialog shows the label beside a labelled folder.
- **New Battle shows them as their own group.** In New Battle's list of bots in the bot directories, the person's own bots come first, sorted as today. The bots from labelled download folders follow, under a header row with the label and the number of bots, each shown by its own folder name. The header row and the group sit below the list of the person's own bots, outside its sideways scrolling, so the header is always in view; a prototype that put the header inside that list was scrolled out of view, as the maintainer saw. The group is collapsed when New Battle opens; clicking the header opens or closes it. A downloaded bot can be booted like any other once the group is open.
- **Bot Info shows the license.** New Battle's Bot Info panel gains a License field beside the other fields, for every bot, not only downloaded ones: the license the bot's own configuration file declares, as written, or "No license declared". A standard SPDX license identifier links to its page on the SPDX license list, the same official text the Download bots screen links to. The booter already reads the license from the bot's folder; today it is dropped on the way to the panel. For a bot that has joined the battle on its own, the license is taken from the bot's folder when a bot with the same name and version is found in the bot directories, and otherwise the field says that it is not known. This is the maintainer's wish while New Battle is being changed anyway.
- **The default folder no longer moves.** The suggested download folder is worked out from the person's first own bot directory, skipping labelled download folders. With no own bot directory, it stays in the home folder as today.
- **Texts** are resources in every language file, English until translated.

Nothing here changes how bots are booted, how the booter lists a folder, or what the ranked contribution reads. Ranked contribution does not read the bot directories at all.

## Challenge the commitment

The riskiest assumption is that adding a folder to the bot directories by itself is welcome. The bot directories are the person's own setting, and a program that edits it can surprise. The change keeps that edit small and visible: only a folder this screen just put a bot into, only with a label, said on the screen at that moment, and undone the usual way. The credible alternative is a box on the result screen, "Use these bots in New Battle", that the person ticks. It was not chosen as the default because the plan and the mockups already promise that the folder is added, and an extra box on a screen the maintainer found too full is a cost of its own; it is a question below.

Grouping in New Battle changes a screen used by everybody, also people who never download a bot. For them nothing changes: without a labelled folder there is no header row and the list is exactly as today.

## Out of scope

- The copy-permission level beside the license and a team's members in Bot Info, which the same plan item asks for. They are proposed as a change of their own.
- Removing a downloaded bot from the folder from the screen.
- A different warning when a bot directory no longer exists; today the booter's warning appears each time New Battle opens.
- Translations.
