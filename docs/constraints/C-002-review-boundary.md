---
id: C-002
type: constraint
status: active
links: [PDR-018]
title: Tracked changes enter main only through exact-candidate local human acceptance
source: Maintainer local-acceptance direction; Cliewen local acceptance workflow
enforcement: human
---

# Tracked changes enter main only through exact-candidate local human acceptance

The accepted `.clue/acceptance.yaml` selects local acceptance on `main`. Every tracked change starts on a local branch from accepted `main`, records its proposal before implementation, digests its durable corpus changes, passes applicable checks, and receives a clean bounded agentic review of the committed candidate. The agent prepares a local acceptance brief binding the exact candidate, base, review, evidence and remaining uncertainty, and runs `clue accept` with `--check` before handoff.

Only the human decides acceptance. The maintainer may run the accepting command interactively or explicitly approve the exact candidate, base and complete brief through an identified venue. An agent may execute that specific human approval with the supported approval record bound to the brief hash, but must never infer approval for a changed candidate or fabricate acceptance. An instruction to implement a plan is not acceptance of an unseen final commit. An agent does not merge its own work into `main` outside this mechanism.

No push or hosted PR is required by local acceptance. Publishing a branch, opening a PR, or pushing accepted `main` requires the maintainer's explicit instruction for that action. Coordinated identity reservations continue using the configured allocator independently of candidate publication. A hosted PR, if separately requested, is a publication/review surface; it does not silently replace the accepted local mechanism. Changes to the acceptance mechanism itself require human direction.

Direct work remains outside the tracked acceptance loop. It may be committed or pushed directly to `main` only when the maintainer explicitly authorizes that integration or push for the specific change; direct classification alone grants neither. An authorized direct push carries an `Authorized-Push:` trailer identifying the authorization. Earlier authorizations do not carry over to another change.

The separate `rumble-bots` bot-submission policy may permit a dedicated GitHub App to merge an external contributor's clean bot-only PR after exact-commit security and battle checks. That operational policy does not accept an agent's tracked product changes and is not changed by this repository's local acceptance choice. Each external repository retains its own accepted integration policy.

**Promotion trigger:** the repository may record stronger machine enforcement after the actual local acceptance checks and their limits are verified. Until then this constraint remains human-enforced. `clue validate --forbid-changes`, exact-commit checks, and a recorded brief support the decision but do not authenticate the human's intent. Local verification does not claim hosted branch protection or required-CI enforcement.

**Residual:** The human determines whether the reviewed outcome is wanted and whether an approval is genuine. Local checks and Git identity cannot make that decision; publication authority is separate from local acceptance.
