# Contributing Guide

Thank you for your interest in contributing to Robocode Tank Royale. This guide explains how to get involved, what to
expect, and how to make your contributions effective.

### Motivation behind Robocode

Robocode is a spare-time, non-profit project with these goals:

- Provide a [programming game] that’s genuinely fun while helping users develop programming and AI skills.
- Serve education: a platform for learning, with clear goals and obvious ways to benchmark performance.
- Encourage competition: events like [LiteRumble] (for the [original Robocode game](https://robocode.sourceforge.io/))
  are e-sports for programming games and a great way to showcase your skills.
- Make development enjoyable: building Robocode should be a fun way to learn and experiment.

### Guiding principles

Big changes to the core game rules and physics are a no-go.

Why:

- Rules and physics should remain stable. If you want new ones, fork the project and give it a new name.
- Large changes break backward compatibility, making it hard to mix older bots with newer versions.
- Drastic changes frustrate users who invested time mastering subtle details to gain a competitive edge.
- Documentation becomes harder when it must describe multiple behaviors side by side.

Robocode should evolve, but breaking changes must be avoided. Focus on improvements that align with the expectations of
most users. Significant additions are welcome as long as they remain backward compatible.

## Roadmap

See the [roadmap] for upcoming ideas and directions.

Note: There's no timeline or ETA. Robocode is a spare-time project, not a full-time product.

## Developer Requirements

Before you start contributing code to Robocode Tank Royale, please note:

- **Dev Container:** It is strongly recommended to use the provided [devcontainer](.devcontainer) for a pre-configured
  development environment.
  See [Developing inside a Container](https://code.visualstudio.com/docs/devcontainers/containers) for details.
- **JDK Version:** You must use **JDK 17+** to build the project with Gradle.
- **Build Tools:** See the complete list of required tools in [tools.md](docs-build/docs/dev/tools.md).

End users running Robocode only need Java 11 or newer, but developers building from source have different requirements.

## How to contribute

- **Support**
  - Answer questions when you can.
  - Be polite and constructive in discussions and reviews.
  - Participate in conversations: issues, bug reports, and feature requests.

- **Documentation**
  - All docs are Markdown in the repository. Fix typos, clarify wording, add missing content, and open
    a [pull request].
  - Documentation and communication use American English.

- **Bug reports**
  - Report issues you encounter.
  - Before filing, search for existing issues.
  - Include clear steps to reproduce, expected vs. actual behavior, and relevant logs or error messages. Add
    environment details (OS, Java version, etc.) when useful.
  - If you understand the cause, feel free to propose a fix in a [pull request].

- **Feature requests**
  - Start a discussion or issue describing the idea and motivation in detail.
  - Expect healthy debate about acceptance and implementation details.
  - Treat feature requests as wishes, not requirements. Good ideas are likely to be implemented; others may be
    declined.
  - No ETA is guaranteed. If you want it sooner, consider contributing a [pull request] and/or collaborating with
    others.

- **Implementing a feature request**
  - Confirm the feature is accepted or likely to be accepted; when in doubt, contact the [maintainer].
  - Add or update documentation for new behavior.
  - Provide tests where reasonable; manual testing is acceptable when automation isn’t practical.
  - Changes to a [Bot API] are non-trivial: all official APIs (JVM, .NET, web, etc.) must stay in sync, not just one
    platform.

## Regarding pull requests

### Keep PRs buildable and testable

Ensure the code compiles and the application can be run for review. Don’t break existing functionality.

### Provide context

Link to the related issue or discussion. Explain how to verify or test the change. Add code comments where decisions
might be non-obvious. Use `TODO`/`FIXME` where appropriate.

### Prefer small, focused PRs

Avoid “big bang” changes. Don’t mix unrelated work (e.g., a bug fix plus multiple features). Submit separate PRs. For
very large changes, coordinate with the [maintainer] before implementation.

## Regarding Bot APIs

### 1:1 Semantic Equivalence (Hard Requirement)

**All official Bot APIs must be 1:1 semantically equivalent to the Java Bot API**, which is the reference
implementation. This is a non-negotiable requirement for the following reasons:

- **Maintainability**: Cross-platform code must stay familiar and consistent across Java, .NET, and Python.
- **Testing**: Identical semantics allow test strategies and scenarios to be shared across platforms.
- **Documentation**: API documentation can be written once and adapted, not rewritten from scratch.
- **Learning**: Users moving between platforms should find the same concepts, methods, and behaviors.

**Why Java is the reference implementation:**

The classic Robocode API was originally written for Java, and Tank Royale remains loyal to this heritage. While there
are some changes and improvements, the Java Bot API is the most battle-tested implementation and serves as the
authoritative source for all other platform implementations.

**What 1:1 equivalence means:**

- Method names match (adjusted for language conventions: `camelCase` in Java/C#, `snake_case` in Python).
- Method signatures match (same parameters, same return types, same semantics).
- Blocking methods block, non-blocking (setter) methods queue actions for execution on `go()`.
- Event handlers have identical names and receive equivalent event objects.
- Default values, validation rules, and error handling are consistent.

**Allowed language-specific idioms:**

- Python and C# may use **properties** instead of Java's getter/setter methods (e.g., `bot.energy` instead of
  `bot.getEnergy()`).
- Naming conventions follow each language's standards (`camelCase` vs `snake_case` vs `PascalCase`).
- Language-specific constructs (decorators, attributes) may be used if they don't change semantics.

**Not allowed:**

- Different default values across platforms.
- Different method behavior (e.g., async in one API, sync in another).
- Missing methods or extra methods that don't exist in the Java reference.
- Different event firing order or timing.

If you have ideas that deviate from the Java reference, you're welcome to create your own Bot API in a separate
repository. The official Bot APIs prioritize consistency and maintainability over innovation.

### Alternative Bot APIs

You're welcome to create alternative APIs in separate repositories with different paradigms (async, reactive,
functional, etc.). We can link to them as community options. The official APIs will remain 1:1 with Java.

## Booters, GUIs, and servers

### Alternative booter/GUI

Alternative booters or GUIs are welcome in separate repositories. Please link them from this project.

### Server

The official server is the heart of the game; drop-in replacements are a no-go. You can, however, build services on top
of it (e.g., controllers for scheduling, ranking, analytics) in a separate repository.

## Tools used for building all modules of Robocode

[Here](docs-build/docs/dev/tools.md) you can find a list of all the tools required for building all parts of Robocode.

[programming game]: https://www.makeuseof.com/tag/best-programming-games/

[LiteRumble]: https://literumble.appspot.com/

[pull request]: https://github.com/robocode-dev/tank-royale/pulls

[Bot API]: https://robocode-dev.github.io/tank-royale/api/apis.html

[maintainer]: https://github.com/flemming-n-larsen "Mr. Robocode"

[roadmap]: https://github.com/robocode-dev/tank-royale/wiki/Roadmap

[original game]: https://robocode.sourceforge.io/
