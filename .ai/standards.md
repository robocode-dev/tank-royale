# File and Encoding Standards

<!-- KEYWORDS: encoding, UTF-8, standards, file format, characters, ANSI, escape sequences -->

## Character Encoding

- **UTF-8**, no BOM
- Emojis OK in comments and documentation
- **Forbidden:** escape sequences, ANSI color codes, non-printable characters, terminal control sequences

## File Handling

- Strip terminal sequences from output before writing to files
- Use file tools; stay within repository boundaries
- No files outside repo without explicit permission
- Git manages line endings via `.gitattributes` — don't manually convert

## Repository Boundaries

Do not modify: files outside the repo, system configs, global Git settings, user IDE configs.

Ask before: broad structural changes, new dependencies, build system changes, breaking changes across modules.
