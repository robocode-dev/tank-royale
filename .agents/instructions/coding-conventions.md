# Coding Conventions

<!-- KEYWORDS: style, conventions, naming, format, Java, Kotlin, Python, C#, PEP 8, immutability, type hints, modern, GUI text, resources, properties, i18n, localization -->

## Modern Language Features

- **Java 11+**: `Path.of()`, `Files.*`, `var`, `Optional`, `Stream`, `HttpClient`, text blocks
- **Kotlin**: `kotlin.io.path.*`, `runCatching`, `Result`, scope functions, `sealed class`, coroutines
- **Python 3.10+**: `match`, `|` union types, `@dataclass`, `pathlib.Path`, type hints, f-strings
- Prefer NIO.2 `Path` API over `java.io.File`

## Java Conventions

- `final` for immutability wherever possible
- Descriptive names, no unexplained abbreviations
- Named constants over magic numbers
- `@Nullable` / `@NotNull` annotations
- Immutable objects and collections preferred

## GUI Text

- Every text a person sees in the GUI (labels, buttons, titles, menu items, messages, error texts, tooltips) is a **resource**, never a string literal in code. Use `Strings`, `Messages`, `MenuTitles`, `UiTitles`, `Hints` and `About` (`gui/src/main/kotlin/dev/robocode/tankroyale/gui/ui/ResourceBundles.kt`) with the files in `gui/src/main/resources`.
- Choose the file by kind: `Menu` (menus and their items), `UI titles` (window and dialog titles), `Messages` (messages and dialog texts), `Strings` (labels), `Hints` (tooltips), `About`.
- Every new key goes in the base file and in all language files (`_ca`, `_da`, `_es`); `PropertiesConsistencyTest` requires the same keys everywhere. Text that is not translated yet is written in English with the comment `(English until translated)`.
- Logic outside Swing (behavior classes, libraries) returns codes or keys, never English sentences; the GUI turns them into resource texts. A sentence from a lower layer may be shown only as a labelled detail line.
- Parameters in texts use `String.format` (`%s`, `%d`). Pure formats such as `m:ss` and markup are not text.
- **Texts must wrap.** A sentence in a window must wrap at the window's edge and never run out over it, because translations are longer than English and any line may become too long. Use a wrapping component (a read-only, transparent `JTextArea` with `lineWrap` and `wrapStyleWord`, or an HTML label with a width) and give each component the width of the window and no more (in MigLayout, `growx, wmin 10`). Never rely on a single-line `JLabel` for a sentence. Check a new window with a long text before handing it over.
- Some language files are ISO-8859-1 although `standards.md` says UTF-8 (Java reads both). Add plain ASCII only, or keep the file's encoding.

## Python Conventions

- Type hints required on all public functions (PEP 484)
- Return type `-> None` on void functions
- Keep `py.typed` marker valid; run `mypy`
- `@dataclass(frozen=True)` for data structures
