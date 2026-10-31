---
id: CH-067-tasks
type: tasks
status: open
links: [CH-067]
title: Read the Rumble bot catalog and fetch bot sources with hash verification in the shared library
---

- [ ] Settle the open question about how much of the repository to fetch and when to change that.
- [ ] Reserve identities and write acceptance criteria for catalog reading, verified source fetching and safe extraction in the Rumble client capability, before any code, each with its test type.
- [ ] Implement the typed catalog model and its reading, including unknown fields, missing team members, unsupported schema versions and the active-only view, with positive and negative tests.
- [ ] Implement the tree hash with a test vector from the bot repository's own algorithm, including a source file with Windows line endings and a skipped cache folder.
- [ ] Implement the network interface, the HTTP implementation and the verified fetch with temporary extraction, refusing mismatches and paths that escape the target, with fixture-archive tests.
- [ ] Update the evidence producer settings if needed, regenerate the evidence manifest and run the module build.
- [ ] Update the participation design's module table, regenerate indexes and validate the corpus.
