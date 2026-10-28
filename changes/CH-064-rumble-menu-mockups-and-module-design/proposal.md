---
id: CH-064
type: change
status: open
links: [P-007, M-029, ARCH-027, AN-023, ADR-052, UC-001, UC-002]
title: Draft the Rumble menu mockups, module dependencies and sign-in design
---

# Draft the Rumble menu mockups, module dependencies and sign-in design

This is the first slice of the Rumble participation plan's fourth milestone (the one that delivers the shared modules and the test command). It does not write the modules. It produces what the maintainer must review before they are written: what the Rumble menu looks like and how a person moves through it, which module may depend on which, and what the participation design now says about signing in, now that the outside-account experiment has run.

## Why

The milestone's own exit criterion starts with maintainer-reviewed mockups and a dependency diagram. Building the shared modules first would fix their shape before anyone has seen the screens that use them. The sign-in experiment also changed the picture: a participant now needs three one-time browser steps (sign in with a code, fork the bot repository on the web, install the App on the fork), and the design document still describes the intended direction as untested. The mockups need to show those steps honestly, so the plan stays simple for the person it serves.

## What changes

- A new design document with text mockups of every Rumble menu entry (get started, submit or update a bot, my bots, contribute battles, results and queue, rankings, GitHub account, help) and the author and contributor journeys as step diagrams. Each screen says what the person sees, what they can press, and what happens when something fails.
- A dependency diagram for the new shared library, the new headless controller, the bot-project library, the interface and the existing battle runner, with the rules that keep the battle worker free of GitHub credentials and the shared library free of any desktop interface code.
- The participation design's sign-in section is rewritten from "intended direction" to what the experiment proved: the public contributor App, code sign-in, eight-hour tokens renewed without a secret, registration by an issue that a workflow in the result repository handles, label-free intake of results, the web-page fork and App installation on the fork, and revocation. It also records what is still unproven (the bot repository's own ban list against a fork, and the move of the registration workflow into the live result repository).
- The plan row for this milestone records that this slice is done and what remains for later slices.

## Challenge

The assumption most likely to undermine this work is that a person who only wants to submit a bot will accept three browser steps before their first pull request opens. The credible alternative is to avoid the fork and the App installation entirely by letting the author submit the bot source as an issue that a workflow turns into the pull request. That would change the bot repository's moderation model, so it is not chosen here without the maintainer. The cheapest test is the one this slice is built around: the mockups show the exact steps and the maintainer judges whether the journey is simple enough before any code exists. If the maintainer finds the steps too heavy, the work stops and the issue-based submission is decided on its own, with a reviewed decision.

An implementation could meet every later criterion and still fail the person if each step is correct but the person must switch browser accounts, copy codes between windows or guess what to do after a refusal. The mockups therefore spell out the waiting and failure states, not just the happy path, so the reviewer sees them before the screens are built.

## Not in this change

No module, no menu code, no test command, no change to the live Rumble repositories and no acceptance criteria. Criteria arrive with the slices that build the behavior, as the plan requires. The existing browser-form and token contracts stay as they are until their replacements retire them.

## Publication and acceptance

This branch is local and not pushed, and has no pull request, because local acceptance is the accepted policy and the maintainer has not asked for publication of this change. The work serves the simple-participation goal; the repository states no vision. The maintainer reviews the mockups and the diagram before the change is digested.
