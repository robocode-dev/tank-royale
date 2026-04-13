# Slash Commands

<!-- KEYWORDS: dot-prime, dot-scout, dot-audit, audit, scout, prime, principles, review, quality -->

## Command: /dot-scout [path]

Analyse a project to detect which principles apply and create or update `.principles` files encoding that analysis. Use when you need to map principles to a codebase.

### Workflow
1. **Resolve Target**: Determine target directory from arguments (default: current directory).
2. **Detect Profile**: Analyse directory for code artifact signals (Java, TypeScript, etc.), domain signals (Financial, Auth, etc.), and non-code signals (docs, infra, etc.).
3. **Propose .principles Placements**:
   - Git root `.principles` for project-wide groups.
   - Subdirectory `.principles` for subtree-specific groups or exclusions.
4. **Write Files**: Create/update `.principles` files with detected groups (e.g., `@java`, `@docs`, `@infra`).
5. **Emit Integration Files**:
   - Generate `REVIEW.md` for AI code review if Claude is detected.
   - Generate `.github/instructions/*.instructions.md` for Copilot if detected.

---

## Command: /dot-prime [target]

Activate principles before working on a file or task. Use before writing or editing code to load relevant principles into context.

### Workflow
1. **Detect Spec**: Check for explicit principle specs (e.g., `@group` or bare IDs like `CODE-CS-DRY`) in arguments.
2. **Resolve Active Set**:
   - **Explicit mode**: Expand `@group` from `.principles-catalog/groups/` and add bare IDs.
   - **Normal mode**: Load scout-generated principles from `.claude/rules/` or `.github/instructions/`.
3. **Select Top Principles**: Select 5–10 most relevant principles based on task and artifact type.
4. **Output Active Rules**: Display rules as a compact block for the AI to follow.

---

## Command: /dot-audit [target]

Review a file, directory, or inline code against its activated principles. Use to check code or docs against quality principles.

### Workflow
1. **Resolve Input**: Determine target (file, directory, or inline code) and detect artifact type.
2. **Resolve Principles**: Load principles from `.principles` hierarchy or scout-generated files.
3. **Pre-Scan**: Run deterministic checks (grep/regex) from `.context-inspect.md` to find potential violations.
4. **Guided Review**: Manually confirm/dismiss pre-scan hits by reading the code.
5. **Semantic-Only Review**: Perform deep reasoning review for principles without inspection patterns.
6. **Output Report**: Generate `audit-output.json` and a text report grouped by severity (Critical, High, Medium, Low).

### Gated Workflow (Requires Approval)
- **Fix**: Ask before applying concrete fixes for findings.
- **Commit**: Ask before committing fixes to a new branch.
- **Pull Request**: Ask before opening a PR for the fixes.
