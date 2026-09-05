---
id: CH-035-open-questions
type: open-questions
status: open
links: [CH-035]
title: Open questions for CH-035
---

# CH-035 — Open questions

No contract question is open. This change only creates a successor plan; it changes no acceptance criterion, capability, decision, or policy, and P-003 stays `completed` and untouched.

Two operational prerequisites sit outside this change and belong to the milestones it tracks, not to its own acceptance. First, `rumble-client#9` needs a maintainer merge before M-011 can close; agents never merge their own pull requests (C-002). Second, M-014's documentation sweep is deliberately gated on M-011 through M-013 landing, because re-reading the guides against pre-fix behavior would bake in the same inaccuracies it is meant to remove.

One sequencing decision is already taken rather than open: the M-009 documentation branch is merged into this branch, so both land under one pull request and `tank-royale#250` closes as superseded. Landing them separately would have left this proposal asserting a P-003 state the corpus did not yet carry.
