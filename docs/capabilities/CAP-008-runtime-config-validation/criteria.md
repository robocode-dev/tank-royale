---
id: CAP-008-criteria
type: criteria
status: draft
links: [CAP-008]
title: Acceptance criteria for CAP-008 (runtime-config-validation)
ac-prefix: RCV
provenance: inferred
---

```gherkin
Feature: runtime-config-validation — TBD - created by change support-bot-discovery-without-json. Update Purpose after archive.

  # Requirement: Bot Configuration Runtime Validation
  # The Bot APIs (Java, Python, .NET) SHALL strictly validate that required properties are set before allowing a bot to connect to the server. Required properties include: `name`, `version`, and `authors`.

  @RCV-001
  Scenario: Missing required properties at connection time
    When a bot attempts to connect to the server
    And any of the required properties (`name`, `version`, `authors`) are missing or empty
    Then the Bot API SHALL throw a `BotException` (or platform-equivalent error)
    And the error message SHALL specify which properties are missing

  @RCV-002
  Scenario: Valid properties at connection time
    When a bot attempts to connect to the server
    And all required properties (`name`, `version`, `authors`) are provided via code, environment variables, or a `.json` file
    Then the Bot API SHALL proceed with the connection process
```
