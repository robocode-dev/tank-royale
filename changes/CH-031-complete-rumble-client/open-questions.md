---
id: CH-031-open-questions
type: open-questions
status: open
links: [CH-031]
title: Open questions for CH-031
---

# CH-031 — Open questions

## TwinDuel catalog and result identity

Status: blocking.

The client selector treats the engine pin's `participants: 4` as four distinct catalog entries. Battle Runner instead expects two team entries that each expand to two bot processes, and `rumble-data` validates two `isTeam: true` result entries. The catalog contract carries no team marker or expanded participant count, so the client cannot select a valid TwinDuel battle from published metadata before booting untrusted code.

Should CH-031 expand the `rumble-bots` and `rumble-data` contracts with explicit team metadata and revise selection accordingly, or should TwinDuel execution be deferred through a new criterion and plan door?

Recommendation: add explicit immutable team metadata because TwinDuel is already accepted as a V1 ranked game type; silently treating four individual bots as two teams would corrupt selection and result identity.

## Live completion evidence

Status: blocking.

The public synchronized catalog currently contains one active Python bot. It cannot produce even a `1v1` ranked battle, does not contain the Java, .NET, Python, and TypeScript boundary assumed by RCL-008, and cannot support the clean-install submission required by RCL-009. A literal RCL-009 proof also requires a registered client identity, an Issues-only credential, and authorization to create a live issue whose workflow writes accepted facts to `rumble-data`.

Should CH-031 expand and populate the public prerequisites and perform an authorized live submission, or should it deliver hermetic client evidence while leaving RCL-008, RCL-009, and M-008 incomplete for a later full change?

Recommendation: split live ecosystem population and credential-bearing proof into a later full change; complete the client against hermetic cross-repository fixtures now, but do not claim RCL-008, RCL-009, or M-008 complete until the public prerequisites exist.
