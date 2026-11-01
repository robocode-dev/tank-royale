---
id: CH-070
type: change
status: open
links: [P-007, M-029, ARCH-027, ARCH-028, AN-023, CAP-016, ADR-052]
title: Add GitHub sign-in with a code, and token renewal, to the shared Rumble library
---

# Add GitHub sign-in with a code, and token renewal, to the shared Rumble library

This is the fifth slice of the Rumble participation plan's fourth milestone (the one that delivers the shared modules and the test command). It teaches `lib:rumble` to sign a person in to GitHub with a short code and to keep that sign-in alive by renewing it, using the steps the outside-account experiment proved. It writes no screen and stores nothing on disk: the sign-in lives in memory for now. Registration, saving the sign-in safely, bot submission and result upload all come after it and all need it.

## Why

Every Rumble action that changes something needs a GitHub identity: registering as a contributor, opening a bot pull request, posting a result batch. The experiment showed the whole sign-in works without a hosted service and without a stored secret: the participant gets a code, enters it on GitHub's own page, and the program receives a token that lasts eight hours and comes with a refresh token that renews it without any secret. That behaviour is known and small, which makes it the right next piece to build and test before the larger flows that depend on it. Getting the credential handling right once, in the shared library, also matters for the rule that only the host side holds credentials.

## What changes

- A typed sign-in in `lib:rumble` that follows GitHub's device flow as the experiment did it: ask GitHub for a code, give the caller the code and the address to open, then check at the interval GitHub names until the person has approved, denied or let the code expire. The answers are typed results (waiting, told to slow down, signed in, expired, denied, failed with a reason in plain words), not text to parse.
- Renewal: the library trades the refresh token for a new sign-in without any client secret, and reports clearly when GitHub refuses because the person revoked access or the refresh token has expired.
- A token holder type that never shows the token: its text form, its error messages and any log line carry no token, and the library never writes a token to disk or to the console.
- The network behind a small interface, with a real implementation on Java's built-in HTTP client against GitHub's public addresses. Tests use fixtures and never touch GitHub.
- The sign-in App's public identifier per environment, added to the shared environments data file. The development environment gets the test App from the experiment. The public Beta has no sign-in App yet, so its entry is empty and the library refuses to sign in there with a plain message, which is correct until that App is created.
- Acceptance criteria written before the code, in the Rumble client capability, with positive and negative tests.
- The participation design's module table is updated on digest.

## Challenge

The assumption most likely to undermine this slice is that tests with fixtures prove the sign-in works. They prove the library handles the answers it is given, but only GitHub's real service proves the answers have that shape. The mitigation is that every fixture is taken from what the experiment recorded and from GitHub's published description of the flow, and the earlier experiment already ran these exact calls against the real service with a real outside account. A final human check, with a real browser and a real account, stays necessary before the sign-in is shown in a screen, and is listed in the plan's later journey checks.

A second failure that every test could miss is a token leaking into a log, a message or a crash report. The change tests this directly: a sign-in with a recognisable token is run through every error and text path, and the output is searched for the token.

## Not in this change

No screen and no menu. No saving the sign-in between runs: that needs a decision about where tokens may be kept on each operating system, and is the next slice. No registration, no bot submission, no result upload. No creation of the public sign-in App; that is a maintainer task done when the public Beta is ready. No change to any Rumble repository.

## Publication and acceptance

This branch is local and not pushed, and has no pull request, because local acceptance is the accepted policy and the maintainer has not asked for publication of this change. The new library code stays unpublished and shows nothing to users, as decided on 2026-10-31. The work serves the simple-participation goal; the repository states no vision. The maintainer reviews this proposal before any code or document is changed.
