---
id: CH-080
type: change
status: open
links: [P-007, M-029, ARCH-027, ARCH-028, CAP-021, CAP-022, UC-001]
title: Prove the credential boundary between the host and the battle worker end to end
---

# Prove the credential boundary between the host and the battle worker end to end

This is the fourteenth slice of the Rumble participation plan's fourth milestone (the one that delivers the shared modules and the test command). The participation design promises that the worker never receives a GitHub credential, the host's home folder or a container socket, and says the check for that rule would arrive once the worker existed. The worker and the host now exist, each tested on its own. This change adds the check that the two together keep the promise, makes the build enforce it, and closes one gap that thinking the boundary through turned up. No screen is added.

## Why

The two halves were proven separately: the worker's code names no credential and depends on little, and the host starts a process with a clean environment. Neither test shows that a bot running inside the worker cannot find the saved sign-in by the ways a program normally finds its own files. That matters because the saved sign-in is the one credential on the machine that a hostile bot would want, and it sits in the user's data folder, whose location programs work out from the home folder and a few environment variables. If the worker could still work out the real location, the clean environment would only be decoration.

## What changes

- A closed gap. Java works out the user's home folder from the operating system on Windows, not from the home variable, so a Java process the worker starts, such as the server, the booter or a Java bot, can still learn the real home folder unless it is told otherwise. Today only the usual worker command carries a setting that points the worker's own Java at its empty home folder, and a caller may give any other command. The host will therefore also put the home and temporary folder settings into the environment variable that every Java process reads when it starts, so every Java process in the tree sees the worker's empty folders. This is the open question below.
- An end-to-end check with real processes. A saved sign-in is written for real into a stand-in for the host's data folder, with a fake token inside. A worker is then started by the host in the ordinary way and asked to find it by every ordinary means: the home folder it sees, the user data folder the shared helper works out, the saved-sign-in store's own default location and its environment. In every case it must land inside its own empty folders, and loading the sign-in through the store must report nothing saved. The saved sign-in is also tied to the user name and home folder it was made for, so a copy opened from the worker's different home folder cannot be decrypted; the check shows this too.
- A check of what the worker is handed. The worker's own command line, as the operating system reports it, and everything on its standard input must contain no environment value and no token. Standard input holds exactly the job and nothing else.
- A check of what the worker carries. The worker's runtime classpath, the exact list its process loads, must contain no class of the host library, the bot project library, the desktop program or a GitHub client, found by scanning the classpath entries and not by trusting the build file. The existing project-level rule is kept and this is the independent second look.
- A check of the messages. No field of the job or of any event may be named like a credential, so a later change cannot add a token to the messages without a test failing.
- The fixture check gets a case of its own for these checks, named the credential boundary, so a maintainer sees them separately in the report.
- Acceptance criteria written before the code, added to the battle worker capability, with positive and negative tests. The participation design says which ordinary means are now closed and repeats, in the same place, what remains open.

## Challenge

The assumption most likely to be wrong is that closing the ordinary ways is the same as closing the boundary. It is not. A bot that knows the real folder can still open the saved sign-in file by its full path, and it can read the encrypted file's bytes. What the tie to the user name and home folder adds is that those bytes cannot be opened from a different home folder, but a bot that reads the user name and the original home folder from somewhere else, such as a path in an error message or a file on disk, could rebuild the key. The check proves the ordinary ways are closed and the file is bound to its home. The design must keep saying that only a container or a separate account closes the rest, and the new tests must not be written or named in a way that suggests more.

The second weak spot is the new environment variable itself. It makes Java print a line saying it picked the settings up, on the standard error of every Java process in the tree, which is read and discarded by the host but also lands in the logs of the server and the booter that the worker reads. It also reaches Java bots, so a bot that really wants its home folder sees the empty one. The first is harmless noise. The second is the point.

The third is that the classpath scan could be fooled by a library that repackages host code. It looks for package names and for class names, which catches what is there today and what an accidental dependency would bring, but it cannot catch an adversarial copy. It guards against mistakes, not against a hostile maintainer, and the report says so.

## Not in this change

No container, no separate account, no sandbox of the file system or the network, and no restriction of what a bot may read by full path. No change to the saved sign-in's format or key. No controller and no screen. No check on Linux or macOS, where the data folder rules differ; the tests are written to run there but only Windows is run from here.

## Publication and acceptance

This branch is local and not pushed, and has no pull request, because local acceptance is the accepted policy and the maintainer has not asked for publication of this change. The new checks and the changed host stay unpublished and show nothing to users, as decided on 2026-10-31. The work serves the simple-participation goal; the repository states no vision. The maintainer reviews this proposal before any code or document is changed.
