---
id: PDR-017
type: decision
status: inferred
links: [CAP-014, ARCH-022]
title: Ranked Rumble derivatives must contribute to battle strategy
author: agent
accepted-by: []
---

# Ranked Rumble derivatives must contribute to battle strategy

## Context

The public bot catalog supports learning and licensed reuse. Separate ranked entries for copies can fill the competition with essentially the same bot, even when the copied implementation has been rewritten to disguise its origin. License permission to reuse code and eligibility for a separate ranking entry serve different purposes.

## Decision

Moderators may reject a derivative of an existing catalog bot that retains substantially the same battle strategy without a substantial independent strategic contribution. This applies even when extensive rewriting, reorganization, translation or small parameter adjustments conceal the copied implementation; rejection is not limited to cosmetic changes. Licensed derivatives with a substantial strategic contribution remain eligible under this rule, whether or not their scores improve. Similar behavior alone is insufficient evidence of derivation; independently written bots can use the same strategy.

Moderators compare the underlying implementation and strategic decisions, using source and battle behavior as evidence, consider the author's explanation, and record the compared entry, evidence of derivation and reasoning about the strategic contribution. The author may seek reconsideration by a moderator without a conflict of interest. No numerical similarity threshold or automatic plagiarism verdict is introduced. Existing automatic checks do not establish compliance with this human rule, and reported cases can be reviewed after automatic acceptance under the existing dispute procedure. Repeated member identities within one TwinDuel team remain valid.

The [catalog submission design](../design/rumble/bot-submission.md#distinct-ranked-entries) describes how the rule fits the existing moderation path.
