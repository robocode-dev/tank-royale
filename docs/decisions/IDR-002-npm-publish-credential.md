---
id: IDR-002
type: decision
status: inferred
links: [CAP-013]
title: TypeScript npm publishing reads the Gradle credential property
author: agent
accepted-by: []
---

# IDR-002 — TypeScript npm publishing reads the Gradle credential property

## Context

Publishing credentials for the repository's package ecosystems are kept in user Gradle properties rather than repository environment configuration.

## Decision

The TypeScript `npmPublish` task reads `npmjs-api-key` from Gradle properties and does not use an `NPM_TOKEN` environment variable.

## Consequences

Credential handling follows the repository's existing release mechanism, while the retired TNP-002 and TNP-003 criteria remain tombstones and TNP-005 and TNP-006 describe the shipped contract.
