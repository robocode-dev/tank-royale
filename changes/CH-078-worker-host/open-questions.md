---
id: CH-078-open-questions
type: open-questions
status: open
links: [CH-078]
title: How the host finds the command that starts the worker
---

## How should the host know what command starts the worker?

The worker is the executable module `rumble` in worker mode. Today it has no installable form: no packaged archive, no launcher script, and no decision on where it ships (inside the desktop program's folder, next to the runner, or as its own download). The host needs a command to start it.

Recommendation: the host takes the command as an input (a program and arguments, plus the folder to run in), and this change does not decide packaging. A fixture and the live check build the command from the Gradle output. Packaging and where the executable lives are decided when the controller and the desktop program need to find it, which is soon but is a different decision with its own users. The alternative is to decide the packaging now, which would add a release artifact and a distribution question to a change about the process boundary.
