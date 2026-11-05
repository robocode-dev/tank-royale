---
id: CH-086
type: change
status: open
links: [P-007, M-029, M-031, CAP-024, ADR-051]
title: Tell a bot's author before sending that its launchers need a moderator, and record the first real send
---

# Tell a bot's author before sending that its launchers need a moderator, and record the first real send

The first real send from the command line, in the test environment, worked from end to end: sign-in, registration, `bot check`, a dry run, the send and the status. It also showed two things the documents do not yet say, and one thing the commands should have told the author before the bot was sent.

## What the real send showed

- **It works.** A bot was sent as a pull request from the participant's own copy, the automatic checks ran, and `bot status` followed it. The copy of the bot repository and the sign-in App were set up by the person, as designed.
- **The program cannot make the copy.** GitHub answers the request to create it with 403, "Resource not accessible by integration", for a sign-in made through the Rumble sign-in App. The design already said so; it is now tested, not only read.
- **Moderation stopped the bot for its launchers.** The bot's two launcher scripts were edited by hand from another bot's. The moderator App compares them, after replacing the name, with the starter launchers in the bot repository's templates and asks a human when they differ. Nothing in `bot check`, the dry run or the send said so. The author found out only from a comment on the pull request.

## Why

A launcher that differs is not forbidden, but it means a person must look at the bot before it can enter the Rumble. An author, or an agent working for one, should know that before sending, not after, and should be told how to avoid it: use the starter launcher for the language and replace the name.

## What changes

- **The dry run and the send compare the launchers with the starter ones.** They read the starter launchers of the bot's language from the Rumble's bot repository at its current head, which needs no sign-in, replace the name as the moderator does, and compare. A difference does not stop the send, because a person may want a custom launcher and the moderator is the authority. It is reported as a finding in the answer, in both forms, with the file, the plain reason ("a moderator must look at this bot, because its launcher differs from the starter launcher") and the fix ("use the starter launcher for this language, with the bot's name in place of StarterBot").
- **`bot check` stays offline.** It does not look at the launchers against the template, because it would need the network or a copy of the template that can go out of date. The dry run is the place, and it is as harmless as `bot check`. The answer of `bot check` says that the launchers are compared at the dry run. The alternative is in the open questions.
- **An unreadable template does not block anything.** As with the catalog, the check says what it could not see.
- **Documents.** The participation design records the first real send, what it proved and what it did not, and the launcher finding. The command line capability gains the criteria for the finding. The plan gets a progress note.

## Challenge

**The comparison copies the moderator's rule, which the moderator owns.** If the rule changes there, the program's advice is wrong until it is changed here. The check is therefore advisory, says it is, and the moderator stays the authority. The program reads the template from the repository itself and does not keep a copy, so a change in the template alone does not make it wrong; only a change in how the comparison is done does, such as the name replacement or the line endings.

**What the real send did not prove.** The copy was made just before the send, so it was not behind the Rumble's repository. Building the branch from the Rumble's head when the copy is behind is still unproven. Linux, macOS and the public environment are also unproven.

## Not in this change

No creating a bot from a starter, no checking of the other things the moderator sends to a person (extra launchers, file types, size limits, restricted source patterns), no screens. Those can follow from the same pattern.

## Publication and acceptance

This branch is local and not pushed, and has no pull request, because local acceptance is the accepted policy and the maintainer has not asked for publication. The work serves the simple-participation goal; the repository states no vision. The maintainer reviews this proposal before any criterion, code or document is changed.
