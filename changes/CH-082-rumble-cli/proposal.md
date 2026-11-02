---
id: CH-082
type: change
status: open
links: [P-007, M-029, M-030, ARCH-027, ARCH-028, CAP-021, CAP-022, CAP-023, UC-001]
title: A runnable rumble command line program with a machine-readable first set of commands
---

# A runnable rumble command line program with a machine-readable first set of commands

The Rumble library can sign in, register, stage and submit a bot, read the progress of a pull request, run a battle in an isolated worker and import the old client's state, but nothing a person or a program can run. The only runnable piece, the battle worker, is not even a runnable jar. This change turns the library into a program in the way Tank Royale's other processes are programs, with a command line that works as well for an AI agent as for a person, and a first small set of commands that only read or are safe to repeat. The participation design already planned a controller beside the worker and said the desktop program and the command line should behave the same; the maintainer confirmed on 2026-11-02 that the command line should come. No screen is added.

## Why

The desktop program is not the only way to take part. An AI agent, a script or a person at a terminal can run a command and read its answer, but cannot click. Giving the library a command line first makes the library usable by all of them, and it forces every operation to have a clear input, a clear answer and a clear ending, which the screens will need as well. It also gives the project, for the first time, a Rumble program that is started the way the server and the booter are: as a jar with its dependencies, with an entry point, run with `java -jar`.

## What changes

- A new program module for the command line, `rumble-cli`, with the program name `rumble`. It is built with the command line library the booter already uses and the same fat jar task as the other programs, so it runs with `java -jar` and nothing else on the classpath. Neither program is published to Maven Central or attached to a release in this change, and neither is bundled in the desktop program yet. Only the command line would ever be a candidate for publication, the worker being an internal part of it, and the shared libraries stay unpublished like all the others. Whether and when it is published is an open question below, to be decided at a release where it gives users something real.
- The battle worker becomes a runnable jar in the same way, with its dependencies and entry point, so it can be started by hand as the booter can and by the host with the library's usual command. The command line program carries the worker's jar as an embedded resource, off its own classpath, and extracts it into its state folder when needed, checking its hash, the way the battle runner carries and extracts the server and the booter. A test extracts the embedded worker and runs a real battle with it through the host.
- A first set of commands, all safe: `rumble version` (program, engine behavior version and Java), `rumble environments` (the known Rumble environments with their repositories and whether state exists for them), `rumble status` (for one environment, whether a registration is saved, whether a sign-in is saved and whether it can be used, and how much is queued, with no secret shown), and `rumble import-legacy` (the preview by default, the copy only when asked, the old files never touched).
- An output contract for every command. By default a short text for a person. With `--json`, one JSON document with a schema version and the same facts, so an agent never parses prose. Exit codes that mean something: 0 done, 1 failed, 2 refused or bad input, 3 a sign-in is needed. Nothing is ever asked on the keyboard; everything is an argument. Standard output holds the answer and standard error holds nothing an agent needs.
- Where state lives. By default the Tank Royale user folder, and an option to point it somewhere else, so tests and agents can work in a folder of their own without touching the real one.
- Output is redacted the same way the fixture report is: nothing secret, no absolute path of this machine in the answers that are meant to be shared. Every answer lists only what the command is allowed to show.
- The fixture check gets a case of its own for the command line.
- Acceptance criteria written before the code, in a new capability for the Rumble command line, with positive and negative tests, some of them running the real jar as a real process.

## Challenge

The maintainer chose on 2026-11-02 to keep two programs. The assumption that did not hold was that one executable can be both the controller and the worker, as the design says. The controller carries the GitHub code and the saved sign-in, and the credential boundary check I added requires that the worker's classpath holds none of that. A single jar with a `worker` mode would put the host code on the worker's classpath and break that check. This change therefore keeps two programs: the command line program, which is the controller, and the worker, which stays its own small jar, started by the controller. That departs from the wording of the design, which speaks of one executable with two modes, and the design must be changed to match. This is the open question below.

The second weak spot is the worker's jar. The battle runner extracts the server and the booter from resources it carries, and the runner's own fat jar copies them in first. A fat jar of the worker that merges classes but not those resources would start and then fail at the first battle. The test therefore runs a real battle with the worker jar, not only its start.

A third is that a command line for agents makes the saved sign-in more reachable. An agent that runs as the participant's user can already read the saved sign-in file, as a bot could; the commands in this change do not sign in, upload or write to GitHub, so nothing new is exposed yet. Before any command uses the sign-in, the question of whether an agent should work with its own account instead of a participant's must be decided, and this change records that as a condition for the sign-in commands, not as something solved.

## Not in this change

No sign-in, registration, staging, submission, battle running or any command that writes to GitHub or starts a bot; those are separate, later slices of the command line. No bundling in the desktop program or the release process. No change to the fixture command beyond its new case. No screen.

## Publication and acceptance

This branch is local and not pushed, and has no pull request, because local acceptance is the accepted policy and the maintainer has not asked for publication of this change. The new programs stay unpublished and show nothing to users, as decided on 2026-10-31. The work serves the simple-participation goal; the repository states no vision. The maintainer reviews this proposal before any code or document is changed.
