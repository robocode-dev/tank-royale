---
id: PDR-018
type: decision
status: inferred
links: [C-002]
title: Tracked changes use exact-candidate local human acceptance
author: agent
accepted-by: []
---

# Tracked changes use exact-candidate local human acceptance

## Context

The accepted `.clue/acceptance.yaml` selects local acceptance on `main`, while repository instructions still prescribe pull requests. The maintainer explicitly selected local acceptance rather than PRs. A local workflow must retain the human decision and reviewed evidence without requiring hosting.

## Decision

Follow Cliewen's local acceptance workflow: prepare a tracked branch from accepted `main`, commit its proposal, implement and digest, verify the exact candidate, obtain a clean bounded agentic review, and prepare an acceptance brief bound to candidate and base. Run the accepting command only after the human accepts those exact inputs; an agent may execute a human-approved acceptance using the supported approval record but may not invent that approval.

No branch push or hosted pull request is part of local acceptance unless the maintainer explicitly requests it. Coordinated identity allocation continues through the configured allocator and does not publish the candidate. The [human integration constraint](../constraints/C-002-review-boundary.md) owns the local acceptance and separate publication boundaries.

## Consequences

Local verification and review replace hosted readiness checks; no protected-CI enforcement is claimed. The existing configuration remains unchanged. Live carriers are AGENTS.md, the human integration constraint, core-principles instructions, Copilot instructions, and the contributor PR template; the generic skills support local acceptance, while the repository overrides their proposal-suffix convention with the canonical change identity and `type: change` header required by the released acceptance command. Historical descriptions and completed plans retain their historical meanings. Hosted CI remains available for separately authorized publication.
