---
id: CH-052
type: change
status: open
links: [P-006, AN-014, G-003]
title: Schedule Rumble announcement readiness
---

# Schedule Rumble announcement readiness

This change creates the next campaign, P-006, rather than implementing one of its milestones. The maintainer requested a plan for the Rumble readiness audit's findings. The independent Cliewen upgrade is in https://github.com/robocode-dev/tank-royale/pull/280 and must land first; this branch remains based on accepted main, not on that unmerged upgrade.

Record the audit, a public-participation goal, and a sequenced plan that starts with outside-contributor authentication and intake, then completes bot templates, participant instructions, cross-repository consistency and external onboarding verification. Creating the plan is tracked work. No Rumble behavior or authentication architecture is chosen here.

The largest risk is assuming that maintainer-account success establishes public participation. The credible alternative is to keep Rumble an explicitly limited pilot until the public path is proven. The cheapest useful test is an outside-account submission through registration, intake, receipt and rankings. If that cannot meet the existing credential boundary, the first milestone must bring back a reviewed design decision before implementation. A campaign could complete every document and still fail Robocoders if nobody follows it without maintainer help; the final milestone therefore requires that journey and records every intervention.

The corpus has no vision document; this change proceeds under the existing learning-and-competition goal without inventing a new vision. The maintainer's explicit instruction authorizes this plan preparation, not merging the PR or starting the planned implementation.
