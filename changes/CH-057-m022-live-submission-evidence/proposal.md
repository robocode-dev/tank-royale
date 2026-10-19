---
id: CH-057
type: change
status: open
links: [M-022, P-006, AN-018, ADR-050]
title: Record live bot submissions and complete M-022
---

# Record live bot submissions and complete M-022

M-022 requires evidence that an official starter can become a published bot and that a later source change and version bump produces a superseded entry and a new active entry. The starter and author-guide work is merged, and the test owner has now submitted both versions through the bot repository. Record the public PR, workflow and generated catalog evidence in a new historical analysis, then mark M-022 done with links to both the existing template verification and this live evidence. AN-018 remains unchanged because it records the earlier template spike as it stood before submissions.

The milestone contract remains unchanged. The plan remains active and wanted; its next work is the contributor walkthrough. The current catalog shows RumbleTestScout 1.0.0 superseded and 1.0.1 active under the same outside owner, with distinct generated source hashes. Both automatic publication attempts needed documented manual recovery. [rumble-bots PR 16](https://github.com/robocode-dev/rumble-bots/pull/16) added a bounded retry for GitHub's merged-PR association delay, but a later live submission has not yet exercised that retry path.

The maintainer selected PR acceptance for this tracked change, following the repository's AGENTS.md and C-002. The accepted checkout has no `.clue/acceptance.yaml`, so the Cliewen CLI's default would otherwise be local acceptance. This route choice applies to CH-057 only and does not change the repository-wide acceptance configuration.

## Challenge

The risky assumption is that the catalog's final state proves the full submission workflow. The catalog confirms the two merged versions and their source hashes; the PRs and workflow runs show how they arrived. Preserve the initial publication failures and manual recoveries, and state that the new retry path in [rumble-bots PR 16](https://github.com/robocode-dev/rumble-bots/pull/16) remains unexercised by a later live submission. Do not claim broader end-to-end verification than these observations support.
