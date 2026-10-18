---
id: CH-053
type: change
status: open
links: [P-006, M-020]
title: Establish a supported outside-contributor path
---

# CH-053 — Establish a supported outside-contributor path

## What

Find out, with a real GitHub account that is not a member of the organization and has no push access to any Rumble repository, whether a registered outside Robocoder can submit a ranked result today. The pre-announcement audit predicts two blockers: a fine-grained token cannot be issued for another owner's public repository to a non-member (F1), and GitHub silently drops the `result-submission` label that the client requests when the poster lacks push access, so the ingestion workflow never sees the issue (F2). This change reproduces or disproves each prediction, records the exact prerequisites that hold, and settles the supported authentication and intake design as a reviewed decision.

The deliverable is evidence and a decision, not an implementation. If the experiment shows the current design already works for outsiders, the change records that and the plan moves on. If it fails, the change proposes the smallest repository-controlled mechanism that recognizes permitted submissions without granting participants push access, and records it as a decision for review before any client or data-repository code changes.

## Why

The plan for announcement readiness puts this first because no documentation can repair an inaccessible service path: every later milestone (accepting results, contributor walkthrough, public entry points, the outside-Robocoder trial) depends on an outsider being able to submit at all. Both findings were derived from documentation and API contracts, not observed; the audit says so itself. Acting on a predicted failure without observing it risks redesigning the credential boundary for a problem that may not exist, or documenting a path that fails on the first real attempt.

## Scope

- Run the outside-account experiment against authorized test data: create an Issues-scoped token as the non-member account, attempt to post a result issue through the client's transport, and observe whether the label is applied and whether ingestion picks the issue up. Record account access and observed behavior without secrets.
- Recheck the audit's cited revisions of `rumble-client` and `rumble-data` against their current heads before relying on them, since the audit is historical evidence.
- Record the exact prerequisites for a registered outside contributor (account type, token kind and scope, registration step, expiry and recovery) as observed.
- If a path fails, evaluate credible intake alternatives against the existing credential boundary (Issues-only credential, no repository-content write access), choose one, and record it as a decision with the rejected alternatives. Candidates include repository-side labeling of issues from registered accounts, ingestion keyed on a registered author rather than a label, and a limited pilot while the conflict is resolved.
- Update the plan's M-020 status and evidence, and update the affected capability criteria only if the chosen design changes accepted behavior; any such criterion change is scoped here as a proposal and implemented under the next milestone.

## Non-goals

- Implementing the chosen mechanism in `rumble-client` or `rumble-data`; that belongs to the milestone that accepts and publishes outside-contributor results.
- Granting participants maintainer, write, or organization access, or widening the Issues-only credential to make onboarding pass.
- Scoring changes, new game formats, new authentication architecture beyond what the decision records, a new central service, or a release.
- Contributor-facing documentation beyond the prerequisites needed as evidence; the walkthrough is a later milestone.

## Challenge

The assumption most likely to undermine this work is that the audit's predictions hold. Both are inferences from GitHub's documented rules; GitHub behavior for public repositories can differ from the documentation (for example, token scope for public-repo issue creation, or label handling through a different endpoint). The credible alternative course is to skip the experiment and design the label-free intake directly. That is cheaper to start but would commit the credential boundary to a redesign on prediction alone. The cheapest useful test is the outside-account experiment itself, which takes one account and one test issue.

The result that stops or revises the work is a finding that the experiment needs broader credentials, repository privileges, manual labeling, or a new service. In that case implementation of that path stops and the real tradeoff goes to a reviewed decision; participants are not given maintainer access to make the test pass.

A change could satisfy every criterion and still fail the person it serves if the experiment is run with a privileged or prepared account and reported as an outsider result. The evidence must name the account's relationship to the organization and its repository permissions, and a privileged-account success does not count.

## Compatibility

No behavior ships in this change. Existing registered clients, the ingestion workflow, and published rankings are untouched. The decision it records may later change the client's submission behavior and the data repository's intake contract, which the follow-up milestone handles with its own compatibility analysis.
