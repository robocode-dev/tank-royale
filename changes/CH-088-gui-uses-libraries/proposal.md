---
id: CH-088
type: change
status: open
links: [P-007, M-029, M-038, ARCH-027, ARCH-028, CAP-024, ADR-052, ADR-053]
title: Decide how the desktop program uses the Rumble code
---

# Decide how the desktop program uses the Rumble code

The Rumble menu is the next big piece, and everything in it rests on one choice that is not written down yet: does a screen in the desktop program call the Rumble code inside its own process, or does it start the `rumble` command line as a separate program and read its JSON answers? The reviewed design already draws the desktop program using shared Rumble services, but the command line was built after that, with a JSON contract that a screen could also use, so the question is open again. This change decides it before any screen is built.

## Why

Both ways work, and they lead to different programs. Choosing late means building screens twice. The choice also settles what the desktop program must fetch, which the fetching change left open ("the screens use the library directly and only take the worker from `rumble.jar`, or they call `rumble` as a process").

## What the numbers say

The shared libraries are small. The Rumble library is about 0.5 MB, the bot project library 0.07 MB and the message library 0.06 MB, and the desktop jar is 22 MB. What made the command line large was the battle worker inside it, 14 MB, which carries its own battle runner, server and booter. So the size worry was about the worker, not about the code a screen needs.

## The options

- **In-process (recommended).** The desktop program depends on the Rumble library and the bot project library and calls them directly: sign-in, registration, checking, sending and following a bot, the catalog and downloads. The command line is a second front end over the same libraries, for people without the desktop program and for agents. Only the battle worker is fetched, because only contributing battles need it, and it is not a library the desktop program can link.
- **As a process.** The desktop program fetches `rumble.jar` and runs its commands with `--json`, as an agent would. One implementation, one tested contract, and the desktop program never holds a token. But every screen waits for a Java start, a long-running contribution session needs a streaming form of the contract that does not exist yet, the desktop program must have the file even to sign in, and its screens read text answers instead of typed results.
- **A mix.** Read-only parts in-process, anything that signs in or sends through the process. It is the most flexible and the hardest to keep consistent, and it keeps both costs.

## What changes

- **A decision is recorded** in a new decision record: the desktop program uses the shared libraries in its own process, the command line is a second front end over the same libraries, and what is fetched is only what the desktop program cannot link.
- **The design is updated.** The module table and the diagram say that the desktop program and the command line are two front ends over the same libraries, with the rules that follow: the libraries never depend on the desktop program (already checked), and the desktop program never depends on the command line or on the worker.
- **A build check.** The desktop program's build gets a check that it does not depend on the command line or the worker, like the check the Rumble library already has, with a test that it fails when it should.
- **The fetching paragraph is updated.** The program fetched at first use is needed for contributing battles, because the worker is inside it; signing in, registering, checking, sending and following a bot do not need it. Sending a bot therefore works with no fetch at all.
- **Documents.** The participation design and the plan note record the decision, and one new criterion says what the documents must say.

## Challenge

**The desktop program holds the sign-in token in its own memory.** With the process route it would not. This is a real difference, but small in this setting: the desktop program does not run bots (the booter starts them as separate programs), the saved sign-in file is readable by anything running as the same user either way, as the account decision already states, and the battle worker, which does run bots, is still a separate program with no access to it.

**Two front ends can drift.** The command line contract is tested through the real jar, and the desktop program would use the libraries without that contract. The answer is that the behavior lives in the libraries and is tested there, and the command line is a thin layer on top. A bug in the command line's answers would not reach the desktop program, and a screen's own mistakes are not caught by the command line's tests.

**The fetching change just built becomes narrower.** It still fetches `rumble.jar`, which holds the worker. If the worker is wanted alone, a separate release file is a possible later change; it is not decided here.

**Contribution sessions are not designed.** They need progress while battles run. In-process this is the library's events, as the existing design says; the process route would need a streaming contract first. This favors the in-process route, but the session design itself is later work.

## Not in this change

No screens, no menu, no calls from the desktop program, and no change to the command line. The only code is the build check. Which release file carries the worker for the desktop program is left to the open questions.

## Publication and acceptance

This branch is local and not pushed, and has no pull request, because local acceptance is the accepted policy and the maintainer has not asked for publication. The work serves the simple-participation goal; the repository states no vision. The maintainer reviews this proposal before any criterion, decision record, code or document is changed.
