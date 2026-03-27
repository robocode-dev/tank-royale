---
description: Activate principles before working on a file or task. Use when the user runs /prime [target] to load the relevant principles into the active frame before writing or editing.
argument-hint: "[file|directory|description]"
allowed-tools: Read, Glob, Grep, Bash
version: 0.7.0
authors: Flemming N. Larsen (https://github.com/flemming-n-larsen)
---

# Prime

You are activating principles before working on a file or task. Follow these five phases exactly.

## Phase 1 — Scan Context and Detect Artifact Type

If `$ARGUMENTS` is provided, use it as the context (file path, directory, or description). If `$ARGUMENTS` is empty, scan the current working directory — look at open files, recent edits, and the project structure.

### Artifact type detection

Detect the artifact type of the target file(s) by reading `.principles-catalog/layers/artifact-types.yaml` and matching against its type definitions.

Record the detected type: **`code`** | **`docs`** | **`config`** | **`infra`** | **`schema`** | **`pipeline`**

### Additional context (code and infra types)

For **code** targets, also identify:
- **Language**: Which programming language(s) are in use?
- **Framework**: Which frameworks or libraries are present?
- **Domain**: What problem domain does this code serve?
- **Risk signals** present: authentication, payments, PII, public API, concurrency, high-throughput

Record the **target path** for use in Phase 2.

## Phase 2 — Resolve .principles Hierarchy

### Fast Path — Compiled Block

Before walking `.principles` files, check for a pre-compiled block injected by `/scout`. Search in this order — stop at the **first file that exists**:

1. `.claude/rules/principles.md`
2. `.ai/principles.md`
3. `AGENTS.md`
4. `.github/copilot-instructions.md`

If the file contains `<!-- .principles: begin`, the compiled block is present. When found:

1. Parse all principle IDs from the block — lines matching `- <ID>: ...` where the ID is uppercase letters and hyphens
2. Optionally cross-reference `.principles-catalog/index.tsv` (each line: `ID|LAYER|SUMMARY`) to get Layer groupings for display in the prime header (e.g. "Layer 1: N always-active, Layer 2: M stack-specific").
3. Use these as the **active principle set** — skip the tree walk below
4. Record source as: `compiled-block: <filename>`

**Load principle content from the catalog:**

Determine the namespace for each active ID using this mapping:

| ID Prefix | Directory |
|-----------|-----------|
| `CODE-API-*` | `code/api/` |
| `CODE-AR-*`  | `code/ar/`  |
| `CODE-CC-*`  | `code/cc/`  |
| `CODE-CS-*`  | `code/cs/`  |
| `CODE-DX-*`  | `code/dx/`  |
| `CODE-OB-*`  | `code/ob/`  |
| `CODE-PF-*`  | `code/pf/`  |
| `CODE-RL-*`  | `code/rl/`  |
| `CODE-SEC-*` | `code/sec/` |
| `CODE-TP-*`  | `code/tp/`  |
| `CODE-TS-*`  | `code/ts/`  |
| `CODE-*` | `code/` |
| `DDD-*` | `ddd/` |
| `SOLID-*` | `solid/` |
| `GOF-*` | `gof/` |
| `GRASP-*` | `grasp/` |
| `CLEAN-ARCH-*` | `clean-arch/` |
| `SIMPLE-DESIGN-*` | `simple-design/` |
| `EFFECTIVE-JAVA-*` | `effective-java/` |
| `CODE-SMELLS-*` | `code-smells/` |
| `OWASP-*` | `owasp/` |
| `EIP-*` | `eip/` |
| `FP-*` | `fp/` |
| `12FACTOR-*` | `12factor/` |
| `A11Y-*` | `a11y/` |
| `PIPELINE-*` | `pipeline/` |
| `INFRA-*` | `infra/` |
| `CONFIG-*` | `config/` |
| `SCHEMA-*` | `schema/` |
| `DOCS-*` | `docs/` |
| `DB-*` | `db/` |
| `CD-*` | `cd/` |
| `ARCH-*` | `arch/` |
| `PKG-*` | `pkg/` |
| `SEC-ARCH-*` | `sec-arch/` |

For each namespace, read:
```
.principles-catalog/principles/<namespace>/.context-prime.md
```
Filter to only entries whose `### ID` heading is in the active set.

If `.principles-catalog/` is absent, fall back to Phase 4 logic (using `.principles-catalog`).

**→ Active set and principle content are now loaded. Skip to Phase 5.**

---

If no compiled block is found in any of the four files above, continue with the normal resolution below.

### Normal Resolution

Walk **up** from the target path to the git repo root (directory containing `.git/`) or a maximum of 10 levels, collecting every `.principles` file found along the way. Order them **root → target** (outermost first, innermost last).

**If no `.principles` files are found, skip to Phase 3.**

### Seed — Universal + Stack Layer 1

**Step 1 — Universal principles** (active for ALL artifact types):

Read `.principles-catalog/layers/artifact-types.yaml` → `universal` section. Seed the active set with:

| ID | Title |
|----|-------|
| SIMPLE-DESIGN-REVEALS-INTENTION | Reveals intention |
| CODE-CS-DRY | DRY: Don't Repeat Yourself |
| CODE-CS-KISS | KISS: Keep It Simple |
| CODE-DX-NAMING | Name things by what they represent |
| ARCH-DECISION-RECORDS | Architecture Decision Records |
| CODE-CS-YAGNI | YAGNI: You Aren't Gonna Need It |

**Step 2 — Stack layer 1** (active for the detected artifact type):

Read `.principles-catalog/layers/<detected-type>/layer-1-universal.md`. Add all principle IDs from the table in that file to the active set.

### Process Each .principles File (root → target)

For each `.principles` file encountered:

1. Skip blank lines and `#` comment lines
2. For each `@group` entry: read `.principles-catalog/groups/<group>.yaml`, expand its `principles` list into the active set. Recursively process any `includes` entries (detect and abort on cycles).
3. For each bare `ID` entry: add the ID to the active set (case-insensitive)
4. For each `!ID` entry: add the ID to an exclusion set

After processing all files: `final_active = active_set MINUS exclusion_set`

Record source as: `.principles hierarchy (N files)`

## Phase 3 — Dynamic Detection (fallback)

**Only run this phase if Phase 2 found no `.principles` files.**

### Layer 1 — Seed

Same as Phase 2 seeding: universal principles + stack layer 1 from `.principles-catalog/layers/<detected-type>/layer-1-universal.md`.

### Layer 2 — Context-Dependent

Read `.principles-catalog/layers/<detected-type>/layer-2-contexts.yaml`.

Based on the context detected in Phase 1, activate ALL principles from matching contexts. Scan the target file(s) content and the Phase 1 context signals for matches.

### Layer 3 — Risk-Elevated

Check for `.principles-catalog/layers/<detected-type>/layer-3-risk-signals.yaml`. If present, apply matching risk categories based on Phase 1 signals. Elevated principles carry extra weight during generation.

Record source as: `dynamic detection (<type> stack)`

## Phase 4 — Load Principle Content

Determine the namespaces present in the active ID set (e.g. `CODE-CS-DRY` → `code`, `DOC-PURPOSE` → `docs`, `CONFIG-NO-HARDCODED-SECRETS` → `config`).

For each namespace, read its single pre-compiled context file:
```
.principles-catalog/principles/<namespace>/.context-prime.md
```

Filter to only the entries whose `### ID` is in the final active set. Do not load entries for inactive principles.

Use the **Principle**, **Why it matters**, and **Good practice** content as your active frame in Phase 5.

## Phase 5 — Output

Present your results in this format:

### Active Principles

| Layer | ID | Title | Source |
|-------|----|-------|--------|
| Universal | CODE-CS-DRY | DRY: Don't Repeat Yourself | artifact-types universal |
| Stack L1 | DOC-PURPOSE | Every document has one clear purpose | docs stack layer 1 |
| Stack L2 | DOC-AUDIENCE | Write for a specific audience | docs architecture-docs context |
| Stack L3 | OWASP-01-BROKEN-ACCESS-CONTROL | Broken Access Control | authentication risk |

Omit Stack L2 and Stack L3 rows if none were activated.

Then state:

> **Artifact type:** <detected-type>
> **Principle source:** .principles hierarchy (N files) | dynamic detection
>
> I will work with these N principles as my active frame. I have read the full guidance for each. Proceed with your request.
