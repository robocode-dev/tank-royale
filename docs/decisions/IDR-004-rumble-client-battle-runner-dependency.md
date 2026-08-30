---
id: IDR-004
type: decision
status: verified
links: [CAP-016, ADR-024, ADR-046]
title: Rumble client uses the released Battle Runner dependency
author: agent
accepted-by: Flemming N. Larsen (2026-08-29, Codex conversation)
---

# IDR-004 — Rumble client uses the released Battle Runner dependency

## Context

CAP-016 requires the Rumble client to execute pinned battles through the Battle Runner API. The client repository currently has no Runner dependency, and its contribution rules require explicit maintainer approval before adding one.

## Decision

`robocode-dev/rumble-client` depends on the first released `dev.robocode.tankroyale:robocode-tank-royale-runner` version that provides the BR-049 behavior-version precondition. The client pins that concrete release rather than consuming an unreleased local or snapshot build.

## Consequences

The client uses the released public API for server lifecycle, bot processes, compatibility enforcement, full-round execution, results, and replay recording. Runner upgrades become explicit client dependency updates and ranked compatibility remains governed by the synchronized behavior version rather than inferred from the dependency version.
