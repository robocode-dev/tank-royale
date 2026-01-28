# Coding Conventions

<!-- METADATA: ~60 lines, ~600 tokens -->
<!-- KEYWORDS: style, conventions, naming, format, Java, Kotlin, Python, C#, PEP 8, immutability, type hints, modern -->

## Modern Approach (All Languages)

**Always use modern language features and APIs:**

- **Java 11+**: Use `Path.of()`, `Files.*`, `var`, `Optional`, `Stream` API, `HttpClient`, text blocks
- **Kotlin**: Use `kotlin.io.path.*` extensions, `runCatching`, `Result`, scope functions, `sealed class`, coroutines where appropriate
- **Python 3.10+**: Use `match` statements, `|` union types, `@dataclass`, `pathlib.Path`, type hints, f-strings

**Prefer:**
- NIO.2 `Path` API over legacy `java.io.File`
- Early returns over nested conditionals
- Immutable data structures
- Functional transformations over imperative loops

## Java Conventions

**Follow existing project style:**

- Use `final` for immutability wherever possible
- Clear, descriptive names (no abbreviations unless obvious)
- No magic numbers - use named constants
- Explicit nullability annotations (`@Nullable`, `@NotNull`)
- Defensive programming - validate inputs
- Prefer immutable objects and collections

**Example:**

```java
public final class BotState {
    private final int energy;
    private final double x;

    public BotState(final int energy, final double x) {
        if (energy < 0) throw new IllegalArgumentException("Energy cannot be negative");
        this.energy = energy;
        this.x = x;
    }
}
```

## Python Conventions

**Follow PEP 8 + PEP 484:**

- Type hints required for all public functions
- Keep `py.typed` marker valid
- Run `mypy` for type checking
- Prefer `@dataclass` for data structures
- Read-only state objects (use `frozen=True`)

**Example:**

```python
from dataclasses import dataclass
from typing import Final

@dataclass(frozen=True)
class BotState:
    energy: int
    x: float
    
    def __post_init__(self) -> None:
        if self.energy < 0:
            raise ValueError("Energy cannot be negative")
```

## General Conventions

**Universal principles across all languages:**

- **SRP** (Single Responsibility Principle)
- **DRY** (Don't Repeat Yourself)
- **YAGNI** (You Aren't Gonna Need It)
- Composition over inheritance
- Pure functions when possible
- Early returns for guard clauses
- Precise, actionable error messages
