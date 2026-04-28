# Coding Conventions

<!-- KEYWORDS: style, conventions, naming, format, Java, Kotlin, Python, C#, PEP 8, immutability, type hints, modern -->

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

## Python Conventions

- Type hints required on all public functions (PEP 484)
- Return type `-> None` on void functions
- Keep `py.typed` marker valid; run `mypy`
- `@dataclass(frozen=True)` for data structures
