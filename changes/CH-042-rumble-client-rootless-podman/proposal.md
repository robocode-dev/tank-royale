---
id: CH-042
type: change
status: open
links: [P-004, M-015, CAP-016]
title: Fix rumble-client's rootless Podman gaps on Linux
---

# CH-042 — Fix rumble-client's rootless Podman gaps on Linux

## What

Fix two concrete defects in `rumble-client` that break `docker/rumble.sh validate|runtimes|sync` under rootless Podman on real Linux, verified hands-on in a WSL2 Ubuntu distribution running Podman 5.7.0 rootless (not Podman Desktop's own VM):

1. `Dockerfile`'s four `FROM` lines (`eclipse-temurin:11-jdk`, `gradle:8.14.3-jdk17`, `python:3.14-slim`, `ubuntu:26.04`) are unqualified. A stock Podman install has no `unqualified-search-registries` configured, so `podman build` refuses them with `short-name did not resolve to an alias`; Docker silently defaults to Docker Hub. Fix: fully qualify all four as `docker.io/library/...`, which resolves identically under both engines.
2. `docker/rumble.sh` passes `--user "$(id -u):$(id -g)"` for the writable-state-directory commands. Under rootless Podman this does not map to the invoking host UID inside the container's user namespace (it resolves through the subuid map instead, exactly as P-004/M-015's known-risk note predicted), leaving the bind-mounted `.rumble-client` state directory unwritable (`mkdir: Permission denied` on `.rumble-client/cache`). Docker's `--user` maps directly, so this defect is Podman-only. Fix: when `CONTAINER_ENGINE=podman`, also pass `--userns=keep-id` alongside the existing `--user` flag (Docker does not understand `--userns=keep-id`, so it must stay conditional on engine).

## Why

P-004/M-015 requires `validate`, `runtimes`, and `sync` to complete under rootless Podman on Linux with a writable state directory, and every flag difference from Docker to be either handled by the launcher scripts or documented. The milestone's own risk note already named the `--user`/subuid mismatch as the concrete, expected failure mode; this change is that fix, verified rather than assumed.

This restores design intent that already exists, not a new promise: [`docs/design/rumble/client-battles-and-results.md`](../../docs/design/rumble/client-battles-and-results.md) already states "pull the image ... with Docker or Podman for container users" and "forks can rebuild the image with Docker or Podman," and [RCL-008](../../docs/capabilities/CAP-016-rumble-client/criteria.md) already requires the client to boot every supported platform "within its declared boundary" via "the primary container" without naming an engine. Podman was already an accepted alternative engine; it just did not work end to end. Fixing it restores RCL-008's existing meaning under the second engine the design already names; it does not change what RCL-008 requires.

## Route

Recommended route: full. P-004 requires every milestone delivered outside this repository (`rumble-client`, `rumble-bots`) to carry its own change proposal and decision record here before implementation, mirroring CH-037/CH-038/CH-041. Discovery would change the route only if fixing the writable-state-directory gap turned out to require a new engine-selection promise beyond what the design document and RCL-008 already accept — nothing found during verification suggests that; both fixes are narrow and mechanical.

## Plan

Serves [P-004/M-015](../../docs/plans/P-004-rumble-hardening.md) without changing RCL-008's or any other accepted criterion's meaning.

## Scope

- In `robocode-dev/rumble-client`'s `Dockerfile`, fully qualify the four `FROM` images as `docker.io/library/...`.
- In `robocode-dev/rumble-client`'s `docker/rumble.sh`, add `--userns=keep-id` to both `podman run` invocations (the `runtimes` no-mount path and the config/state-directory path) only when `CONTAINER_ENGINE=podman`.
- Add or extend evidence proving `validate`, `runtimes`, and `sync` complete under rootless Podman on Linux with a writable, correctly-owned `.rumble-client` state directory (host-side files land owned by the invoking host user, not root or an unrelated subuid).
- Document the two flag differences (unqualified-image resolution, `--userns=keep-id`) for Podman users in whichever `rumble-client` doc already covers Podman as a Docker alternative, so the launcher script's engine-conditional behavior is not a silent surprise.
- Reviewed and merged in `robocode-dev/rumble-client` through its normal pull-request flow (that repository carries no Cliewen workspace of its own).
- Reconcile P-004/M-015 bookkeeping here after that pull request merges, closing out the container-runtime half of the milestone; the Linux rootless state-directory criterion becomes verified.

## Non-goals

- The Linux rootless cgroups v2 resource-flag delegation half of M-015's risk note (`--cpus`, `--memory`, `--pids-limit` under rootless Podman) — this change's verification environment (WSL2) did not exercise cgroups v2 delegation, and the `runtimes`/`validate`/`sync` commands used here did not require it; if a follow-up check finds a gap there, it is separate scope.
- The `Vector` bot `sourceHash` mismatch surfaced incidentally while verifying `sync` end-to-end — that is a `rumble-bots` catalog data issue (M-013's domain), not a Podman/rootless defect, and is out of scope here.
- Any change to RCL-008 or another accepted criterion's meaning.
- M-019's scheduled runtime-lane refresh work.
- A Tank Royale release.
