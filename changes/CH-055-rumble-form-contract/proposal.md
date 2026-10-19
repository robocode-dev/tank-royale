---
id: CH-055
type: change
status: open
links: [P-006, M-021, CAP-016, ADR-049]
title: Align the Rumble client contract with browser-form submission
---

# Align the Rumble client contract with browser-form submission

This change supplies the contract prerequisite for accepting and publishing outside-contributor results in the announcement-readiness campaign (M-021). The milestone remains wanted: ordinary Robocoders must be able to contribute a ranked battle and recognize its durable acceptance without repository privileges.

The accepted browser-form decision requires a credential-free client to prepare plain-text batches and let the contributor paste them into GitHub's result form. The current submission criterion instead requires an Issues-only token, the end-to-end criterion forbids any person copying a result, and the live design still promises automatic posting. Bring those contracts into agreement with the accepted decision before continuing implementation. Preserve the existing submission-criterion identity reserved on `cap-016-browser-submission`, retire the incompatible end-to-end criterion rather than changing its meaning, and keep both replacements draft until their external evidence exists.

The existing documentation commit `b952543aa604de43545f003d62488ef07271e08d` is input to this change, not its base or an accepted dependency. This branch starts at accepted `main`, `59e16b4d10d27347ddc5e676508b52432a621da0`; the existing documentation and Rumble Client implementation branches remain intact. The prepared client implementation is not proof of an outside-account journey and will need its own verification and integration.

Align the capability design, cross-cutting submission flow, trust-boundary explanation and readiness-plan progress wording. This change does not complete M-021, ship a client, change ingestion or receipt semantics, adopt a GitHub App, publish the participant walkthrough, or announce Rumble. The campaign's existing later milestones retain those responsibilities.

The assumption most likely to fail is that a form-based handoff can preserve an unchanged batch and recover its receipt reliably enough for a newcomer. A credible alternative is the separately evaluated GitHub App described by the accepted decision. The cheapest useful next test is one genuine ranked result posted by the registered outside account, followed by anonymous receipt collection and ranking observation; inability to retain or acknowledge that result stops the implementing milestone. Even a client that passes batch-format tests could fail the contributor through an inaccessible container file path or an ambiguous instruction about when to rerun submission; those remain required journey checks, not claims made by this contract-only change.

There is no corpus vision document. This change proceeds under the existing learning-and-competition purpose and the accepted public-participation goal. The maintainer selected the tracked workflow in the Codex conversation on 2026-10-19. PR acceptance follows the latest supplied repository instructions and the human-integration constraint; the accepted base currently has no `.clue/acceptance.yaml`, so no claim is made that the CLI's local default enforces that PR boundary.
