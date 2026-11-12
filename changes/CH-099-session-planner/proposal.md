---
id: CH-099
type: change
status: open
links: [P-007, CAP-016, ADR-052, ADR-054]
title: Choose the next ranked battle for a contribution session in the shared library, for all, preferred and strict selection
---

# Choose the next ranked battle for a contribution session in the shared library, for all, preferred and strict selection

The plan item for contributor-controlled unattended sessions (M-030) asks that the desktop program and the command line behave the same when a contributor picks which bots to fight for, in which formats, and for how many battles. The shared library (`lib:rumble`) has no code for that yet: it can download bots, sign in and send a bot, but it cannot say which battle to run next. The old client's selection lives in a separate program that the new library does not use.

This first slice builds only the decision, as a plain library class with no screen, no worker and no network. Running the battles, the durable journal, automatic batches and receipts, resource limits and pause and stop come in later slices of the same plan item, each on top of this one.

## Why

Everything else in a session depends on the answer to "which battle next, or must we wait". If the desktop program and the command line each answer it themselves, they will drift, and the rule that matters most to contributors ("strict never quietly spends my computer on other bots") has no single place to be proved. Building and proving it first, away from battles and screens, makes that rule cheap to test and impossible to bypass later.

## What changes

A new class in `lib:rumble` takes the eligible active entries, the contributor's selected entries, one of three modes, the chosen game formats, an optional battle count, the published advice about under-sampled pairings (which may be missing) and what the session has already run, and returns either the next battle (the format and the participants) or a plain reason to wait.

- **All bots:** the selection is ignored; opponents come from all eligible entries.
- **Prefer selected bots:** a valid battle with at least one selected entry is chosen first; only when none exists does the session fall back to any valid battle.
- **Only selected bots:** every battle contains at least one selected entry. When no valid battle exists the answer is a wait with its reason, never a battle with other bots.
- **Rotation:** selected entries take turns fairly, and the chosen formats take turns; the advice and the coverage so far choose the opponents; a team entry in TwinDuel counts as one selected entry.
- **Bound:** after the chosen battle count the answer is "finished"; with no count it never finishes by itself.
- **Missing advice** never makes an otherwise valid battle impossible.
- The desktop program and the command line will both call this class; neither gets its own rules.

## Challenge

- **Assumption most likely to be wrong:** that a plain function of its inputs can describe a whole session's choosing. If the old client's selection leans on state the new library does not keep (for example per-pairing counts only the data repository knows), the planner will need an input it does not have yet.
- **Credible alternative:** build the whole session loop in one go, including running battles and the journal, and let the selection emerge. Rejected: it is the largest unsettled risk of the milestone and the cheapest part to prove alone.
- **Cheapest test:** write the planner against fixtures taken from the public Rumble's real catalog and advice, and check the strict case with a selection that has no valid opponent.
- **Result that revises the work:** a needed input the library cannot get without a design change stops this slice and becomes an open question for the maintainer.
- **How it could pass every criterion and still fail a contributor:** a planner that is correct but picks the same few opponents because "coverage" is read too narrowly, so a session ranks nothing new. The rotation criterion therefore checks that, over a session, every eligible opponent is eventually used.

## Out of scope

Running battles, resource limits, pause, stop and restart, the journal and its durability, automatic batches and receipts, and the screens. Those are the next slices of the same plan item, which stays open.

## Acceptance criteria

These will be written as criteria of the Rumble client capability, each with its identity, when the work starts. Test class Unit unless stated.

- **All bots ignores the selection.** With any selection, opponents may come from every eligible entry.
- **Prefer takes a selected entry when one is valid and falls back otherwise.** Both directions are tested.
- **Strict never picks a battle without a selected entry and waits with a reason when none is valid.** Both directions are tested; the wait names what is missing.
- **Selected entries and formats take turns, and every eligible opponent is eventually used.**
- **A battle count ends the session; no count never does; missing advice blocks nothing.**
- **A TwinDuel team counts as one selected entry.**

## Identity allocation

The change was allocated with the repository's coordinated allocation. Criterion identities are allocated when the criteria are written.

## Human evidence

None expected: every criterion here is proved by tests.
