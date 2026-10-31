---
id: CH-066-open-questions
type: open-questions
status: open
links: [CH-066]
title: Questions before building the first part of the shared Rumble library
---

## Where should the criteria for the shared library live?

The environment and dependency rules are new behaviour, so they need acceptance criteria before code. The Rumble client capability already holds the criteria for the external client and its draft download criteria, but the shared library is meant to replace that client's runtime code over time. The recommendation is to add them to the Rumble client capability now, because that capability is the one the library will eventually replace, and to decide on a separate capability only when the library carries enough behaviour to deserve one. The alternative is a new capability for the shared Rumble library from the start.

## How should the environment names be kept in one place?

The reset script in `scripts/rumble` and the new library would both need the four repository names. The recommendation is one small data file in the library's resources that the library reads and the script also reads by path, with a test that fails when either side lists something else. The alternative is to keep two lists and a test that compares them, which is simpler but leaves two places to edit.
