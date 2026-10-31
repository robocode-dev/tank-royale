---
id: UC-001
type: use-case
status: draft
links: [G-004, CAP-001, CAP-012, CAP-014, ARCH-028]
title: Develop, test, submit and update a Rumble bot
provenance: inferred
reversal-cost: low
---

# Develop, test, submit and update a Rumble bot

## Actors

A bot author, the desktop application, GitHub, and catalog moderation.

## Trigger

The author creates a bot or selects an existing project and wants to enter the competition.

## Main flow

1. Create a runnable project in the chosen official language or select an existing folder; edit it in the author's editor and run local practice without an account.
2. Choose Submit or update bot under Rumble, select the target environment, and sign in through GitHub's browser flow when needed.
3. Validate a separate staged source tree, correct actionable errors, and review the exact submitted files and license declaration.
4. Publish the submission through automated repository operations; follow checks, moderation and catalog publication in My bots.
5. Inspect readable results when ranked battles arrive. Submit a new strategy version through the same flow without losing ownership or historical identity.

The [Rumble menu mockups](../design/rumble/gui-mockups.md) show the screens for these steps, including the waiting and failure states, as a planned design.

## Outcome

The accepted version becomes available for ranked play without requiring the author to operate Git, edit infrastructure JSON, or contribute computing time. Submitting does not guarantee immediate battles or publication.

## Failure and recovery

Validation failure preserves original project files. Cancelled sign-in does not publish. Network retry must not create duplicate submissions. Review rejection identifies the required action. A version/ownership conflict requires an explicit correction rather than overwriting another entry. Public and development submissions remain distinct.
