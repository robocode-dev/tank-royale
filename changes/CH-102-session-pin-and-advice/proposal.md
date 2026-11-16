---
id: CH-102
type: change
status: open
links: [P-007, CAP-016, CAP-015, CAP-021]
title: Read the Rumble's engine pin and matchmaking advice from the data repository for a contribution session, and refuse a session this program cannot run under the pin
---

# Read the Rumble's engine pin and matchmaking advice from the data repository for a contribution session, and refuse a session this program cannot run under the pin

The session loop in the shared library runs a contribution session's battles, but it is handed the Rumble's engine pin, the rounds and sizes of each game format, and the published advice about which battles are needed. In the running Rumble these live in the result repository of each environment: `engine.json` pins the behavior version and the rounds, battlefield and participant count of `1v1`, `twinduel` and `melee`, and `matchmaking/matches_needed-<format>.json` lists the battles that are needed most. The plan item for contributor-controlled unattended sessions (M-030) needs a session to run exactly what the Rumble ranks, so the session must take these from the result repository and not from its caller.

This slice reads them in the shared library only: no screen and no command. Batches, posting, receipts, restart after a crash and the screens are later slices.

## Why

A battle run under another behavior version, another number of rounds or another battlefield than the pin is rejected by the result data, so the contributor's time is wasted and they are not told why. Reading the pin before the first battle, and refusing the session with a plain reason when this program cannot run under it, makes that impossible. The advice makes contributors spread over the battles the rankings need instead of repeating well-sampled ones.

## What changes

- **The engine pin is read from the environment's result repository.** A reader in `lib:rumble` fetches `engine.json` of the chosen environment over the public network without a credential, the way the bot catalog is already read, and checks its schema version, a positive behavior version, and rounds, battlefield and participant count for each game format.
- **A session is refused when this program cannot run under the pin.** Before the first battle, a behavior version other than this program's, a format whose battlefield or participant count differs from what the battle worker runs for that format, or a pin that cannot be read or understood, ends the request with a reason code and its values. The message for the contributor (such as "update Tank Royale to take part") belongs to the screens and the command line.
- **The session runs the pin's rounds and participant counts.** The rounds per format and the battle sizes the planner uses come from the pin, and the record's engine behavior version is the pin's.
- **The advice is read from the result repository and is optional.** The needed-battles file of each format becomes the planner's advice groups. A missing, unreadable or unknown-schema advice file gives no advice and the session goes on by coverage; advice is read again at most every few minutes during a long session, and a failed re-read keeps the last advice.
- **Both readers take their source as an interface,** so tests use fixtures and nothing in a test reaches the network.

## Challenge

- **Assumption most likely to be wrong:** that the battle worker's built-in setups for `1v1`, `twinduel` and `melee` match the pin's battlefield and participant counts. If they do not, the session is refused for every format, which is the honest outcome, and passing the battlefield to the worker becomes its own change.
- **Credible alternative:** read the pin and advice from the bot repository or ship them with the program. Rejected: the result repository is where the result data checks battles against the pin, so a copy elsewhere can drift from the check.
- **Cheapest test:** fixture pins and advice files read through a fake source, a session that is refused for each reason, and one session that runs with the pin's rounds and the advice's battle first. A one-off check reads the real public pin and advice files with the reader and reports what it found, without running a battle.
- **Result that revises the work:** a real pin or advice file the reader cannot understand without guessing a field stops the slice.
- **How it could pass every criterion and still fail a contributor:** the reader refuses a session for a reason the contributor cannot act on. Every refusal therefore names what differs (the pin's value and this program's) so the screens can say what to do.

## Out of scope

Choosing or pinning the commit of the bot catalog, passing a battlefield or other settings to the worker, cutting batches, posting, receipts, restart after a crash, and the screens and commands.

## Acceptance criteria

These will be written as criteria of the Rumble client capability, each with its identity, when the work starts. Test class Unit unless stated.

- **The engine pin is read and checked**, and a pin that cannot be read or understood refuses the session with a reason.
- **A session is refused before any battle when this program cannot run under the pin**, naming what differs, and otherwise runs the pin's rounds and battle sizes with the pin's behavior version in each record.
- **The advice is read for each format and is optional**: a session with advice runs an advised battle first; without advice or with an unreadable advice file it runs by coverage; a failed re-read keeps the last advice.
- **The real public pin and advice files are understood by the readers.** (Integration, reads the network; skipped without it.)

## Identity allocation

The change was allocated with the repository's coordinated allocation. Criterion identities are allocated when the criteria are written.

## Human evidence

None expected: every criterion here is proved by tests.
