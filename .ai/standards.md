# File and Encoding Standards

<!-- KEYWORDS: encoding, UTF-8, standards, file format, characters, ANSI, escape sequences -->

## Character Encoding

- **UTF-8**, no BOM
- Emojis OK in comments and documentation
- **Forbidden:** escape sequences, ANSI color codes, non-printable characters, terminal control sequences

### Java `.properties` files

Store all `.properties` files as **UTF-8**. Since Java 9, `ResourceBundle.getBundle()` reads `.properties`
files as UTF-8 by default. Write non-ASCII characters directly (e.g. `å`, `ö`, `é`) — do **not** use
`\uXXXX` escape sequences for readable text. The `\uXXXX` syntax is still valid for symbols and emoji
(e.g. `\u2192` for →, `\uD83D\uDC1B` for 🐛) where the raw character would be harder to read.

## File Handling

- Strip terminal sequences from output before writing to files
- Use file tools; stay within repository boundaries
- No files outside repo without explicit permission
- Git manages line endings via `.gitattributes` — don't manually convert

## Repository Boundaries

Do not modify: files outside the repo, system configs, global Git settings, user IDE configs.

Ask before: broad structural changes, new dependencies, build system changes, breaking changes across modules.
