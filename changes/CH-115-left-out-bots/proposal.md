---
id: CH-115
type: change
status: open
links: [P-007, M-039, CAP-001, CAP-016, CAP-021, G-004]
title: Leave a bot out of a session after it twice did not start, end a battle at once when a bot's process ends before it connected, and plan the Rumble's own start check of every active bot on each new image
---

# Leave a bot out of a session after it twice did not start, end a battle at once when a bot's process ends before it connected, and plan the Rumble's own start check of every active bot on each new image

This is a slice of the milestone that releases the complete Rumble menu (M-039) in the [Rumble participation plan](../../docs/plans/P-007-rumble-participation.md), and it adds a later milestone to that plan. It proceeds under the [simple-participation goal](../../docs/goals/G-004-simple-rumble-participation.md).

## Why

A bot that cannot start on a contributor's computer costs every battle it is chosen for the Battle Runner's whole connect limit of 30 seconds, also when its process ended at once, such as a C# bot whose build fails because the computer lacks the .NET version it targets, which the agent saw on 2026-11-24. The session chooses it again and again, so good bots in the same battles wait too, and after a few such battles in a row the whole session gives up. The maintainer chose on 2026-11-24 to keep the 30 seconds and to leave a bot out that times out again and again. A bot that fails on every start, also in the Rumble's own image, should not run or be downloaded at all, and its author must be told what failed and where; the maintainer decided the rule for that on 2026-11-24, and it belongs in the Rumble's repositories, so this change only plans it.

## What changes

- **A battle ends at once when a bot's process ends before it connected.** The booter already reports when a bot process stops or cannot be started at all; the Battle Runner now fails the start of the battle at once with the bots that had not connected, instead of waiting the rest of its connect limit. A bot that keeps running without connecting is still waited for until the limit.
- **The worker says which bots did not connect.** When a battle could not start because bots did not connect, in time or at all, the worker's failure carries a fixed code and the names and versions of those bots, from the catalog, never their output.
- **The session leaves such a bot out.** A bot that did not connect in two battles in a row of a session is left out for the rest of that session, with every team that holds it, as a bot that can never be placed is today; the session says which bot and why in its log, its status and on the screen. A battle it was left out of does not count toward the session giving up. The next session tries the bot again, because the cause may be the computer, such as a missing platform, and may be gone.
- **The plan gets the Rumble's own start check** as a new milestone in the Rumble's repositories, as the maintainer decided: on each new image and once a month the Rumble runs the moderation's start check again on every active bot; a bot that fails it twice in a row in the same run is set inactive at once, so it neither runs nor can be downloaded, and its owner gets one issue with the image's name and version and the command to run it, links to the platform versions of that image and of the last image the bot passed on, the failure code and the bot's own exception and stack trace, cleaned of the Rumble's own paths and anything like a secret and cut to its first hundred lines with the rest kept for download. The bot comes back when a new version passes, or when the same version passes a later check. A failure that repeats without a new version gives no new issue. When many bots of one language fail after a new image the check stops and tells the maintainer instead.

## Challenge

- **Assumption most likely to be wrong:** that the booter's stop report is reliable enough to end a battle on. A bot whose launcher starts a second process and ends, such as a script that hands over to its runtime, could look stopped while its real process still starts. The runner therefore ends the battle only for a booted process whose stop it saw and whose bot never connected, and a launcher that hands over keeps its first process running in every official starter; a test proves the official starters stay up.
- **Credible alternative:** read the booter's error lines, such as a failed build, to tell the contributor why. They differ by language and version and are not a stable interface; the stop of the process is. The reason stays a fixed code, and the bot's own output stays on the contributor's computer.
- **Cheapest test:** the runner with a booter whose bot process ends at once, the worker's failure code with the bots that did not connect, the session's count and its leaving out with teams on fakes, and a real session on the development Rumble with a bot that cannot start, such as a C# bot on a computer without its .NET version.
- **Result that revises the work:** if real starts of the official starters report a stop while they still start, the runner cannot end a battle early, and only the session's leaving out remains.
- **How it could pass every criterion and still fail a contributor:** a bot left out on a slow computer that only once missed the limit. Two battles in a row are needed, and the next session tries again.

## Out of scope

Telling the contributor that a platform is missing from the bot's own error output, which the platform check on Download bots already covers; sending a bot's start failures to the Rumble; and building the Rumble's own start check, which this change only plans.

## Acceptance criteria

Test class Unit unless stated.

- **BR-053: The Battle Runner ends the start of a battle at once when a booted bot's process ends before the bot connected**, naming the bots that had not connected, and still waits the connect limit for a process that runs without connecting.
- **RBW-026: The worker reports a battle whose bots did not connect** with a fixed code and the names and versions of those bots, and nothing else about them.
- **RCL-163: A session leaves a bot out for the rest of the session after it did not connect in two battles in a row**, with every team that holds it, says which bot and why, does not count those battles toward giving up, and tries the bot again in its next session.
- **RCL-164: A real session on the development Rumble with a bot that cannot start leaves it out after its second battle and runs on with the others.** (Human.)

## Identity allocation

The change, the criteria and the new milestone were allocated with the repository's local allocation. A clone that has not pushed is invisible to a check of the remote.

## Human evidence

To be written after the trial.
