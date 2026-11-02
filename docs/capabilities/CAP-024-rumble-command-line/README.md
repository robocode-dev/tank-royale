---
id: CAP-024
type: capability
goal: G-001
status: draft
links: [G-001, P-007, CAP-021, CAP-022, CAP-023]
title: Rumble command line
---

# CAP-024 — Rumble command line

Let a person, a script or an AI agent use the Rumble from a terminal, with commands that work without being asked anything, answer in a form a program can read, and end with an exit code that means something, so the command line can be a main way to take part and not only the screens.

The command line is the program `rumble`, started like Tank Royale's other programs with `java -jar`. It is the controller: it holds the Rumble library and, later, the sign-in. The battle worker is a second, separate program that the command line carries inside itself and starts when it needs to run a battle, so the worker never carries the controller's code. The first commands only read or are safe to repeat: `version`, `environments`, `status` and `import-legacy`. Commands that sign in, change something on GitHub or run bots are later slices, and none of them may be added before it is decided whether an agent works with its own account or with a participant's. Whether and when the program is published is decided at the first release where it gives users something real.
