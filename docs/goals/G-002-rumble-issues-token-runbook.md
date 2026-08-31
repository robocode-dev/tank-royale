---
id: G-002
type: goal
status: proposed
links: [G-001]
title: Internal Rumble Issues token runbook
provenance: verified
---

# G-002 — Internal Rumble Issues token runbook

**Who:** Rumble maintainers and trusted contributors who operate a ranked-result client.

**What:** An internal `rumble-data/docs/` runbook for creating, approving, supplying, rotating, and revoking the dedicated fine-grained GitHub token used by the Rumble client's Issues-only result transport.

**Why:** Preserve the Rumble trust boundary during live submissions and operational recovery by making the minimum repository scope and permission set repeatable without recording a token value or publishing the procedure as participant-facing documentation.
