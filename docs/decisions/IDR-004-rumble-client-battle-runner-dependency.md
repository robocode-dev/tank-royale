---
id: IDR-004
type: decision
status: inferred
links: [CH-030, CAP-016, ADR-024]
title: Rumble client uses the released Battle Runner dependency
author: agent
accepted-by: []
---

# IDR-004 — Rumble client uses the released Battle Runner dependency

## Context

CAP-016 requires the Rumble client to execute pinned battles through the Battle Runner API. The client repository currently has no Runner dependency, and its contribution rules require explicit maintainer approval before adding one.

## Decision

`robocode-dev/rumble-client` depends on `dev.robocode.tankroyale:robocode-tank-royale-runner:1.1.0` for the ranked battle-execution boundary.

## Consequences

The client uses the released public API for server lifecycle, bot processes, full-round execution, results, and replay recording. Runner upgrades become explicit client dependency updates and ranked compatibility remains governed by the synchronized behavior version rather than the dependency version.
