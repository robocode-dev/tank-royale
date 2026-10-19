---
id: AN-017
type: analysis
status: active
links: [CAP-016, M-021, ADR-049, AN-016]
title: Outside-account result accepted, recovered and published in rankings
---

# Outside-account result accepted, recovered and published in rankings

## Consumer and conclusion

This evidence continues the [prepared browser-submission verification](AN-016-rumble-browser-submission-verification.md) for the milestone to accept and publish outside-contributor results. The registered non-member `robocode-rumble-test` submitted the genuine prepared battle through [rumble-data issue 25](https://github.com/robocode-dev/rumble-data/issues/25). The issue carries the expected title and complete JSON envelope, was labelled `result-submission`, and was closed after the [ingestion workflow succeeded](https://github.com/robocode-dev/rumble-data/actions/runs/39298661602). No maintainer labelling or repair was needed. The human performed the browser step; the agent observed its outcome through the public API.

The outside-account submission, publication-first receipt, local acknowledgement and publicly served ranking data are now observed. Completion of the milestone still depends on human integration of [rumble-client PR 20](https://github.com/robocode-dev/rumble-client/pull/20); the agent does not merge the implementation or its evidence PR.

## Separate observations

| Boundary | Observed evidence |
|---|---|
| Submitted battle | Battle ID `36561530-ee47-4b7e-9e31-db19c308fc1a`, client `robocode-rumble-test-01`, Crazy 1.0.0 versus Fire 1.0.0, 35 rounds, behavior version 1. The generated payload and replay provenance remain recorded in the preceding verification document. |
| Durable acceptance | The [GitHub Actions receipt](https://github.com/robocode-dev/rumble-data/issues/25#issuecomment-6273750700) explicitly accepts that battle ID. The issue's author is the non-member test account. |
| Canonical publication | Data commit `3f56dbe8bbe0e48903239eb2e774b6ef60a524bc` contains the [canonical raw fact](https://github.com/robocode-dev/rumble-data/blob/3f56dbe8bbe0e48903239eb2e774b6ef60a524bc/results/raw/2026/10/ddd1e59ee251902a13faa69e68871cf33e57f2816178e79bce9aa5fcb068762e.json), with the original payload and `submittedBy: robocode-rumble-test`. |
| Client recovery | The unchanged verification image and client candidate `b98afc7f7449802479a434be31b61c0e5207747d` ran `docker/rumble.ps1 submit build/outside-verification/rumble-client.json rumble-client:browser-form-verification` without a token. Two attempts returned HTTP 500 before discovery had been journalled; the pending record and prepared batch remained intact. A later attempt reported `Observed 1 accepted result receipts.`, recorded issue 25 in `submissions.jsonl` and appended one acknowledgement for the battle. No client code change, manual journal edit or repost was needed. |
| Repeated collection | Another fresh `submit` process reported zero new receipts and no waiting batch. The append-only journal retained one result and one acknowledgement, leaving zero pending IDs. |
| Published ranking data | The [Pages deployment succeeded](https://github.com/robocode-dev/rumble-data/actions/runs/39298685251) for the same data commit. The publicly served [1v1 projection](https://robocode-dev.github.io/rumble-data/data/leaderboard/1v1.json) has projection ID `281f8d602f204d5e536f06929c57ebaed285207829bfbdb39b756d393ce9a758`. Crazy advances from 6 to 7 battles and APS 34.7656 to 49.8483; Fire advances from 7 to 8 battles and APS 29.4665 to 26.3065. The [public client totals](https://robocode-dev.github.io/rumble-data/data/clients.json) contain one battle for `robocode-rumble-test-01`. This verifies the dashboard's publicly served data; no browser screenshot of the rendered table was taken. |

The transient HTTP 500 responses were not reproduced by subsequent endpoint probes from the same container using either HTTP/1.1 or HTTP/2. Their server-side cause remains unestablished. Successful recovery demonstrates that these failed reads did not lose pending work or require resubmission.

## Focused failure and retry evidence

The client candidate's 66 passing tests cover receipt spoofing, exact envelope and 64-bit seed comparison, paginated discovery and comments, missing or changed canonical facts, body limits, interrupted publication checks and rejected/open retries across journal reconstruction. The client's GitHub build, Docker verification and corpus-validation checks are green.

On an isolated local checkout of the published data commit, replaying the original envelope through `scripts/ingest.py --account robocode-rumble-test` returned accepted without changing any tracked file: the published fact was recovered idempotently. `scripts/validate.py` rejected the same envelope for `unregistered-verification-account`, and rejected a copy with `rounds: 0` for the registered test account. These are local canonical-validator checks, not additional remote submissions. Earlier real outside-account invalid-form receipts are linked in the access milestone's evidence, including [issue 24](https://github.com/robocode-dev/rumble-data/issues/24).

Local logs and journal state are retained under `C:/Code/rumble-client/build/outside-verification/`, including `submit-accepted.log`, `submit-accepted-retry.log`, `submit-recovery.log` and `submit-after-ack.log`. The original configuration and batch remain available. No full clean-environment onboarding journey or new release is claimed by this verification.
