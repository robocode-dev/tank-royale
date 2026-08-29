---
id: PDR-002
type: decision
status: inferred
links: [P-001]
title: Release tooling uses the repository version and verifies documentation locally
author: agent
accepted-by: []
---

# PDR-002 — Release tooling uses the repository version and verifies documentation locally

## Context

The Gradle build already owns the repository release version, while generated Pages output is not a checked-in artifact and direct `main` mutation is human-gated.

## Decision

The release skill reads `VERSION`, documentation is verified locally, and generated Pages output is built from accepted `main` by its deployment workflow.

## Consequences

Release metadata has one source and local verification does not create an unauthorized repository mutation.
