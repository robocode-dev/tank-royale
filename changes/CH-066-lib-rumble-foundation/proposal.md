---
id: CH-066
type: change
status: open
links: [P-007, M-029, ARCH-027, ARCH-028, CAP-016, ADR-052]
title: Start the shared Rumble library with environments and a dependency rule check
---

# Start the shared Rumble library with environments and a dependency rule check

This is the second slice of the Rumble participation plan's fourth milestone (the one that delivers the shared modules and the test command). It creates the new shared library `lib:rumble` and puts two things in it: the idea of which Rumble a piece of code is talking to, and an automated check that keeps the library free of desktop code. Everything later in the milestone and in the plan stands on these two, and neither is visible to a user yet.

## Why

The participation design says the shared library has no Swing code, that public Beta and the development Rumble must never be mixed, and that a pending queue is never retargeted when the environment is switched. Today those rules are words in a design document. The only code that knows the four repository names, and refuses lookalikes, is a Python script for resetting the test environment. If the library grows catalog sync, sign-in, a queue and a download feature first, each of them will carry its own idea of "which Rumble", and the dependency rule will be broken by the first convenient import.

Making the environment a real type first, and making the dependency rule fail the build when broken, is the cheapest point to do it: there is no other code in the library yet to rework. It is also the part of the design the maintainer has already reviewed and approved, so no new mockup is needed.

## What changes

- A new Gradle module `lib:rumble`, written in Kotlin like the existing client library, built and tested with the rest of the project, and not published anywhere. It depends only on the common library and standard tools.
- An environment type with two values, the public Beta and the development Rumble. Each knows its bot repository, result repository, dashboard address and a short name, and the development one carries a flag the screens use to show their permanent warning. A repository counts as belonging to an environment only when its owner and name match exactly, so lookalike and foreign names are refused, as the reset script already does. The public Beta is the default.
- A state root per environment, so cache, queue, receipts and registrations of one Rumble can never be read or written as another's. A pending queue records the environment it belongs to, and the type refuses to hand it to a different one; switching environments never retargets it.
- An automated check, run as part of the normal build, that fails when `lib:rumble` depends on the desktop program, the battle runner, or any Swing or AWT class. The rule that only the host side holds credentials needs the worker mode, so it is added with the worker, not here.
- All Rumble code stays on `main`, but dark: the new modules are not published and no Rumble menu is shown until a later change turns it on. The maintainer chose this over a long-lived feature branch on 2026-10-31, so that `main` stays free for fixes. If a fix must ship while the work is unfinished, it is made on a short-lived branch from the latest release tag. Every later Rumble change keeps this rule: it adds nothing a user can see until the plan says so.
- Focused unit tests for each of the above, and acceptance criteria for the environment rules written before the code, in the Rumble client capability.
- The participation design's module table is updated on digest to say the library exists and what it contains today.

## Challenge

The assumption most likely to undermine this slice is that a library holding only environments earns its place now. A person gains nothing from it today, and if the next slices are not started soon it is infrastructure nobody uses. The credible alternative is to start with the part users will notice: reading the bot catalog and checking each bot's hash, which the download feature and ranked contribution both need. That would bring value sooner. This change puts environments first because the catalog fetch needs to know which Rumble's catalog to read and where to cache it, and fixing the environment type afterwards means touching all of it again. The cheapest test of the assumption is the maintainer's judgement on this proposal: if the catalog is the better first step, the environment type becomes the first part of that change instead of a change of its own.

A second way it could fail while every test passes: the environment names would then exist twice, once in Kotlin and once in the reset script, and drift apart, so the reset tool refuses a repository the library accepts. The change must remove that duplication rather than test around it; see the open questions.

## Not in this change

No screen, no menu and no change to the existing client or its settings. No catalog fetch, no sign-in, no registration, no queue, no worker and no headless controller; each is a later slice with its own criteria. No publication of the library to Maven or any other registry. No change to the live Rumble repositories. No new decision record: the environments follow the accepted Rumble participation decision.

## Publication and acceptance

This branch is local and not pushed, and has no pull request, because local acceptance is the accepted policy and the maintainer has not asked for publication of this change. The work serves the simple-participation goal; the repository states no vision. The maintainer reviews this proposal before any code or document is changed.
