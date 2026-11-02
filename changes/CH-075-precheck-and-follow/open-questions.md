---
id: CH-075-open-questions
type: open-questions
status: resolved
links: [CH-075]
title: What the pre-check does with a lower version
---

## Should the pre-check warn when the new version is lower than the active one?

The repository's validator refuses a bot only when the version is unchanged and the source differs. It does not require the version to be higher, so a lower version passes its check, and the catalog then keeps only one active entry per name. The contributing guide says a source change requires a version bump, which most people read as "higher".

Recommendation: the pre-check gives a warning, not a problem, when the version is lower than the active one, saying that the Rumble accepts it but the older version would replace the newer. It never blocks, in line with the check being advice only. The alternative is to say nothing and mirror the validator exactly, which keeps the copy simple but lets an author step backwards without noticing.

Resolved by the maintainer on 2026-11-02, approving the recommendation: a warning, never a problem and never a block.
