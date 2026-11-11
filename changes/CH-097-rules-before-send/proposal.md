---
id: CH-097
type: change
status: open
links: [P-007, CAP-016, G-004]
title: Submit or update bot checks the Rumble's rules for the bot before sending, and a sent-back pull request shows its reason on a line of its own
---

# Submit or update bot checks the Rumble's rules for the bot before sending, and a sent-back pull request shows its reason on a line of its own

The maintainer sent RumbleOnboardingScout 1.0.1 to the test environment from the desktop program, and the pull request was opened. The bot repository's moderation then sent it back: the bot belongs to the owner `robocode-rumble-test`, the outside test account, and only its owner may update it. The sixth step showed this as one red line, "Mislykkedes: Status: Sendt tilbage: bot `RumbleOnboardingScout` belongs to owner `robocode-rumble-test`", with three prefixes and the moderation's English text with code marks. The pull request had to be closed.

The command line already checks a staged bot against the Rumble's per-bot rules before sending: a banned account, a disqualified bot, a name that belongs to another owner, and a version already published. The desktop program does not, so an author can open a pull request that the moderation must refuse.

This change is part of the plan item for bot creation, submission and updates.

## Why

A pull request that is bound to be refused costs the author a wait and a public failure, and costs the moderators work. The maintainer said that sending must under no circumstances hurt the participants.

## What changes

- **The fifth step checks the Rumble's rules.** Before offering to send, the step reads the Rumble's catalog and rules at one commit and runs the same pre-check as the command line. A banned account, a disqualified bot or a name that belongs to another owner is shown with its own text, saying what to do, and the bot is not offered to send. A version lower than the active one is shown as a note, since the Rumble accepts it. A version already published is told by the existing content check, as today. When the rules cannot be read, nothing stops the send, as on the command line, because the bot repository checks them anyway.
- **The Rumble is read with the sign-in.** In the maintainer's first trial of this change, the fifth step did not stop RumbleOnboardingScout, and pull request 14 was opened. The step looked up the Rumble's newest commit without the sign-in; GitHub allows 60 such requests an hour from one address, the author's diagnostics had used them, and GitHub answered 403. The catalog and the rules were then left unread, which by design stops nothing. The fifth step, the sixth step and the command line now look up the commit with the sign-in, which GitHub allows 5,000 times an hour, and the note when they cannot be read says that the name, owner and version were not checked. The Download bots screen needs no account and keeps reading without one.
- **A sent-back pull request reads plainly.** The sixth step shows "Sent back by the moderation." as one line, and the moderation's reason on a line of its own, without code marks.
- **Texts** are resources in every language file, translated; the pre-check gives codes and the values in them, not sentences, to the screen.

## Out of scope

- Translating the moderation's own reason, which comes from the bot repository.
- The other items found during the maintainer's trial: a folder of several bots in the first step, translated details of the bot project checks, and the remembered download folder.

## Acceptance criteria

- **The fifth step checks the Rumble's rules before sending.** With a fake Rumble: a banned account, a disqualified bot and a name owned by another account each give their own text with their values and offer nothing to send; a lower version gives a note and the send is offered; an owner's own bot and a new name are offered; rules that cannot be read stop nothing; and every text is in every language file. (Unit)
- **A sent-back pull request shows its reason on a line of its own.** The sixth step's text for a sent-back pull request has no value in it, and the reason is shown on its own line without code marks. (Unit)
- **The maintainer is stopped before sending a bot owned by another account.** In the desktop program on the test environment, the maintainer stages RumbleOnboardingScout, owned by the outside test account, and the fifth step says that the name belongs to another owner and offers nothing to send. (Human)

## Identity allocation

The change and its three criteria were allocated with the repository's coordinated allocation.

## Decision

The maintainer asked for items 1 and 2 of the list after the trial ("Gå igang med 1 og 2"), so the author goes on through implementation without a separate stop for review of this proposal.

## Human evidence

For the scenario where the maintainer is stopped before sending a bot that belongs to another account.

- **Observer:** the maintainer, with the author of this change, who checked GitHub with the maintainer's `gh` sign-in.
- **Conditions:** the desktop program in Danish, started with the menu switched on for the test environment, staging RumbleOnboardingScout 1.0.1, which the Rumble's owner list gives to the outside test account `robocode-rumble-test`.
- **Observation:** in the first build of this change the fifth step did not stop the bot and pull request 14 was opened, because GitHub refused the anonymous lookup of the Rumble's newest commit (60 requests an hour, used up), as described above. With the lookup made with the sign-in, the maintainer showed the fifth step: "Mislykkedes: Navnet RumbleOnboardingScout tilhører en anden ejer, robocode-rumble-test. Kun ejeren kan opdatere denne bot, så vælg et andet navn til din.", with the button to update the pull request greyed out. The same screen showed the open pull request 14 as "Sendt tilbage af moderationen." on its own red line and "Begrundelse: bot RumbleOnboardingScout belongs to owner robocode-rumble-test" on the next, without code marks. The maintainer found the green "Lykkedes: Dette er en ny version til Rumble." beside the red refusal odd; the line is now neutral when the bot cannot be sent.
- **Not observed:** the neutral line on screen, a banned account, a disqualified bot, a lower version, and rules that cannot be read.
