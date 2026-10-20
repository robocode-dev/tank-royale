---
id: ADR-051
type: decision
status: inferred
links: [CAP-014, C-002, M-024, ARCH-022]
title: Clean bot-only submissions may be merged by a confined GitHub moderator
author: agent
accepted-by: []
---

# Confined GitHub moderation for bot submissions

## Context

The Rumble has one available human moderator, so requiring a human merge of every routine bot submission imposes an avoidable wait and a three-moderator staffing gate cannot currently be met. Startup smoke does not prove a bot joins a server or finishes a battle. A privileged workflow running pull-request code would expose the catalog and its credentials.

## Decision

Trusted automation from `rumble-bots/main` triages every incoming pull request. It runs submitted source only in a credential-free, resource-limited container, after structural, dependency, launcher and conservative security checks, and requires a completed one-round battle against a trusted reference. A separate privileged job receives only a bounded verdict and confirms the current head before a repository-scoped GitHub App approves and merges a clean, single-bot-directory submission. The required moderation status blocks a failing or pending submission. A bot that cannot run receives a changes-requested review; uncertainty, non-bot files and team metadata go to Flemming with a reason. The App never bypasses the branch rule. Generated catalog publication remains a separate trusted `main` workflow.

This is a narrow exception to the [human integration boundary](../constraints/C-002-review-boundary.md) for external bot submissions. Tracked Cliewen changes and changes to moderation rules, workflows, source validators or governance remain human-merged. Three human moderators remain a staffing goal, not a prerequisite.

## Consequences

Automatic screening cannot prove arbitrary source harmless. Its conservative patterns and exact starter launchers define the subset eligible for automatic merge; all unsupported cases wait for human judgment. Client-side containment remains necessary. A moderator App installed only on `rumble-bots`, a workflow-specific Actions event allowance, and a tested required-status rule are deployment prerequisites. The [bot-submission architecture](../design/rumble/bot-submission.md) records the trust boundaries.
