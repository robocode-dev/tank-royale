# Documentation Standards

<!-- KEYWORDS: docs, README, VERSIONS.md, Javadoc, docstring, changelog, user-visible -->

## When to Update

Update for user-visible changes: new features, breaking changes, behavior modifications, deprecations.

**Files to update:**
- `/README.md` — project overview
- `/VERSIONS.md` — changelog
- Module-specific `README.md` files
- API docs: Javadoc (Java) · docstrings (Python) · XML comments (C#)

## VERSIONS.md Format

```markdown
## Version X.Y.Z (YYYY-MM-DD)

### Added
- ...

### Changed
- ...

### Fixed
- ...

### Deprecated
- ... (include migration path)
```

## API Doc Alignment

Javadoc (Java) is authoritative. Python docstrings and C# XML comments must match Java semantics: same parameter descriptions, return values, and examples.

## Checklist

- [ ] API docs match actual behavior
- [ ] Breaking changes noted with migration path
- [ ] Minimal diff (no unnecessary formatting changes)
- [ ] Cross-language naming consistent
