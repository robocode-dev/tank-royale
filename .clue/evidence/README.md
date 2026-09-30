# Repository-owned acceptance evidence

Gherkin describes behavior. Tests carry AC identity, proof type and direction in the framework's native metadata, custom executable-bound metadata, or a stable name/title. This folder holds examples, not a framework support registry. Establish and test the repository's export command before activating machine-proven criteria.

The command aggregates all declared producers into `.clue/evidence.yaml`. A producer is a testsuite or export source; several producers can use the same framework. Producer ID, source path and executable subject distinguish identities. Framework and language are optional descriptions with open values. The validator reads the common contract regardless of those descriptions and never starts a producer.

## Version one

```yaml
version: 1
producers:
  - id: backend
    framework: JUnit
    language: Java
    include: ["backend/**/*.java", "tools/export-backend.*", "backend/pom.xml"]
    exclude: ["backend/target/**"]
    inputs:
      - path: backend/ExampleTest.java
        sha256: "<SHA-256 of source with CRLF normalized to LF>"
    references:
      - id: PDO-115
        path: backend/ExampleTest.java
        subject: ExampleTest.preservesPages
        type: Unit
        direction: positive
```

This abbreviated illustration omits the other matching inputs. A real export lists every matching input, including exporter and discovery configuration. Patterns are repository-relative slash-separated glob segments; `**` matches zero or more whole segments. Excludes apply to directories and their descendants. Inputs and references cannot escape the repository or traverse symlinks. The manifest cannot fingerprint itself.

Each executable carries one ID and classification. A legacy unclassified reference omits both type and direction. Canonical IDs preserve exact case, punctuation and letter suffixes. References to unknown or retired criteria fail. Export diagnostics name `path`, optional `subject`, and `message`; they invalidate the complete exchange and give no credit. Unsupported versions, duplicate producers, duplicate/conflicting executables and stale file inventories fail as well.

## Choosing a carrier

Prefer native metadata; use a custom annotation/attribute/decorator directly attached to the executable when native metadata is unsuitable. Container AC metadata and ordinary comments next to a test do not count. When using a naming fallback, encode canonical hyphens as underscores, retain the numeric spelling and lowercase suffix, and put the type and direction together: `TestPDO_115_UnitPositive_preserves_pages` in Go, `test_PDO_115_UnitPositive_preserves_pages` in Python, or `[PDO-115 Unit Positive] preserves pages` in a literal title. The exporter normalizes only that documented identity encoding. Existing compact Go/JVM names remain usable through the compatibility exporter example.

See the examples in this folder for metadata and export. Adapt and test a producer for each actual suite, then have one aggregate command write a temporary complete export and atomically replace the manifest. Missing or failed producers must not publish a partial success. Sort outputs and omit timestamps so repeat exports are byte-identical. Test attribution, malformed metadata, multiple producers, and input addition/deletion before relying on the result.

Run the repository's exporters and tests locally, commit the export, and run `clue validate`. Optional CI can regenerate it and compare the result. Cliewen checks traceability and freshness, not test execution or whether an assertion proves the behavior. The repository owns that judgment and its normal test gates.
