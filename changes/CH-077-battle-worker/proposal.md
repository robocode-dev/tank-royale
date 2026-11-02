---
id: CH-077
type: change
status: open
links: [P-007, M-029, M-032, ARCH-027, ARCH-028, CAP-014, UC-001]
title: Run one pinned battle in an isolated worker that speaks versioned messages
---

# Run one pinned battle in an isolated worker that speaks versioned messages

This is the eleventh slice of the Rumble participation plan's fourth milestone (the one that delivers the shared modules and the test command). The participation design says battles run in an isolated worker that is started by the host with a pinned job, never receives a GitHub credential, the home folder or a container socket, and answers only in versioned, machine-readable messages. Nothing of that exists yet. This change builds the worker side and the message definitions; the host side that starts it comes in the next slice. No screen is added.

## Why

Ranked battles run source code that other people wrote. The design's credential boundary rests on a process boundary: the host holds the GitHub token and the worker holds only a job. If the worker were just a library call inside the host, a bot's process could in principle reach the token through the same process or its environment. Building the worker as its own entry point, with its own small dependency list, makes the boundary something the build can check instead of something people remember. It also gives the later controller and the single fixture command one stable way to run a battle and read the outcome without parsing logs.

## What changes

- A new small unpublished module for the host and worker messages. It holds a job (a schema version, a job identifier, the engine pin, the game type and its settings, and the participating bots as a name, version, source hash and a folder the host has already verified) and the worker's answers as events (started, bots ready, one result per finished battle with every bot's rank and score components, failed with a stable code and a plain message, and finished). Each message carries a schema version. A message with a version the reader does not know is refused with an actionable message that says which version was read and which are understood; it is never half read. A line codec writes and reads one message per line.
- A new executable module, `rumble`, with a worker mode. It reads one job message from standard input, checks the engine pin against the running server's behavior version and refuses a mismatch, runs the battle through the existing battle runner with an embedded server, writes events to standard output as lines, and exits with a code that says finished, refused or failed. Human-readable diagnostics go to standard error and are never needed to understand the outcome.
- The worker's dependencies are the runner and the message module only. The build fails if it depends on the shared Rumble library, the bot project library, the desktop program, or any GitHub or network client code of the Rumble, and a scan of its sources fails if it names a token, the user's home folder or a container socket. This is the credential boundary check for the worker side; the host side of the same check arrives with the host.
- The design's module table and dependency diagram are updated to put the messages in their own small module, because the host library must read them and must not depend on the runner, and the common library is published to Bot API users and should not gain a serialization dependency for this.
- Acceptance criteria written before the code, in the bot catalog capability or a new one if the maintainer prefers (open question), with positive and negative tests.

## Challenge

The assumption most likely to undermine this is that a worker with a short dependency list is actually isolated. A separate process only helps if the host starts it with a scrubbed environment, and that is the host's job in the next slice; the worker cannot enforce it on itself. This change therefore proves only the worker's half: it does not read credentials, and nothing in its code path could. The criteria say so, and the plan keeps the whole boundary check, host included, as its own later item.

The second weak spot is the engine pin. The behavior version is a plain integer owned by the server, and today the server may not report it in a way the worker can read. If it does not, the worker cannot honestly refuse a mismatch, and a refusal that is silently skipped would defeat the pin. The change must find out first whether the running server reports its behavior version; if not, the pin check is limited to comparing the declared pin with a version the worker itself is built with, and the proposal's criteria say exactly that.

A third risk is that a real battle in a test is slow and needs the server and sample bots. The runner's own integration tests already do this; the worker's tests use the message codec and a fake battle engine for every message and exit code, and one real battle follows the runner's existing integration test pattern and is skipped unless asked for.

## Not in this change

No host side: nothing yet starts the worker, scrubs its environment or limits its resources. No controller command line, no selection of battles, no reading of the job from the catalog, no hash verification of the sources (the host does that before it writes the job), no replay evidence, no result upload and no container. No screen.

## Publication and acceptance

This branch is local and not pushed, and has no pull request, because local acceptance is the accepted policy and the maintainer has not asked for publication of this change. The new modules stay unpublished and show nothing to users, as decided on 2026-10-31. The work serves the simple-participation goal; the repository states no vision. The maintainer reviews this proposal before any code or document is changed.
