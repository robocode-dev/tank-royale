# Code Stack — Layer 1: Universal Principles

These principles are **always active** for any source code file, regardless of language, framework, or project context. They represent the foundational rules of good software that apply to every code review. No configuration or context detection is needed — if source code is being reviewed, these principles apply.

Layer 1 draws from Kent Beck's four rules of simple design (except "reveals intention", which is truly universal across all artifact types), core SOLID principles, essential security hygiene, and fundamental readability standards.

Note: `SIMPLE-DESIGN-REVEALS-INTENTION`, `CODE-CS-DRY`, `CODE-CS-KISS`, `CODE-CS-YAGNI`, `CODE-DX-NAMING`, and `ARCH-DECISION-RECORDS` are activated by the universal section of `artifact-types.yaml` and apply to all stacks, not just code.

| ID | Title | Summary |
|----|-------|---------|
| SOLID-SRP | Single Responsibility Principle | A module, class, or function should have one, and only one, reason to change. |
| GOF-COMPOSITION-OVER-INHERITANCE | Favor Composition over Inheritance | Prefer assembling behavior from small, focused components rather than extending base classes. |
| GOF-PROGRAM-TO-INTERFACE | Program to an Interface, Not an Implementation | Depend on abstractions rather than concrete classes to reduce coupling. |
| SIMPLE-DESIGN-PASSES-TESTS | Passes all tests | Code must pass all its tests; correctness is the first rule of simple design. |
| SIMPLE-DESIGN-NO-DUPLICATION | No duplication | Every piece of knowledge should have a single, unambiguous representation in the system. |
| SIMPLE-DESIGN-FEWEST-ELEMENTS | Fewest elements | After passing tests, revealing intention, and removing duplication, remove anything that remains unnecessary. |
| CODE-SEC-VALIDATE-INPUT | Validate input at system boundaries | Never trust data crossing a trust boundary; validate type, format, range, and length at every entry point. |
| CODE-CS-WET | WET: Write Every Time | Avoid abstractions that only hide duplication temporarily; duplication should be removed deliberately, not obscured. |
| CODE-CS-NIH | NIH: Not Invented Here | Challenge custom solutions when a well-understood existing approach would solve the problem more reliably. |
| CODE-CS-NO-SILVER-BULLET | No Silver Bullet | Avoid treating any tool, pattern, or framework as a universal cure for software complexity. |
| CODE-CS-CQS | CQS: Command-Query Separation | Keep operations that change state separate from operations that only return information. |
| CODE-CS-BOY-SCOUT | The Boy Scout Rule | Leave the code a little cleaner than you found it whenever you touch it. |
| CODE-CS-BROKEN-WINDOWS | Broken Windows | Fix visible design and maintenance problems early so they do not normalize further decay. |
| CODE-CS-POSTELS-LAW | Postel's Law | Be conservative in what you send and explicit about what you accept at system boundaries. |
| CODE-CS-HYRUMS-LAW | Hyrum's Law | Assume consumers will depend on any observable behavior, not just the behavior you intended to promise. |
| ARCH-CONWAYS-LAW | Conway's Law | Consider how team and communication boundaries shape software structure and integration seams. |
| CODE-DX-SMALL-FUNCTIONS | Keep functions small and single-purpose | Functions should do one thing, do it well, and be small enough to understand at a glance. |
| CODE-DX-DELETE-DEAD-CODE | Delete dead code | Remove code that is no longer executed or referenced; it adds noise and misleads readers. |
| CODE-CS-FAIL-FAST | Fail fast, fail loudly | Detect errors as early as possible and report them clearly; never silently swallow failures. |
