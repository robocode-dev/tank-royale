---
id: CH-073-tasks
type: tasks
status: open
links: [CH-073]
title: Stage a bot project as a separate, validated copy that is ready to submit
---

- [ ] Settle the open questions with the maintainer: launcher line endings and where the content hash lives (which files to take is settled).
- [ ] Reserve identities and write acceptance criteria for what is copied and what is left out, the fixes made only in the copy, the check of the copy, the description and its stable hash, and the folder safety rules, in the bot catalog capability, before any code, each with its test type.
- [ ] Move the content hash and its tests into `lib:bot-project`, make `lib:rumble` use it, and keep every existing test green.
- [ ] Implement the copy of the configuration file, launchers, source files, declaration files and optional license file, the grouped not-included list, the symbolic link rule and the launcher fixes, with positive and negative tests for each group.
- [ ] Implement the check of the staged copy and the description with sizes, executable flags, fixes and hash, with a test that staging twice gives the same result.
- [ ] Implement the folder safety rules and the marker file, with tests for a folder inside the project, a project inside the staging folder and a foreign folder.
- [ ] Prove that the original project is untouched by comparing it before and after staging.
- [ ] Extend the opt-in live check so every active public bot stages to its recorded hash and passes validation, and run it once.
- [ ] Update the participation design's module table, regenerate the evidence manifest and indexes and validate the corpus.
