---
description: Analyse a project to detect which principles apply and create or update .principles files encoding that analysis. Use when the user runs /scout [path] to map principles to a codebase.
argument-hint: "[directory-path]"
allowed-tools: Read, Write, Edit, Glob, Grep, Bash
version: 0.6.0
authors: Flemming N. Larsen (https://github.com/flemming-n-larsen)
---

# Scout

You are analysing a project to determine which principles apply and creating or updating `.principles` files to encode that. Follow these five phases exactly.

## Phase 1 — Resolve Target and Bootstrap Catalog

Determine the target directory:

- If `$ARGUMENTS` is a **directory path**: use it as the target.
- If `$ARGUMENTS` is **empty**: use the current working directory.
- If `$ARGUMENTS` is a **file path**: use its containing directory.

Confirm the target exists. If not, report an error and stop.

Walk up from the target to find the **git root** (directory containing `.git/`). Record both the target directory and the git root — the hierarchy spans between them.

### 1.1 — Bootstrap Catalog

Check whether `.principles-catalog/index.tsv` exists at the git root.

If **not present**, try to auto-vendor it now (before any other phase):

1. Search for the dot-principles `install.sh` in these locations (in order):
   - `<git-root>/../dot-principles/install.sh`
   - `<git-root>/../../dot-principles/dot-principles/install.sh`
   - `~/Code/dot-principles/dot-principles/install.sh`
   - Run: `find ~ -maxdepth 5 -name "install.sh" -path "*/dot-principles/*" 2>/dev/null | head -1`

2. If found: run `<path-to-install.sh> vendor <git-root>` and report:
   > "✓ Catalog vendored to .principles-catalog/ — proceeding."

3. If not found: report:
   > "⚠️ `.principles-catalog/` not found. Group lookups will use the hardcoded catalog below."
   > "  To vendor: clone dot-principles and run `./install.sh vendor <git-root>`"
   > Continue using the hardcoded group list in Phase 3 (custom groups won't be available).

Record whether the catalog is available: **catalog-available: true/false**

## Phase 2 — Detect Profile

Analyse the target directory (and subdirectories) to build a profile per directory. For each directory, detect:

### Code artifact signals

| Signal | Language / Framework |
|--------|---------------------|
| `*.java`, `pom.xml`, `build.gradle` | Java |
| `*.ts`, `tsconfig.json` | TypeScript |
| `*.py`, `pyproject.toml`, `requirements.txt` | Python |
| `*.go`, `go.mod` | Go |
| `*.cs`, `*.csproj`, `*.sln` | C# |
| `*.rs`, `Cargo.toml` | Rust |
| `*.rb`, `Gemfile` | Ruby |
| `*.php`, `composer.json` | PHP |
| `@SpringBootApplication`, `spring-boot` in build file | Spring Boot |
| `@Entity`, `spring-data-jpa` dependency | Spring Data JPA |
| `react`, `jsx`, `tsx` imports | React |
| `@NgModule`, `@Component` | Angular |
| `django` in requirements | Django |
| `fastapi` import | FastAPI |
| `express` in package.json | Express |

### Domain signals (for code artifact type)

| Signal | Domain |
|--------|--------|
| `payment`, `billing`, `invoice`, `stripe`, `checkout` | Financial |
| `auth`, `login`, `oauth`, `jwt`, `session` | Authentication |
| `user`, `profile`, `email`, `address`, `PII` | Personal data |
| `microservice`, `service-mesh`, `saga` | Distributed systems |

### Non-code artifact type signals

| Directory / files | Artifact type | Group |
|-------------------|---------------|-------|
| `docs/`, `*.md` files (README, DESIGN, ADR, CONTRIBUTING) | docs | `@docs` |
| `.github/workflows/`, `Jenkinsfile`, `*.gitlab-ci.yml`, `azure-pipelines.yml` | pipeline | `@pipeline` |
| `*.tf`, `*.tfvars`, `Dockerfile`, `docker-compose.*`, `Chart.yaml`, `k8s/`, `infra/`, `terraform/` | infra | `@infra` |
| `*.proto`, `*.graphql`, `openapi.yaml`, `swagger.yaml`, `schema.sql` | schema | `@schema` |
| `.env`, `application.yaml`, `appsettings.json`, `*.properties` | config | `@config` |

### Per-directory profiling

For projects with multiple subdirectories, detect profiles per directory:
- `src/main/` vs `src/test/` — different testing principles for test dirs
- `src/security/`, `src/auth/` — security-focused principles
- `frontend/`, `ui/`, `web/` — UI interaction principles
- `docs/`, `doc/` — documentation principles (`@docs`)
- `infra/`, `terraform/`, `k8s/`, `deploy/` — infrastructure principles (`@infra`)
- `.github/workflows/` — pipeline principles (`@pipeline`)

Record a profile map: `{ directory → [detected groups] }`

## Phase 3 — Propose .principles Placements

Based on the profile map from Phase 2, propose where to place `.principles` files and what to put in each.

### Placement strategy

1. **Git root `.principles`**: Activate groups that apply to the whole project
2. **Subdirectory `.principles`**: Activate additional groups or exclude principles that don't apply to that subtree

### Available groups (from `.principles-catalog/groups/`)

Reference these groups by their filename (without `.yaml`):

**Language groups:** `java`, `typescript`, `python`, `go`, `csharp`, `rust`
**Framework groups:** `spring-boot`, `spring-data-jpa`, `react`, `angular`, `django`, `fastapi`
**Cross-cutting code groups:** `microservices`, `security-focused`
**Artifact-type groups:** `docs`, `infra`, `config`, `schema`, `pipeline`

Also list any custom groups found in `.principles-catalog/groups/` that aren't listed above.

### Proposal format

For each proposed file, show:
```
[path]/.principles
  @group1          ← reason
  @group2          ← reason
  CODE-OB-SERVICE-LEVEL-OBJECTIVES      ← specific principle for this directory
  !CODE-TS-TEST-FIRST     ← exclusion and why
```

Ask for confirmation before writing: "I propose creating/updating N .principles files. Proceed? (yes to write, no to review proposals)"

Wait for user confirmation. If the user says no or requests changes, adjust proposals and ask again.

## Phase 4 — Check Existing .principles Files

Before writing, check for existing `.principles` files at the proposed paths.

For each existing file:
- Read its current contents
- Preserve all existing entries (including `!exclusions` and comments)
- Only **add** new entries that aren't already present
- **Never remove** existing entries — that is the human's decision
- If the file already has all proposed additions, mark it as **unchanged**

Determine final action per file: `created` | `updated` | `unchanged`

## Phase 5 — Write Files and Report

Write or update each file as determined in Phase 4.

### File format

```
# Generated by /scout
# Detected: [artifact-type] / [language/framework/domain]
# Last analysed: [date]

@group1
@group2

# Direct includes
CODE-OB-SERVICE-LEVEL-OBJECTIVES
```

Do not add comments to lines that were already present in an existing file — only add comments to newly added entries.

### Report

After writing, output:

```
.principles analysis complete

Files written:
  ✓ created   /path/to/.principles         (@spring-boot, @security-focused)
  ✓ created   /path/to/docs/.principles    (@docs)
  ✓ updated   /path/to/src/.principles     (added @react)
  — unchanged /path/to/infra/.principles   (no changes needed)

Active groups resolved:
  @spring-boot → @java, CODE-API-STANDARD-HTTP-METHODS, DDD-REPOSITORY, OWASP-03-INJECTION ... (N principles)
  @docs → DOC-PURPOSE, DOC-MINIMAL, DOC-AUDIENCE, DOC-ACCURACY, DOC-EXAMPLES, DOC-PROGRESSIVE-DISCLOSURE ... (N principles)

Next steps:
  - Run /prime to activate principles before writing
  - Run /audit <file> to review against these principles
  - Edit .principles files manually to add !exclusions or direct principle IDs
```


## Phase 6 — Compile Principles Block and Inject into AI Instruction Files

### 6.1 — Resolve the Compiled Set

If **catalog-available: false** (set in Phase 1), report:
> "⚠️ Compiled block skipped — catalog not available. Run `./install.sh vendor <git-root>` and re-run /scout."
> Skip the rest of Phase 6.

Read `.principles-catalog/index.tsv`. Each line is `ID|LAYER|SUMMARY`.

From the active principle set (resolved via `.principles` hierarchy), look up each active ID in the index to get its Layer and Summary. Build three groups:
- **Layer 1** → "Always (Layer 1)"
- **Layer 2** → "This stack" (note the @groups that activated them)
- **Layer 3** → "Risk-elevated" (note which subdirectory triggered them)

Any active IDs not found in index.tsv: include with summary "—" and log a warning.

### 6.2 — Detect AI Instruction Files and Injection Targets

Scan the git root for the following files and classify each:

**Claude Code target:**
- Check for `CLAUDE.md` (root) or `.claude/CLAUDE.md`
- If found and is a **pointer file** (>80% of non-empty lines are `@<ref>` imports): note it delegates, do not inject into it
- Inject into `.claude/rules/principles.md` — create the file if it does not exist
- Ensure CLAUDE.md (or `.claude/CLAUDE.md`) has `@.claude/rules/principles.md` in it — add if absent

**AGENTS.md target** (de facto cross-agent standard):
- **Case A — Hub**: AGENTS.md exists and has a structured instruction table (markdown table with file links, or `## Instruction Files` section) → create `.ai/principles.md` with the compiled block → add row to AGENTS.md's instruction table: `| [.ai/principles.md](.ai/principles.md) | Active engineering principles (compiled by /scout) |`
- **Case B — Simple**: AGENTS.md exists with direct content, no instruction table → inject block directly into AGENTS.md
- **Case C — Absent**: AGENTS.md does not exist → create it with the compiled block

**Copilot target:**
- Check `.github/copilot-instructions.md`
- If it is a **pointer file** (primarily delegates via `@<ref>` or markdown links to another hub file): skip injection
- Otherwise: inject compiled block directly

### 6.3 — Compiled Block Format

The block to inject (replacing any existing `<!-- .principles: begin -->...<!-- .principles: end -->` block, or appending if absent):

```
<!-- .principles: begin — compiled by /scout vVERSION, DATE -->
## Active Principles

**Always (Layer 1):**
- PRINCIPLE-ID: Summary text here
- PRINCIPLE-ID: Summary text here

**This stack (@group1 + @group2):**
- PRINCIPLE-ID: Summary text here

**Risk-elevated (subdir/):**
- PRINCIPLE-ID: Summary text here
<!-- .principles: end -->
```

- VERSION from `.principles-catalog/` or the repo's VERSION file
- DATE as YYYY-MM-DD
- Omit a layer section if it has no principles
- Keep summaries on one line — ID: summary

### 6.4 — Report

After injection, output:

```
Compiled block written to:
  ✓ .claude/rules/principles.md        (N principles — Layer 1: X, Layer 2: Y, Layer 3: Z)
  ✓ .ai/principles.md                  (hub pattern — added to AGENTS.md instruction table)
  ✓ .github/copilot-instructions.md    (N principles)
  — CLAUDE.md                          (pointer to AGENTS.md — skipped; @.claude/rules/principles.md added)
  ⚠ .github/copilot-instructions.md   (pointer file — skipped)

Tip: commit .principles-catalog/ so CI and PR bots can use it without local install.
```

---

## Pointer file detection logic

A file is a **pointer** if, among its non-empty non-comment lines:
- More than 80% are `@<filepath>` import lines (Claude Code style)
- OR the file consists primarily of a single `> Use the context in X` directive plus `@<filepath>` references

Examples of pointer files:
- `CLAUDE.md` containing just `@AGENTS.md` and a brief note
- `.github/copilot-instructions.md` containing `Use the context in /AGENTS.md for all tasks`
