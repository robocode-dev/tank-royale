---
id: ADR-055
type: decision
status: verified
links: [ADR-049, ADR-043, ADR-044, AN-023, P-007, CAP-015, CAP-016]
title: A signed-in contributor's program sends Rumble result batches and reads their receipts itself
author: agent
accepted-by: Flemming N. Larsen (2026-11-16, Claude Code conversation)
---

# ADR-055: A signed-in contributor's program sends Rumble result batches and reads their receipts itself

## Context

[ADR-049](ADR-049-rumble-browser-form-submission.md) made the web form the way an outside contributor submits ranked results: the client prepares each batch and the contributor pastes it into the form, so the client holds no result-submission credential. It recorded a GitHub App with a browser sign-in, limited to Issues on the result repository, as the later option for fully automatic submission, to be decided on its own once its assumptions were tested.

The [outside-account experiment](../analysis/AN-023-outside-account-sign-in-experiment.md) tested them in the test Rumble on 2026-10-28. An account outside the organization signed in with a short code through a public App that keeps no secret; the sign-in lasts eight hours and renews with a refresh token and no secret. With only Issues access on the result repository it opened issues there; GitHub dropped the label it asked for, so the receiving workflow was changed to accept a `[result]` issue by its title and label it with its own token. A valid ranked batch was then accepted with a receipt, and a batch under another account's client ID, a repeated battle ID and a banned account were refused with receipts, all without a maintainer. A revoked sign-in stopped working at once.

The shared Rumble library already signs the contributor in this way, keeps the sign-in in one protected file, and registers the account by opening an issue with it. The participation plan promises contribution sessions that an outside account completes without manual posting.

## Decision

A contributor who is signed in through the environment's sign-in App sends their ranked batches from the program: the program opens one `[result]` issue per batch in the environment's result repository with that sign-in, holding exactly one fenced `json` envelope, the same envelope the form takes. It then reads the receipt on each issue and acknowledges, journal record by journal record, only what the receipt accepted after publication, as [ADR-044](ADR-044-durable-rumble-result-acknowledgement.md) requires.

The only credential is the contributor's own sign-in to the App. The App asks for Issues access to the result repository and nothing that can change code, branches, releases, Pages or result projections; it holds no secret, and the desktop program and the command line embed none. Battle workers never receive the sign-in. A record is marked handed over in the journal before its batch is sent, so a crash or a failed send never loses it, and sending the same batch again is safe because the result data answers an identical retry with the same receipt.

This revises the client-credential part of ADR-049 and of [ADR-043](ADR-043-rumble-client-trust-boundary.md): the client may hold this one sign-in. Everything else in them stands: issue-ops is the transport, batches hold one to sixty results and are bounded by size, every completed ranked record is sent with no selection, and ingestion, validation and publication-first receipts are unchanged. The web form stays usable for a contributor who does not sign in.

## Rationale

The experiment showed the narrow credential ADR-049 hoped for: Issues on one repository, for an ordinary account, with no hosted service and no embedded secret. It removes the one manual step per batch that unattended sessions cannot have, without weakening the trust model: the receiving workflow still takes the account from the issue author, checks the client ID against that account's registration, and refuses repeats and banned accounts. A hosted broker would need a service and a secret the Rumble does not want; a personal token would give the program write access to every repository the contributor can write to.

## Consequences

- A session can send and acknowledge its results unattended while the contributor stays signed in; when the sign-in has expired or been revoked, records wait, nothing is lost, and the program says that signing in again is needed.
- Each environment needs a sign-in App and a receiving workflow that accepts a `[result]` issue by its title. The test Rumble has both. The public Rumble has neither yet, so sending there waits for the maintainer to set them up; until then a public contributor has no supported way to send from the new program.
- Reading receipts uses the sign-in, so anonymous rate limits do not apply to a signed-in contributor.
- The client capability's submission criteria and the contributor guides are revised by the change that implements this decision.

## References

- [ADR-049: Outside contributors submit Rumble results through the web form](ADR-049-rumble-browser-form-submission.md)
- [ADR-043: Rumble client trust boundary](ADR-043-rumble-client-trust-boundary.md)
- [ADR-044: Durable Rumble result acknowledgement](ADR-044-durable-rumble-result-acknowledgement.md)
- [AN-023: Outside account sign-in experiment](../analysis/AN-023-outside-account-sign-in-experiment.md)
- [Participation design](../design/rumble/participation.md)
