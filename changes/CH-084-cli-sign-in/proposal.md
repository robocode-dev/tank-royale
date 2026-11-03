---
id: CH-084
type: change
status: open
links: [P-007, M-029, ADR-053, CAP-024]
title: Sign in, sign out and register from the Rumble command line
---

# Sign in, sign out and register from the Rumble command line

The command line can read and import, but it cannot yet sign a participant in. The library already does the work: it asks GitHub for a sign-in code, waits for the person to approve it, saves the sign-in in one protected file per environment, renews it, and registers the account through the result repository. This change makes those steps commands, under the rules the maintainer approved on 2026-11-03: an agent works with the participant's own account, the person approves the sign-in at GitHub, a command without a usable sign-in ends with exit code 3, and no answer shows a token. It adds no command that uploads a bot.

## Why

Sending a bot, running a session and reading a pull request all need a signed-in, registered account. Until the command line can sign in, none of them can be commands, and an agent or a person at a terminal cannot take part. These three commands are the smallest set that gives the others something to stand on.

## What changes

- `rumble login <environment>`: asks GitHub for a sign-in code and shows the code and the GitHub address to open. The person opens the address and types the code; nothing else is done for them. With `--json` the command answers at once, in one document, with the code, the address, when the code runs out and a note that it is waiting, and saves the waiting code in the environment's folder so a second call can finish. `rumble login <environment> --finish` checks for the person's approval, waits up to a given number of seconds (default 0, meaning one check), and on approval saves the sign-in and answers who is signed in. Without `--json`, `login` shows the code and then waits for the approval itself, since a person at a terminal expects one command; it prints a short progress text, and ends as `--finish` does. The code is a short-lived single-use secret, so the waiting file is protected like the saved sign-in, removed when it is used, expired or refused, and never shown after the first answer.
- `rumble logout <environment>`: removes the saved sign-in, the waiting code and the saved registration for that environment only. It succeeds when there was nothing to remove.
- `rumble register <environment>`: renews the saved sign-in, registers the account through the result repository as the library does, saves the client ID and answers with the account name and whether it was already registered. Registering again returns the same answer and writes nothing new.
- `rumble status` keeps only reading: it says whether a sign-in and a registration are saved and does not renew anything.
- Exit code 3 now has its meaning: no sign-in is saved, it cannot be used, or GitHub refuses to renew it, and the JSON answer says which, with the command to run next. Refusals of the registration (not allowed, banned, unknown environment, no sign-in App for the environment) keep exit code 2, and network or GitHub failures exit 1.
- The answers and the waiting code follow the existing answer rules: no token, no home folder, no user name, no absolute path. The account name of the signed-in participant is shown, because it is public and is what registration is about; the redaction is taught to leave that one name alone only in the field that carries it.
- Documentation: the participation design's command list and the capability description add the three commands and the meaning of exit code 3, and the fixture check's command line case covers them with fakes.
- Acceptance criteria, written before the code, in the command line capability: each command's answer in both forms, the code flow in two calls and in one, exit code 3 and its reasons, no token in any answer or file listing, the waiting code protected and removed, logout touching one environment only, register idempotent, and refusal for an environment without a sign-in App. Tests use a fake GitHub and a fake clock, call the commands in process, and run the real jar as a real process for one case with a closed input.

## Challenge

The main weak point is that a person's approval happens in a browser, while an agent has no keyboard. The two-call flow lets an agent hand the code to its person in whatever way it can, then call `--finish`; but it means the waiting code is saved between two process runs, which a one-process flow would avoid. The alternative, one call that prints the code on standard error and blocks, breaks the rule that standard error holds nothing a program needs, and an agent that only reads the final answer would never show the code to the person in time. This change takes the two-call flow for `--json` and keeps the single blocking call for a person.

The second weak point is the live proof. The sign-in has been proved against GitHub in an experiment and the library has a live-gated check, but the commands will be proved only against fakes plus the live check already in the library. A real code round trip through the new commands in the test environment is a manual step the maintainer takes, and it is stated as not done in the handoff, not assumed.

The third is that `register` runs unattended, with a wait of up to two minutes for the workflow in the result repository. A command that can wait that long must say so in its answer as a state, and `register` must be safe to run again after a timeout, which the library already guarantees by reading the registration file first.

## Not in this change

No staging, pre-check, sending or reading of a pull request, no battle running, no change to the library's sign-in rules, the registration workflow or the moderation, and no support for an agent with an account of its own. Nothing is published. The public environment has no sign-in App yet, so its sign-in commands are refused with exit code 2 and a clear reason until it has one.

## Publication and acceptance

This branch is local and not pushed, and has no pull request, because local acceptance is the accepted policy and the maintainer has not asked for publication. The work serves the simple-participation goal; the repository states no vision. The maintainer reviews this proposal before any criterion, code or document is changed.
