---
id: CH-068-open-questions
type: open-questions
status: open
links: [CH-068]
title: Where the criteria live, how the rules stay in step, and the launcher's executable bit
---

## Where should the criteria for the bot project library live?

The rules being checked are the bot repository's validation policy, which the bot catalog capability already owns and describes. The recommendation is to write the new criteria there, each saying that the author gets the same answer before submitting. The alternative is the Rumble client capability, where the shared library's earlier criteria went, but that capability is about running battles, not about what a submission must contain.

## How do we keep the library's rules in step with the bot repository's validator?

The repository's Python script stays the authority. The recommendation is the pair of protections in the proposal: an opt-in run over every active public bot that must all pass, and the repository's own negative test cases copied as fixtures that must each fail with the same reason. When the repository changes a rule, a later change updates the data file and the fixtures. The alternative is to call the Python script from the library, which would require Python on every author's computer and is the unrelated host runtime the plan wants to avoid.

## Should the library check the executable bit of the Unix launcher?

The repository requires the `.sh` launcher to be committed executable, but a Windows author cannot set that bit. The recommendation is not to check it locally, to report in plain words that the launcher will be marked executable when the submission is staged, and to make the later staging step record it as executable. The alternative is to fail on Windows, which blocks every Windows author for something they cannot fix by hand.
