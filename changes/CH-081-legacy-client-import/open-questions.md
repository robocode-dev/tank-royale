---
id: CH-081-open-questions
type: open-questions
status: open
links: [CH-081]
title: Whether the import may ever move or delete the old files
---

## Should the import ever remove the old client's files after copying them?

A copy that leaves the old files where they are means the contributor ends up with two copies of the same journal and replays, and the old client could keep running and keep appending to its own copy. Moving them would avoid that, but it is the one step that cannot be undone, and the new session slices that read the copy do not exist yet, so a move now would make the old client unusable without giving anything usable back.

Recommendation: never move or delete in this change. The import only copies and verifies. When a replacement session exists and the old client is retired, a separate, explicit action can offer to remove the old folder, after showing what would go and checking that every file is in the new record with a matching hash. The alternative is to move the files at the end of a verified import, which is tidier for the contributor and makes a mistake in the new slices unrecoverable.
