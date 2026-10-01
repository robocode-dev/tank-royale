---
id: CAP-019-criteria
type: criteria
status: active
links: [CAP-019]
title: Acceptance criteria for CAP-019 (battle scoring)
ac-prefix: SCR
---

```gherkin
Feature: battle scoring — Classic-compatible team survival scoring

  @SCR-001
  Scenario: Award survival points once for each newly defeated opponent
    Test-type: Unit
    Given a battle has bots assigned to teams, with unteamed bots treated as individual teams
    When one or more bots are newly defeated
    Then each defeated bot SHALL award 50 survival points to every bot that remains alive on another team
    And a bot SHALL NOT earn survival points for a teammate's defeat
    And simultaneous defeats SHALL award points once for each newly defeated bot
    And reporting an already defeated bot again SHALL NOT award survival points again

  @SCR-002
  Scenario: Award last-survivor points to every member of the last team
    Test-type: Unit
    Given a battle has bots assigned to teams, with unteamed bots treated as individual teams
    When only one team has living bots after a defeat
    Then each living member of that team SHALL receive 10 last-survivor points for every bot on opposing teams
    And the last-survivor award SHALL be applied no more than once in the round
    And results for a team SHALL equal the sum of its members' individual scores
    And a draw SHALL award no last-survivor points
```
