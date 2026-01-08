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

- Keep this file compact and avoid redundancy.
- Do not insert escape/ANSI or other non-printable control characters into repository files; strip terminal color/escape
  sequences before committing.
- All files MUST use UTF-8 encoding. Emojis are welcome in comments and documentation (UTF-8 encoded).
- Do not create separate summary files (e.g. summary.md) unless explicitly requested.
- Be concise in answers; keep summaries short and compact.
- Build requirement: after changes that affect code, config, or build artifacts, run the Gradle wrapper and ensure the
  build succeeds before marking the task complete.
    - Preferred command: `./gradlew clean build`
    - Exception: purely textual changes (plain text, markdown, documentation) do not require a build.
    - If unsure whether a change affects the build, run the Gradle wrapper to be safe.
    - Tasks remain incomplete until the required build finishes successfully.
