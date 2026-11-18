---
id: CH-106-tasks
type: tasks
status: open
links: [CH-106]
title: Run sessions as a program of their own, followed and controlled from the Contribute battles screen and the command line
---

- [x] Write the criteria, each with its identity and test class.
- [x] Keep the session folder in `rumble contribute`: lock, process ID, owner, status, log, summary and control requests, and refuse a second session of the same environment.
- [x] Watch the owner and stop after the running battle when it is gone; release a session from its owner on request.
- [x] Add `rumble session status`, `pause`, `resume`, `stop` and `stop-now`.
- [x] Fetch the command line program in the desktop program after asking.
- [x] Build the Contribute battles screen's controller without a window and its dialog, with every text in the resource files.
- [x] Find a running session, or the summary of one that ended, when the desktop program starts, and give a notice when the battles are done.
- [x] Ask on the first close while a session runs and keep the answer as a setting.
- [ ] Prove each criterion in both directions, and let the maintainer try the screen in the test Rumble.
- [x] Add the cases to the fixture command, and update the designs and the plan item.
