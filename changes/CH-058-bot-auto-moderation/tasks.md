---
id: TASKS-012
type: tasks
status: open
links: [CH-058]
title: Automate bot moderation tasks
---

# Tasks

- [x] Declare the revised bot submission, escalation, and narrow auto-merge contract (RBC-007, RBC-008, RBC-009) and its human integration exception.
- [x] Implement isolated validation, security screening, completed-battle evidence, and focused positive/negative tests in `rumble-bots` (RBC-007, RBC-008).
- [x] Implement trusted verdict, exact-commit auto-merge, manual escalation, required check, and catalog publication compatibility in `rumble-bots` PR [#18](https://github.com/robocode-dev/rumble-bots/pull/18) (RBC-008, RBC-009).
- [x] Update bot governance and public author/moderator guidance, install both repository-scoped Apps, and enable the narrowly scoped Actions event (RBC-007, RBC-008, RBC-009).
- [x] Verify that [test bot PR #22](https://github.com/robocode-dev/rumble-bots/pull/22) passed the battle and was approved and merged automatically, then that the Publisher App opened [catalog PR #23](https://github.com/robocode-dev/rumble-bots/pull/23) (RBC-008, RBC-009).
- [x] Merge the [catalog verifier correction](https://github.com/robocode-dev/rumble-bots/pull/27) and close the superseded, escalated [catalog PR #26](https://github.com/robocode-dev/rumble-bots/pull/26) (RBC-009).
- [x] Rerun catalog publication for merged test bot [PR #22](https://github.com/robocode-dev/rumble-bots/pull/22); verify that the Publisher App opened [catalog PR #28](https://github.com/robocode-dev/rumble-bots/pull/28) and that the Moderator App verified, approved and merged it ([publication run](https://github.com/robocode-dev/rumble-bots/actions/runs/39659028827), [verification run](https://github.com/robocode-dev/rumble-bots/actions/runs/39659079038)) (RBC-009).
- [x] Activate and inspect the default-branch ruleset requiring a pull request, one approval, and the `bot-moderation` status from Moderator App `5402066`; verify code-owner review and Flemming's pull-request-only bypass, with neither App bypassing the rule ([active ruleset](https://github.com/robocode-dev/rumble-bots/rules/25580362), [effective rules for `main`](https://api.github.com/repos/robocode-dev/rumble-bots/rules/branches/main)) (RBC-009).
- [ ] Merge the human-reviewed [governance update PR #29](https://github.com/robocode-dev/rumble-bots/pull/29) so the active ruleset and completed live test are recorded in `rumble-bots/GOVERNANCE.md`.
