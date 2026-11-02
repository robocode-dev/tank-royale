---
id: CH-078
type: change
status: open
links: [P-007, M-029, ARCH-027, ARCH-028, CAP-021, CAP-016, UC-001]
title: Start the battle worker from the host with a scrubbed environment and limits
---

# Start the battle worker from the host with a scrubbed environment and limits

This is the twelfth slice of the Rumble participation plan's fourth milestone (the one that delivers the shared modules and the test command). The previous slice built the worker and the messages it speaks. Nothing yet starts it. This change builds the host side in the shared Rumble library: it starts the worker as a separate process with a clean environment, gives it one job, reads its events, and stops it if it takes too long or says too much. No screen is added.

## Why

The worker only protects the participant's GitHub credentials if the process that runs bot code cannot see them. The worker's own code can promise not to look, but its child processes inherit whatever the host hands them. The booter starts the bots from the worker, so a token left in the environment, a path to the participant's home folder or a pointer to a container socket would reach other people's code. The only place that can prevent that is the code that starts the worker. That makes this the half of the credential boundary that actually decides the outcome, and it should be tested as such: with a fake secret in the host's own environment, not just by reading code.

## What changes

- A worker host in `lib:rumble`. Given the command that starts the worker (a list of program and arguments plus the folder to run in, supplied by the caller exactly as Tank Royale's other launchers pass a command to the process builder, because packaging comes later), a job and limits, it starts the process, writes the job as one line to its standard input, reads event lines from its standard output, and returns a typed outcome: the battle's result, a refusal with its code, a failure with its code, a timeout, the worker ending without a final event, or the worker being unable to start. It reads events with the shared message module only and never parses a log.
- A small helper that builds the usual command, the Java program from the shared Java helper, the heap limit, `-jar`, the jar and `worker`, for a caller that has the worker's jar. Packaging and where the jar lives are not decided here.
- A clean environment built from an allowlist, never by removing known bad names. The worker gets only what it needs to find Java and the bot runtimes (the path, the Java and .NET locations, the Windows system folders and the temporary folder, and the language settings), and a fresh empty working folder and temporary folder of its own. It gets no GitHub, token or credential variable, no home folder (the Java setting for the user's home also points at the empty folder), and no container engine variables or socket paths. Bots started by the worker inherit this environment, so they see none of it either.
- Limits that the host can enforce portably: a total time for the battle after which the whole process tree is stopped, a ceiling on how much output it will read from the worker, and a ceiling on the length of one line. The worker's memory is limited with the Java heap setting on its command line. A job that exceeds a limit ends as a timeout or as the worker failing, never as a result.
- The host reads the worker's standard error and discards it after a small bounded amount, because it is for people and may contain a bot's own output. It is never needed to know the outcome.
- Acceptance criteria written before the code, added to the battle worker capability, with positive and negative tests, one of which puts a recognisable fake token in the test's own environment and proves a real child process cannot read it.
- The participation design and the plan's progress note are updated on digest, including what these limits do not cover.

## Challenge

The assumption most likely to be wrong is that a cleaned environment is enough. It is not. A process running as the participant's own user can still read files in the participant's home folder by their full path, connect to a container engine socket if its path is known, and read the credential file that the program itself writes, because all of those are open to that user at the operating system level. Clearing the environment removes the easy ways and the accidental ones, and it is necessary, but only a container or a separate operating system account would make the boundary hold against a bot that goes looking. The design already says that the bare-metal fallback accepts that residual risk knowingly. This change must say the same thing in plain words in its criteria and in the design, so that nobody reads a passing test as a promise of more than it gives. The saved sign-in file is the concrete case to name, because it is the credential a hostile bot would want.

The second weak spot is stopping a runaway process. Stopping the worker is not enough: the worker starts the server and the booter, and the booter starts the bots, so killing only the first process leaves the rest running. The host must stop the whole tree, and a test must start a worker that spawns a long-running child and prove that nothing is left running after the timeout. On Windows and on Linux the way to find descendants is the same standard Java call, but it is the part most likely to differ in practice and cannot be proven on the other systems from here.

A third, smaller one is that the allowlist can be too tight for a real setup: a bot written in a language whose runtime is found through a variable that is not on the list will fail to start with a message that looks like a bot error. The first version errs on the side of too tight, since a missing variable is a visible failure while a leaked one is not, and the live check with the sample bots in each language will show which variables are really needed.

## Not in this change

No controller command line, no selection of battles, no hash verification of the bot sources, no replay evidence, no upload and no container or separate account. No sandbox of the file system or network: the criteria say so. No change to the worker or the messages unless the live check shows a real gap. No screen.

## Publication and acceptance

This branch is local and not pushed, and has no pull request, because local acceptance is the accepted policy and the maintainer has not asked for publication of this change. The new code stays unpublished and shows nothing to users, as decided on 2026-10-31. The work serves the simple-participation goal; the repository states no vision. The maintainer reviews this proposal before any code or document is changed.
