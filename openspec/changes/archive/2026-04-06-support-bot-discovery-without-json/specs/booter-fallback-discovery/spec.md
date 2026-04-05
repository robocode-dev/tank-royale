## ADDED Requirements

### Requirement: Booter Fallback Bot Discovery
The `booter` SHALL discover bot directories that do not contain a `.json` configuration file matching the directory name.

#### Scenario: Bot directory without .json file
- **WHEN** the `booter` scans bot root paths for directories
- **AND** a directory is found that does not contain a `.json` file with the same name as the directory
- **THEN** the `booter` SHALL consider this directory as a potential bot directory

### Requirement: Booter Fallback Metadata Generation
The `booter` SHALL generate fallback metadata for bot directories that lack a `.json` file, allowing them to be listed and booted.

#### Scenario: Metadata generation for bot without .json
- **WHEN** the `booter` generates a `BootEntry` for a directory without a `.json` file
- **THEN** it SHALL set the `name` property to the directory name
- **AND** it SHALL set the `base` property to the directory name
- **AND** it SHALL provide empty lists for `authors`, `gameTypes`, and other optional fields

### Requirement: Bot Platform Detection
The `booter` SHALL attempt to detect the bot's platform (JVM, .NET, Python) based on the presence of common files in the bot's directory if the platform is not explicitly specified.

#### Scenario: JVM platform detection
- **WHEN** a bot directory contains a `.jar` file or `.class` files
- **AND** no `.json` file specifies the platform
- **THEN** the `booter` SHALL use the `jvm` boot template

#### Scenario: Python platform detection
- **WHEN** a bot directory contains a `.py` file
- **AND** no `.json` file specifies the platform
- **THEN** the `booter` SHALL use the `python` boot template

#### Scenario: .NET platform detection
- **WHEN** a bot directory contains a `.dll` or `.exe` file (not a JVM executable)
- **AND** no `.json` file specifies the platform
- **THEN** the `booter` SHALL use the `dotnet` boot template
