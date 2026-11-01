---
id: CH-069-tasks
type: tasks
status: open
links: [CH-069]
title: Make all four Bot APIs read and write colour text the same way
---

- [x] Settle the open questions with the maintainer: whether Java's writer keeps transparency, and where the criteria live.
- [x] Run a small check in each of the four APIs of what a colour written into a team message looks like and which colour texts each reader accepts today, and record the result in the proposal's table before changing anything.
- [x] Reserve identities and write acceptance criteria for reading the four colour forms in both letter cases, refusing non-colours, and writing a colour into a team message, in the protocol capability, each with its test type.
- [x] Add shared test cases for those rules to the shared test definitions and the helper methods the four test runners need, and see them fail where an API disagrees.
- [x] Make the Java API read the four forms and write transparency in team messages, with its tests.
- [x] Make the C# API read the four forms and write like the others, replacing the converter's formats, with its tests.
- [x] Make the Python API read the four forms in all its colour helpers and write like the others, with its tests.
- [x] Make the TypeScript API read the four forms and write colour text in team messages like the others, with its tests.
- [x] Correct the shared schema's colour description and pattern, and add a line per API to the bot developers' changelog.
- [x] Run the four APIs' full test suites, update the test registry, regenerate the evidence manifest and validate the corpus.
