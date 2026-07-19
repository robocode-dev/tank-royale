---
version: 0.3.0
cliewen-skill: true
---

# clue-verify

Pre-merge checklist. Run before opening or updating any PR. When the `clue` CLI exists, `clue validate` performs the mechanical half; until then, check by hand. **Never fix a failure by weakening the check.**

- [ ] The change is in the right tier: light only if no decision was made (no ADR, no PDR, no decision-log row), no AC or capability meaning changed, no semantic plan mutation, and no methodology carrier (skills, AGENTS.md rules, lint rules) touched — then the PR description is the proposal and no `/changes/` folder exists. Anything else is a full change with a digested `/changes/CH-xxx-slug/` workspace.
- [ ] Every artifact touched has frontmatter: `id`, `type`, `status`, `links`, `title` (+ type extensions: decision records (ADRs and PDRs) `author`/`accepted-by`; constraints `source`/`enforcement`; capabilities `goal`).
- [ ] Every `links` entry resolves to an existing ID.
- [ ] The proposal references a real plan item, or is declared plan-less.
- [ ] Plan bookkeeping updated (milestone statuses reflect this merge).
- [ ] No completed (`status: completed`) plan was modified.
- [ ] Every AC has a test tag with a positive + negative pair — or the capability is honestly `status: draft` with the gap stated.
- [ ] Every `/docs/**` folder has README.md; index blocks list every sibling artifact and reference no deleted file.
- [ ] Change assessed against every constraint in `/docs/constraints/` and every quality scenario in `/docs/quality/`.
- [ ] Repo-local conventions declared in AGENTS.md are honored (e.g. a changelog entry for user-visible impact). AGENTS.md extends the methodology, never overrides it — an AGENTS.md rule conflicting with a skill was surfaced as an open question, not silently obeyed or ignored.
- [ ] Diagrams are inline Mermaid and readable when rendered.
- [ ] `/changes/CH-xxx-slug/` is deleted in the digest commit; after merge, `main` contains no `/changes/`.
- [ ] Decisions made during the change are recorded and correctly routed: expensive-to-reverse ones as ADRs (architecture) or PDRs (project/process) per C-011 (`author: agent` starts `inferred`; merging makes a decision binding but only an explicit human approval promotes it to `verified`, and an explicit objection keeps it `inferred` regardless), cheap-and-local-to-reverse ones as decision-log rows.
- [ ] Pending clerical signings are performed: every explicit approval given since the last digest is recorded — `status: verified` with each approver signed in `accepted-by:` (date = first approval, venue cited).
- [ ] The branch roots at the current tip of `main` (`git merge-base HEAD origin/main` after `git fetch` equals `origin/main`) — if a sibling change merged first, rebase onto `main` and re-run this checklist.
- [ ] The branch was not cut from another change's unmerged work, and no other CH's `/changes/` folder is visible on it.
- [ ] The author's previous change is merged — this is not a second change in flight; if work had to build on an unmerged change, the human's explicit go-ahead is recorded.
- [ ] The hosted PR is this change's only route into `main`: the agent opens (or updates) it as the review surface and will not merge it, create a local merge commit into `main`, or push to `main` — the merge is the human's act.
