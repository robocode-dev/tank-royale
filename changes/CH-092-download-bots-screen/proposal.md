---
id: CH-092
type: change
status: open
links: [P-007, ARCH-028, CAP-016, UC-005, G-004]
title: Add the Download bots screen to the Rumble menu of the desktop program
---

# Add the Download bots screen to the Rumble menu of the desktop program

The Rumble menu of the desktop program, behind its developer switch, has two screens today: the GitHub account and Submit or update bot. This change adds the third, **Download bots**: a person picks active competitor bots from the Rumble's catalog, chooses a folder, and gets verified copies of the bots to read and to practise against. It is the first screen that needs no GitHub account.

## Why

Reading other people's bots is the quickest way to learn what works in the Rumble, and practising against them is the natural next step after a first bot. The shared library already reads the pinned catalog, fetches sources from the exact catalog commit and places each bot only when its source matches the hash the catalog records, and the command line already uses it. The screen puts that behind the layout the maintainer reviewed, so a participant does not need a terminal to get the competitors. Of the unbuilt screens it is the smallest and the safest: it publishes nothing, needs no sign-in and sends nothing to GitHub.

## What changes

- **A Download bots item in the Rumble menu** and a dialog for it, behind the same developer switch and with the same environment shown in the title as the other screens.
- **Choose.** The dialog reads the pinned catalog and lists the active bots with name, version and platform. The person picks all active bots or selected bots, can narrow by platform, and chooses the target folder. The default is a folder called `rumble-bots` beside the person's normal bots folder.
- **The licenses in short.** A list box holds the four licenses the Rumble allows (MIT, Apache-2.0, BSD-3-Clause, GPL-3.0-or-later). Choosing one shows a few plain lines of what a person may do, what they must do when they share the code, and what is not promised, with a link to the official text and a link to a fuller explanation. The screen also says that reading a bot or practising against it asks nothing of the person, because the conditions apply when code is shared.
- **A state for every bot.** Each row says whether the bot is new, an update of an earlier download, up to date, or no longer active. A retired, withdrawn or disqualified bot that was downloaded earlier stays in the folder and is marked; a new download never offers an inactive bot. The screen knows its earlier downloads by an empty marker file it puts in each bot folder it placed; the version comes from the bot's own configuration file and the content check from the same checksum the catalog records. A folder without the marker is never touched, and a downloaded bot the person has changed is shown as changed and is not overwritten unless the person asks.
- **Download and result.** Pressing Download fetches the chosen bots on a background thread and then shows one result line per bot: placed (new or updated, with the license its own configuration file declares and its links), hash differs and skipped, not in the archive, or kept because a folder with that name already exists. One failing bot never stops the others. An interrupted download can be continued, and every bot that was placed is shown as placed.
- **Never overwrites the person's own bots.** A downloaded bot is placed only inside the chosen download folder and never replaces a bot of the person's own, even when the names match; an update replaces only an earlier download of the same bot.
- **A plain answer for every failure.** The catalog cannot be read or is of an unsupported version, the folder cannot be written (told before anything is fetched), the network fails, an archive entry tries to leave the folder: each has its own text and a way on.
- **The behavior stays separate from Swing**, as for the earlier screens: the states, transitions and texts live in a plain class in the shared library with a port for the catalog and the fetch, every call to GitHub or the disk runs off the event thread, results arrive in order and a result that arrives after closing is dropped. All texts are resources in every language file (English until translated), every text wraps at the window edge, and success and failure use the green and red the maintainer chose.
- **Documents.** The participation design and the mockups' status note record what is built and what is proved. The acceptance criteria for this screen are written before any code; the criteria for verified, kept-apart downloads and for retired bots already exist and are made precise for the screen where needed.

## Challenge

**The downloaded code is other people's code.** The screen says so in words before the download, never calls the bots safe, and does nothing with them but place them in the folder: nothing is run, built or added to any list of bots to start. A test shows that the screen only places files.

**A download must not be able to write outside its folder or replace the person's own work.** The library already refuses archive entries that leave the folder and leaves an existing folder untouched. Tests show both through the screen, with an archive that tries to escape and with a bot of the person's own with the same name in the same folder.

**Nothing is fetched in silence or from the wrong place.** Bots are fetched only after the person presses Download, only from the pinned catalog commit and only into the chosen folder. All tests use a fake catalog and fake archive; nothing in the tests or the build contacts the real Rumble. The hand trial reads the real public catalog of the test environment and downloads a few bots into a temporary folder that is deleted afterwards.

**The short explanations are ours, and they can be wrong.** They are a few lines in our own words, built on the permissions, conditions and limitations that choosealicense.com lists for each license, whose content is under Creative Commons Attribution 3.0, and the screen credits it. They say plainly that they are not legal advice, and the official text decides. The maintainer reads every explanation before it is accepted. The program holds no license text. A license is opened only from a fixed list of addresses in the library, only when the person presses, and a test fails if the Rumble allows a license that has no explanation and no links. The three-level reading aid in the mockups is left out, because the explanations do its work; whether Bot Info needs it is for the change that adds the License rows there.

**The bot's license is not known before the download.** The catalog does not hold it, and reading it would take one call per bot. Reading a bot or practising against it asks nothing of the person, so the license is shown where it starts to matter: in the result, and for a bot already in the folder, both read from the bot's own file on the disk. This changes what the use case, the mockups and the participation design say, and they are corrected in this change.

**Things the proof cannot reach.** One real download of many bots from the public environment, other operating systems than Windows, and how a very large catalog behaves in the list.

## Not in this change

Adding the download folder to the New Battle bot directories and showing it as a collapsed group (the person adds the folder by hand in the existing settings), the three-level license reading aid, a `LICENSES/` folder with the four license texts in the bot repositories, a license chooser for authors in Submit or update bot, translations of the explanations, the License and Team members rows in Bot Info, the other Rumble menu items (Get started, My bots, Contribute battles, Results and queue, Rankings, Help), fetching the command line's worker, and the public sign-in App. The Rumble menu stays behind its developer switch until it is whole.

## Publication and acceptance

This branch is local and not pushed, and has no pull request, because local acceptance is the accepted policy and the maintainer has not asked for publication. The work serves the simple-participation goal; the repository states no vision. The maintainer reviews this proposal before any criterion, code or document is changed.
