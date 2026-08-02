---
id: CAP-002-criteria
type: criteria
status: draft
links: [CAP-002]
title: Acceptance criteria for CAP-002 (booter-fallback-discovery)
ac-prefix: BFD
provenance: inferred
reversal-cost: low
---

```gherkin
Feature: booter-fallback-discovery — TBD - created by change support-bot-discovery-without-json. Update Purpose after archive.

  # Requirement: Booter Fallback Bot Discovery
  # The `booter` SHALL discover bot directories that do not contain a `.json` configuration file matching the directory name.

  @BFD-001
  Scenario: Bot directory without .json file
    When the `booter` scans bot root paths for directories
    And a directory is found that does not contain a `.json` file with the same name as the directory
    Then the `booter` SHALL consider this directory as a potential bot directory

  # Requirement: Booter Fallback Metadata Generation
  # The `booter` SHALL generate fallback metadata for bot directories that lack a `.json` file, allowing them to be listed and booted.

  @BFD-002
  Scenario: Metadata generation for bot without .json
    When the `booter` generates a `BootEntry` for a directory without a `.json` file
    Then it SHALL set the `name` property to the directory name
    And it SHALL set the `base` property to the directory name
    And it SHALL provide empty lists for `authors`, `gameTypes`, and other optional fields

  # Requirement: Bot Platform Detection
  # The `booter` SHALL attempt to detect the bot's platform (JVM, .NET, Python) based on the presence of common files in the bot's directory if the platform is not explicitly specified.

  @BFD-003
  Scenario: JVM platform detection
    When a bot directory contains a `.jar` file or `.class` files
    And no `.json` file specifies the platform
    Then the `booter` SHALL use the `jvm` boot template

  @BFD-004
  Scenario: Python platform detection
    When a bot directory contains a `.py` file
    And no `.json` file specifies the platform
    Then the `booter` SHALL use the `python` boot template

  @BFD-005
  Scenario: .NET platform detection
    Test-type: Unit
    When a bot directory contains a `.dll` or `.exe` file (not a JVM executable)
    And no `.json` file specifies the platform
    Then the `booter` SHALL use the `dotnet` boot template

  # Requirement: Bot License Metadata
  # The general booter configuration SHALL carry an optional SPDX license identifier without affecting booting.

  @BFD-006
  Scenario: Preserve optional SPDX license metadata
    Test-type: Unit
    When a bot configuration contains a valid SPDX `license` identifier
    Then the booter SHALL retain it in the parsed entry and directory listing
    And when the field is omitted, the booter SHALL still parse and boot the entry with no license value

  @BFD-007 @draft
  Scenario: Distributed sample bots declare their SPDX license
    Test-type: Integration
    When a maintainer inspects every maintained sample-bot configuration
    Then each configuration SHALL declare an SPDX `license` identifier
    And every maintained bot-configuration example and guide SHALL show the field
```
