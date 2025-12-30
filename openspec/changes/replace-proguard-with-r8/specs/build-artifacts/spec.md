## ADDED Requirements

### Requirement: Distributable CLI archives MUST pass a runtime smoke check

The build system SHALL provide an automated runtime smoke check for distributable CLI application archives.

#### Scenario: Booter archive `--version`

- **WHEN** the build produces the distributable `booter` application JAR
- **THEN** executing `java -jar <booter-jar> --version` MUST succeed (exit code 0)
- **AND** it MUST print a version string

#### Scenario: Server archive `--version`

- **WHEN** the build produces the distributable `server` application JAR
- **THEN** executing `java -jar <server-jar> --version` MUST succeed (exit code 0)
- **AND** it MUST print a version string

#### Scenario: Recorder archive `--version`

- **WHEN** the build produces the distributable `recorder` application JAR
- **THEN** executing `java -jar <recorder-jar> --version` MUST succeed (exit code 0)
- **AND** it MUST print a version string

### Requirement: Shrinking rules MUST be maintainable and module-scoped

The build system SHALL avoid shared shrinker rule files when this makes the rule set harder to understand.

#### Scenario: Investigating a shrinker keep rule

- **WHEN** a developer needs to understand why a class must be kept from shrinking
- **THEN** the rule SHOULD be located in the module that consumes it
- **AND** the rule SHOULD be scoped no broader than necessary to preserve runtime behavior

