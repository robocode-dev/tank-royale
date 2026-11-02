---
id: CH-083
type: change
status: open
links: [P-007, M-029, ARCH-027, CAP-024, CAP-013]
title: An AI agent works with the participant's own GitHub account
---

# An AI agent works with the participant's own GitHub account

The Rumble command line exists, but the commands that sign in, register and send bots are held back by a condition in the design: they may not be added until it is decided whether an AI agent works with its own GitHub account or with the participant's. The maintainer decided on 2026-11-02 that an agent works with the participant's own account, and that an agent with an account of its own may be added later if someone asks for it. This change records that decision, turns the condition into the rules the sign-in commands must follow, and adds no command.

## Why

The sign-in commands are what give the command line real value, and they cannot start while the question is open. The question is about identity, not about code: whose name stands on a registration, on a pull request and in the rankings, and whose permissions an agent holds. Settling it first keeps the later command slices from deciding it by accident.

## What changes

- A decision record that states the choice and why it constrains later work. An agent acts for one participant, through that participant's GitHub account and the sign-in the participant gave. Pull requests, registration and results carry the participant's name, which fits the existing rules that a registration belongs to one account and that one account has at most one open pull request per bot. An agent with a GitHub account of its own is not supported now; it would need its own rules for registration, ownership of a bot and the rankings, and a person who vouches for it, and it can be added by a later decision.
- The rules for the sign-in commands, written down as the design's condition replaced by a contract: the person starts and approves the sign-in at GitHub with the device code, and an agent never approves anything in a browser and never creates or registers an account; a command that needs a sign-in and finds none or an unusable one ends with exit code 3 and says so in its JSON answer, without asking anything; the answers never show a token; the command line works on one environment at a time and never mixes the public and the test environment. The residual risk stays stated as it is: an agent that runs as the participant's operating system user can read the saved sign-in, as a bot could; that is accepted for the agent, which acts for the participant, and the worker boundary still keeps bots away from it.
- The acceptance criterion that says the documentation holds the condition on sign-in commands is retired and replaced by one that says the documentation holds the decision and the rules. No behavior changes and no code is written.
- The participation design, the command line capability's description and the plan are updated: the condition becomes the decision, and the plan notes that the old client repository is retired when Rumble can be run entirely from the desktop program, which the maintainer confirmed on 2026-11-02, as a stated condition and not a date.

## Challenge

The weak point is that "the participant's own account" hides two different situations. A person who signs in and then lets an agent use the saved sign-in is the case this change accepts. A person who hands an agent a sign-in from a machine the person does not control, or lets several agents share one, is not covered: the saved sign-in is bound to the user and home folder it was made in, and nothing here stops a person from copying it. The decision therefore says what it promises, that the commands treat the saved sign-in as the participant's and show it to nobody, and not that an agent can be kept from misusing a sign-in it was given. The second weak point is that exit code 3 was reserved before any command used it; the rules fix its meaning now, so the first sign-in command does not invent one.

## Not in this change

No sign-in, registration, staging, sending or status command, no change to any code or build, no change to the registration or moderation rules, and no support for an agent with its own account. The old client repository is not changed or retired; only the condition for retiring it is written in the plan.

## Publication and acceptance

This branch is local and not pushed, and has no pull request, because local acceptance is the accepted policy and the maintainer has not asked for publication. The work serves the simple-participation goal; the repository states no vision. The maintainer reviews this proposal before any document is changed.
