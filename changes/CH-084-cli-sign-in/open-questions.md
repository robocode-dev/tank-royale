---
id: CH-084-open-questions
type: open-questions
status: open
links: [CH-084]
title: How an agent gets the sign-in code to its person
---

## Should sign-in be two calls for programs and one blocking call for a person?

A sign-in needs the person to open a GitHub address and type a short code. A program that reads only the final answer of a command cannot show the code while the command is still waiting, and the answer contract says standard error holds nothing a program needs.

Recommendation: with `--json`, `rumble login` answers at once with the code, the address and when the code expires, and saves the waiting code protected in the environment's folder; `rumble login --finish` then checks for approval, optionally waiting up to a number of seconds, and saves the sign-in. Without `--json`, `login` shows the code and waits itself, so a person types one command. The waiting code is removed when used, expired or refused.

The alternative is one blocking call for everybody that prints the code on standard error. It avoids the saved waiting code but breaks the contract and leaves an agent unable to hand over the code in time, so I advise against it. A third option, only the two-call form for everybody, is simpler to test and to explain but makes a person run two commands.
