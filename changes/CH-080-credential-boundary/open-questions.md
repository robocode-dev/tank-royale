---
id: CH-080-open-questions
type: open-questions
status: open
links: [CH-080]
title: Whether the host sets the home folder through the Java environment variable
---

## Should the host tell every Java process in the worker's tree to use the worker's empty home folder?

On Windows, Java finds the user's real home folder by asking the operating system, and ignores the home variable in the environment. The host already points the worker's own Java at an empty home folder, but only when the worker is started with the usual command that carries that setting; a caller may give any command, and the server, the booter and every Java bot the worker starts are Java processes of their own that do not get the setting at all. That means a Java bot inside the worker can still learn the real home folder, and with it the place where the saved sign-in lives.

There is an environment variable that every Java process reads when it starts, `JAVA_TOOL_OPTIONS`. If the host puts the home and temporary folder settings there, every Java process in the tree uses the worker's empty folders, whatever command started it. The cost is that each of those processes prints one line saying it picked the settings up (the host throws that away, but it ends up in the server and booter logs the worker reads), and a Java bot that really wanted the real home folder will not get it.

Recommendation: yes, set it. It closes the gap at the one place that starts the tree, instead of relying on every caller to remember the right command. The alternative is to leave the host as it is and document that a custom command must carry the settings itself, which leaves a boundary that depends on callers being careful.
