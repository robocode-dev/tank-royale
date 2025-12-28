## 1. Investigation & baseline

- [x] 1.1 Identify all GUI panels that append ANSI formatted log content (e.g. `BotEventsPanel`, other console-like
  panels).
- [x] 1.2 Create a reproducible workload:
    - [x] a) real captured log(s) from a battle
    - [x] b) synthetic stress log generator (high append rate + heavy ANSI sequences)
- [x] 1.3 Measure baseline performance and resource usage:
    - [x] a) UI responsiveness (EDT stalls, scroll latency)
    - [x] b) throughput (lines/sec appended)
    - [x] c) CPU usage while logging
    - [x] d) heap growth / retained memory after clearing or closing panel
- [x] 1.4 Profile hotspots and document findings in `design.md`.

## 2. Decide strategy

- [x] 2.1 Pick primary culprit(s) (e.g. StyledDocument style churn, repaint frequency, ANSI parsing) based on evidence.
- [x] 2.2 Choose mitigation strategy and document decision + alternatives.

## 3. Implementation (after approval)

- [x] 3.1 Implement chosen mitigation(s) in GUI console components.
- [x] 3.2 Ensure correct Swing threading (EDT) behavior.
- [x] 3.3 Add UX affordances if needed (e.g. clear/pause, max lines/chars setting, export/copy all).

## 4. Regression protection

- [x] 4.1 Add an automated test or small benchmark that appends large ANSI logs and asserts the operation completes
  within a reasonable time budget.
- [x] 4.2 Verify memory is bounded (if retention limit is introduced).

## 5. Validation & docs

- [x] 5.1 Update/confirm specs and ensure OpenSpec validation passes.
- [x] 5.2 Document any new settings/limits in the GUI documentation (if applicable).
- [x] 5.3 Verify full project build passes with `./gradlew clean build`.
- [x] 5.4 Fix `JsonDecodingException` in `BootProcess.info` by handling non-JSON output and empty strings.

