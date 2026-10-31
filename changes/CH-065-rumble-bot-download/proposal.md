---
id: CH-065
type: change
status: open
links: [P-007, M-033, ARCH-027, ARCH-028, CAP-014, CAP-016, UC-001, UC-002]
title: Design downloading Rumble competitor bots into a separate bots directory
---

# Design downloading Rumble competitor bots into a separate bots directory

This change adds one planned feature to the Rumble participation design: a Robocoder can download selected or all competitor bots from the Rumble into a bots directory of their own, kept beside the directory they normally use. It is design only. It writes no code and no acceptance criteria. It serves the Rumble participation plan by adding one milestone to it, placed after the one that makes bot details and metadata readable.

## Why

Reading and fighting against the best bots is how a bot author learns, and the Rumble already publishes every active bot with its source and a declared open-source license. Today the only way to get another entry is to find its folder in the bot repository by hand, which is the repository-and-command work this plan exists to remove. A download option closes that gap and makes practice battles against real opponents a few clicks. It also fits the existing rules: the bots are public, licensed for copying, and each carries a content hash that can be checked.

## What changes

- A new use case: a Robocoder downloads competitor bots to study their source and practice against them.
- The Rumble menu mockups gain a Download bots screen: choose all active bots or a selection, filter by platform, choose the destination folder (by default a `rumble-bots` folder beside the normal bots directory), see each bot's license, watch progress, and read a result list. The screen marks bots that have a newer version and bots the Rumble has since withdrawn or disqualified. Failure cases are written out: offline, a hash that does not match, a folder that cannot be written, and a name that clashes with one of the user's own bots.
- The participation design gains a short section with the rules: downloading is optional and needs no GitHub sign-in because the data is public; only active bots are fetched, from the pinned catalog commit, and each is checked against its recorded hash; the license is shown before and after download; the user's own bots are never overwritten; a bot the Rumble later withdraws is marked in the folder's list rather than deleted without asking; ranked contribution keeps using only the pinned catalog and never reads this folder. The section also says who owns what: the catalog fetch and verification belong to the shared Rumble library, and the folder choice and labelling belong to the GUI.
- The Bot Info panel in New Battle gains License and Team members rows for every bot, with a three-level reading of the license (free to copy, copy with conditions, do not copy) shown as an icon and words.
- The plan gains a milestone for the feature, with its exit condition, and records that the bot-directory setting needs a label and migration of existing entries without loss.
- The draft client capability gains draft criteria for download, marked as not yet proven, because criteria for behaviour that nobody has built do not claim evidence.

## Challenge

The assumption most likely to undermine this work is that people want a managed folder of other people's bots rather than a plain way to open one bot. The cheapest alternative is a single Download button on each bot's details page that fetches one bot into the normal bots directory. That would need no new setting at all. This design prefers a separate folder because it keeps other people's code apart from the author's own, lets the folder be refreshed or removed in one step, and cannot overwrite an author's own bot, but the maintainer should say if the single-bot button is enough for now.

Another way it can fail while every rule above holds: a downloaded bot is run by the booter on the user's computer, and bot source from outside is untrusted code. The Rumble already reviews submissions and runs ranked battles in an isolated container, but a practice battle in the GUI runs the bot directly, as every bot in the normal directory does. The screens must therefore say that a downloaded bot runs with the user's rights, and the design must not imply that moderation makes it safe. This risk is stated in the design rather than left implicit.

## Not in this change

No code, no menu, no change to the bot-directory setting and no change to the live Rumble repositories. No new decision record: the rules above follow the existing licensing and catalog decisions. Whether downloaded bots appear in the New Battle bot list by default is an open question for the maintainer.

## Publication and acceptance

This branch is local and not pushed, and has no pull request, because local acceptance is the accepted policy and the maintainer has not asked for publication of this change. The work serves the simple-participation goal; the repository states no vision. The maintainer reviews this proposal before any document is changed.
