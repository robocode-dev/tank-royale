---
id: CH-042-tasks
type: tasks
status: open
links: [CH-042]
title: Task breakdown for CH-042
---

# CH-042 — Tasks

- [x] Reserve CH-042 and branch from the accepted tip of `main`
- [x] Reproduce the rootless Podman failures by hand on real Linux (WSL2 Ubuntu, Podman 5.7.0 rootless): confirmed unqualified-`FROM` build failure and `.rumble-client` state-directory write failure, and confirmed `--userns=keep-id` combined with the existing `--user` flag fixes the write failure
- [x] Confirm RCL-008 and the existing design documentation already accept Podman as an alternative engine, so this is a defect correction rather than a new promise
- [x] Capture the full-route proposal and selected scope
- [x] Commit and push the proposal, then open the required draft PR before implementation
- [x] Spec-first pause: reported the proposal and implementation plan, received explicit direction to proceed
- [x] In `robocode-dev/rumble-client`, fully qualify the four `Dockerfile` `FROM` images as `docker.io/library/...` (RCL-008)
- [x] In `robocode-dev/rumble-client`'s `docker/rumble.sh` and `docker/rumble.ps1`, add `--userns=keep-id` only when the selected engine is podman (RCL-008)
- [x] Add RCL-008 evidence for `validate`, `runtimes`, and `sync` completing under rootless Podman on Linux with a writable, correctly-owned state directory — verified by hand in WSL2 Ubuntu with Podman 5.7.0 rootless; recorded in this proposal and in the `rumble-client` PR description
- [x] Document the two Podman-specific flag differences in the README's Docker/Podman section
- [x] Open [`robocode-dev/rumble-client#14`](https://github.com/robocode-dev/rumble-client/pull/14) for human review and merge; no Kotlin/Java source changed, so the full Gradle test suite was not re-run (it would not exercise container/launcher behavior differently)
- [ ] Reconcile P-004/M-015 bookkeeping in the permanent corpus after `rumble-client#14` merges
