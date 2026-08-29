---
id: IDR-001
type: decision
status: inferred
links: [CAP-013]
title: npmPack is the TypeScript publish preview
author: agent
accepted-by: []
---

# IDR-001 — npmPack is the TypeScript publish preview

## Context

The never-built `npmPublishDryRun` task duplicated the package artifact inspection already provided by `npmPack`.

## Decision

The unused dry-run task is retired rather than implemented; `npmPack` remains the local preview of the package that publishing would upload.

## Consequences

The build exposes one working preview path and the retired TNP-001 criterion remains as its required tombstone.
