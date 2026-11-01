---
id: CH-071
type: change
status: open
links: [P-007, M-029, ARCH-027, AN-024, CAP-016]
title: Save the GitHub sign-in between runs in one Java-only file
---

# Save the GitHub sign-in between runs in one Java-only file

This is the sixth slice of the Rumble participation plan's fourth milestone (the one that delivers the shared modules and the test command). It lets the shared Rumble library keep a person signed in between runs, so they do not have to enter a code every time. It uses the one-file design that the [experiment on saving the sign-in](../../docs/analysis/AN-024-saving-the-github-sign-in-in-one-java-only-file.md) tested on Windows and Linux: the same Java code on every operating system, with no use of any operating system's own secure store. It adds no screen.

## Why

The sign-in token lasts eight hours. Without saving, a participant who contributes battles in the evening and again the next morning signs in twice a day, and an unattended contribution session that outlives eight hours cannot renew itself after a restart. The refresh token that renews the sign-in lasts months, so saving it is what makes "sign in once" true. The participation design said to use the Windows credential store, which is Windows-only code. The maintainer wants one solution for all systems, and the experiment showed a single protected file works.

## What changes

- A sign-in store in `lib:rumble` with three actions: save, load and forget. It keeps one file per Rumble environment in that environment's state folder, below the Tank Royale user folder that the common library already chooses for each system.
- What is saved: only the refresh token and when it stops working. The access token is never saved; it is renewed in memory when the program starts.
- How the file is protected, using standard Java only: it is created so that only its owner may use it, and the rights are read back afterwards; the content is encrypted with AES-256 in GCM mode, with a key derived from the user name, the home folder, the operating system name and a fixed text, plus the environment name as protected context so a file from one environment cannot be used in another; it is written to a temporary file and moved into place in one step. The key comes from the user and the machine, so the file is useless if copied elsewhere, but it does not protect against a program running as the same user on the same computer. The design says so plainly and does not call it strong encryption.
- One ending for every problem: if the rights cannot be made owner-only, or the file is damaged, belongs to another user, home folder or environment, or is truncated, nothing is saved or used, nothing crashes, and the person signs in again. No token is ever put in an ordinary file as a fallback.
- Renewing takes care with a refresh token that can be used only once: GitHub replaces the refresh token each time it is used. The library saves the new one before it uses the new sign-in. Because the desktop program and a separate headless session can share the same saved sign-in, renewal is done while holding a lock on the file, and a program that loses the race reads the file again instead of using a refresh token that has just been replaced.
- Saving can be switched off by the caller; then nothing is written.
- One earlier criterion is retired because its meaning changes: the criterion that said the library writes no token to disk. It is replaced by a criterion that only the sign-in store writes a token, in this protected form, and the remaining rule, that no token appears in text, messages or logs, is kept under a new identity. The tests that carried the old identity move to the new ones.
- The participation design's sentence about Windows credential storage is replaced by this design, and its module table is updated on digest.

## Challenge

The assumption most likely to undermine this slice is that the key derived from the user and the machine stays the same between runs. If a person renames their account, moves their home folder, or runs the program as another user, the saved file cannot be opened and they simply sign in again. That is acceptable and is tested. A worse case is a Java installation that reports a different user name on different runs, which would make saving useless without a visible reason, so the library reports that it could not open a saved sign-in and why in plain words, rather than failing silently. The experiment showed the inputs were stable across separate runs on Windows and Linux; macOS was not tested and is an inference.

A second way it could fail while every test passes: two programs renewing at once. The refresh token is used only once, so if the desktop program and a headless session both renew, one of them can invalidate the other's token and leave the person signed out. The lock is there for this, and the change must test it with two simulated programs sharing one file, not only with one.

## Not in this change

No screen and no settings dialog. No registration, bot submission or result upload. No migration of anything. No use of any operating system's own credential store, and no support for several accounts in one environment. No change to any Rumble repository.

## Publication and acceptance

This branch is local and not pushed, and has no pull request, because local acceptance is the accepted policy and the maintainer has not asked for publication of this change. The new library code stays unpublished and shows nothing to users, as decided on 2026-10-31. The work serves the simple-participation goal; the repository states no vision. The maintainer reviews this proposal before any code or document is changed.
