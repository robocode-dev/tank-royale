---
id: CH-030-open-questions
type: open-questions
status: open
links: [CH-030]
title: Open questions for CH-030
---

# CH-030 — Open questions

## May the client add the published Battle Runner dependency?

CAP-016 requires the external client to execute pinned battles through Battle Runner, but `robocode-dev/rumble-client` currently depends only on Gson. Its local contribution rules require explicit approval before adding a dependency. The expected coordinate is `dev.robocode.tankroyale:robocode-tank-royale-runner:1.1.0`, the released runner API documented by Tank Royale. Approving it permits the RCL-004 and RCL-005 execution implementation; rejecting it leaves no accepted mechanism for the client to satisfy those criteria.
