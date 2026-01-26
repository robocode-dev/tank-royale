# Documentation Standards

<!-- METADATA: ~30 lines, ~300 tokens -->
<!-- KEYWORDS: docs, README, VERSIONS.md, Javadoc, docstring, changelog, user-visible -->

## When to Update Documentation

**Always update for user-visible changes:**

- New features or APIs
- Breaking changes
- Behavior modifications
- Deprecated functionality

**Files to update:**

- `/README.md` - Project overview and getting started
- `/VERSIONS.md` - Version history and changelog
- Module-specific `README.md` files
- API documentation (Javadoc/docstrings)

## VERSIONS.md Format

**Follow existing format:**

```markdown
## Version X.Y.Z (YYYY-MM-DD)

### Added
- New feature description

### Changed
- Modified behavior description

### Fixed
- Bug fix description

### Deprecated
- Deprecated feature (migration path)
```

## Cross-Platform Documentation Alignment

**Keep documentation synchronized:**

1. **Javadoc** (Java) - Must match behavior exactly
2. **Docstrings** (Python) - Must match Java semantics
3. **XML comments** (C#/.NET) - Must match Java semantics

**Verification checklist:**

- [ ] Java Javadoc updated
- [ ] Python docstrings updated
- [ ] .NET XML comments updated
- [ ] Parameter descriptions identical
- [ ] Return value documentation aligned
- [ ] Example code consistent

## Review Checklist

**Before completing documentation task:**

- [ ] Java reference documentation matches actual behavior
- [ ] Backward compatibility noted for breaking changes
- [ ] Minimal diff (no unnecessary formatting changes)
- [ ] Tests documented if behavior is complex
- [ ] Cross-language naming consistency verified
