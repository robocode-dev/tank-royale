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

- No escape code characters should be inserted into repository files. Avoid embedding ANSI escape sequences or other
  non-printable control characters in committed files.
- All files MUST use UTF-8 encoding. When generating files or text, ensure UTF-8 output.
- Emojis are welcome in comments and documentation and should be encoded in UTF-8.
- If tooling produces colored or escaped output, strip terminal escape sequences before committing.
- Do not create summary files (for example: separate summary.md or notes files) unless explicitly instructed to do so.
- Be concise when answering questions and keep any summaries compact and short.
