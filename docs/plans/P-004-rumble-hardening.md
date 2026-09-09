---
id: P-004
type: plan
status: draft
links: [G-001, P-003]
title: Rumble hardening
provenance: verified
reversal-cost: low
---

# P-004 — Rumble hardening

Follow-up hardening for the Tank Royale Rumble launched by [P-003](P-003-rumble.md), which closed as `completed` when its sixth milestone landed. Closing a plan does not close the system: delivery work for P-003's own milestones is still unmerged upstream, and hands-on testing after the V1 launch found two real defects that were never tracked anywhere. This plan carries that follow-up work as its own campaign. P-003 stays immutable as the historical record of the launch; new scope belongs in a successor plan rather than in a reopened one.

Serves G-001 through the same route as P-003: the rumble is the competition half of "learning and competition", and a competition whose client cannot run in practice mode, whose catalog fails its own validator, and whose documentation describes pre-fix behavior is not yet trustworthy to the people it asks to donate compute.

Four of the five milestones are delivered outside this repository, in `robocode-dev/rumble-client` and `robocode-dev/rumble-bots`. Their proposals and decision records still live here, in the system of record, exactly as P-003/M-006 through M-008 did.

| ID | Milestone | Exit criterion | Status | Evidence |
|---|---|---|---|---|
| M-011 | In-flight P-003 delivery reconciled | The Rumble documentation for P-003/M-009 is on `main`, and [`rumble-client#9`](https://github.com/robocode-dev/rumble-client/pull/9) (working Docker quickstart plus `run`/`submit` launchers) is merged | todo | Documentation carried by CH-035's own branch; `rumble-client#9` open and mergeable, awaiting maintainer merge |
| M-012 | Practice mode has a working CLI path | `sync`, `run`, and `submit` execute in practice mode; `RumbleSynchronizer.synchronize()` no longer requires ranked mode unconditionally | done | CH-037; [`robocode-dev/rumble-client#10`](https://github.com/robocode-dev/rumble-client/issues/10) closed by [`robocode-dev/rumble-client#12`](https://github.com/robocode-dev/rumble-client/pull/12) (merged `39b2e7c`), restoring RCL-004; full `rumble-client` test suite passes (52 tests) |
| M-013 | Catalog passes its own validator | A repo-wide `validate_bot.py` run succeeds on `rumble-bots` `main`; `Vector`'s recorded `sourceHash` in `bots/index.json` matches its checked-in source | done | [`robocode-dev/rumble-bots#8`](https://github.com/robocode-dev/rumble-bots/issues/8) closed; fixed by `robocode-dev/rumble-bots@9632a0e` ("fix: bump Orbit/Vector versions to resolve stale catalog hash"), confirmed by a clean `validate_bot.py --root . --owner <any>` run on `rumble-bots` `main` (`validated 5 bot(s)`) |
| M-014 | Documentation matches post-fix behavior | Every Rumble document under `web/docs/rumble/` is re-read against the system's actual behavior after M-011 through M-013 land, and anything describing pre-fix behavior is corrected | todo | |
| M-015 | Container image runs under rootless Podman | `validate`, `runtimes`, and `sync` complete under rootless Podman on Linux with a writable state directory; every flag difference from Docker is either handled by the launcher scripts or documented for Podman users | todo | Manual Podman Desktop/WSL2 validation on Windows now covers image build, all four bundled runtimes, hardened execution, and one-round Java, C#, Python, and TypeScript bot battles; the Linux rootless state-directory criterion remains unverified |

M-012, M-013, and M-015 are independent of each other and of M-011, and may proceed in parallel. M-014 is gated on M-011 through M-013 and on M-015: re-reading the guides against pre-fix behavior, or against a runtime story that only covers Docker, would bake in the same inaccuracies the sweep exists to remove. M-011 depends only on maintainer merges; agents never merge their own pull requests ([C-002](../constraints/C-002-review-boundary.md)).

M-015 exists because rootless Podman is the default container runtime on Fedora and RHEL and therefore a realistic profile for a battle contributor. The known risk is concrete rather than speculative: `docker/rumble.sh` passes `--user "$(id -u):$(id -g)"` alongside bind mounts, which under rootless Podman resolves through the subuid map to a different host UID and leaves the mounted `.rumble-client` state directory unwritable. That surfaces as a client failure writing its journal or evidence, not as an obvious runtime mismatch. The resource flags (`--cpus`, `--memory`, `--pids-limit`) additionally require cgroups v2 with delegation under rootless Podman. A Windows Podman Desktop/WSL2 pass has now verified the image and direct bot execution, and the launchers select Podman while documenting the remaining bind-mount caveat; this does not close the Linux rootless criterion. Podman is not expected to become a second supported-and-tested runtime in CI; the milestone asks for one deliberate manual pass and an honest written answer for people who do not run Docker.

The fuller aspirational document set described in [`docs/design/rumble/user-documentation.md`](../design/rumble/user-documentation.md) (a separate `rumble-data` moderator handbook, an `onboarding.md`, and a `faq.md`) stays out of scope here, as it was in P-003's closing note. It remains a candidate for future work, not a promise of this plan.
