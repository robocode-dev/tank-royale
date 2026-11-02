---
id: CAP-023
type: capability
goal: G-001
status: draft
links: [G-001, P-007, CAP-016, CAP-022]
title: Rumble legacy client import
---

# CAP-023 — Rumble legacy client import

Let a contributor who has run the old, separate Rumble client bring its state into the new shared Rumble modules without losing or changing any of it, so the old client can be retired later without leaving unsubmitted results, receipts or replays behind.

The import reads the old client's configuration file and work folder, works out which Rumble environment they belong to, and copies the journal and the replay evidence byte for byte into a folder of imported old state below that environment's state folder, with a hash check of every file and a record that is written last. It also reads the client ID and the own-bot preferences into a small typed record. It never moves, changes or deletes an old file. Converting the journal, the receipts and the replays into the new formats, and offering to remove the old files, belong to later changes. The external [Rumble client](../CAP-016-rumble-client/README.md) keeps its behavior until it is replaced.
