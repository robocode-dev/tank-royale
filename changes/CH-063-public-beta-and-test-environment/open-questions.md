---
id: CH-063-open-questions
type: open-questions
status: resolved
links: [CH-063]
title: Where the reset tool lives was settled by the maintainer
---

## Where does the reset tool live, and how are workflows shared?

The plan says the fixture and reset tooling is shared with the integration harness delivered in the next-but-one milestone, and that workflow sources are shared or pinned reusable workflows. The tool could live in the Tank Royale repository as a command, or as a workflow in the test result repository. The maintainer decides which, since it shapes the later harness.

Resolved by the maintainer on 2026-10-27: the reset tool lives in the Tank Royale repository, as recommended, so the later shared test command can call it.
