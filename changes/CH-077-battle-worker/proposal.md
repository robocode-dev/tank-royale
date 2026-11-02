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

- A new small unpublished module, `lib:rumble-protocol`, for the host and worker messages, built like the other Rumble modules (`lib:rumble`, `lib:bot-project`): Kotlin with serialization, not published, a build check of its dependency rules and a source scan for forbidden imports. The maintainer chose a module of its own on 2026-11-02 rather than adding the messages to the published common library. Like every other Rumble document, the messages carry an integer `schemaVersion` and are not described by the JSON schemas in the `schema` folder, because those generate the Java and C# code of the Bot APIs and describe the battle protocol with bots, not this internal channel. It holds a job (a schema version, a job identifier, the engine pin, the game type and its settings, and the participating bots as a name, version, source hash and a folder the host has already verified) and the worker's answers as events (started, bots ready, one result per finished battle with every bot's rank and score components, failed with a stable code and a plain message, and finished). Each message carries a schema version. A message with a version the reader does not know is refused with an actionable message that says which version was read and which are understood; it is never half read. A line codec writes and reads one message per line.
- A new executable module, `rumble`, with a worker mode. It reads one job message from standard input, requires the job's engine pin from the battle runner, which refuses a mismatch, runs the battle through the existing battle runner with an embedded server, writes events to standard output as lines, and exits with a code that says finished, refused or failed. Human-readable diagnostics go to standard error and are never needed to understand the outcome.
- The worker's dependencies are the runner and the message module only. The build fails if it depends on the shared Rumble library, the bot project library, the desktop program, or any GitHub or network client code of the Rumble, and a scan of its sources fails if it names a token, the user's home folder or a container socket. This is the credential boundary check for the worker side; the host side of the same check arrives with the host.
- The design's module table and dependency diagram are updated to put the messages in their own small module, because the host library must read them and must not depend on the runner, and the common library is published to Bot API users and should not gain a serialization dependency for this.
- Acceptance criteria written before the code, in a new capability for running contributed battles, separate from the bot catalog capability (chosen by the maintainer on 2026-11-02), with positive and negative tests. The existing Rumble client capability describes the external client repository and is linked, not extended.
- The internal documentation of Tank Royale is brought up to date on digest: the participation design, the architecture and design indexes, the capability and decision indexes, and the plan's progress note. The architecture views have no diagram for the runner or the Rumble libraries today, so none is added without asking.

## Challenge

The assumption most likely to undermine this is that a worker with a short dependency list is actually isolated. A separate process only helps if the host starts it with a scrubbed environment, and that is the host's job in the next slice; the worker cannot enforce it on itself. This change therefore proves only the worker's half: it does not read credentials, and nothing in its code path could. The criteria say so, and the plan keeps the whole boundary check, host included, as its own later item.

The second weak spot was the engine pin, and reading the code settled it. The server reports its behavior version in its handshake, and the battle runner can already be told to require a given version and then refuses to start a battle on a mismatch. The worker therefore uses that existing check with the job's pin and does not invent a second one. The remaining risk is a worker that forgets to pass the pin, so a criterion states that a job whose pin differs from the running server's version is refused before any bot starts, and a test proves it.

A third risk is that a real battle in a test is slow and needs the server and sample bots. The runner's own integration tests already do this; the worker's tests use the message codec and a fake battle engine for every message and exit code, and one real battle follows the runner's existing integration test pattern and is skipped unless asked for.

## Not in this change

No host side: nothing yet starts the worker, scrubs its environment or limits its resources. No controller command line, no selection of battles, no reading of the job from the catalog, no hash verification of the sources (the host does that before it writes the job), no replay evidence, no result upload and no container. No screen.

## Publication and acceptance

This branch is local and not pushed, and has no pull request, because local acceptance is the accepted policy and the maintainer has not asked for publication of this change. The new modules stay unpublished and show nothing to users, as decided on 2026-10-31. The work serves the simple-participation goal; the repository states no vision. The maintainer reviews this proposal before any code or document is changed.
