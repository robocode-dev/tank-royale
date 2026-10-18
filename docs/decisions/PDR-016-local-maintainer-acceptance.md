---
id: PDR-016
type: decision
status: inferred
links: [C-002, CH-054]
title: The maintainer accepts tracked changes locally
author: agent
accepted-by: []
---

# PDR-016 — The maintainer accepts tracked changes locally

## Context

Tracked changes were accepted by the maintainer merging a pull request. The maintainer is the only person who accepts them, and most are authored by an agent in a session with the maintainer, so the pull request added a hosted round trip without an independent reviewer. Outside contributors still need pull requests to propose changes.

## Decision

The maintainer accepts a tracked change by running `clue accept` on the exact candidate, base and acceptance brief; removing `.clue/acceptance.yaml` makes local acceptance on `main` the effective policy. Agents prepare, verify and review the candidate but never decide its acceptance, and they may push `main` only for the push that follows the maintainer's own `clue accept`, which the maintainer authorized for this flow. Outside contributors keep proposing changes through pull requests, and the maintainer merges them.

## Rejected alternative

Keeping pull-request acceptance for the maintainer's own work preserves a hosted check before `main` changes and resolvable review conversations, at the cost of a round trip that no second person reviews. It was rejected for the maintainer's flow because the hosted validation workflow still runs on every push to `main`.

## Consequences

The first hosted validation of a locally accepted change runs after it reaches `main`, so the local checks and `clue accept --check` carry the pre-integration burden. If a hosted failure reaches `main` this way, the policy is revisited.
