# Fix workflow (Phases 8-10)

Read this file **only after** Phase 7 has produced at least one finding. Phases 8-10 fix, commit and open a pull request. Each one needs explicit user approval; the rules below apply to all three.

## GATED WORKFLOW - Mandatory Approval Checkpoints

Phases 8-10 form a strict state machine. Each gate is a mandatory stop point - the **default is to stop and ask**, never to proceed.

**Rules:**
- Identifying issues does **not** grant permission to fix them.
- Fixing does **not** grant permission to commit.
- Committing does **not** grant permission to push or open a PR.
- Silence, hints, context, or likely intent do **not** count as approval.
- Never skip ahead. Never combine phases. Never infer permission.

---

## Phase 8 - Fix

**GATE — Requires explicit user approval.**

After Phase 7 output, if there are no findings, stop - skip remaining phases.

Otherwise output this question as plain text - call no tools, write nothing else, and end your response:

> Would you like me to fix these findings?
> - Yes, fix them
> - No, just the report

**End your response here. Do not call any tools. Wait for the user's reply before continuing.**

- User declines → stop. Skip remaining phases.
- User approves → proceed.

### Step 1 - Create a fix branch

```
git checkout -b fix-<target-slug>
```

`<target-slug>` is a short kebab-case name derived from the audit target (e.g. `fix-data-fetcher`, `fix-auth-service`).

### Step 2 - Implement fixes

Fix every finding from `audit-output.json`, file by file:

- Apply the concrete fix from each finding's `fix` field.
- Do not change unrelated code.
- Run existing tests after all fixes to confirm nothing is broken.

After all fixes are applied, briefly summarise what was changed (one line per file). Then output:

> Fixes applied. Ready to commit - how would you like to proceed?

**End your response here. Do not call any tools. Do not proceed to Phase 9 automatically. Wait for the user's next message.**

---

## Phase 9 - Commit

**GATE — Requires explicit user approval. Only enter this phase after the user replies to the Phase 8 Step 2 prompt.**

Compose the commit message and PR body (see format below). Present both **in full inline** so the user can review before deciding.

Then output this question as plain text - call no tools, write nothing else, and end your response:

> How would you like to proceed?
> 0. **Re-run audit** - scan again to surface issues hidden by the findings just fixed *(shown only if the audit found at least one Medium or higher finding)*
> 1. **Commit only** - commit to the local branch
> 2. **Commit and push** - commit and push to origin
> 3. **Exit** - leave changes uncommitted

**End your response here. Do not call any tools. Wait for the user's reply before continuing.**

- User chooses **re-run audit** → jump back to Phase 5 (Pre-Scan) using the same target and already-resolved principles. Re-run Phases 5, 6, and 7 in full. Do not create a new branch; continue on the branch from Phase 8 Step 1. After Phase 7 output, re-enter Phase 8 gate. Track the pass number (pass 2, pass 3, …) and include it in the commit message when the user eventually commits.
- User chooses **exit** → stop. Skip Phase 10.
- User chooses **commit only** → run the commit commands below. Stop. Skip Phase 10.
- User chooses **commit and push** → run the commit commands below, then push. Proceed to Phase 10.

### Step 1 - Commit

```
git add -A
git commit -m "<commit message>"
```

### Step 2 - Push (only if user chose "commit and push")

```
git push -u origin fix-<target-slug>
```

---

## Phase 10 - Pull Request

**GATE — Requires explicit user approval.**

Output this question as plain text - call no tools, write nothing else, and end your response:

> Shall I open a pull request?
> - Yes, open PR
> - No, keep the branch

**End your response here. Do not call any tools. Wait for the user's reply before continuing.**

- User declines → stop.
- User approves → create a PR targeting the default branch using the PR body from Phase 9, then stop.

---

## Commit Message & PR Body Format

### Commit message

```
fix(<target>): resolve <N> audit findings (<severities>)

- [PRINCIPLE-ID] one-line description (file:line)
- ...
```

- Prepend any project-specific ticket prefix required by the repo's contributing guidelines (e.g. `PROJ-123: fix(...)`). Omit if no convention exists.
- `<severities>` summarises the breakdown, e.g. `HIGH×3, MEDIUM×2, LOW×1`.

### PR body

```markdown
## Summary

Brief description of what was audited and what was fixed.

---

## Why each change was required

### 🔴 CRITICAL - <finding title> (<PRINCIPLE-ID>)
One paragraph: root cause and production impact of leaving it unfixed.

### 🟠 HIGH - <finding title> (<PRINCIPLE-ID>)
...

### 🟡 MEDIUM - <finding title> (<PRINCIPLE-ID>)
...

### 🔵 LOW - <finding title> (<PRINCIPLE-ID>)
...

---

## Changes

| Severity | Finding | Change |
|----------|---------|--------|
| 🔴 CRITICAL | <what was wrong> | <what was done> |
| 🟠 HIGH  | <what was wrong> | <what was done> |
| 🟡 MEDIUM| ...              | ...             |
| 🔵 LOW   | ...              | ...             |

---

**Files changed:** N production + M test | **Tests:** X/X passing
```

Severity emoji: 🔴 CRITICAL · 🟠 HIGH · 🟡 MEDIUM · 🔵 LOW
