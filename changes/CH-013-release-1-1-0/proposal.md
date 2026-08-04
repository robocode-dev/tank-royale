---
id: CH-013
type: change
status: open
links: [P-003, M-008, M-010]
title: Release Tank Royale 1.1.0 with TwinDuel GUI
---

# CH-013 — Release Tank Royale 1.1.0 with TwinDuel GUI

## What

Prepare Robocode Tank Royale 1.1.0 as the first released engine containing the M-005 Rumble contracts and the visible GUI support for TwinDuel. Promote the unreleased changelog entry from the provisional 1.0.3 patch version to 1.1.0, set the repository's authoritative version to 1.1.0, make TwinDuel selectable and runnable from the GUI using the shared game-type preset, verify the user-facing release notes, and validate the complete release build without publishing artifacts.

## Why

CH-012 cannot publish and pin the Rumble Client runtime image until the Tank Royale engine, Battle Runner, Booter, schema, and Bot APIs containing the M-005 contracts have an official release. The GUI is the primary way most people encounter Robocode Tank Royale, so shipping the server-side TwinDuel preset without GUI selection would expose an incomplete product feature. These compatible additions warrant a minor release, and 1.1.0 provides the immutable engine version that CH-012 can consume after this change is accepted and the release is published from `main`.

## Scope

- Change the authoritative project version from 1.0.2 to 1.1.0.
- Finalize the top changelog entry as the 1.1.0 release dated 2026-08-04, with bot-developer-facing notes for the shipped changes since 1.0.2.
- Define and implement the GUI TwinDuel capability: users can select `twinduel`, receive the common 800×800, four-participant preset, and start a battle through the existing GUI flow.
- Generate and review the 1.1.0 documentation before merge, and align the `/release` skill with the authoritative `VERSION` source and C-002.
- Verify the complete build and release artifact assembly without publishing to Maven Central, NuGet, PyPI, npmjs, GitHub Releases, or documentation hosting.
- Keep CH-012 paused; after 1.1.0 is published from accepted `main`, CH-012 can resume and complete against the released engine and client image.

## Non-goals

- Publishing artifacts or triggering release workflows before this change is accepted into `main`.
- Completing M-008 or changing Rumble Client behavior, contracts, or acceptance criteria.
- Completing M-009's audience guides; they follow the settled client and GUI interfaces.
- Adding product behavior beyond the changes already present on `main`.

## Compatibility

This is a backward-compatible minor release. Existing 1.x bots remain supported, and the new positive `behaviorVersion` field is additive and readable by clients whose typed handshake models tolerate older servers without the field.

## Plan

Serves [P-003/M-008](../../docs/plans/P-003-rumble.md) and P-003/M-010 by making the already completed M-005 contracts and their GUI TwinDuel experience available in an official Tank Royale release. The maintainer explicitly authorized CH-013 to proceed from `main` while CH-012 remains open because CH-012 depends on this release.
