---
id: CH-042-open-questions
type: open-questions
status: open
links: [CH-042]
title: Open questions for CH-042
---

# CH-042 — Open questions

No contract question is open. RCL-008 and the existing design documentation already describe Podman as an accepted alternative engine; this change restores that already-accepted behavior on Linux rather than deciding it. Implementation happens in `robocode-dev/rumble-client`, a repository with no Cliewen workspace of its own, so the pull request there follows that repository's normal review flow rather than this repository's PR mechanics.

The cgroups v2 resource-flag delegation half of M-015's original risk note remains unverified by this change (see the proposal's non-goals); if a future check finds a gap there, it opens as separate scope rather than reopening this change.
