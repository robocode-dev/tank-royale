---
id: CH-085
type: change
status: open
links: [P-007, M-029, M-031, ADR-053, CAP-024]
title: Check, send and follow a bot from the Rumble command line
---

# Check, send and follow a bot from the Rumble command line

A participant, or an agent working for one, can now sign in and register from the command line, but cannot yet send a bot. The library already does the work: it inspects and validates a bot folder, stages it as a separate copy with only the files a bot is made of, checks it against the Rumble's owner list, ban list and catalog, checks that the account has its own copy of the bot repository with the sign-in App installed, sends the bot as one pull request, and reads where the pull request stands. This change makes those steps commands. It adds no screen and no download of other bots.

## Why

Sending a bot is the thing a participant most wants to do, and an agent that can write a bot and send it for its person is the clearest use of a command line. The commands also give the later screens a tested, agent-usable path to build on.

## What changes

- **`rumble bot check <folder>`** (offline, read-only): inspects the folder and validates it against the bot repository's per-folder rules, and answers every problem with its code, file and the change that fixes it. It needs no sign-in and no network, so an agent can run it as often as it likes while writing a bot. Exit 0 when the bot passes, 2 when it does not.
- **`rumble bot send <folder> -e <environment>`**: runs the check, stages the bot into the state folder, checks it against the catalog and the lists of the chosen environment (advisory, as in the library), checks that the account is ready, and sends it as a pull request. It first says what it would send. With **`--dry-run`** it does everything up to the pull request, so a person or an agent can see the result before anything leaves the computer.
- **The author's license confirmation is the author's.** The library refuses to send without it. The command takes it only from the option `--certify-license`, and the help says what it means: the person certifies that they have the right to publish the bot under the license in its files. An agent may pass the option only when its person has told it to; the answer repeats the confirmation as a fact, so it can be seen afterwards. Nothing in the program ever sets it by itself, and it is not remembered between runs.
- **`rumble bot status <name>`**: reads where the bot's pull request stands: waiting for the checks, being checked, sent back with the reason, waiting for a moderator, accepted, merged, in the catalog, or closed. It only reads.
- **Setup that only the person can do.** The account needs its own copy of the bot repository with the sign-in App installed, which the program cannot make through the API. When it is missing, `send` ends with exit code 2, outcome `setup-needed`, the address of the page to open and what to do there, and nothing has been changed on GitHub.
- **The answer contract holds.** Both forms, an `outcome` word, the environment, a `next` step that can be run as it stands, exit codes 0 done, 1 failed, 2 refused, 3 a sign-in is needed, 4 not finished. Nothing is asked, no token is shown, and absolute paths are removed. The bot's own name, version and file names come from the folder and are free text, so the user name is removed from them as from any text from outside.
- **Same account, same rules.** An account has at most one open pull request per bot: sending the same files again answers `unchanged`, sending other files adds a commit to it. No branch is forced, overwritten or deleted, as in the library.
- **Documents.** The participation design and the command line capability gain the commands, their outcomes and the rule about the license confirmation. The plan gets a progress note. New acceptance criteria in the command line capability are written before the code.

## Challenge

**This is the first command that publishes something under the participant's name.** Until now every command read, saved locally or registered an account. Sending creates a branch and a pull request in the participant's copy and in the public bot repository, and the pull request text is public for good. The command therefore never acts on the first call without saying what it will do: `--dry-run` shows it, and `send` always prints the facts it sends. It still sends on the first call when the option is given, because an agent cannot answer a question, and a command that asks would break the contract.

**The license confirmation could be given by an agent that was not told to.** The program cannot tell. What it can do is make the confirmation an explicit option that a human reads in the agent's command, keep it out of every default and every saved setting, and put it in the answer and the pull request text, which the library already does. The documentation says that an agent must not pass it on its own judgment. That is a rule for the agent, enforced by the person who reads what the agent does, not by the program.

**The proof against the real GitHub is still missing.** The library's sending is proved against a fake GitHub only, and one point is unproven: building the branch from the Rumble's current head rather than from the copy's own main branch, which GitHub may refuse when the copy is behind. This change adds no new GitHub calls, so it adds no new risk there, but it also cannot settle it. A real send in the test environment, by the maintainer, is the proof, and the handoff will say it has not been done.

**The commands depend on a catalog read.** The advisory check needs the catalog, and the catalog can be unreachable. An unreadable catalog must not block sending, because the validator stays the authority; it makes the check say what it could not see.

## Not in this change

No download of other bots, no creating a bot from a starter, no withdrawal, no running of battles or sessions, no screens. Nothing is published. Sending a bot to the public Rumble also needs the public sign-in App, which does not exist yet, so `send` is refused there with the same answer as `login`. The change cannot send to the public Rumble until that App exists.

## Publication and acceptance

This branch is local and not pushed, and has no pull request, because local acceptance is the accepted policy and the maintainer has not asked for publication. The work serves the simple-participation goal; the repository states no vision. The maintainer reviews this proposal before any criterion, code or document is changed.
