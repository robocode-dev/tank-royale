---
id: CH-072
type: change
status: open
links: [P-007, M-029, ARCH-027, ARCH-028, AN-023, CAP-016, CAP-015]
title: Register the signed-in account with the Rumble from the shared library
---

# Register the signed-in account with the Rumble from the shared library

This is the seventh slice of the Rumble participation plan's fourth milestone (the one that delivers the shared modules and the test command). It teaches `lib:rumble` to register the signed-in GitHub account as a Rumble participant and to return the client ID the Rumble gives it, using the way the outside-account experiment proved: a person opens a registration issue in the result repository, and a trusted workflow there registers the account that opened it and replies. No screen is added. Bot submission and result upload need the client ID, so they come after this.

## Why

A result batch must carry the client ID of a registered account, and the registration file in the result repository is what makes an account known. Before the experiment a maintainer had to add that file by hand. Now the workflow does it from an issue, and the library can do the whole step on the signed-in person's behalf with the token it already holds, without opening a browser or asking the person to do anything. Doing it in the shared library, once, means the desktop program and the headless controller register in exactly the same way.

## What changes

- A registration step in `lib:rumble` that, for a signed-in account in a given environment, ends in one of these typed answers: registered (with the client ID, and whether the account already had one), refused (with the workflow's reason, for example a banned account), not finished in time (with the address of the issue, so the step can be resumed later), the person must sign in again (GitHub did not accept the token), or failed (with a plain-words reason).
- The step first asks GitHub who the token belongs to, then looks for that account's registration file in the environment's result repository. If it is there, the client ID is returned and no issue is opened. Otherwise it reuses an earlier registration issue opened by the same account, and opens one titled `[register]` only when there is none, so repeated attempts never pile up issues. It then waits, checking at a polite pace, until the registration file appears or the workflow's reply says the registration was refused. The file is the source of truth for success; the reply text is read only for a refusal reason.
- The network behind a small interface, with a real implementation on Java's HTTP client that sends the signed-in token only to GitHub's API address and never prints it. Tests use fixtures and never touch GitHub.
- A small registration record per environment, saved in the environment's state folder as plain text because a client ID is not a secret. It remembers the account and the client ID so later starts do not need the network. It is ignored when it belongs to another account or environment, and removed when the person signs out.
- Acceptance criteria written before the code, in the Rumble client capability, with positive and negative tests, and the existing no-token-in-text test is extended to the new code.
- The participation design's module table is updated on digest.

## Challenge

The assumption most likely to undermine this slice is that the registration workflow behaves as it did in the experiment. It exists only in the test result repository so far, and the live result repository has no such workflow, so registering against the public Beta would sit waiting until its reply never comes. The step must handle that: it gives up after a bounded wait and reports not finished, never waits forever. Rolling the workflow out to the live repository is a separate task already listed in the plan, and until it is done only the development environment can register, which matches the fact that only the development environment has a sign-in App.

A second failure that every test could miss: reading the success from the reply text of a workflow comment. That text can change without anyone noticing and would break registration while the account is in fact registered. The step therefore trusts the registration file for success and uses the comment only to learn why a registration was refused. A stolen or leaked token would register an account in someone else's name only if it was already that person's token, which gives it nothing new.

## Not in this change

No screen and no menu. No bot submission, result upload or batch preparation. No change to the registration workflow or to any Rumble repository, and no rollout to the live repositories. No support for registering a different account than the signed-in one, and no removal of a registration. No saving of the sign-in beyond what the earlier change did.

## Publication and acceptance

This branch is local and not pushed, and has no pull request, because local acceptance is the accepted policy and the maintainer has not asked for publication of this change. The new library code stays unpublished and shows nothing to users, as decided on 2026-10-31. The work serves the simple-participation goal; the repository states no vision. The maintainer reviews this proposal before any code or document is changed.
