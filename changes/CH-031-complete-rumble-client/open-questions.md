---
id: CH-031-open-questions
type: open-questions
status: open
links: [CH-031]
title: Open questions for CH-031
---

# CH-031 — Open questions

## TwinDuel catalog and result identity

Status: answered.

The client selector treats the engine pin's `participants: 4` as four distinct catalog entries. Battle Runner instead expects two team entries that each expand to two bot processes, and `rumble-data` validates two `isTeam: true` result entries. The design already settles the shape: `docs/design/rumble/bot-submission.md` states that a team entry has exactly two `teamMembers`, the booter starts both member bots, and ranking is based on the team result reported by the server. That shape was never carried into the accepted RBC-001 acceptance criterion or the generated `bots/index.json` entry, both of which enumerate only per-bot fields, so the client cannot select a valid TwinDuel battle from published metadata before booting untrusted code.

Should CH-031 promote the design's `teamMembers` shape into the `rumble-bots` and `rumble-data` contracts and revise selection accordingly, or should TwinDuel execution be deferred through a new criterion and plan door?

Answer: approved by Flemming N. Larsen on 2026-08-30 in the Codex conversation. CH-031 promotes the existing `teamMembers` shape into the synchronized catalog contract and revises selection accordingly. ADR-047 records the durable decision.

## Live completion evidence

Status: answered.

The public synchronized catalog currently contains one active Python bot. It cannot produce even a `1v1` ranked battle, does not contain the Java, .NET, Python, and TypeScript boundary assumed by RCL-008, and cannot support the clean-install submission required by RCL-009. A literal RCL-009 proof also requires a registered client identity, an Issues-only credential, and authorization to create a live issue whose workflow writes accepted facts to `rumble-data`.

Should CH-031 expand and populate the public prerequisites and perform an authorized live submission, or should it deliver hermetic client evidence while leaving RCL-008, RCL-009, and M-008 incomplete for a later full change?

Answer: approved by Flemming N. Larsen on 2026-08-30 in the Codex conversation. CH-031 completes the client against hermetic cross-repository fixtures. Public catalog population, credential-bearing submission proof, RCL-008, RCL-009, and completion of M-008 remain for a later full change.
