---
id: CAP-012-criteria
type: criteria
status: draft
links: [CAP-012]
title: Acceptance criteria for CAP-012 (user-documentation)
ac-prefix: UD
provenance: inferred
---

```gherkin
Feature: user-documentation — TBD - created by archiving change document-gui-installers. Update Purpose after archive.

  # Requirement: GUI Installer Documentation
  # The user documentation SHALL provide comprehensive instructions for installing the Robocode Tank Royale GUI using native
  # installer packages on Windows, macOS, and Linux platforms.

  @UD-001
  Scenario: User wants to install GUI on Windows
    When a user reads the installation documentation
    Then they SHALL find clear instructions for installing the Windows MSI package
    And they SHALL be informed that Java 11+ must be installed and available via JAVA_HOME
    And they SHALL be provided with a link to JAVA_HOME setup instructions

  @UD-002
  Scenario: User wants to install GUI on macOS
    When a user reads the installation documentation
    Then they SHALL find clear instructions for installing the macOS PKG package
    And they SHALL be informed that Java 11+ must be installed and available via JAVA_HOME

  @UD-003
  Scenario: User wants to install GUI on Linux with RPM
    When a user reads the installation documentation
    Then they SHALL find clear instructions for installing the Linux RPM package
    And they SHALL be informed that Java 11+ must be installed and available via JAVA_HOME

  @UD-004
  Scenario: User wants to install GUI on Linux with DEB
    When a user reads the installation documentation
    Then they SHALL find clear instructions for installing the Linux DEB package
    And they SHALL be informed that Java 11+ must be installed and available via JAVA_HOME

  # Requirement: Release Documentation Updates
  # The release documentation template SHALL include information about available native installer packages for each GitHub
  # release.

  @UD-005
  Scenario: User views a GitHub release
    When a user views the release notes on GitHub
    Then they SHALL see a table of available installer formats (Windows MSI, macOS PKG, Linux RPM, Linux DEB)
    And they SHALL see download links for each installer format
    And they SHALL be informed about the Java 11+ requirement
    And they SHALL be provided with a link to JAVA_HOME setup instructions

  # Requirement: Version History Documentation
  # The version history documentation SHALL document the addition of native installer packages in the release where they
  # become available.

  @UD-006
  Scenario: User checks version history
    When a user reads the CHANGELOG.md file
    Then they SHALL find an entry documenting the availability of native GUI installer packages
    And they SHALL see which platforms are supported (Windows, macOS, Linux)
    And they SHALL see which package formats are available (msi, pkg, rpm, deb)

  # Requirement: Bot Configuration Documentation
  # The user documentation SHALL explicitly mention that `.json` configuration files are optional for bot development and how to configure bots using code or environment variables.

  @UD-007
  Scenario: User reads bot development guide
    When a user reads the bot development guide
    Then they SHALL find a section explaining how to configure a bot through code (setting name, version, and authors)
    And they SHALL be informed that a `.json` file is only a fallback mechanism
    And they SHALL find a list of required properties and the consequences of missing them (runtime exception)
```
