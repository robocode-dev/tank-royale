# User Documentation - Spec Delta

## ADDED Requirements

### Requirement: GUI Installer Documentation

The user documentation SHALL provide comprehensive instructions for installing the Robocode Tank Royale GUI using native
installer packages on Windows, macOS, and Linux platforms.

#### Scenario: User wants to install GUI on Windows

- **WHEN** a user reads the installation documentation
- **THEN** they SHALL find clear instructions for installing the Windows MSI package
- **AND** they SHALL be informed that Java 11+ must be installed and available via JAVA_HOME
- **AND** they SHALL be provided with a link to JAVA_HOME setup instructions

#### Scenario: User wants to install GUI on macOS

- **WHEN** a user reads the installation documentation
- **THEN** they SHALL find clear instructions for installing the macOS PKG package
- **AND** they SHALL be informed that Java 11+ must be installed and available via JAVA_HOME

#### Scenario: User wants to install GUI on Linux with RPM

- **WHEN** a user reads the installation documentation
- **THEN** they SHALL find clear instructions for installing the Linux RPM package
- **AND** they SHALL be informed that Java 11+ must be installed and available via JAVA_HOME

#### Scenario: User wants to install GUI on Linux with DEB

- **WHEN** a user reads the installation documentation
- **THEN** they SHALL find clear instructions for installing the Linux DEB package
- **AND** they SHALL be informed that Java 11+ must be installed and available via JAVA_HOME

### Requirement: Release Documentation Updates

The release documentation template SHALL include information about available native installer packages for each GitHub
release.

#### Scenario: User views a GitHub release

- **WHEN** a user views the release notes on GitHub
- **THEN** they SHALL see a table of available installer formats (Windows MSI, macOS PKG, Linux RPM, Linux DEB)
- **AND** they SHALL see download links for each installer format
- **AND** they SHALL be informed about the Java 11+ requirement
- **AND** they SHALL be provided with a link to JAVA_HOME setup instructions

### Requirement: Version History Documentation

The version history documentation SHALL document the addition of native installer packages in the release where they
become available.

#### Scenario: User checks version history

- **WHEN** a user reads the VERSION.md file
- **THEN** they SHALL find an entry documenting the availability of native GUI installer packages
- **AND** they SHALL see which platforms are supported (Windows, macOS, Linux)
- **AND** they SHALL see which package formats are available (msi, pkg, rpm, deb)

## MODIFIED Requirements

None

## REMOVED Requirements

None

