---
id: CH-092-open-questions
type: open-questions
status: resolved
links: [CH-092]
title: The slice, the license level and the folder in New Battle
---

## Should this change stop at the screen, or also put the downloaded bots into New Battle?

The mockups show the download folder added to the bot directories, with its bots as a collapsed group after the person's own in New Battle. That touches the settings of the desktop program and the New Battle dialog, which are outside the Rumble menu and used by everyone, while the screen alone touches nothing but its own dialog.

Settled by the maintainer: the screen only. The person can add the folder as a bot directory by hand in the existing settings, and the New Battle group becomes its own change.

## Should the screen show the three-level license reading aid now?

The mockups show a level (free to copy, copy with conditions, do not copy) beside each license, and the same levels in the Bot Info panel. The rule that maps a license to a level has to be written and tested once for both places.

Settled by the maintainer, with a better idea than the recommendation: no levels in this change, and no license texts copied or kept by the program. The screen shows the license each bot declares, and a link to the official text of that license, so the program never holds or retypes a license text that could drift from the real one. The link is built only from a declared license identifier of the plain SPDX form, only to the official SPDX license list over https, and is opened only when the person presses it; a license that is not a plain identifier gets no link and its declared words are shown as they are. The bot's own files, including any license file the author put in them, are downloaded as they are, so the license stays with the bot without the program doing anything. The levels stay for the change that adds the License rows to Bot Info.

## How does the screen know earlier downloads?

The library fetches and verifies bots and leaves an existing folder untouched. The screen's states (new, update, up to date, retired) need to tell which folders in the download folder were downloaded by the screen and which version they hold.

Settled by the maintainer: a small marker file, as a touch file. The proposal goes one step simpler than recording names and versions: an empty marker file in each bot folder the screen placed says only that the screen downloaded that folder. Everything else is read from the folder itself: the name and version from the bot's own configuration file, and whether the content is unchanged by computing the same checksum the catalog records and comparing it with the catalog. A folder without the marker is never touched, so the person's own bots are safe even when the names match. A downloaded bot whose files the person has changed shows as changed here and is never overwritten by an update unless the person asks. Nothing is recorded outside the download folder, and deleting the folder forgets everything.
