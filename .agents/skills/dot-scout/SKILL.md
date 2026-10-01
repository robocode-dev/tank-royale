---
name: dot-scout
description: Analyse a project to detect which principles apply and create or update .principles files encoding that analysis. Use when the user runs /dot-scout [path] to map principles to a codebase.
argument-hint: "[directory-path] | --explain <path> | --yes"
allowed-tools: Read, Write, Edit, Glob, Grep, Bash
version: 0.15.0
authors: Flemming N. Larsen (https://github.com/flemming-n-larsen)
license: MIT
generated-by: ".principles v0.15.0"
---


# Scout

You are analyzing a project to determine which principles apply, creating or updating `.principles` files to encode that, and generating the review files agents use. Judgement (profiling, placement) is yours; everything mechanical is done by scripts in `.agents/principles-catalog/bin/`. Follow these six phases exactly.

**Shortcuts** (check `$ARGUMENTS` first):

- `--explain <path>`: run `bash .agents/principles-catalog/bin/resolve.sh --format explain <path>`, show the output (the active principles for that path and which `.principles` file added, excluded, locked or waived each one), and stop.
- `--yes`: skip the confirmation question in Phase 3 and write the proposal as shown. Everything else is unchanged.

## Phase 1 - Resolve Target and Check the Catalog

Determine the target directory:

- If `$ARGUMENTS` is a **directory path**: use it as the target.
- If `$ARGUMENTS` is **empty** (or only `--yes`): use the current working directory.
- If `$ARGUMENTS` is a **file path**: use its containing directory.

Confirm the target exists. If not, report an error and stop.

Walk up from the target to find the **git root** (directory containing `.git/`). Record both the target directory and the git root - the hierarchy spans between them.

### 1.1 - Check the Catalog

Check that `.agents/principles-catalog/index.tsv` and `.agents/principles-catalog/bin/emit.sh` exist at the git root.

If either is missing, **stop** and report:

> "⚠️ `.agents/principles-catalog/` is missing or out of date. From your dot-principles checkout, run `./install.sh vendor <git-root>` and then re-run /dot-scout."

Do not search the file system for `install.sh`.

### 1.2 - Load Scout Extensions

Glob `.agents/principles-catalog/principles/*/.context-scout.md`. Only extra catalogs ship these. For each file found, read it and record the detection rules it defines: `{ namespace → [detection rules] }`. They supplement Phase 2. If there are none, use the built-in detection only.

## Phase 2 - Detect Profile

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

### Extension-based detection

After applying the built-in signals above, apply any detection rules loaded in Phase 1.2. For each rule:
- Check whether the directory (or subtree) matches the rule's file pattern criteria
- If matched: assign the artifact type and add the suggested group to that directory's profile

### Per-directory profiling

For projects with multiple subdirectories, detect profiles per directory:
- `src/main/` vs `src/test/` - different testing principles for test dirs
- `src/security/`, `src/auth/` - security-focused principles
- `frontend/`, `ui/`, `web/` - UI interaction principles
- `docs/`, `doc/` - documentation principles (`@docs`)
- `infra/`, `terraform/`, `k8s/`, `deploy/` - infrastructure principles (`@infra`)
- `.github/workflows/` - pipeline principles (`@pipeline`)
- Any directory matching an extension-based detection rule (Phase 1.2) - apply the group from that rule

Record a profile map: `{ directory → [detected groups] }`


## Phase 3 - Propose Placements and Confirm

Based on the profile map from Phase 2, propose where to place `.principles` files and what to put in each. Check exclusion density (3.3) **before** showing anything, so the user confirms once.

### 3.1 - Placement strategy

1. **Git root `.principles`**: Activate groups that apply to the whole project
2. **Subdirectory `.principles`**: Activate additional groups or exclude principles that don't apply to that subtree

List the available groups with `ls .agents/principles-catalog/groups/` and reference them by filename without `.yaml`. The common ones are:

**Language groups:** `java`, `typescript`, `python`, `go`, `csharp`, `rust`
**Framework groups:** `spring-boot`, `spring-data-jpa`, `react`, `angular`, `django`, `fastapi`
**Cross-cutting code groups:** `microservices`, `security-focused`
**Artifact-type groups:** `docs`, `infra`, `config`, `schema`, `pipeline`

Custom groups from extra catalogs or an org baseline appear in the same directory. Groups suggested by extension detection rules (Phase 1.2) are included automatically.

If `.agents/principles-catalog/org.principles` exists, the organization has locked some principles (`:lock`). Never propose a `!ID` for a locked principle; if a directory truly needs an exception, propose `:waive ID until YYYY-MM-DD "reason"` instead and say why.

### 3.2 - How exclusions work (read before proposing any `!`)

Files are applied from the outside in, root first. The **deepest file that mentions a principle decides**: `!ID` or `!@group` in an outer file removes it, and an `@group` or ID in a deeper file adds it back. Inside one file, exclusions win over additions whatever the line order. A principle locked by the organization can never be removed.

Groups overlap: every language group includes `source-code`, and `kotlin` includes `java`. So `!@kotlin` also removes the shared principles that a Java directory needs. That is safe only when a **deeper** directory adds them back with its own group (`@java` in `bot-api/java/.principles`). Two rules follow:

- Never put `!@group` and a group that overlaps it in the **same** file: the exclusion wins and removes the shared principles.
- Put an exclusion in the directory above the ones that add their own group, or keep it out and place the group in each directory that needs it.

### 3.3 - Exclusion density analysis

Before writing, check whether any parent-level proposals would generate unnecessary exclusions in child directories.

### When to run

Only when the profile map from Phase 2 contains **two or more** directories that would each receive their own `.principles` file (i.e., there is at least one parent-child pair in the proposed hierarchy). Skip this phase entirely if every proposed `.principles` file is a leaf with no applicable children.

### Algorithm

For each proposed parent `.principles` file (root or intermediate directory), evaluate every proposed entry - groups (`@group`) and bare principle IDs - against all proposed child directories detected in Phase 2:

1. **Count applicable children**: child directories that inherit from this parent (would have their own `.principles` or would inherit the parent's entries).
2. **Count excluding children**: children where the entry does not match the child's detected profile and would therefore need a `!@group` or `!ID` exclusion to suppress it. Remember the overlap rules in 3.2: an exclusion of a language group also removes `source-code` principles, so each such child needs a deeper directory that adds its own group back, or the entry should be demoted instead.
3. Compute `exclusion_ratio = excluding_children / applicable_children`.
4. If `exclusion_ratio > 0.5` (strict majority excluded):
   - **Demote the entry**: remove it from the parent proposal; add it directly to each *including* child's proposal (the minority that actually benefits).
   - Record the demotion for reporting: `⬇ @<group> demoted from <parent> → <child1>/, <child2>/ - excluded in N/M children`
5. For each principle activated by a parent-level group that >50% of children would individually suppress with `!PRINCIPLE-ID`:
   - **Consolidate**: add `!PRINCIPLE-ID` at the parent level instead (one exclusion line replaces N child-level exclusion lines). The children that do need the principle must add it back in their own `.principles` file, as a bare ID or through a group of their own (the deeper file wins). Add those lines to the proposal. If more children would have to add it back than the exclusion saves, do not consolidate.
   - Record the consolidation: `↑ !PRINCIPLE-ID consolidated to <parent> - excluded in N/M children`

Skip analysis for any parent with `applicable_children ≤ 1` (a majority cannot be computed from a single child).

### Reporting demotions and consolidations

After running the analysis, show a summary before the updated proposals if any changes were made:

```
Exclusion density analysis:
  ⬇ @docs demoted from root → docs/ - excluded in 3/4 children
  ⬇ @infra demoted from root → infra/, deploy/ - excluded in 3/4 children
  ↑ !CODE-TS-TEST-FIRST consolidated to src/ - excluded in 4/5 children

Updated proposals incorporate these changes.
```

If no changes were made, output: `Exclusion density: no demotions needed.`

The proposals you show in 3.4 already incorporate these changes.

### 3.4 - Proposal format and confirmation

For each proposed file, show:
```
[path]/.principles
  @group1          ← reason
  @group2          ← reason
  CODE-OB-SERVICE-LEVEL-OBJECTIVES      ← specific principle for this directory
  !CODE-TS-TEST-FIRST     ← exclusion and why
```

Ask once: "I propose creating/updating N .principles files. Proceed? (yes to continue, no to review proposals)". Skip the question if `--yes` was given.

Wait for user confirmation. If the user says no or requests changes, adjust proposals and ask again.

## Phase 4 - Check Existing .principles Files

Before writing, check for existing `.principles` files at the proposed paths.

For each existing file:
- Read its current contents
- Preserve all existing entries (including `!exclusions` and comments)
- Only **add** new entries that aren't already present
- **Never remove** existing entries - that is the human's decision
- If the file already has all proposed additions, mark it as **unchanged**

Determine final action per file: `created` | `updated` | `unchanged`

## Phase 5 - Write Files and Report

Write or update each file as determined in Phase 5.

### File format

```
# Generated by dot-scout vVERSION
# Detected: [artifact-type] / [language/framework/domain]
# Last analysed: [date]

@group1
@group2

# Direct includes
CODE-OB-SERVICE-LEVEL-OBJECTIVES
```

Do not add comments to lines that were already present in an existing file - only add comments to newly added entries.

### Report

After writing, output:

```
.principles analysis complete

Files written:
  ✓ created   /path/to/.principles         (@spring-boot, @security-focused)
  ✓ created   /path/to/docs/.principles    (@docs)
  ✓ updated   /path/to/src/.principles     (added @react)
  - unchanged /path/to/infra/.principles   (no changes needed)

Active groups resolved:
  @spring-boot → @java, CODE-API-STANDARD-HTTP-METHODS, DDD-REPOSITORY, OWASP-03-INJECTION ... (N principles)
  @docs → DOC-PURPOSE, DOC-MINIMAL, DOC-AUDIENCE, DOC-ACCURACY, DOC-EXAMPLES, DOC-PROGRESSIVE-DISCLOSURE ... (N principles)

Next steps:
  - Run /dot-audit <target> to review against these principles
  - Edit .principles files manually to add !exclusions or direct principle IDs
```

### Verify exclusions

For every directory whose `.principles` file contains a `!`, and for the directories below it, run

```
bash .agents/principles-catalog/bin/resolve.sh --format explain <dir>
```

Check that each directory ends up with the principles of its own language group, and with none from groups it should not have. `Reinstated:` lines show where a deeper file added something back, which is expected. If a directory lost principles it needs, fix the placement (move the exclusion up one level, or move the group down) and write the files again before Phase 6.

## Phase 6 - Emit Generated Files

Run one command. It resolves the active principles for every `.principles` file, writes everything the review tools read, and removes generated files that no longer apply:

```
bash .agents/principles-catalog/bin/emit.sh --stacks <stacks>
```

`<stacks>` is the comma-separated list of artifact stacks you detected in Phase 2 (`code`, `docs`, `config`, `infra`, `schema`, `pipeline`), for example `--stacks code,docs`. They are remembered in `install.cfg`, so a later refresh needs no flag.

What it writes (deterministic - the same inputs give the same files):

- `.agents/principles-catalog/active.md` - every active principle with its summary.
- `.agents/instructions/review.md` - agent-neutral review instructions, plus a short managed block in `AGENTS.md` that points to it. Claude Code, Codex CLI, Copilot CLI and other agents that read `AGENTS.md` use it.
- `.github/instructions/<group>.instructions.md` - Copilot Code Review, only if enabled (`copilot-review` in `install.cfg`).
- `REVIEW.md` - Claude Code Review, only if enabled (`claude-review` in `install.cfg`).

The tools come from `install.cfg` (written by the installer). Do not ask the user which tools to generate for. If neither review integration is enabled, say so in the report and mention that `./install.sh <git-root>` (interactive) enables them, or `emit.sh --tools copilot,claude` generates them once.

Show the script's output as the report, then add:

```
Next steps:
  - Review with /dot-audit <target>, or ask your agent to review; it reads .agents/instructions/review.md
  - Edit .principles files by hand to add !exclusions or direct principle IDs, then run
    bash .agents/principles-catalog/bin/emit.sh to refresh the generated files
  - Commit .principles files, .agents/principles-catalog/, .agents/instructions/ and the generated review files
```

If `emit.sh` reports warnings (unknown groups, principles missing from `index.tsv`, expired waivers), repeat them to the user.

