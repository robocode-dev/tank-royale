---
id: CH-118
type: change
status: open
links: [P-007, M-039, CAP-016, G-004]
title: Let a contributor send results again after the Rumble refused a whole batch, and let a new program version do so by itself
---

# Let a contributor send results again after the Rumble refused a whole batch, and let a new program version do so by itself

This is a slice of the milestone that releases the complete Rumble menu (M-039) in the [Rumble participation plan](../../docs/plans/P-007-rumble-participation.md). It proceeds under the [simple-participation goal](../../docs/goals/G-004-simple-rumble-participation.md).

## Why

When the Rumble's receiving workflow refuses a whole result batch before it reads its records, the sending stops on this computer, as it should: sending the same batch again would only be refused again. The stop is kept in the list of sent batches, and only a call that nothing in the program makes lifts it. A code review of the Rumble library found on 2026-11-25 that a single refused batch therefore stops a computer's sending for good. Results pile up in the journal across every later session, and the only way out is to edit a file by hand.

## What changes

- **A new program version lifts the stop by itself.** The refusal records the version of the program that sent the refused batch. When a session of another version starts, it lifts the stop and says so, because a refusal is most often about the format of what this program sends, which a new version may have fixed.
- **The contributor can lift the stop.** The Results and queue screen shows the Rumble's reason with a button, Send again. The command line gets `rumble session send-again`, beside the other commands that act on an environment's sessions. Both lift the stop only when no session of the environment runs, because a running session holds the list of sent batches. While one runs, they say to stop it first. The waiting results are sent by the next session, as now.
- **What the screen says while sending is stopped.** It names the reason the Rumble gave, the version that was refused, and the two ways on: update the program, or send again after fixing the cause. It also links to the Rumble feedback form, since a refusal the person cannot explain is worth reporting.
- **Every text** is in English, Danish, Spanish and Catalan.

## Challenge

- **Assumption most likely to be wrong:** that a refusal is most often fixed by a new program version. A refusal can also come from the contributor's account or registration. The stop then comes back at the next batch, so a lift costs only one batch that is refused again, and the reason is shown each time.
- **Credible alternative:** lift the stop at every session start and let the Rumble refuse again. That sends a doomed batch every session and fills the Rumble's issue list. The version rule and the person's own lift keep that to one batch per cause.
- **Cheapest test:** the stop recorded with the version, lifted by another version and kept by the same; the lift refused while a session runs; the screen's and the command's outcomes with fakes; and the texts in every language.
- **Result that revises the work:** if the receiving workflow refuses batches for reasons a new version never fixes, such as a banned account, the automatic lift must leave those reasons alone. The receipt's reason would then have to say so as a code.
- **How it could pass every criterion and still fail a contributor:** the person lifts the stop again and again without fixing anything. Each lift sends one batch, and each refusal is shown with its reason, so the cost is bounded.

## Out of scope

Telling the Rumble's maintainers about refused batches, reasons as codes from the receiving workflow, and resending one batch by hand.

## Acceptance criteria

Test class Unit unless stated.

- **RCL-170: A refusal records the program version, and a session of another version lifts it by itself and says so**; a session of the same version keeps it.
- **RCL-171: The contributor lifts the stop from the Results and queue screen or with `rumble session send-again`**, only when no session of the environment runs, and is told to stop the session first otherwise; the screen shows the reason, the refused version and the ways on in every language.
- **RCL-172: The maintainer sees the stop and lifts it from Results and queue on the development Rumble.** (Human.)

## Identity allocation

The change and the criteria were allocated with the repository's local allocation. A clone that has not pushed is invisible to a check of the remote.

## Human evidence

**Seen by the agent on 2026-11-27** on Windows, with the desktop program built from this change on the development Rumble. A refusal line was added for the trial to a copy of the list of sent batches, which was put back exactly afterwards. Results and queue showed, in Danish:

- the reason in red;
- "Den afviste pakke blev sendt af Tank Royale 1.4.0-trial";
- the ways on;
- the buttons Send igen and Meld det.

After Send igen the screen said "Afsendelsen kan fortsætte. Den næste session sender de resultater, der venter." The reason and the buttons were gone, and the list held the line that lifts the stop. A real refusal from the Rumble's receiving workflow was not caused, and a session of a new version lifting a real refusal was not seen; both are proved with fakes.
