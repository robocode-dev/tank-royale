## 1. Baseline (ProGuard)

- [ ] 1.1 Build `:booter:assemble`, `:server:assemble`, `:recorder:assemble`, `:gui:assemble` and capture final JAR byte
  sizes
- [ ] 1.2 Add automated smoke tests for CLI archives that run:
    - [ ] `java -jar <booter-jar> --version`
    - [ ] `java -jar <server-jar> --version`
    - [ ] `java -jar <recorder-jar> --version`
  - [ ] `java -jar <gui-jar> --version`
- [ ] 1.3 Ensure baseline smoke tests pass with current ProGuard shrinking

## 2. Replace ProGuard with R8 (build tooling)

- [ ] 2.1 Identify all Gradle wiring that depends on the shrink task name (e.g. `gui` copy tasks, root jpackage
  convention)
- [ ] 2.2 Replace `com.guardsquare:proguard-gradle` usage with an R8-based shrink task for the following modules:
    - [ ] `booter`
    - [ ] `server`
    - [ ] `recorder`
    - [ ] `gui`
- [ ] 2.3 Ensure the publishing artifact remains the shrunk JAR (not the "-all" fat jar)
- [ ] 2.4 Remove `proguard-common.pro` and migrate required rules into new module-specific R8 rule files

## 3. Verification (post-migration)

- [ ] 3.1 Re-run the smoke tests against the R8-shrunk artifacts
- [ ] 3.2 Run relevant unit tests (at minimum `:server:test` and `:gui:test`)
- [ ] 3.3 Verify Bot APIs are still functional:
    - [ ] Build `bot-api:java` artifacts
    - [ ] Ensure sample bots can compile (or run a minimal compilation check) against the Bot API

## 4. Measurements & Reporting

- [ ] 4.1 Capture post-migration archive sizes for R8 output artifacts
- [ ] 4.2 Produce a comparison table (ProGuard vs R8) including absolute and percentage deltas

## 5. OpenSpec hygiene

- [ ] 5.1 Add a delta spec that defines the new build quality gate for distributable archives
- [ ] 5.2 Run OpenSpec validation for the change in strict mode

