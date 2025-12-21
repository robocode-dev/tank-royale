# Implementation Tasks

## 1. Update VERSIONS.MD

- [x] 1.1 Add entry in the latest version section documenting the new GUI installer packages
- [x] 1.2 Mention available formats: Windows (msi), macOS (pkg), Linux (rpm and deb)
- [x] 1.3 Include note about Java 11+ requirement

## 2. Update Release Documentation Template

- [x] 2.1 Update `buildSrc/src/main/resources/release/release-docs-template.md`
- [x] 2.2 Add new section for native installer packages before "Running Robocode" section
- [x] 2.3 Include table showing available installer formats
- [x] 2.4 Add Java 11+ requirement notice
- [x] 2.5 Add JAVA_HOME setup instructions with link to Baeldung article
- [x] 2.6 Include download links using the `{VERSION}` placeholder pattern

## 3. Update Installation Guide

- [x] 3.1 Update `docs-build/docs/articles/installation.md`
- [x] 3.2 Add new section "Installing with Native Installers" after Java section
- [x] 3.3 Add Windows (msi) installation instructions
- [x] 3.4 Add macOS (pkg) installation instructions
- [x] 3.5 Add Linux (rpm) installation instructions
- [x] 3.6 Add Linux (deb) installation instructions
- [x] 3.7 Include note that Java 11+ must be installed first
- [x] 3.8 Link to JAVA_HOME setup article
- [x] 3.9 Add note about installers being added to GitHub Releases

## 4. Validation

- [x] 4.1 Review all documentation changes for clarity and completeness (release template + installation guide)
- [x] 4.2 Verify all links are correct (checked target anchors/URLs used in these two files)
- [x] 4.3 Check that version placeholders are used consistently (used `{VERSION}` in both files)
- [x] 4.4 Ensure formatting is consistent with existing documentation style

---

Notes:

- The release docs template (`buildSrc/.../release-docs-template.md`) and the installation guide (
  `docs-build/.../installation.md`) were updated and aligned. Filenames and download links now use the `{VERSION}`
  placeholder and include platform-specific installer guidance and `JAVA_HOME` setup instructions.
- Remaining work: update `VERSIONS.MD` to document the new GUI installer packages (see section 1). Also consider
  gradually replacing remaining `x.y.z` occurrences across other docs (some tutorials and READMEs still reference
  `x.y.z`).
