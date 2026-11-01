---
id: CH-072-open-questions
type: open-questions
status: open
links: [CH-072]
title: Where the client ID is kept, how long to wait, and how the repository file is read
---

## Should the client ID be kept in the protected sign-in file or in its own plain file?

A client ID is not a secret: it appears in every result batch and in the public registration file. The recommendation is a separate plain file per environment, naming the account too, so it is cheap to read, easy to inspect, and survives a sign-out when the same account signs in again, and is ignored for a different account. The alternative is to put it inside the protected sign-in file, which would remove it whenever saving the sign-in is switched off or the file cannot be opened, for no gain in safety.

## How long should registration wait for the workflow?

The workflow in the test repository answered within a few seconds in the experiment but runs on a hosted machine and can be slower. The recommendation is to check every five seconds for up to two minutes, then report not finished with the address of the issue, so the person or program can try again later; a later attempt finds the registration file or the earlier issue and continues without opening another. The alternative is a longer wait, which holds a screen up for no benefit, or a shorter one, which gives up while the workflow is still working.

## Should the registration file be read through the repository contents interface or from the public web address?

The contents interface sees a new file at once and needs the signed-in token, which the program holds anyway. The public web address is cached for a few minutes, so a fresh registration could be missed. The recommendation is the contents interface on the repository's default branch, so no branch name is written into the program. The alternative is the public web address, which needs no token but may lag.
