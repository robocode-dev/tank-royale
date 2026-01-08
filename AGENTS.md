<!-- OPENSPEC:START -->

# OpenSpec Instructions

These instructions are for AI assistants working in this project.

Always open `@/openspec/AGENTS.md` when the request:

- Mentions planning or proposals (words like proposal, spec, change, plan)
- Introduces new capabilities, breaking changes, architecture shifts, or big performance/security work
- Sounds ambiguous and you need the authoritative spec before coding

Use `@/openspec/AGENTS.md` to learn:

- How to create and apply change proposals
- Spec format and conventions
- Project structure and guidelines

Keep this managed block so 'openspec update' can refresh the instructions.

<!-- OPENSPEC:END -->

# Agent Specifications

## Core Principles

- Clean code: readability over cleverness, meaningful names, small functions, clear responsibilities, minimal coupling
- Minimal changes: implement only what's required; no drive-by refactors unless necessary
- Small atomic commits explaining why, not just what; concise answers; no summary files unless requested. But AI agent
  should never make git commits.

## Source of Truth & Cross-Platform

- **Java Bot API** is reference for all platforms: align behavior, API names, defaults, event semantics
- Keep JSON/wire behavior consistent; platform idioms OK if semantics identical
- **Public API**: stable; avoid breaking changes; coordinate all ports if unavoidable
- **Protocol**: no breaking changes; additive only; update `/schema` and docs

## Coding Conventions

**Java**: existing style; immutability/`final`; clear names; no magic numbers; explicit nullability; defensive
programming

**Python**: PEP 8 + PEP 484 type hints; keep `py.typed` valid; run `mypy`; prefer dataclasses; read-only state objects

**General**: SRP, DRY, YAGNI; composition over inheritance; pure functions; early returns; precise errors

## Cross-Language Workflow

1. Implement/verify Java Bot API first
2. Port to other languages matching names, defaults, behavior
3. Update sample bots if user-visible
4. Build and test each module

## Testing & Build

- Build modules; run tests; add tests for bugs/new behavior
- Protocol changes: update JSON examples, schema, docs; verify backward compatibility
- Timing/state changes: validate with sample bot
- **Build**: run `./gradlew clean build` after code/config/build changes (exception: pure text/markdown/docs)

## Documentation & Review

- Update README/docs/VERSIONS.MD for user-visible changes
- Keep Javadoc/docstrings aligned across ports
- Verify: Java reference match, backward compatibility, minimal diff, tests/docs updated, naming consistency

## Standards

- UTF-8 encoding; emojis OK in comments/docs
- No escape/ANSI/non-printable chars; strip terminal sequences
- Use file tools; no interactive programs; no files outside repo
- Ask before broad/breaking changes
