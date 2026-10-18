---
id: P-006
type: plan
status: active
links: [G-003, AN-014, PDR-015]
title: Rumble announcement readiness
---

# Rumble announcement readiness

This is the next campaign to start after its planning PR is merged. It serves Robocoders who want to submit bots and contribute ranked battles without maintainer privileges. The [pre-announcement audit](../analysis/AN-014-rumble-announcement-readiness.md) supplies the initial findings; it is historical evidence, not proof that every reported risk still exists when implementation begins. Recheck the relevant revisions before each milestone.

## Scope and sequence

Tank Royale owns this cross-repository campaign and its public user guides. Implementing changes may also belong in [rumble-client](https://github.com/robocode-dev/rumble-client), [rumble-bots](https://github.com/robocode-dev/rumble-bots), [rumble-data](https://github.com/robocode-dev/rumble-data), [the Book of Robocode](https://github.com/robocode-dev/book-of-robocode), and [the organization profile](https://github.com/robocode-dev/.github). Each milestone records full links to its implementing PRs, pinned revisions and verification evidence; external corpus references use the `clue:` identity form. Completion depends on merged changes and published user-facing content, not merely prepared PRs.

| ID | Milestone | Exit criterion | Status | Evidence |
|---|---|---|---|---|
| M-020 | Establish a supported outside-contributor path | Reproduce or disprove the audit's token and label risks with a non-member account lacking repository push access. Record exact prerequisites and the supported authentication/intake design against the existing credential boundary; any required contract or policy change has a reviewed decision before implementation. A privileged-account success does not satisfy this exit. | todo | Not started; audit findings F1–F2. |
| M-021 | Accept and publish outside-contributor results | Using the path established above, a registered outside contributor runs and submits a ranked battle, receives a durable acceptance receipt and observes its eligible result in published rankings without maintainer labeling or repair. Focused failure/retry checks cover unregistered identities, invalid submissions and interrupted submission without duplicate facts or lost pending results. Implementing PRs and result evidence are linked. | todo | Depends on M-020. |
| M-022 | Make bot submission reproducible in every official language | Java, C#, Python and TypeScript have linked copy-and-rename templates validated by the actual submission workflow, with source-only dependencies, launchers, metadata, smoke behavior and executable-bit instructions. A JSON-only TwinDuel submission is covered. At least one first bot and a version update proceed from the published instructions through merged catalog entries; language-template checks are recorded separately from that live submission. | todo | Audit findings F3–F4; can proceed alongside M-020. |
| M-023 | Publish a complete battle-contributor walkthrough | The recommended container path gives ordered shell and PowerShell steps from clone and registration to accepted receipt, visible ranking and repeat contribution. Copyable defaults match one-battle behavior; native instructions match released dependencies. Troubleshooting covers authentication, registration, receipts, catalog delay, engine upgrades, state retention and invalid selections. Supported architecture/resource limits and any unverified paths are explicit. Commands are exercised under recorded OS, image and source revisions. | todo | Depends on M-021; audit findings F5–F6 and F10–F12. |
| M-024 | Align public entry points and resolve launch gates | The Book, bot repository, organization profile, main README, tutorial exits and dashboard consistently link submission, battle contribution and rankings. Contradictory availability statements are corrected after confirming their meaning. Any genuine pre-opening validation or moderator-staffing gate is evidenced as met, or revised through an explicit reviewed decision. Deployed pages and relevant links are checked. | todo | Depends on M-022 and M-023; audit findings F7–F9. |
| M-025 | Demonstrate announcement readiness with an outside Robocoder | A non-maintainer follows the published author and contributor journeys through a catalog entry, accepted result receipt and visible ranking. Evidence distinguishes clean and prepared environments, names every prerequisite and intervention, and records exact versions. Any undocumented necessary intervention is fixed and the affected journey repeated. The maintainer receives the remaining limitations and evidence for the announcement decision. | todo | Depends on M-021 through M-024. |

Authentication/intake is the first work item because documentation cannot repair an inaccessible service path. Template work can proceed independently; contributor instructions follow the settled intake behavior. Cross-repository consistency follows the working guides, and external onboarding is the final check. All milestones are currently unstarted. This plan does not execute its own milestones.

## Challenge and stopping conditions

The main assumption is that the intended open participation model can work within the accepted credential and trust boundaries. A credible alternative is an explicitly limited pilot while that conflict is resolved. The cheapest useful test is the outside-account experiment in the first milestone, using authorized test data and recording account access without secrets. If it requires broader credentials, repository privileges, manual labeling, or a new service, stop implementation of that path and bring the actual tradeoff to a reviewed decision. Do not grant participants maintainer access just to make onboarding pass.

A technically passing campaign could still fail if the author needs undocumented runtimes or the contributor cannot distinguish a posted issue from an accepted score. The final milestone follows both complete public journeys rather than treating green CI or a documentation checklist as sufficient evidence. Historical maintainer tests and prepared-machine tests remain useful but are labeled honestly.

## Boundaries

No scoring change, new game format, unattended client-session feature, authentication architecture, or release is approved by this plan. A bounded repeat-command recipe may document current behavior; introducing a session feature needs separate scope. Separate FAQ/onboarding filenames are optional: complete, reachable guidance is the promised outcome. Publishing the announcement remains the maintainer's decision after readiness evidence is available.

The corpus has no vision document. This plan proceeds under the existing learning-and-competition purpose and the maintainer's explicit request. The completed original Rumble and hardening plans remain immutable. Adoption's already-evidenced milestones are not reopened or silently made prerequisites of this campaign.
