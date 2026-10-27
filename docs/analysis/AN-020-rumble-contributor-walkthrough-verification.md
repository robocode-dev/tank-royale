---
id: AN-020
type: analysis
status: active
links: [P-007, M-023, AN-017]
title: Rumble contributor walkthrough and image verification
---

# Rumble contributor walkthrough and image verification

## Consumer and conclusion

This finding supports M-023 of [P-006](https://github.com/robocode-dev/tank-royale/blob/1f4953f2f23cead50c6aaaf1acf49a4cb4c79aff/docs/plans/P-006-rumble-announcement-readiness.md), which requires a complete and exercised battle-contributor walkthrough. A replacement guide is prepared in `web/docs/rumble/client-guide.md`, and the current browser-form client candidate successfully ran the Windows Docker path through a real ranked battle and credential-free batch preparation. The public `latest` image is still v0.2.0 and uses the superseded token-based submission flow, so the guide is a draft until the client v0.3.0 image is published. The required accepted-receipt and published-ranking outcome is already evidenced by [AN-017](AN-017-rumble-outside-account-result-evidence.md); no new public result was posted during this verification.

## Evidence boundary

The prepared run used the `rumble-client` release candidate commit [`c034829`](https://github.com/robocode-dev/rumble-client/commit/c034829), built as local image `rumble-client:m023-test` with image digest `sha256:0e3a8f64e5760f1804f2f1a08231d4839f552614bf9eb7e70737d7e85fec578e`. The image includes Tank Royale source commit [`9c25770c02671d1f9d941aad312013562a441427`](https://github.com/robocode-dev/tank-royale/commit/9c25770c02671d1f9d941aad312013562a441427) and Battle Runner 1.3.1.

The host was Windows 11 Pro build 26300, PowerShell 7.6.7, Docker Desktop 4.90.0, Docker Engine 29.7.2, Docker context `desktop-linux`, architecture `linux/amd64`. The environment was prepared: it used the registered `robocode-rumble-test-01` identity, a local one-battle configuration, and retained `.rumble-client` state containing an earlier prepared batch. This is not a clean onboarding test.

## Observed results

| Command | Result |
|---|---|
| `runtimes` | Passed with Java 25, .NET 10, Python 3.14, and Node.js 24.21. |
| `validate` | Passed for ranked mode and printed only the successful validation message; the stale “battle execution is not available yet” message was removed and covered by a regression test. |
| `sync` | Passed against `rumble-data` commit `d49e2b9b68b60bdb975e17da4b61b9beb05a7117`; behavior version 1, 15 active bots, catalog commit `4a290569f87378b697234d452bd49c2a7a2aeefa`, one selected game type. |
| `run` | Passed one ranked 1v1 battle, ID `0fe93c7d-cb7b-4189-9f68-c6de4df7896f`, using Orbit 1.0.0 and Corners 1.0.0; replay evidence was retained locally. |
| `submit` | Passed without a token and prepared two pending one-result batches: the pre-existing batch and the new battle. No public issue was created, so this run produced no new acceptance receipt. |

The public image was independently inspected after the local test. `ghcr.io/robocode-dev/rumble-client:latest` still resolves to digest `sha256:ff82d1d3394fde74f62bb24e8d116f1e8b36a2363e368670fdc90b35780a325a`, and the latest release is [`v0.2.0`](https://github.com/robocode-dev/rumble-client/releases/tag/v0.2.0). Its CLI requests an Issues-write token, while current client main commit [`80af3517affb912c45073ba513ad016f586e6e47`](https://github.com/robocode-dev/rumble-client/commit/80af3517affb912c45073ba513ad016f586e6e47) prepares a browser-form submission without a token. This mismatch is why the walkthrough cannot be declared published-ready before the updated image exists.

The existing [outside-account result evidence](AN-017-rumble-outside-account-result-evidence.md) establishes that the browser-form issue was accepted, receipted, recovered by the client, and reflected in the public ranking projection. That observation used the prepared client candidate and a human browser submission; it is distinct from the unposted batch in this walkthrough run.

## Rejected paths and remaining limits

The old public `latest` image was rejected for the recommended steps because its credential-based submission cannot complete the current outside-contributor flow. The older native instructions that require a sibling Tank Royale checkout and `-PtankRoyaleSource` were also rejected: the current client normally uses the published Battle Runner 1.3.1 artifact, while the container build uses the pinned Tank Royale source to package the matching Python API. The guide pins the native Python API to 1.3.1.

The verified container architecture is Linux/amd64. ARM64, Apple Silicon, Podman in this walkthrough, native Windows/macOS execution, and a clean participant onboarding run remain unverified. The launchers apply limits of 4 CPUs, 8 GiB memory, 512 processes, and a 1 GiB temporary filesystem; the guide records these limits and the hardened container settings.

The exact prepared configuration and work directory remain under `C:/Code/tank-royale/.git/m023-walkthrough-20261020/test/`. Those files are local test evidence, not public submissions or repository artifacts.
