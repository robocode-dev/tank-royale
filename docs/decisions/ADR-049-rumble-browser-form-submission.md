---
id: ADR-049
type: decision
status: verified
links: [P-007, M-020, ADR-043, ADR-044, CAP-015, CAP-016]
title: Outside contributors submit Rumble results through the web form
author: agent
accepted-by: Flemming N. Larsen (2026-10-18, Claude Code conversation)
---

# ADR-049: Outside contributors submit Rumble results through the web form

## Context

[ADR-043](ADR-043-rumble-client-trust-boundary.md) made issue-ops the only V1 transport and limited the client credential to Issues access on the result-data repository. The client implements this by creating the result issue through the GitHub API with a personal access token and asking for the `result-submission` label in the same call.

An outside contributor is a registered account that is not a member of the organization and has no push access. On 2026-10-18 the first milestone of the announcement-readiness plan tested the path with such an account (a free personal account whose registration was merged by a moderator) and observed three facts. A fine-grained token cannot be limited to the result-data repository, because GitHub offers only the account itself as resource owner and says such a token can change only resources that owner owns. A classic token with the `public_repo` scope can be created, but it grants write access to every public repository the account can write to, which is not an Issues-only credential. An issue created through the API by that account arrived without the label, because GitHub drops labels for users without push access, and the ingestion workflow, which handles only labelled issues, skipped it, leaving an unprocessed issue with no receipt.

The same account created an issue through the repository's `Result submission` web form with no token. The form's template applied the label, ingestion ran, one rejection receipt per record was posted for a deliberately invalid batch, and the issue was closed. This worked for a pasted batch of 60 results of about 45,500 characters. A form link with the batch prefilled in the address was refused as too long at about 11,800 characters, so only the batch's title can travel in the link. A batch copied from a rendered Markdown view lost its code fences and was rejected by the extraction step.

## Decision

An outside contributor submits a ranked batch through the web form. The client writes the complete batch envelope, inside a single fenced `json` block, to the clipboard or to a plain text file, never to a Markdown file, and opens the form's short address with only the title prefilled. The contributor pastes the batch and creates the issue. Ingestion, receipts, durable acknowledgement and journal rollover are unchanged, and the client reads the receipt from the public issue without a credential.

The client holds no result-submission credential for this path, so the Issues-only credential rule of ADR-043 is satisfied by having none. Accounts with push access to the result-data repository may keep using the API transport, because the label is applied for them; this is an optional convenience, not a second supported contributor path.

The client splits a ranked journal into batches by serialized size as well as by the limit of sixty results, keeping each batch comfortably below the issue body limit.

A GitHub App with a device-flow login, limited to Issues on the result-data repository, is recorded as the later option for fully automatic submission by outside contributors. It is not adopted here: it is new authentication design that the readiness plan does not approve, and its assumptions (that an app user token can create issues for an outsider, what labels it can apply, and who installs it) have not been tested.

## Rationale

The form is the only path observed to work end to end for an outside account today, needs no change to ingestion, and removes the credential rather than weakening it. Accepting the classic token would make every contributor hand the client write access to all of their public repositories, defeating the purpose of the credential boundary. Changing ingestion to recognize unlabelled issues from registered authors would fix the label but leave the token problem unsolved.

## Consequences

- Submission has one manual step per batch. Contributors who expected unattended sessions must repeat it, and the guides say so plainly.
- The client's container needs no egress to the forge for submission, which fits its egress-constrained design.
- The client's submission criteria and the part of ADR-043 that describes the client credential are revised by the milestone that implements this decision, together with the contributor guides, templates and failure and retry checks.
- Reading receipts without a credential depends on anonymous API limits, which have not been measured; the implementing milestone verifies receipt correlation for an outside account.
- If the manual step proves unacceptable, the GitHub App option is evaluated as its own decision after testing its assumptions.

## References

- [Cross-cutting design overview and submission flows](../design/README.md)
- [ADR-043: Rumble client trust boundary](ADR-043-rumble-client-trust-boundary.md)
- [ADR-044: Durable Rumble result acknowledgement](ADR-044-durable-rumble-result-acknowledgement.md)
- [Rumble announcement readiness plan](../history/HIST-001-rumble-readiness.md)
- [CAP-016: Rumble client](../capabilities/CAP-016-rumble-client/README.md)
