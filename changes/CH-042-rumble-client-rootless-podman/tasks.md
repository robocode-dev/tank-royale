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
- [ ] Commit and push the proposal, then open the required draft PR before implementation
- [ ] Spec-first pause: report the proposal and implementation plan, wait for explicit direction to proceed
- [ ] In `robocode-dev/rumble-client`, fully qualify the four `Dockerfile` `FROM` images as `docker.io/library/...` (RCL-008)
- [ ] In `robocode-dev/rumble-client`'s `docker/rumble.sh`, add `--userns=keep-id` to both `podman run` invocations only when `CONTAINER_ENGINE=podman` (RCL-008)
- [ ] Add or extend RCL-008 evidence for `validate`, `runtimes`, and `sync` completing under rootless Podman on Linux with a writable, correctly-owned state directory
- [ ] Document the two Podman-specific flag differences in the existing Podman-alternative documentation
- [ ] Verify the full `rumble-client` test suite passes and open the pull request for human review and merge
- [ ] Reconcile P-004/M-015 bookkeeping in the permanent corpus after the `rumble-client` pull request merges
