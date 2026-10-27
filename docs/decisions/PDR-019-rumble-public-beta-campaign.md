---
id: PDR-019
type: decision
status: inferred
links: [G-004, P-007]
title: Invite public Beta participation while replacing the difficult onboarding journey
author: agent
accepted-by: []
supersedes: [PDR-015]
---

# Invite public Beta participation while replacing the difficult onboarding journey

## Context

The earlier readiness campaign demonstrated working pieces but left a demanding end-to-end walkthrough. The maintainer wants people to try the existing competition as a public Beta and wants a simpler experience with an isolated development test environment.

## Decision

The [replacement participation campaign](../plans/P-007-rumble-participation.md) supersedes the unfinished readiness campaign. Keep the existing repositories and rankings URL for the public Beta, preserve public history, and invite feedback using accurate descriptions of currently available behavior. Do not hold the Beta designation behind completion of the future GUI. Public messaging is delivered by the early environment milestone; broader announcement activity remains a maintainer decision.

Create a separate resettable development Rumble for routine integration experiments. Accept the replacement plan locally before implementation, and review GUI mockups before the full GUI. Prior completed evidence remains useful; the uncompleted old walkthrough is retired, not passed. The later Windows walkthrough verifies the replacement journeys and cannot be satisfied with prepared-account evidence alone.

The campaign serves the current simple-participation goal, not the old goal's pre-announcement ordering. Explicit human endorsement of this decision, the local-acceptance decision and the target architecture is required by the plan's acceptance gate; accepting a commit alone does not promote inferred provenance.

## Consequences

The previous blanket pre-announcement sequence no longer governs the public Beta invitation. Beta is not permission to purge public facts or snapshots. No deployment, cleanup, public invitation, or production reset is performed by accepting the plan itself.
