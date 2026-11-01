---
id: CH-071-open-questions
type: open-questions
status: resolved
links: [CH-071]
title: One account per environment and a lock between programs
---

## Is one saved account per environment enough?

The store keeps one file per environment, so each environment holds one signed-in account. The recommendation is to keep it that way: a participant uses one GitHub account, and a second account is a rare case that can sign out and in. The alternative is a file that can hold several accounts, which needs a way to choose between them on every start.

Resolved by the maintainer on 2026-11-01: one saved account per environment. A person who needs more can ask for it later as a feature request.

## Should two programs be allowed to share the saved sign-in?

The desktop program and a separate headless session can both run on the same computer and the same account. The recommendation is yes, with the lock on the file described in the proposal, because otherwise the person would have to sign in separately for each. The alternative is to let only one program at a time use the saved sign-in and make the other ask for its own, which is simpler but a worse experience and still needs a way to detect that another program is using it.

Resolved by the maintainer on 2026-11-01: yes, programs in Tank Royale may share the saved sign-in, with the lock.
