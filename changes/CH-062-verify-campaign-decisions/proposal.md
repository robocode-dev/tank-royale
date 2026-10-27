---
id: CH-062
type: change
status: open
links: [P-007, PDR-018, PDR-019, ADR-052]
title: Verify the replacement campaign decisions and complete its adoption gate
---

# Verify the replacement campaign decisions

This change completes the first milestone of the Rumble participation plan: accept the replacement campaign and verify its decisions (M-036). The campaign plan was accepted locally by the maintainer with the previous change, but its three agent-authored decisions still carry `status: inferred` and `accepted-by: []`, and the milestone still reads `todo`. The plan says product work may not begin until the maintainer's actual endorsement is recorded, so this gate blocks every later milestone.

The three decisions are the local acceptance process (tracked changes are accepted by the maintainer locally, with no pull request unless asked), the public-Beta campaign ordering (the existing service becomes a labeled public Beta before the GUI is complete), and the shared desktop and test architecture (shared modules plus isolated public and development environments).

## What changes

- Each of the three decisions moves to `status: verified` and records the approver, the first approval date and the venue in `accepted-by`.
- The milestone is marked done with its evidence, and the plan's wording that describes the gate as outstanding is brought in line.
- No product criterion, code, repository setting, deployment or live bot changes. The decision texts themselves are not edited.

## The approval evidence and its limit

The maintainer's recorded approval for the previous change (approver `flemm (maintainer)`, source `Codex conversation, reply to exact-candidate acceptance and PDR-018/PDR-019/ADR-052 endorsement request`, statement `Jeg godkender planen. :)`, recorded 2026-10-27) is the only existing evidence. It was given as a reply to a request that named all three decisions, but the statement itself does not name them. Decision records move to `verified` only on explicit human approval, so this change treats that record as the proposed evidence and asks the maintainer to confirm, in the acceptance of this exact candidate, that it covers PDR-018, PDR-019 and ADR-052. If the maintainer says it does not, the decisions stay inferred and the milestone stays `todo`.

## Publication and acceptance

This branch is local, is not pushed, and has no pull request, because local acceptance is the accepted policy and the maintainer has not asked for publication. Identity allocation used the repository's coordinated allocator. Acceptance happens only through exact-candidate local human acceptance. No vision document exists; the change proceeds under the learning-and-competition purpose and the simple-participation goal.
