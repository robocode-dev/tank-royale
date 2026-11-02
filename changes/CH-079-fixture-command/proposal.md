---
id: CH-079
type: change
status: open
links: [P-007, M-029, M-037, ARCH-027, ARCH-028, CAP-021, UC-001]
title: One command that runs the Rumble fixture checks and writes a redacted report
---

# One command that runs the Rumble fixture checks and writes a redacted report

This is the thirteenth slice of the Rumble participation plan's fourth milestone (the one that delivers the shared modules and the test command). The plan asks for one documented command, run from Tank Royale, that exercises the Rumble pieces with fixtures and produces a result a program can read and a report a person can read, with secrets removed automatically. Later feature milestones add their own cases to it and the final milestone completes the matrix. This change builds the command, the result, the report and the redaction, and puts the cases that already exist under it. No screen is added.

## Why

Today the Rumble checks are spread over four modules and a Python folder, each run by its own command, and their output is plain test logs. The plan promises a maintainer one place to run and one report to read, and promises that nothing in a local fixture run touches a public resource. Without the command, each new feature slice would invent its own way to run and report, and without automatic redaction a report pasted into an issue or a chat could leak a token, a home folder or a machine name. Building the frame now, while it has few cases, is cheaper than retrofitting it after the matrix is large.

## What changes

- One Gradle command, `rumbleFixtureCheck`, documented together with the other Gradle commands in the development guide and in the participation design. It runs the offline Rumble checks of the message module, the worker, the shared Rumble library and the bot project library, plus the Python checks of the environment and reset tool, with every failing case reported and not only the first.
- The cases are named for what a maintainer cares about and are built from the existing classified tests, grouped by their criteria: environments and reset boundaries, sign-in and registration, catalog download and staging, bot submission and progress, and the battle worker and its host. A case with no tests found is a failure, so a case cannot pass by running nothing.
- A machine-readable result, `build/rumble-fixture/result.json`, with a schema version, the versions of Tank Royale, the engine behavior version, Java and the operating system, the identity of each Rumble environment (name and repository names, never an account), one entry per case with counts of passed, failed and skipped checks, each failure with its test and a redacted message, and where the retained evidence lives.
- A short human report, `build/rumble-fixture/report.md`, built from the same data, ending with a plain line that fixture success does not claim anything about public installation permissions.
- Automatic redaction of both files before they are written: GitHub token shapes, authorization headers, the values of environment variables whose names suggest a secret, the user's home folder and name, and absolute paths (shown relative to the repository). After redaction the files are scanned again for what a secret looks like, and the command fails rather than write a file that still contains one.
- A guard for the plan's promise that no local fixture test writes to a public resource: the command refuses to run when the live-check switch is on, and the report lists the live checks that were skipped by design, so a maintainer sees what the fixture run did not cover.
- The report generator is a standard-library Python script beside the existing Rumble scripts, with its own tests, like the reset tool. A Gradle task runs the tests, then the script, and fails if either fails.
- Acceptance criteria written before the code, in a new capability for the fixture check, separate from the battle worker capability.

## Challenge

The assumption most likely to be wrong is that a green fixture run means the Rumble works. It does not, and the plan says so: fixtures do not prove that the App is installed on a real fork or that a real account can sign in. The report therefore states what it did not cover (the skipped live checks and the public-installation permissions) in every run, not only when something fails. A reader who sees only green should still see that sentence.

The second weak spot is the redactor. A list of known token shapes will not catch a secret shape nobody thought of. The design therefore fails closed: after redaction the output is scanned again for the same shapes plus any long run of base64 or hexadecimal characters next to a secret-sounding word, and the command refuses to write a file that still matches. The tests include the token shapes in use today, a token inside a path, inside a URL and inside a stack trace, and a secret in the environment that is not of a known shape. This reduces the chance; it cannot reach zero, and the report says that the redaction is automatic and not proven complete.

The third is coupling to the shape of Gradle's test result files. The script reads the standard JUnit result files that the existing evidence producers already read, so the coupling is one the repository already depends on, and a missing or unreadable result file is a failed case with a plain message.

## Not in this change

No real container battles, no simulated-GitHub matrix beyond the tests that exist, no scheduling or session cases, and no live checks: the command never runs them. The complete matrix, the single report across every feature and the cleanup obligation for public smoke tests belong to the final milestone. No change to any Rumble repository. No screen.

## Publication and acceptance

This branch is local and not pushed, and has no pull request, because local acceptance is the accepted policy and the maintainer has not asked for publication of this change. The new tooling shows nothing to users, as decided on 2026-10-31. The work serves the simple-participation goal; the repository states no vision. The maintainer reviews this proposal before any code or document is changed.
