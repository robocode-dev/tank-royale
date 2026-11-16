---
id: CH-103
type: change
status: open
links: [P-007, CAP-016, CAP-015, ADR-055, ADR-049, ADR-044]
title: Send a signed-in contributor's completed ranked results in batches from the program and acknowledge each record only by its published receipt
---

# Send a signed-in contributor's completed ranked results in batches from the program and acknowledge each record only by its published receipt

A contribution session now keeps every completed ranked battle in the journal, but nothing sends it. The plan item for contributor-controlled unattended sessions (M-030) promises that an outside account completes a multi-battle session without manual posting. The decision this change brings, [ADR-055](../../docs/decisions/ADR-055-signed-in-contributors-send-rumble-results.md), lets a contributor signed in through the environment's sign-in App send their batches as issues in the result repository and read the receipts themselves; it revises the client-credential part of the web-form decision and keeps everything else.

This slice builds sending and receipts in the shared library: no screen and no command. Restart after a crash, the screens and the command line are later slices.

## Why

Without sending, results never reach the rankings unless the contributor pastes each batch into a form, which an unattended session cannot do. Acknowledging only by receipt, and only after the receipt says the result was published, keeps the journal the truth about what reached the Rumble: nothing is lost on a crash, a failed send, a lapsed sign-in or a rejected batch.

## What changes

- **The decision is recorded** as ADR-055, and the client capability's design and the participation design say what it changes.
- **Batches are made from the journal.** Every waiting record of the current engine epoch goes into a batch, with no selection, in the order kept. A batch holds one to sixty results and stays well under the issue body limit by its size. Each record becomes the result-data record shape: battle identity, completion time, the client's registered identity and version, the engine's behavior version, the game type, the pin's rounds and battlefield, and each participant's result, marked as a team for TwinDuel.
- **A batch is sent as the signed-in contributor.** Each batch's records are marked handed over in the journal before the issue is opened; the issue has a `[result]` title unique to the batch and exactly one fenced `json` envelope. A send that fails returns its records to waiting; a crash after the handover leaves them handed over, and they are sent again, which the result data answers with the same receipt.
- **Receipts are read and acknowledged per record.** The program reads the reply on each issue it opened. A record the receipt accepted is accepted in the journal; a record it rejected is rejected with the reason, kept for diagnosis and never sent again by itself; a batch refused before its records could be read returns its records to waiting with the reason. A record with no receipt yet stays handed over and is looked at again later.
- **Records of another engine epoch are set aside** before batches are made, so incompatible facts are never sent.
- **Sending runs between battles.** The session sends when enough results wait or after a set time, and collects receipts, without delaying the battle it is about to run by more than the calls themselves.
- **No sign-in, no sending.** Without a saved sign-in, with one that cannot be renewed, or with no registered client ID, nothing is sent, records wait, and the session reports why in a plain code.

## Challenge

- **Assumption most likely to be wrong:** that the receipt's text can be read reliably per battle. The receiving workflow writes a fenced block of lines `<battleId>: accepted` or `<battleId>: rejected: <reason>`, and one line for a batch refused before its records could be read. If a real receipt cannot be read without guessing, the slice stops there.
- **Credible alternative:** keep the web form and only prepare batches. Rejected by the plan's promise of sessions without manual posting; the form stays for those who do not sign in.
- **Cheapest test:** a fake GitHub API that records the issues it is given and answers with receipts in the real format, for every path: accepted, rejected, refused batch, no receipt yet, failed send, lapsed sign-in, crash between handover and send. Then one real batch sent and receipted in the test Rumble by the outside test account.
- **Result that revises the work:** a real receipt that does not correlate to the records sent, or a send that loses a record, stops the slice.
- **How it could pass every criterion and still fail a contributor:** the public Rumble has no sign-in App and does not accept an unlabelled `[result]` issue yet, so sending works only in the test Rumble until the maintainer sets those up. The proposal says so and the plan item records it.

## Out of scope

Setting up the sign-in App or the label-free intake in the public result repository, restart after a crash, the screens and commands, replay evidence, and changes to ingestion or validation.

## Acceptance criteria

These will be written as criteria of the Rumble client capability, each with its identity, when the work starts. Test class Unit unless stated.

- **Batches hold every waiting record of the current epoch**, one to sixty each and bounded by size, in the result-data record shape.
- **A batch is sent only after its records are handed over in the journal**, and a failed send or crash never loses a record.
- **Receipts acknowledge per record**: accepted, rejected with its reason, a refused batch back to waiting, and no receipt yet left handed over.
- **Without a usable sign-in or client ID nothing is sent**, and the reason is reported.
- **Sending and receipts run between a session's battles.**
- **One real batch is sent and receipted in the test Rumble.** (Integration, needs the outside test account's saved sign-in; skipped without it.)

## Identity allocation

The change and the decision were allocated with the repository's coordinated allocation. Criterion identities are allocated when the criteria are written.

## Human evidence

One real batch sent from a session by the outside test account in the test Rumble, with its receipt, if the integration test cannot use a saved sign-in on the maintainer's computer.
