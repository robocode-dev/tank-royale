---
links: P-004/M-014
---

# CH-043: Reconcile Rumble documentation with post-fix behavior

## What

Every Rumble document under `web/docs/rumble/` is re-read against the system's actual current behavior, now that P-004/M-011, M-012, M-013, and M-015 have all landed, and any passage that still describes pre-fix behavior is corrected in place. No acceptance criterion changes; this closes P-004/M-014.

## Why

M-014 exists precisely because the four gating milestones changed real system behavior after the Rumble guides were last written or reconciled: M-011 shipped a working Docker quickstart with `run`/`submit` launchers in `rumble-client`, M-012 restored a working practice-mode CLI path (`--sync`/`--run`), and M-015 closed the rootless-Podman state-directory gap and documented its cgroups v2 delegation caveat. `docs/plans/P-004-rumble-hardening.md` already names this exact risk: re-reading the guides before those fixes landed, or against a Docker-only runtime story, would bake the same inaccuracies back in.

Comparing the four guides against `rumble-client`'s current `README.md` and `docker/rumble.sh` (checked out under `.external/rumble-client`) surfaces concrete stale claims:

- `web/docs/rumble/index.md` and `web/docs/rumble/client-guide.md` both say the container launcher exposes only validation, runtime checks, and synchronization, and that native commands remain the only documented path for ranked `run` and `submit`. `rumble-client`'s `docker/rumble.sh`/`.ps1` support `run` and `submit` today, and its own README now recommends the Docker quickstart as the primary path.
- `client-guide.md` says `--run` executes ranked battles only and points practice users at the GUI instead. `RumbleSynchronizer`/`RankedBattleSelector`/`RankedBattleExecution` no longer reject practice-mode configurations, so `--sync` and `--run` both work in practice mode against local bot sources; only `--submit` correctly rejects practice mode early.
- `client-guide.md` says rootless Linux Podman state-directory behavior "remains a separate verification target and is not part of CI." M-015 closed that gap: `--userns=keep-id` keeps the bind-mounted state directory writable under rootless Podman, verified end to end on WSL2 Ubuntu Podman 5.7.0, with the remaining cgroups v2 delegation caveat now documented rather than open.

`bot-author-guide.md` and `moderator-guide.md` describe the `rumble-bots`/`rumble-data` catalog and moderation workflows, which M-011/M-012/M-013/M-015 did not touch; a full read finds no pre-fix claims there to correct.

## Scope

In scope: `web/docs/rumble/index.md`, `web/docs/rumble/client-guide.md`. Also update `docs/plans/P-004-rumble-hardening.md` to mark M-014 `done` with its evidence once the corrections land.

Out of scope: `bot-author-guide.md` and `moderator-guide.md` (read and found accurate; no change needed), the aspirational `rumble-data` moderator handbook / `onboarding.md` / `faq.md` set named in `docs/design/rumble/user-documentation.md` (already out of scope per that document and P-004's closing note), and any change to `rumble-client` itself (its behavior is already correct; only the Tank Royale-side guides describe it incorrectly).
