---
id: CH-104
type: change
status: open
links: [P-007, CAP-016, CAP-024, ADR-055, ADR-052]
title: Run a whole contribution session from the command line with `rumble contribute`, from sign-in and pin to sent results
---

# Run a whole contribution session from the command line with `rumble contribute`, from sign-in and pin to sent results

The shared library can now choose battles, run them on the worker, keep them in the journal, check the Rumble's engine pin, read its advice and send the results as the signed-in contributor. Nothing joins these for a person yet. The plan item for contributor-controlled unattended sessions (M-030) asks that the desktop program and the command line share session behaviour; the command line comes first because it is the second front end over the same libraries and the quickest way to run a whole session end to end before the screen is built on the same pieces.

This slice adds one command to the Rumble command line. The desktop screen, the results-and-queue view and resource settings beyond the worker's limits are later slices.

## Why

Until a person can start a session, none of the parts built so far does anything for a contributor. A command that runs a whole session also shows where the parts do not fit together before a screen hides it, and gives scripts and agents an unattended way to contribute.

## What changes

- **`rumble contribute`** starts a session in the chosen environment with the options a session needs: the bots chosen (`--bot "Name version"`, any number), how they are used (`--mode all|prefer|only`), the game formats (`--format`, any number, default all the pin names), and a battle count (`--battles`, default none, so it runs until stopped).
- **Before the first battle** it renews the saved sign-in and reads the account's registered client ID, reads the catalog at the bot repository's current commit, reads and checks the engine pin, and puts the carried battle worker in place. A missing sign-in or registration, a pin this program cannot run under, or a catalog that cannot be read ends the command before any battle, with the existing exit codes and a reason the person can act on.
- **The session** runs with the existing session loop, journal, bot cache and automatic sending, all in the environment's state folder, and reads the advice again at its own interval. Starting the command again after a crash or a stop continues from the journal and the list of sent batches: kept results are never run again or lost, and batches that were handed over are sent again.
- **Stopping**: the first Ctrl+C finishes the active battle, keeps it, sends what waits and ends; a second Ctrl+C ends the worker now and keeps nothing of that battle.
- **What it reports**: each event as one short line, or with `--json` as one JSON object per line followed by a final document, so a script or the desktop program can follow it. Reason codes stay codes; the line text is plain English like the other commands.
- **Exit codes** follow the command line's rule: 0 the battle count was reached or the person stopped it, 1 the session gave up or the journal failed, 2 refused or bad input (including a pin this program cannot run under), 3 a sign-in or registration is needed.

## Challenge

- **Assumption most likely to be wrong:** that the carried worker runs real bots placed from the catalog the way the integration test ran sample bots. Real catalog bots need their own runtimes and launchers; a bot that cannot start ends its battle as not completed, the session reports it, and too many in a row end the session. If no real catalog bot can run on Windows this way, the slice stops there.
- **Credible alternative:** build the desktop screen first. Rejected: the command is smaller, needs no translations, and proves the whole chain the screen will depend on.
- **Cheapest test:** the command with a fake catalog, a fake pin and advice, a fake GitHub and the fake worker for every exit path; then one real session in the test Rumble with a battle count of one, sent and receipted.
- **Result that revises the work:** a real session that keeps a result the Rumble rejects for its shape, or a stop that leaves a worker running, stops the slice.
- **How it could pass every criterion and still fail a contributor:** the public Rumble cannot take results yet (no sign-in App, no unlabelled intake, behavior version 1), so the command works only in the test Rumble until the maintainer sets those up. The command says so when the public pin refuses this program.

## Out of scope

The desktop screen, a results-and-queue command or screen, CPU and memory settings beyond the worker's limits, more than one battle at a time, and the public Rumble's setup.

## Acceptance criteria

These will be written as criteria of the Rumble client capability (and the command line's capability where it owns the command shape), each with its identity, when the work starts. Test class Unit unless stated.

- **A session starts only when the sign-in, registration, catalog, pin and worker are all ready**, and otherwise ends before any battle with the right exit code and reason.
- **The session runs the chosen bots, formats and battle count with the shared session loop**, keeps results in the environment's journal and sends them automatically.
- **Starting again continues from the journal and the sent batches**, without running kept battles again or losing any.
- **The first Ctrl+C finishes and stops; a second cancels now.**
- **Events are reported as lines or JSON lines with a final document**, and the exit code says how it ended.
- **One real session in the test Rumble runs, keeps and sends one result that is receipted.** (Integration, needs the saved sign-in and the bots' runtimes; skipped without them.)

## Identity allocation

The change was allocated with the repository's coordinated allocation. Criterion identities are allocated when the criteria are written.

## Human evidence

One real `rumble contribute --battles 1` run in the test Rumble by the maintainer, if the integration test cannot run the bots' runtimes on the maintainer's computer.
