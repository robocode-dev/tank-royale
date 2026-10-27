---
id: CH-061
type: change
status: open
links: [G-003, P-007, C-002]
title: Replace the Rumble readiness campaign with simpler participation and isolated development testing
---

# Replace the Rumble readiness campaign

This plan-less change adopts a replacement campaign; it does not implement or announce its product features. The maintainer requested GUI-led bot submission and contribution, multiple preferred bots with a strict option, local acceptance, a public Beta at the existing address, a separate resettable development Rumble, withdrawal, readable bot pages, official sample labels, and canonical documentation reviewed across the organization.

Preserve completed readiness evidence and retire the unfinished old walkthrough without claiming it passed. Record the target architecture, user journeys, milestone exits, compatibility boundaries, and the first outside-account authentication experiment. Existing operational contracts remain in force until their implementing changes replace them. No live bot, result, repository setting, or deployment changes in this change.

## Consequential uncertainty

The main risk is that narrow GitHub App authorization cannot support an ordinary outside account's automatic publication and results flow. The cheapest useful test is a small end-to-end experiment in an isolated development Rumble before building the full GUI. A hosted broker is a credible alternative, but it is not authorized by this campaign; if the no-service path needs broad credentials or maintainer intervention, stop that path and present the tradeoff. A polished GUI that still requires hidden Git or JSON repair would meet component tests but fail the intended user: retain an outside-user Windows walkthrough and measure interventions.

## Acceptance and publication

The maintainer explicitly selected local acceptance in this conversation. The accepted base already contains `mode: local` and `branch: main`; update the contradictory PR-only prose and record the process decision. This branch is local, is not pushed, and has no PR. Coordinated identity allocation continues using the repository's established allocator; identity reservations are not publication of the change. Present a verified exact candidate and local acceptance brief to the maintainer; do not integrate without exact human acceptance. No vision document exists; the campaign proceeds under the learning-and-competition purpose.

## Local acceptance metadata correction

The original proposal was committed before implementation in `d032a779f`. This reintroduction preserves its intent and corrects its header to the canonical `id: CH-061`, `type: change` shape required by the released local acceptance command. It is an additive history repair, not a claim that a new proposal preceded the already committed implementation. The final digest removes the transient workspace again.
