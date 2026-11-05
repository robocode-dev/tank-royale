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

The command line is the program `rumble`, started like Tank Royale's other programs with `java -jar`. It is the controller: it holds the Rumble library and the sign-in. The battle worker is a second, separate program that the command line carries inside itself and starts when it needs to run a battle, so the worker never carries the controller's code. The commands `version`, `environments`, `status` and `import-legacy` only read or are safe to repeat; `login`, `logout` and `register` sign in, sign out and register, under the rule that an agent works with the participant's own account: the person approves the sign-in, and a command without a usable sign-in ends with exit code 3. `bot check`, `bot send` and `bot status` check a bot offline, send it as a pull request and follow it; sending makes the whole source public, says so in every answer, and needs the author's explicit license confirmation; the dry run and the send also say whether the bot's launcher scripts are the starter launchers, because a moderator must look at a bot whose launchers differ. Commands that run battles are a later slice. The desktop program will fetch the program from the release with its own version at first use, so nobody has to fetch it themselves and no separate publication is needed.
