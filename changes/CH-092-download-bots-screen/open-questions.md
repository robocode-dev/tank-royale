---
id: CH-092-open-questions
type: open-questions
status: open
links: [CH-092]
title: The slice, the license level and the folder in New Battle
---

## Should this change stop at the screen, or also put the downloaded bots into New Battle?

The mockups show the download folder added to the bot directories, with its bots as a collapsed group after the person's own in New Battle. That touches the settings of the desktop program and the New Battle dialog, which are outside the Rumble menu and used by everyone, while the screen alone touches nothing but its own dialog.

Recommendation: build the screen only. The person can add the folder as a bot directory by hand in the existing settings, and the New Battle group becomes its own change, because it changes a screen that people who never use the Rumble see.

## Should the screen show the three-level license reading aid now?

The mockups show a level (free to copy, copy with conditions, do not copy) beside each license, and the same levels in the Bot Info panel. The rule that maps a license to a level has to be written and tested once for both places.

Recommendation: show the declared license text in this change and leave the levels, with the Bot Info rows, to a change of their own, so the mapping is written once, with its tests, where both screens use it.

## How much of "update" and "no longer active" can the screen rely on from the library?

The library fetches and verifies bots and leaves an existing folder untouched. The screen's states (new, update, up to date, retired) and the refreshing of an earlier download need the library to tell which folders in the download folder were downloaded earlier and which version they hold.

Recommendation: when the criteria are written, check what the library already does. If it cannot tell, record what was downloaded in a small file inside the download folder itself (name, version, hash), never elsewhere on the computer, so the person can delete the folder to forget everything. I will report what I find before building.
