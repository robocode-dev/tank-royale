## Plan: JDK jpackage-based Distribution for Tank Royale

This plan details how to introduce JDK's jpackage-based distribution packages for Windows, Linux, and macOS in the
tank-royale project. It covers preparing each component (UI, server, booter, recorder), creating jpackage
configurations, updating build scripts, automating packaging and release with GitHub Actions, integrating with existing
release scripts, uploading installers as artifacts, and signing/testing requirements.

### Steps

1. [ ] **Audit and Prepare Components for Packaging**
    - [ ] Review `gui/`, `server/`, `booter/`, and `recorder/` for main class, dependencies, and resource inclusion.
    - [ ] Ensure each has a clear entry point and all runtime dependencies are bundled or referenced.
    - [ ] Update documentation in each component’s `README.md` to clarify packaging requirements.

2. [ ] **Build and Test JAR Artifacts**
    - [ ] Generate runnable JARs for each component.
    - [ ] Test each JAR by running (e.g., `java -jar server.jar`) and verify expected output.

3. [ ] **Create jpackage Configuration for Each Component**
    - [ ] For each component, define jpackage parameters (main jar, main class, icon, app name, version, vendor, etc.).
    - [ ] Add or update a `jpackage-config.json` or equivalent config file in each component directory.
    - [ ] Prepare platform-specific assets (icons, license files, etc.) in each component.
       - [ ] (Developer) Prepare and convert the application icon for each platform:
          - [ ] Windows: `.ico` format (recommended sizes: 16x16, 32x32, 48x48, 256x256)
          - [ ] macOS: `.icns` format (recommended sizes: 16x16, 32x32, 128x128, 256x256, 512x512)
          - [ ] Linux: `.png` format (recommended sizes: 32x32, 48x48, 128x128, 256x256)
       - [ ] (Developer) Convert your `tank.svg` to the required formats using ImageMagick or online tools.

4. [ ] **Update Build Scripts for jpackage Integration**
    - [ ] Modify each component’s `build.gradle.kts` to add jpackage tasks for Windows, Linux, and macOS.
    - [ ] Ensure tasks produce native installers (e.g., `.exe`, `.msi`, `.deb`, `.rpm`, `.pkg`, `.dmg`).
    - [ ] Add Gradle logic to copy or stage output installers in a common distribution directory.

5. [ ] **Set Up GitHub Actions Workflow for Packaging and Release**
    - [ ] Create or update a workflow YAML (e.g., `.github/workflows/package-release.yml`) to:
        - [ ] Build all components for all platforms using matrix builds.
        - [ ] Run jpackage tasks for each component and platform.
        - [ ] Collect and upload resulting installers as workflow artifacts.
        - [ ] Trigger workflow manually (workflow_dispatch) and/or via a "release" task.

6. [ ] **Integrate with Existing Release Scripts**
    - [ ] Review and update any release scripts (e.g., in `scripts/` or root) to invoke new jpackage Gradle tasks.
    - [ ] Ensure versioning and changelog steps are compatible with new packaging process.

7. [ ] **Signing Installers and Artifacts**
    - [ ] Clarify signing requirements for Windows, Linux, and macOS installers.
   - [ ] [ ] (Developer) Decide if signing is required for each platform (Windows, macOS, Linux).
   - [ ] [ ] (Developer) Choose signing method/tool for each platform.
    - [ ] [ ] (Developer) Obtain necessary certificates/keys for signing (if required).
   - [ ] [ ] (Developer) Decide how to securely store and access certificates/keys for CI/CD.
    - [ ] [ ] (Developer) Configure signing in build scripts and GitHub Actions (if required).

8. [ ] **Upload Installers as Artifacts**
    - [ ] In the GitHub Actions workflow, use `actions/upload-artifact` to upload all generated installers.
    - [ ] Name artifacts clearly by component and platform for easy retrieval.

9. [ ] **Test Installers**
    - [ ] Download and install each package on its respective OS.
    - [ ] Launch the application and verify basic functionality (e.g., open GUI, start battle).

10. [ ] **(Optional) Publish Releases Automatically**
    - [ ] Extend the workflow to create GitHub Releases and attach installers as release assets.
    - [ ] Optionally, trigger this on tag creation or manual dispatch.

### Further Considerations

1. [X] Only trigger the release workflow manually (workflow_dispatch or release task).
2. [X] Clarify and implement signing requirements for all platforms.
3. [X] Test JAR artifacts before packaging installers.
4. [X] Test installers after packaging.
5. [ ] (Developer) Confirm application name, vendor, version, and other metadata for each package.
   - [X] Application Name: Set in /settings.gradle.kts (`rootProject.name`)
   - [X] Vendor: `robocode.dev`
   - [X] Version: Set in /gradle.properties (`version`)
   - [X] Description: "Robocode Tank Royale is a programming game where you code autonomous virtual tanks to battle in
     an arena"
   - [X] Copyright: "Copyright © 2022 Flemming N. Larsen"
   - [X] License: https://github.com/robocode-dev/tank-royale/blob/main/LICENSE
   - [X] Homepage/URL: E.g., https://robocode.dev
   - [ ] Main Class: Entry point for each component (e.g., `org.robocode.tankroyale.gui.Main`)
   - [ ] Icon: Path to platform-specific icon file
   - [X] Installation Directory Name: (Optional): "robocode-tank-royale"
   - [ ] Contact Email: (Optional):
6. [X] (Developer) Decide on distribution channels: GitHub Releases
7. [ ] (Developer) Decide on scope of installer testing (automated vs. manual).
