---
id: AN-023
type: analysis
status: active
links: [M-028, ADR-049, AN-019]
title: Can an outside account join the Rumble with a browser sign-in and no hosted service
---

# Outside account sign-in experiment

## Question

The Rumble participation plan (milestone M-028) needs proof that an ordinary GitHub account with no rights in the organization can sign in through a browser, register, submit a bot, submit a result batch, and get a receipt, without a maintainer fixing anything and without a hosted service. Earlier findings in [ADR-049](../decisions/ADR-049-rumble-browser-form-submission.md) showed that a personal access token cannot be limited to the result repository for an outside user, that issues created through the API do not get the `result-submission` label, and that a GitHub App with device-flow login was a possible later option. This experiment tests that option in the isolated test Rumble, never in the live one.

## What was set up

All of this is in the test environment only (`rumble-bots-test` and `rumble-data-test` under `robocode-dev`).

- A third public GitHub App, `tank-royale-rumble-contrib-test`, created under `robocode-dev` with `python scripts/rumble/create_test_apps.py contributor`. It is the App a participant signs in with. It has no private key stored anywhere: device-flow login needs only the public client ID. GitHub limits App names to 34 characters, so the name is `Tank Royale Rumble Contrib Test`.
- Permissions requested: issues write, pull requests write, contents write, metadata read.
- Device flow had to be switched on by hand in the App settings (the option is "Enable Device Flow"). GitHub does not let the App manifest set it, which is why the two existing moderator and publisher test Apps answer `device_flow_disabled`; those two are not meant for user login.
- The App was installed on `rumble-data-test` only, for the first round. `rumble-bots-test` is added later when the bot submission is tested.
- With device flow on, GitHub issues a login code for the App's client ID, so the sign-in step itself works.

## What the data repository does today

Read from `rumble-data-test` before the experiment:

- Results arrive as an issue from the "Result submission" form. The ingest workflow only runs when the issue carries the `result-submission` label (on `labeled`, or on the scheduled sweep that lists open issues with that label).
- There is no automatic registration yet. A registration is a file under `clients/` that a maintainer adds. Milestone M-028 therefore needs a registration path that works without a maintainer, and that path does not exist in the test repository.

## Experiment steps

1. `python scripts/rumble/contributor_probe.py`, signed in as `robocode-rumble-test` (an account outside the organization), prints which installations and rights the signed-in token gets, tries to open a result issue asking for the label, tries to add the label afterwards, closes its own probe issue, and tries to fork `rumble-bots-test`. The token stays in memory only.
2. Based on those answers, prototype a no-service registration path in the test data repository (a trusted workflow reading the issue author's account from the event), then repeat for the bot pull request and a ranked batch with a receipt.

## Findings

Step 1 was run twice on 2026-10-28 as the outside test account `robocode-rumble-test`, with the App installed on `rumble-data-test` only.

| Probe | Result |
|---|---|
| Device-flow sign-in | Works. The token lives 28800 seconds (8 hours) and comes with a refresh token, so renewal needs no second browser visit. |
| Who is signed in | Works, returns the outside account. |
| Installations the sign-in can see | None listed, because the account is not a member of the organization that owns the installation. The App still acts where it is installed. |
| Create an issue in `rumble-data-test` | Works (HTTP 201), without any membership. |
| Ask for the `result-submission` label while creating the issue | The issue is created but the label is silently dropped. The first run got "You do not have permission to create labels" only because the label did not exist in the test repository; it exists in the live repository and now in the test one. |
| Add the label afterwards | Refused (HTTP 403, "Resource not accessible by integration"). |
| Fork `rumble-bots-test` | Refused (HTTP 403, same message). The App was not installed there, so this is not yet a verdict on forking. |

What the receiving side did with the unlabeled issues: the ingest workflow ran on each opened issue and skipped at once, because its job only runs for the `result-submission` label. The labeled sweep every 30 minutes would never pick them up either. Result: an outside user can submit through the App, but the receiver as built ignores the submission.

Consequences:

- Ingestion must not depend on a label the caller applies. The receiver can accept an opened issue by its form marker or title and let its own workflow token apply the label, which needs no extra credential.
- Registration has to follow the same pattern: an opened registration issue is read by a trusted workflow, which takes the account from the issue author (a payload cannot choose another owner) and writes the registration file itself.
- The App's device sign-in works for an outside account with only Issues access on one repository, which is the narrow credential the earlier analysis hoped for.
- Bot submission through a fork is not yet decided. The App must be installed on `rumble-bots-test` and the outside account may also have to install it on its own account before it can write to its fork.

## Second round: receiver changes, no maintainer

Two changes were pushed directly to `main` of `rumble-data-test` (the test repository has no branch protection): an opened issue titled `[result]` is accepted and labeled by the workflow's own token, and a new workflow registers the author of an issue titled `[register]`. Registration reads the account from the issue author, refuses banned or malformed names, keeps an existing client ID on repeat registration, and replies with the client ID.

The probe then ran again as the outside account, still signed in only through the device flow:

| Step | Result |
|---|---|
| Open a `[register]` issue | Registered `robocode-rumble-test` with a new client ID and a `clients/` file. The workflow replied and closed the issue within the wait, with no maintainer action. |
| Open a `[result]` issue without any label | The workflow labeled it, processed it, replied and closed it. The reply was a rejection of the deliberately malformed probe batch ("unsupported submission schemaVersion"), which proves the receipt path works for an outside account. |
| Fork `rumble-bots-test` | Still refused, because the App is not installed there. |

Not yet proven for M-028: an accepted ranked batch with a receipt, renewal of the sign-in with the refresh token, bans and impersonation attempts, a missing or restricted permission, and the bot submission by fork and pull request.

## Decision boundary

If the no-service path needs a broad credential, an app secret embedded in the desktop client, organization membership, moderator labeling, or maintainer intervention, work stops and a reviewed decision records the credential and hosting tradeoff. No hosted broker is authorized.
