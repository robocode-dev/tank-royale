---
id: AN-022
type: analysis
status: active
links: [P-007, AN-020, AN-021]
title: Rumble v0.3.0 practice-mode container run
---

# Rumble v0.3.0 practice-mode container run

## Consumer and conclusion

This finding adds a local container observation for M-025 of [the Rumble announcement-readiness plan](https://github.com/robocode-dev/tank-royale/blob/1f4953f2f23cead50c6aaaf1acf49a4cb4c79aff/docs/plans/P-006-rumble-announcement-readiness.md). The published v0.3.0 image synchronized the public catalog and completed one practice-mode battle from a fresh client checkout and empty state directory. The host was prepared: Docker was already installed, the daemon was started for this run, and the pinned image was already present locally. Practice mode omitted a client identity and did not submit a result, so this is not an outside-user onboarding run and does not meet M-025.

## Evidence boundary

The run was performed on 2026-10-21 with PowerShell 7.6.7 on a Windows host reporting `Windows 10 Pro`, build `26300`, display version `26H2`. Docker client and server were both 29.7.2 using the `desktop-linux` context. The client repository was freshly cloned at tag `v0.3.0`, commit `97530e05ec8d895b5067672e190f59053b9fb284`, into a new temporary directory. The state directory `.rumble-client` began empty.

The run used public image digest `sha256:32ab3259f0ce69f319174b54ba36f4d85949c1a30ea4e65a94010b9c931acf2d` for Linux/amd64. The practice configuration had no `clientId`, selected only `1v1`, used `mode: practice`, and kept the documented default state directory. Runtime checks passed with Java 25.0, .NET SDK 10.0, Python 3.14 and Node.js 24.21. Configuration validation passed. Synchronization read `rumble-data` commit `8e30996a829aa72c54eecc7c741ea8fd19e2a90c`, accepted behavior version 1 and cached 15 active bots from snapshot `89ebc9e78ef40b576dec0bff1cb6ff755183004d`.

The client completed one 35-round 1v1 battle, ID `7fd39f6a-e276-43ae-b1f2-250799813710`, with Vector 1.0.0 and Corners 1.0.0. Replay evidence remains in the temporary checkout at `.rumble-client/evidence/7fd39f6a-e276-43ae-b1f2-250799813710.battle.gz`. The new active `RumbleTestScout` 1.0.2 version was not selected. The `submit` command was not run, so no issue, receipt or public ranking change was created.

## Attempted and rejected configuration

The first practice configuration set `workDirectory` to `.rumble-client-m025-practice`. The pinned launcher mounts its state directory at `/work/.rumble-client`, so the client could not write to that custom path and synchronization stopped with a read-only-filesystem error. This was a nonstandard override introduced for the preflight; the guide's documented `.rumble-client` path worked without a guide change. No battle ran during the failed attempt.

## Meaning for M-025

This prepared-host run confirms that the current published image can synchronize and run a local practice battle with the bundled runtimes. It does not demonstrate a non-maintainer following the author and contributor journeys, running the active test bot, submitting a ranked result, collecting its acceptance receipt or seeing that result in the public ranking. M-025 remains open until a non-maintainer completes those steps in a documented environment.
