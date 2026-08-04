---
id: CH-013
type: change
status: open
links: [P-003, M-008]
title: Prepare Tank Royale 1.1.0
---

# CH-013 — Prepare Tank Royale 1.1.0

## What

Prepare Robocode Tank Royale 1.1.0 as the first released engine containing the M-005 Rumble contracts. Promote the unreleased changelog entry from the provisional 1.0.3 patch version to 1.1.0, set the repository's authoritative version to 1.1.0, verify that the release notes accurately describe the user-visible changes since 1.0.2, and validate the complete release build without publishing artifacts.

## Why

CH-012 cannot publish and pin the Rumble Client runtime image until the Tank Royale engine, Battle Runner, Booter, schema, and Bot APIs containing the M-005 contracts have an official release. Those compatible additions warrant a minor release, and 1.1.0 provides the immutable engine version that CH-012 can consume after this change is accepted and the release is published from `main`.

## Scope

- Change the authoritative project version from 1.0.2 to 1.1.0.
- Finalize the top changelog entry as the 1.1.0 release dated 2026-08-04, with bot-developer-facing notes for the shipped changes since 1.0.2.
- Verify the complete build and release artifact assembly without publishing to Maven Central, NuGet, PyPI, npmjs, GitHub Releases, or documentation hosting.
- Leave CH-012 open and unchanged; after 1.1.0 is published from accepted `main`, CH-012 can pin the released engine and client image.

## Non-goals

- Publishing artifacts or triggering release workflows before this change is accepted into `main`.
- Completing M-008 or changing Rumble Client behavior, contracts, or acceptance criteria.
- Adding product behavior beyond the changes already present on `main`.

## Compatibility

This is a backward-compatible minor release. Existing 1.x bots remain supported, and the new positive `behaviorVersion` field is additive and readable by clients whose typed handshake models tolerate older servers without the field.

## Plan

Serves [P-003/M-008](../../docs/plans/P-003-rumble.md) by making the already completed M-005 contracts available in an official Tank Royale release. The maintainer explicitly authorized CH-013 to proceed from `main` while CH-012 remains open because CH-012 depends on this release.
