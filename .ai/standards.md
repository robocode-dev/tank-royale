# File and Encoding Standards

<!-- METADATA: ~25 lines, ~250 tokens -->
<!-- KEYWORDS: encoding, UTF-8, standards, file format, characters, ANSI, escape sequences -->

## Character Encoding

**Mandatory:**

- **UTF-8 encoding** for all text files
- Emojis OK in comments and documentation
- No BOM (Byte Order Mark)

**Forbidden:**

- Escape sequences in source files
- ANSI color codes
- Non-printable characters (except standard whitespace)
- Terminal control sequences

## File Handling

**When writing files:**

- Strip terminal sequences from output
- Use file tools (don't invoke interactive programs)
- Stay within repository boundaries
- No files outside repo without explicit permission

## Line Endings

**Use consistent line endings:**

- Git handles line ending conversion via `.gitattributes`
- Don't manually convert line endings
- Let Git manage based on platform

## Repository Boundaries

**Do not modify:**

- Files outside the repository
- System configurations
- Global Git settings
- User-specific IDE configurations (unless project-shared)

**Ask before:**

- Broad structural changes
- Breaking changes affecting multiple modules
- New dependencies or external tools
- Changes to build system fundamentals
