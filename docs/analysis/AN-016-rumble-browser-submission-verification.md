---
id: AN-016
type: analysis
status: active
links: [CAP-016, M-021, ADR-049, AN-015]
title: Browser-submission regression evidence and prepared outside-account batch
---

# Browser-submission regression evidence and prepared outside-account batch

## Consumer and boundary

This evidence supports the milestone for accepting and publishing outside-contributor results. It follows the [source review of receipt, retry, discovery and size risks](AN-015-rumble-browser-submission-review.md). The implementation is prepared in [rumble-client draft PR 20](https://github.com/robocode-dev/rumble-client/pull/20); it is not merged, and the outside-account form submission, durable acceptance receipt and published-ranking observation remain unperformed. Keep the milestone unfinished and its draft acceptance criteria draft.

Verification took place on 2026-10-19 in an existing Windows development environment using PowerShell 7.6.7 and Docker Desktop with Linux/amd64 containers. This was a prepared environment, not a clean installation. Browser automation reported no connected browser; no issue was posted on behalf of either the test account or the maintainer.

## Revisions and execution

The final client candidate is [b98afc7f7449802479a434be31b61c0e5207747d](https://github.com/robocode-dev/rumble-client/commit/b98afc7f7449802479a434be31b61c0e5207747d), based on the earlier browser-form candidate. Its pinned Tank Royale source is `9c25770c02671d1f9d941aad312013562a441427` (version 1.3.1, behavior version 1). An initial integration run against current Tank Royale main failed because that source advertises behavior version 2; the positive fixture requires version 1 and the negative fixture rejects version 2. Both pass against the client's pinned source. No engine contract or fixture expectation was changed.

The final `gradlew --no-configuration-cache -PtankRoyaleSource=C:/Code/rumble-client/tank-royale-source build installDist` passed all 66 tests, including the real-runner integration tests. The ordinary published-runner `clean build` also passed earlier, before the final two regression additions. Tests include the real HTTP adapter backed by a local HTTP server, journal reconstruction across process restarts and canonical-publication checks backed by repository fixtures.

The verification container overlays the rebuilt client distribution on the existing published runtime image `ghcr.io/robocode-dev/rumble-client@sha256:ff82d1d3394fde74f62bb24e8d116f1e8b36a2363e368670fdc90b35780a325a`. It is a local verification image, not a release or a fresh full Dockerfile build. Runtime preflight passed for Java 25, .NET SDK 10, Python 3.14 and Node.js 24.21. The final local image ID is `sha256:ec70baade80f73ef88683d3e9ec42d83a37c95518121106e7e14ef9e36ad6be3`.

## Risk checks

| Boundary | Implemented correction and observed regression evidence |
|---|---|
| Receipt authority and publication | Ordinary users' accepted-looking comments and a bot name with the wrong numeric author ID yield no receipt. Workflow-bot comments are read across comment pages. A receipt acknowledges a journal record only when a fresh canonical checkout contains the identical payload submitted by the registered account, either as a raw fact or in a monthly rollup. Missing, foreign and modified facts leave the result pending. Interrupted publication verification retains the journal and recovers on retry. |
| Retry attempts | A rejected issue followed by an open retry remains in flight across three journal reconstructions; no third batch is prepared. Later acceptance acknowledges the record. Prepared titles are unique even when retries share a fixed clock instant. |
| Issue discovery and anonymous limits | Discovery continues beyond three pages, filters by the registered account, excludes pull requests and compares the full prepared JSON envelope. Same-title foreign issues and changed envelopes do not match. An HTTP 403 fails explicitly and a later read can recover. Repeated real anonymous lookup before posting retained the same prepared batch. |
| Body limits and payload identity | Count and size splitting remain covered; a single oversized record reports its battle ID and remains pending without a prepared batch. JSON comparisons distinguish adjacent 64-bit selection seeds without floating-point rounding. |

These checks establish focused client regression evidence. They do not establish remote ingestion idempotency, outside-account receipt recovery or published-ranking updates for the new batch.

## Genuine battle retained for the next step

A container battle using client commit `c53fc7358b3e78f26023eb24cd3afe4b1ac68a7a` completed with registered client ID `robocode-rumble-test-01`: Crazy 1.0.0 versus Fire 1.0.0, 35 rounds, arena 800 by 600, behavior version 1, battle ID `36561530-ee47-4b7e-9e31-db19c308fc1a`. The canonical data revision is `895722a6363a07b727e6ff4b7a4788ac3a50cb7a` and the bot catalog source commit is `aeb34602096ed93ed7252b8d7adb01a54148a0d8`. The replay hash is `sha256:76cee1461e005488ac76459fe8aab09a24ab0f36b991d8190c3ece78fac3e1e9`.

The canonical data repository's `extract_envelope.py` and `validate.py --account robocode-rumble-test` accepted the generated batch locally at that data revision. This is schema and policy validation, not a durable remote acceptance receipt. The final candidate repeated anonymous `submit` without creating another batch. No token was passed to the container.

Local continuation state is retained under `C:/Code/rumble-client/build/outside-verification/`: configuration `rumble-client.json`, battle and submission logs, extracted `envelope.json`, the canonical validation checkout, and `.rumble-client/` containing the journal, replay and prepared text. The prepared file is `.rumble-client/submissions/submission-result-robocode-rumble-test-01-2026-10-19T17-21-04-792Z.txt`. Do not run `clean` in the client checkout before copying or finishing this retained verification state.

To continue, connect a browser signed in as the non-member `robocode-rumble-test`, open the form address printed by `submit`, paste the complete text file and create the issue. Then rerun `docker/rumble.ps1 submit build/outside-verification/rumble-client.json rumble-client:browser-form-verification`. Record the issue and workflow links, receipt, canonical fact, journal acknowledgement and published ranking separately. The remaining milestone failure checks and merged implementation are still required before completion.
