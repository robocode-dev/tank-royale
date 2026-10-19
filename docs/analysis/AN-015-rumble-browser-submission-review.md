---
id: AN-015
type: analysis
status: active
links: [CAP-016, M-021, ADR-044, ADR-049]
title: Browser-submission implementation risks before outside-account verification
---

# Browser-submission implementation risks before outside-account verification

## Question and consumer

Does the prepared browser-form client implementation justify completing the outside-contributor result milestone? This review supplies focused verification work for accepting and publishing outside-contributor results (M-021). It does not complete that milestone or select a replacement transport.

## Evidence boundary

Inspected on 2026-10-19 in a prepared Windows and PowerShell environment with Git and an existing local checkout. The client candidate is [commit 2a172216157f8fcb171da297cc0bad103fb8a8cf](https://github.com/robocode-dev/rumble-client/commit/2a172216157f8fcb171da297cc0bad103fb8a8cf), whose parent is the published main revision `c308a4c9de427d0bad467463049e6eb38a5d1f5a`. Tank Royale's accepted base is `59e16b4d10d27347ddc5e676508b52432a621da0`; its separately prepared contract amendment is `b952543aa604de43545f003d62488ef07271e08d`.

These are source-derived findings. No client test, clean installation, container battle, outside-account post or receipt-spoofing experiment was performed during this review. The proposed regression checks below remain unexecuted. Existing tests use a fake issue transport for the new submission flow; constructing the real transport without a credential does not exercise its HTTP responses or receipt provenance.

## Findings for the implementing milestone

### Receipt text alone does not establish publication

The [transport reads every returned comment body](https://github.com/robocode-dev/rumble-client/blob/2a172216157f8fcb171da297cc0bad103fb8a8cf/src/main/java/dev/robocode/rumble/client/GitHubIssueOpsTransport.java#L60) and turns any line ending in `: accepted` with a UUID prefix into a successful receipt. It does not check the comment's author, association with the ingestion workflow, or a canonical published fact. The submission coordinator filters by expected battle ID, but that ID is already present in the public batch. The same parser existed before this candidate.

Inference: an ordinary comment containing an expected battle ID and accepted marker can be mistaken for a publication-first acknowledgement and cause the local record to be acknowledged prematurely. This would violate the unchanged rule that success follows canonical fact publication. Before integration, exercise the actual HTTP adapter with an ordinary user's accepted-looking comment, a genuine ingestion receipt and a missing/unpublished fact. Resolve receipt authority against the ingestion contract rather than treating a fake transport's accepted result as proof.

### A rejected attempt can hide a newer in-flight attempt

The [coordinator collects terminal battle IDs across all prior submitted batches](https://github.com/robocode-dev/rumble-client/blob/2a172216157f8fcb171da297cc0bad103fb8a8cf/src/main/java/dev/robocode/rumble/client/IssueOpsSubmission.java#L55), then excludes those IDs when constructing the set of in-flight records. The journal retains an older rejected submission while its record remains pending.

Inference: after a rejected issue is retried through a new issue, the old terminal attempt can make the still-open new attempt appear not in flight, allowing another prepared batch for that result. Test a rejected issue followed by an open retry across repeated process restarts, then publication and receipt recovery. An identical raw fact may remain idempotent server-side while the client still creates confusing duplicate work; both boundaries need checks. The global terminal-ID calculation predates this candidate.

### Discovery has a fixed recent-issue window

The [lookup reads at most three pages of 100 issues](https://github.com/robocode-dev/rumble-client/blob/2a172216157f8fcb171da297cc0bad103fb8a8cf/src/main/java/dev/robocode/rumble/client/GitHubIssueOpsTransport.java#L25), sorted newest first, and matches only the title. The returned list can include pull requests, which still consume entries before the code excludes them.

Inference: an issue posted before enough newer activity can become permanently undiscoverable to this client, leaving its batch in the unposted state despite an available receipt. Title equality also does not verify that the discovered issue belongs to the registered contributor or carries the intended batch. Test delayed collection beyond the window, a same-title foreign issue and anonymous request exhaustion. A supported recovery must let the contributor correlate the real issue without losing the retained records; the particular lookup or recovery mechanism remains implementation work.

### The size bound does not reject a single oversized record

The [batch splitter applies its size check only when the current batch is nonempty](https://github.com/robocode-dev/rumble-client/blob/2a172216157f8fcb171da297cc0bad103fb8a8cf/src/main/java/dev/robocode/rumble/client/IssueOpsSubmission.java#L108). A single record larger than its configured body bound is therefore still prepared unchanged.

Inference: the current check alone does not establish that every prepared batch is bounded. Determine whether the synchronized catalog and result schemas make such a record impossible. If they do not, provide a clear failure that retains the record, with a focused regression check. The existing large-batch test proves splitting several records, not handling an individually oversized record.

## Outcome and remaining work

The candidate implements the intended manual handoff and includes useful preparation, batching and fake-transport retry coverage. That is prepared implementation work, not evidence of public acceptance or published rankings. Keep the replacement submission and end-to-end criteria draft and leave the milestone unfinished.

Next verification must cover the real read-only adapter and journal transitions above, anonymous request behavior, and a genuine ranked battle posted through the form by the registered outside account. Record the exact client, catalog and engine revisions, raw fact and receipt links, ranking publication and all interventions. Distinguish posting a batch, receiving acceptance, acknowledging the journal and observing a ranking update.

Replacing the accepted form decision with a GitHub App or another transport was not pursued: that would introduce a new authentication decision before the accepted path has been tested. Treating the existing fake-transport tests or a maintainer-account success as sufficient was also rejected as a verification approach because neither exercises the affected outside-account boundary.
