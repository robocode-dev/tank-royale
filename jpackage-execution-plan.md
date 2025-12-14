## Plan: JDK jpackage-based Distribution for Tank Royale

This plan details how to introduce JDK's jpackage-based distribution packages for Windows, Linux, and macOS in the
tank-royale project. It covers preparing each component (UI, server, booter, recorder), creating jpackage
configurations, updating build scripts, automating packaging and release with GitHub Actions, integrating with existing
release scripts, uploading installers as artifacts, and signing/testing requirements.

### Steps

> **Note:** All code changes made as part of this plan must follow clean code principles, ensuring readability,
> maintainability, and clarity throughout the project.
> **Alert:** If any issue or decision point arises during execution, the user will be notified to make a decision before
> proceeding.
> Update the execution plan accordingly by putting a checkmark [X] next to the relevant step.

1. [X] **Evaluate and Transform `create-release` Task into GitHub Action**
   - [X] Analyze the current `create-release` task and its steps.
   - [X] Check if all steps can be mapped 1:1 to a GitHub Actions workflow (e.g., environment setup, build, versioning,
     changelog, artifact upload, release creation).
   - [X] If possible, implement a GitHub Actions workflow that mirrors the `create-release` task exactly.
   - [X] If not possible, plan to extend or adapt the existing `create-release` task to work with GitHub Actions, and
     update the plan accordingly.

2. [X] **Audit and Prepare Components for Packaging**
   - [X] Review `gui/`, `server/`, `booter/`, and `recorder/` for main class, dependencies, and resource inclusion.
   - [X] Ensure each has a clear entry point and all runtime dependencies are bundled or referenced.
   - [X] Update documentation in each component’s `README.md` to clarify packaging requirements.

3. [X] **Build and Test JAR Artifacts**
   - [X] Generate runnable JARs for each component (server, booter, recorder, and gui)
   - [X] Test each JAR by running (e.g., `java -jar server.jar`) and verify expected output by using the `--version`
     argument.

4. [X] **Create jpackage Configuration for Each Component**
   - [X] For each component, define jpackage parameters (main jar, main class, icon, app name, version, vendor, etc.).
   - [X] Add or update a `jpackage-config.json` or equivalent config file in each component directory.
   - [X] Prepare platform-specific assets (icons, license files, etc.) in each component.
      - [X] (user/developer) Prepare and convert the application icon for each platform:
         - [X] Windows: `.ico` format (recommended sizes: 16x16, 32x32, 48x48, 256x256)
            - Available icon source: `resources/icons/Tank.ico`
         - [X] macOS: `.icns` format (recommended sizes: 16x16, 32x32, 128x128, 256x256, 512x512)
            - Available icon source: `resources/icons/Tank.icns`
         - [X] Linux: `.png` format (recommended sizes: 32x32, 48x48, 128x128, 256x256)
            - Available icon source: `resources/icons/Tank.png`
      - [X] (user/developer) Convert your `tank.svg` to the required formats using ImageMagick or online tools.

5. [X] **Update Build Scripts for jpackage Integration**
   - [X] Modify each component’s `build.gradle.kts` to add jpackage tasks for Windows, Linux, and macOS.
   - [X] Ensure tasks produce native installers (e.g., `.exe`, `.msi`, `.deb`, `.rpm`, `.pkg`, `.dmg`).
   - [X] Add Gradle logic to copy or stage output installers in a common distribution directory.

6. [X] **Set Up GitHub Actions Workflow for Packaging and Release**
   - [X] Create or update a workflow YAML (e.g., `.github/workflows/package-release.yml`) to:
      - [X] Build all components for all platforms using matrix builds.
      - [X] Run jpackage tasks for each component and platform.
      - [X] Collect and upload resulting installers as workflow artifacts.
      - [X] Trigger workflow manually (workflow_dispatch) and/or via a "release" task.

7. [x] **Integrate with Existing Release Scripts**
    - [X] Review and update any release scripts (e.g., in `scripts/` or root) to invoke new jpackage Gradle tasks.
    - [X] Ensure versioning and changelog steps are compatible with new packaging process.

8. [X] **Signing Installers and Artifacts**
    - [X] Clarify signing requirements for Windows, Linux, and macOS installers.
    - [X] (user/developer) Decide if signing is required for each platform (Windows, macOS, Linux).
    - [X] (user/developer) Choose signing method/tool for each platform.
    - [X] (user/developer) Obtain necessary certificates/keys for signing (if required).
    - [X] (user/developer) Decide how to securely store and access certificates/keys for CI/CD.
    - [X] (user/developer) Configure signing in build scripts and GitHub Actions (if required).

9. [ ] **Upload Installers as Artifacts**
    - [ ] In the GitHub Actions workflow, use `actions/upload-artifact` to upload all generated installers.
    - [ ] Name artifacts clearly by component and platform for easy retrieval.

10. [ ] **Test Installers**
    - [ ] Download and install each package on its respective OS.
    - [ ] Launch the application and verify basic functionality (e.g., open GUI, start battle).

11. [ ] **(Optional) Publish Releases Automatically**
    - [ ] Extend the workflow to create GitHub Releases and attach installers as release assets.
    - [ ] Optionally, trigger this on tag creation or manual dispatch.

### Further Considerations

1. [X] Only trigger the release workflow manually (workflow_dispatch or release task).
2. [X] Clarify and implement signing requirements for all platforms.
3. [X] Test JAR artifacts before packaging installers.
4. [X] Test installers after packaging.
5. [X] (user/developer) Confirm application name, vendor, version, and other metadata for each package.
   - [X] Application Name: Set in /settings.gradle.kts (`rootProject.name`)
   - [X] Vendor: `robocode.dev`
   - [X] Version: Set in /gradle.properties (`version`)
   - [X] Description: "Robocode Tank Royale is a programming game where you code autonomous virtual tanks to battle in
     an arena"
   - [X] Copyright: "Copyright © 2022 Flemming N. Larsen"
   - [X] License: https://github.com/robocode-dev/tank-royale/blob/main/LICENSE
   - [X] Homepage/URL: E.g., https://robocode.dev
   - [X] Icon: Path to platform-specific icon file
   - [X] Installation Directory Name: (Optional): "robocode-tank-royale"
   - [ ] Contact Email: (Optional):
6. [X] (user/developer) Decide on distribution channels: GitHub Releases
7. [ ] (user/developer) Decide on scope of installer testing (automated vs. manual).
