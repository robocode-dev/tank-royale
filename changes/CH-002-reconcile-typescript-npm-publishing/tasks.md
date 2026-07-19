---
id: CH-002-tasks
type: change
status: open
links: [CH-002]
title: Task breakdown for CH-002
---

# Tasks — CH-002

- [x] Reconcile CAP-013 `criteria.md` to the shipped implementation
  - [x] Retire `TNP-001` (npmPublishDryRun) — `@TNP-001 @retired` tombstone, task dropped, no remint
  - [x] Retire `TNP-002` and `TNP-003` (NPM_TOKEN) — `@retired` tombstones
  - [x] Mint `TNP-005` (npmPublish requires `npmjs-api-key` Gradle property) — replaces TNP-002
  - [x] Mint `TNP-006` (npmPublish succeeds with valid `npmjs-api-key`, temp `.npmrc` deleted) — replaces TNP-003
  - [x] Update the requirement prose comment: drop the dry-run bullet, change `NPM_TOKEN` to the `npmjs-api-key` Gradle property
  - [x] Leave `TNP-004` (`.npmrc` git-ignored) unchanged
- [x] Reconcile CAP-013 `design.md` — drop dry-run bullet, correct credential mechanism, note the capability shipped
- [x] Reconcile CAP-013 `README.md` — note the capability is implemented and published (brief)
- [x] Digest: update `docs/plans/P-002-typescript-bot-api-npm.md`
  - [x] M-004 `status` `todo` → `done`; rewrite exit criterion to shipped reality; fix `CAP-014` → `CAP-013`; drop the "ACs tested" clause (belongs to M-002)
  - [x] M-004 evidence: commits `b1654f620` / `287ff01ac` / `51c30e21b` and live `@robocode.dev/tank-royale-bot-api@1.0.2`
  - [x] Plan `status: active` → `completed`
- [x] Add decision-log row (`docs/decisions/log.md`) recording the drop of `npmPublishDryRun` and the `npmjs-api-key`-over-`NPM_TOKEN` reconciliation
- [x] Verify: `clue validate` green; no `CAP-014` / non-tombstone `npmPublishDryRun`/`NPM_TOKEN` hits
- [ ] Delete `/changes/CH-002-.../` at digest and open PR (do not merge) — awaiting maintainer go-ahead to commit
