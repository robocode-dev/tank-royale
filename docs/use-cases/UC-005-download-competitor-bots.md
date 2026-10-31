---
id: UC-005
type: use-case
status: draft
links: [G-004, CAP-014, CAP-016, ARCH-028]
title: Download competitor bots to study and practise against
provenance: inferred
reversal-cost: low
---

# Download competitor bots to study and practise against

## Actors

A Robocoder, the desktop application, the published bot catalog, and the bots the Robocoder already keeps.

## Trigger

The Robocoder wants to read how other entries work or to practise against them without finding and copying each bot's folder by hand.

## Main flow

1. Choose Download bots under Rumble. No account is needed, because the catalog is public.
2. Choose all active bots or a selection, optionally filtered by platform, and see each bot's version and declared license.
3. Accept the destination folder or choose another. By default it is a separate `rumble-bots` folder beside the Robocoder's normal bots directory.
4. Start the download. Each bot's source is fetched from the pinned catalog and checked against its recorded hash before it is placed in the folder.
5. Read the result list, then find the downloaded bots in New Battle under a collapsed, labelled group placed after the Robocoder's own bots. Run, read or practise against them. Come back later to refresh or clear the folder.

The [Rumble menu mockups](../design/rumble/gui-mockups.md) show the Download bots screen and the New Battle group as a planned design.

## Outcome

The Robocoder has a set of real competitor bots, clearly kept apart from their own, that they can read and fight against. Nothing of theirs was overwritten.

## Failure and recovery

Offline or interrupted downloads leave completed bots in place and can be continued. A bot whose hash does not match is not placed in the folder and is reported. A folder that cannot be written is explained before anything is fetched. A bot with the same name as one of the Robocoder's own is kept in the download folder only and never replaces the original. A bot the Rumble later withdraws or disqualifies stays in the folder, marked as no longer active, until the Robocoder removes it. The screen states that a downloaded bot is other people's code and runs with the Robocoder's own rights, and that review by moderators does not make it safe.
