---
id: PDR-001
type: decision
status: inferred
links: [C-002]
title: Authorized simple pushes remain distinct from full changes
author: agent
accepted-by: []
---

# PDR-001 — Authorized simple pushes remain distinct from full changes

## Context

The review boundary must keep accepted-contract changes behind a human-merged pull request while allowing explicitly authorized maintenance work to follow a lighter route.

## Decision

Full changes always use a branch and pull request; simple work may reach `main` directly only when the maintainer authorizes that specific push, and each such commit carries an `Authorized-Push:` trailer.

## Consequences

The constraint preserves the full-change merge boundary while making the exceptional simple-work authorization auditable.
