# Change: Document GUI Installers for Windows, macOS, and Linux

## Why

The project now has automated GitHub Actions workflow (`.github/workflows/package-release.yml`) that creates native
installer packages for the GUI application using jpackage for Windows (msi), macOS (pkg), and Linux (rpm and deb
packages). These installers are automatically uploaded to GitHub Releases when the `create-release` Gradle task is
called.

However, end-users currently lack documentation about:

- The availability of these native installer packages
- Java 11+ requirement for running the GUI
- How to set up the JAVA_HOME environment variable
- How to install these packages on different operating systems

This gap in documentation makes it difficult for end-users to discover and use these convenient installation methods,
potentially leading to confusion and support issues.

## What Changes

- Update `VERSION.md` to document the availability of native GUI installer packages
- Update `buildSrc/src/main/resources/release/release-docs-template.md` to include:
    - Information about native installer packages for Windows, macOS, and Linux
    - Java 11+ requirement and JAVA_HOME setup instructions
    - Link to Baeldung article: https://www.baeldung.com/java-home-on-windows-mac-os-x-linux
- Update `docs-build/docs/articles/installation.md` to include:
    - Section on installing GUI using native installer packages
    - Platform-specific installation instructions for Windows (msi), macOS (pkg), and Linux (rpm/deb)
    - Java 11+ runtime requirement for installers

## Impact

- **Affected documentation files**:
    - `VERSION.md` - Add note about new installer packages in the latest version section
    - `buildSrc/src/main/resources/release/release-docs-template.md` - Add installer section before "Running Robocode"
    - `docs-build/docs/articles/installation.md` - Add new section for native installers

- **Affected systems**: None - this is purely documentation changes

- **User impact**: Positive - users will have clear guidance on using native installers and setting up Java correctly

- **Breaking changes**: None

