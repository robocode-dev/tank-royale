---
id: CH-112
type: change
status: open
links: [P-007, M-039, CAP-016, ARCH-028, G-004]
title: Add the Results and queue screen to the Rumble menu, which shows the ranked results kept on this computer, the batches sent and what the Rumble answered, without changing anything
---

# Add the Results and queue screen to the Rumble menu, which shows the ranked results kept on this computer, the batches sent and what the Rumble answered, without changing anything

This is the last missing screen of the milestone that releases the complete Rumble menu (M-039) in the [Rumble participation plan](../../docs/plans/P-007-rumble-participation.md). It proceeds under the [simple-participation goal](../../docs/goals/G-004-simple-rumble-participation.md).

## Why

A contributor's battles become ranked results that wait on the computer, are sent in batches as issues to the Rumble's data repository and are accepted or rejected by its receipt. Today the person sees only counts, on the Contribute battles screen while a session runs. The [Rumble menu mockups](../../docs/design/rumble/gui-mockups.md) give them a screen that shows what is kept, each batch and what became of it, and why something was rejected or held back.

## What changes

- **Results and queue** comes after Contribute battles in the Rumble menu, as drawn. It needs no sign-in, because it reads only this computer.
- **It reads, and never writes, the two files a contribution session keeps** for the Rumble it talks to: the journal of ranked results and the list of sent batches. The files' own `open` repairs a torn last line and may create the file, which could cut a line a running session is writing, so the screen uses a new reading that only reads, leaves a torn last line out and refuses a journal made for another Rumble.
- **A summary line** counts the results: kept in all, waiting to be sent, sent and waiting for the receipt, accepted, rejected, and set aside because the engine changed under them.
- **A list of batches**, newest first: the batch, its number of battles, when it was sent, and where it stands, waiting for the receipt, accepted, or how many were rejected with the Rumble's reasons. Open opens the batch's issue on GitHub, only as an https page on github.com.
- **The situations of the mockup that this computer can tell**: results waiting to be sent, with the note that a contribution session sends them, and without a saved sign-in, that signing in is needed and a button for the GitHub account screen; sending stopped because the Rumble refused a batch, with its reason; results set aside, with why they are kept apart.
- **The list is read when the screen opens**, away from the screen's event thread, with no refresh button, and the behaviour is a plain class in the shared library, proved without a window. Every text is in the resource files in all four languages, and tooltips use the program's own tooltip.

## Challenge

- **Assumption most likely to be wrong:** that reading the two files while a session writes them gives a true picture. The session only appends whole lines, so a reading sees every line written before it, and a last line still being written is left out; the next opening sees it.
- **Credible alternative:** let the screen send waiting results itself and offer Retry now, as the mockup draws. Left out: sending belongs to contribution sessions, which already retry by themselves, and a second sender in the desktop program would race them over the same files. The screen says that a session sends them.
- **Cheapest test:** the reading and the plain class with journals and batch lists written in a temporary folder for every state, a torn last line, another Rumble's journal and missing files, and the maintainer's trial with the results already in the test Rumble's journal.
- **Result that revises the work:** a journal so large that reading it at opening takes seconds would need a reading that is kept up to date instead.
- **How it could pass every criterion and still fail a contributor:** the mockup's "In rankings" count is not shown, because this computer cannot know it; the person may look for it. The Rankings entry of the same menu shows the rankings.

## Out of scope

Sending or retrying from this screen, the "In rankings" count, changing or deleting results, and removing the developer switch.

## Acceptance criteria

Test class Unit unless stated.

- **RCL-151: The results kept on this computer are read without being changed**: the journal and the list of sent batches are only read, a torn last line is left out, a journal of another Rumble is refused, and missing files mean nothing is kept yet.
- **RCL-152: Results and queue counts the results and lists the batches newest first with where each stands**: waiting for the receipt, accepted, or rejected with the Rumble's reasons, and the time each was sent.
- **RCL-153: Results and queue says what holds results back**: waiting results and that a session sends them, a missing sign-in with the account screen, a refused batch with its reason, and results set aside with why; Open opens only a batch issue on github.com; it follows Contribute battles in the menu and every text is in every language.
- **RCL-154: The maintainer opens Results and queue against the test Rumble and sees the results and batches already kept on the computer.** (Human.)

## Identity allocation

The change and the criteria were allocated with the repository's coordinated allocation.

## Human evidence

To be written after the maintainer's trial, before the digest.
