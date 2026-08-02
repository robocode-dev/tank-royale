---
id: CH-009-tasks
type: tasks
status: open
links: [CH-009]
title: Task breakdown for CH-009
---

# CH-009 — Tasks

- [ ] Add the inferred behavior-version decision record and new acceptance criteria (`PRO-005`, `BFD-006`, `BR-047`, and `BR-048`) before changing behavior
- [ ] Add the server-owned initial `behaviorVersion` constant and advertise it in server handshakes; update the wire schema and JVM message model (`PRO-005`)
- [ ] Add optional SPDX `license` metadata to booter entries, directory listings, sample bot configs, and documentation (`BFD-006`)
- [ ] Add `twinduel` to common game types, defaults, server support, Battle Runner factories, and preset tests (`BR-047`)
- [ ] Add the fixed-input deterministic server regression hook and its positive/negative evidence (`BR-048`)
- [ ] Verify the existing Battle Runner `BattleResults` path and document the client-facing result seam (`BR-011`, `BR-014`)
- [ ] Run focused tests, `./gradlew clean build`, `clue validate`, and `clue-verify`; mark each task as it completes
