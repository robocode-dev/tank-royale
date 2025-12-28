## 1. Investigation & baseline

- [ ] 1.1 Identify all GUI panels that append ANSI formatted log content (e.g. `BotEventsPanel`, other console-like
  panels).
- [ ] 1.2 Create a reproducible workload:
    - [ ] a) real captured log(s) from a battle
    - [ ] b) synthetic stress log generator (high append rate + heavy ANSI sequences)
- [ ] 1.3 Measure baseline performance and resource usage:
    - [ ] a) UI responsiveness (EDT stalls, scroll latency)
    - [ ] b) throughput (lines/sec appended)
    - [ ] c) CPU usage while logging
    - [ ] d) heap growth / retained memory after clearing or closing panel
- [ ] 1.4 Profile hotspots and document findings in `design.md`.

## 2. Decide strategy

- [ ] 2.1 Pick primary culprit(s) (e.g. StyledDocument style churn, repaint frequency, ANSI parsing) based on evidence.
- [ ] 2.2 Choose mitigation strategy and document decision + alternatives.

## 3. Implementation (after approval)

- [ ] 3.1 Implement chosen mitigation(s) in GUI console components.
- [ ] 3.2 Ensure correct Swing threading (EDT) behavior.
- [ ] 3.3 Add UX affordances if needed (e.g. clear/pause, max lines/chars setting, export/copy all).

## 4. Regression protection

- [ ] 4.1 Add an automated test or small benchmark that appends large ANSI logs and asserts the operation completes
  within a reasonable time budget.
- [ ] 4.2 Verify memory is bounded (if retention limit is introduced).

## 5. Validation & docs

- [ ] 5.1 Update/confirm specs and ensure OpenSpec validation passes.
- [ ] 5.2 Document any new settings/limits in the GUI documentation (if applicable).

