# AI Agent Instructions Index

This directory contains modular instruction files for AI coding assistants working on Tank Royale.

## Token-Optimized Loading Strategy

Each file is designed for **selective loading** to avoid overwhelming token budgets. Load only what's relevant to your
current task.

## File Metadata

| File                    | Lines | Est. Tokens | Load When Keywords Include                                                      |
|-------------------------|-------|-------------|---------------------------------------------------------------------------------|
| `core-principles.md`    | ~40   | ~400        | clean code, principles, refactor, design patterns                               |
| `cross-platform.md`     | ~55   | ~600        | Bot API, Java, Python, .NET, C#, cross-platform, port, reference implementation |
| `coding-conventions.md` | ~35   | ~350        | style, conventions, naming, format, PEP 8, immutability                         |
| `testing-and-build.md`  | ~40   | ~450        | test, build, Gradle, gradlew, compile, validation                               |
| `documentation.md`      | ~30   | ~300        | docs, README, markdown, mermaid, VERSION.md, Javadoc, docstring, changelog     |
| `standards.md`          | ~25   | ~250        | encoding, UTF-8, standards, file format, characters                             |

**Total if all loaded:** ~225 lines, ~2350 tokens

## Routing Decision Tree

```
Task involves...
├─ Planning/specs/proposals? → Load `/openspec/AGENTS.md` ⚠️ HIGH PRIORITY
├─ Bot API changes? → Load `cross-platform.md` + `core-principles.md`
├─ Testing/building? → Load `testing-and-build.md`
├─ Documentation? → Load `documentation.md`
├─ Code style question? → Load `coding-conventions.md`
├─ File encoding issue? → Load `standards.md`
└─ General task? → Load `core-principles.md` (default)
```

## Cross-References

- **OpenSpec workflow:** `/openspec/AGENTS.md` (508 lines - load separately when needed)
- **Project conventions:** `/openspec/project.md`
- **Main router:** `/AGENTS.md` (this directory's parent)

## Update Guidelines

When modifying these instructions:

1. **Keep files focused:** Single topic per file
2. **Target 50-150 lines:** Split if exceeding 150 lines
3. **Update keyword mappings:** Ensure routing table stays accurate
4. **Test token efficiency:** Verify agents load only relevant files
5. **Maintain consistency:** Cross-language rules must align across all Bot API platforms

## AI Learning Loop - Capturing Feedback

**Capture lessons learned during chat sessions to improve future AI behavior.**

### How It Works

When you provide corrective feedback during our chat, I should:

1. Recognize the feedback pattern
2. Propose an edit to the appropriate `.ai/*.md` file
3. Apply the change (with your approval)

### Trigger Patterns

I should recognize and act on feedback like:

- "Remember ..." (preferred)
- "Always ..." (preferred)
- "@ai-learn: ..." (explicit trigger)

Note: A colon (e.g., "Remember: ...") makes it more command-like.

**When in doubt:** Ask "Would you like me to add this to the AI instructions?"

- Repeated corrections for the same issue

### How to Trigger Explicitly

```
"Remember Always use final for Java method parameters"
"Remember When porting from Java, preserve exact method signatures"
"Always add return type hints in Python, including -> None"
"@ai-learn coding-conventions: Python functions must have explicit return type hints"
```

### Target Files

| Topic                              | Target File             |
|------------------------------------|-------------------------|
| Clean code, principles, general    | `core-principles.md`    |
| Bot API, Java/Python/.NET, porting | `cross-platform.md`     |
| Style, naming, language-specific   | `coding-conventions.md` |
| Tests, Gradle, build               | `testing-and-build.md`  |
| README, VERSION.md, docs          | `documentation.md`      |
| Encoding, file formats             | `standards.md`          |

### Best Practices

- **Be specific:** "Use `-> None` for void functions" vs "Fix return types"
- **Be generalizable:** Avoid instructions tied to specific files or one-time fixes
- **Include context:** "When porting from Java, ..." helps AI understand when to apply

## File Descriptions

### `core-principles.md`

Core coding philosophy: clean code, minimal changes, atomic commits, SRP/DRY/YAGNI principles.

### `cross-platform.md`

Bot API cross-platform workflow: Java as reference, porting to Python/.NET, semantic equivalence requirements.

### `coding-conventions.md`

Language-specific style guides: Java immutability, Python type hints, general design patterns.

### `testing-and-build.md`

Build and test procedures: Gradle commands, test requirements, validation steps.

### `documentation.md`

Documentation standards: README updates, VERSION.md format, Javadoc/docstring alignment.

### `standards.md`

File and encoding standards: UTF-8, character restrictions, repository boundaries.
