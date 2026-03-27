# Copilot Instructions

**FIRST ACTION:** Follow this sequence before starting any task:

1. Read `/.ai/README.md` (metadata index & AI learning loop)
2. Read `/.ai/MAINTENANCE.md` (how to maintain AI instructions)
3. Read `/AGENTS.md` (routing hub for task-specific files)
4. Based on task keywords, load relevant `/.ai/*.md` files
5. Start the task

All AI instructions — governance, git workflow, cross-platform rules, conventions, testing, documentation, standards, architecture, and planning — live in `/.ai/` and are routed through `/AGENTS.md`.

<!-- .principles: begin — compiled by /scout v0.7.0, 2026-03-27 -->
# Code Principles — AI Coding Guidelines

When writing or reviewing code, follow the layered principle system below.

## Layer 1 — Always Active

- INFRA-NO-SECRETS-IN-CODE: Inject secrets at runtime from a secrets manager; never store credentials in code, config files, or logs.
- CODE-DX-NAMING: Every name must reveal intent; never use a name that requires a comment to explain it.
- CODE-DX-SMALL-FUNCTIONS: Keep functions small, doing one thing only; extract additional responsibilities into named functions.
- CODE-DX-CODE-FOR-READERS: Write code to be read by humans first; prioritize clarity over cleverness in every choice.
- CODE-CS-DRY: Every piece of knowledge must have a single, authoritative representation in the system.
- CODE-CS-KISS: Choose the simplest solution that correctly solves the problem; never add unnecessary complexity.
- CODE-CS-FAIL-FAST: Detect errors as early as possible and report them clearly; never silently continue in a broken state.
- SOLID-SRP: Each module or class must have exactly one reason to change, serving a single stakeholder or concern.
- SOLID-OCP: Add new behavior by writing new code that implements an abstraction; never modify existing proven code.
- SOLID-LSP: Subtypes must be fully substitutable for their base type without requiring special-case logic in callers.
- SOLID-ISP: Split fat interfaces into focused role interfaces so each client depends only on methods it actually uses.
- SOLID-DIP: High-level modules must depend on abstractions, never on concrete low-level implementations.
- EFFECTIVE-JAVA-PREFER-DEPENDENCY-INJECTION: Pass dependencies into constructors rather than hardwiring them as singletons or static utilities.
- EFFECTIVE-JAVA-OVERRIDE-EQUALS-CONTRACT: Override `equals` only when fulfilling the full reflexive-symmetric-transitive contract, and always override `hashCode` too.
- FP-IMMUTABILITY: Prefer immutable data structures; produce new values on transformation and make mutation the explicit exception.
- FP-AVOID-SHARED-MUTABLE-STATE: Eliminate shared mutable state; transform data through pipelines to prevent race conditions and action-at-a-distance bugs.
- DOC-AS-CODE: Store documentation in version control alongside code and apply the same review and CI workflow.
- DOC-CLOSE-TO-CODE: Store documentation in the same directory as the artifact it describes to prevent drift.
- DOC-UNIQUE: Maintain a single canonical source for every piece of content and link to it rather than duplicating.
- DOC-SCANNABLE: Use descriptive headings, short paragraphs, and bold anchors so readers can locate information by scanning.
- DOC-OBJECTIVE: Use only factual, demonstrable claims and omit marketing adjectives like "easy", "simple", or "powerful".
- DOC-PURPOSE: Assign each document exactly one purpose—tutorial, how-to, reference, or explanation—and never mix them.
- DOC-MINIMAL: Omit speculative, derivable, or duplicated content; every sentence must earn its place by serving the reader.
- ARCH-DECISION-RECORDS: Capture every significant architectural decision as an ADR recording context, decision, and consequences.
- SCHEMA-SELF-DESCRIBING: Every field, enum value, and method must be described clearly enough to understand without external documentation.
- SCHEMA-FIELD-OPTIONALITY: Every new schema field must be optional with a sensible default; never add required fields to published schemas.
- SCHEMA-NO-POLYMORPHIC-BLOBS: Never use untyped blobs or Any fields; define an explicit, validated schema for every structured payload.
- SCHEMA-ENUM-EVOLUTION: Every enum must include an UNSPECIFIED sentinel and all consumers must handle unknown values without crashing.
- CD-TRUNK-BASED-DEVELOPMENT: Integrate to the main branch at least daily; use feature flags to hide incomplete work instead of branches.
- CD-KEEP-BUILD-GREEN: Treat a failing main branch build as the team's highest priority; nothing ships until it passes.
- CD-FAST-FEEDBACK-LOOPS: Structure the pipeline to return a pass or fail signal to the developer within ten minutes.
- CD-PIPELINE-AS-CODE: Commit every CI/CD pipeline step to version control and review it like application code.
- CD-BUILD-ONCE-DEPLOY-MANY: Build the artifact once and promote that same immutable artifact through every environment.
- CD-SEMANTIC-VERSIONING: Use MAJOR.MINOR.PATCH version numbers to communicate the scope and safety of changes to consumers.

## Layer 2 — This Stack (@java + @kotlin + @typescript + @python + @csharp + @docs + @cd + @schema)

- CODE-DX-REDUCE-COGNITIVE-LOAD: Structure code so developers hold as few concepts in working memory as possible at any point.
- CODE-CS-YAGNI: Never add functionality until it is actually needed; implement only what current requirements demand.
- CODE-CC-PREFER-IMMUTABLE: Design shared objects to be immutable after construction so they require no synchronization to be thread-safe.
- GOF-COMPOSITION-OVER-INHERITANCE: Favour object composition over inheritance to keep classes focused and enable behaviour changes at runtime.
- GOF-PROGRAM-TO-INTERFACE: Declare all variables and parameters against abstract interfaces, never against concrete implementation types.
- CODE-TP-MAKE-ILLEGAL-STATES-UNREPRESENTABLE: Design types so that invalid domain states cannot be constructed, not merely discouraged by convention.
- CODE-TP-EXHAUSTIVE-PATTERN-MATCHING: Use exhaustive pattern matching so the compiler enforces that every variant of a type is handled.
- CODE-TP-PREFER-SUM-TYPES: Model multi-variant values as sum types, not booleans, strings, or integer codes.
- CODE-CC-SYNC-SHARED-STATE: Guard every access to shared mutable data with a consistent synchronization mechanism.
- CODE-CC-SAFE-PUBLICATION: Never let `this` escape a constructor; publish object references to other threads only after construction completes.
- CODE-CC-HIGHER-LEVEL-CONCURRENCY: Use high-level concurrency utilities instead of raw wait/notify to avoid subtle synchronization bugs.
- CODE-CC-DOCUMENT-THREAD-SAFETY: Document every class's thread-safety guarantees explicitly as part of its public contract.
- CODE-CC-AVOID-LOCKS-IN-HOT-PATHS: Hold locks only for the minimum time needed; never perform I/O or long operations inside a critical section.
- CODE-CC-TASK-BASED-CONCURRENCY: Express concurrent work as tasks submitted to an executor; never manage threads directly.
- CODE-CC-STRUCTURED-CONCURRENCY: Scope every concurrent task to a parent that awaits completion, propagates cancellation, and surfaces child errors.
- EFFECTIVE-JAVA-STATIC-FACTORY: Prefer static factory methods over constructors for named, cached, or polymorphic instance creation.
- EFFECTIVE-JAVA-BUILDER: Use the Builder pattern when constructors or factories require many parameters, especially optional ones.
- EFFECTIVE-JAVA-MINIMIZE-MUTABILITY: Make classes immutable by default: prevent extension, make fields final, and return new instances from operations.
- EFFECTIVE-JAVA-MINIMIZE-ACCESSIBILITY: Declare every class and member as inaccessible as possible to enforce encapsulation and hide implementation details.
- EFFECTIVE-JAVA-INTERFACES-FOR-TYPES: Use interfaces to define behavioral types; never use them solely as containers for constants.
- EFFECTIVE-JAVA-PREFER-CLASS-HIERARCHIES: Replace tagged classes that branch on a type field with a proper abstract class hierarchy.
- EFFECTIVE-JAVA-DESIGN-METHOD-SIGNATURES: Keep parameter lists to four or fewer, use enums instead of booleans, and choose method names carefully.
- EFFECTIVE-JAVA-RETURN-EMPTY-COLLECTIONS: Return empty collections or zero-length arrays instead of `null` to eliminate mandatory null checks in callers.
- EFFECTIVE-JAVA-WRITE-DOC-COMMENTS: Document every exported API element with a Javadoc comment covering contract, parameters, return value, and exceptions.
- EFFECTIVE-JAVA-ELIMINATE-UNCHECKED-WARNINGS: Eliminate all unchecked warnings; suppress only when type-safety is proven and suppression is scoped as narrowly as possible.
- EFFECTIVE-JAVA-DESIGN-FOR-INHERITANCE: Document self-use of overridable methods and mark classes `final` unless explicitly designed for inheritance.
- EFFECTIVE-JAVA-PREFER-INTERFACES: Define types with interfaces rather than abstract classes; use abstract classes only for skeletal implementations.
- EFFECTIVE-JAVA-DESIGN-INTERFACES-FOR-POSTERITY: Audit every default method added to an existing interface against all known implementations before release.
- CODE-TP-TYPE-STATE-MACHINES: Encode distinct object states as separate types so invalid state transitions become compile-time errors.
- FP-ALGEBRAIC-DATA-TYPES: Model domain concepts with product and sum types to make invalid states unrepresentable at compile time.
- FP-PATTERN-MATCHING: Use exhaustive pattern matching on algebraic data types to eliminate missing-case bugs at compile time.
- FP-HIGHER-ORDER-FUNCTIONS: Treat functions as first-class values and abstract control structures via higher-order functions to eliminate boilerplate.
- FP-FUNCTION-COMPOSITION: Build complex behaviour by composing small, single-purpose functions where each output feeds the next input.
- FP-OPTION-EITHER-TYPES: Use Option and Either types to make absence and failure explicit in signatures, forcing exhaustive handling by callers.
- FP-TAIL-CALL-OPTIMISATION: Use tail-recursive calls or trampolining to achieve O(1) stack space for deep recursive computations.
- FP-FUNCTIONAL-CORE-IMPERATIVE-SHELL: Put all business logic in a pure functional core and confine I/O and side effects to a thin imperative shell.
- CODE-TP-BRANDED-TYPES: Wrap primitive values in distinct types so the compiler rejects mismatched domain concepts at compile time.
- CODE-SMELLS-LONG-METHOD: Extract logically distinct blocks from long methods into well-named, single-purpose methods.
- CODE-SMELLS-LARGE-CLASS: Split classes with too many responsibilities into focused, single-purpose classes.
- CODE-API-BACKWARD-COMPATIBILITY: Only add to published APIs; never remove, rename, or change fields without introducing a new version.
- DOC-TASK-ORIENTED: Structure documentation around user goals expressed as tasks, not around internal product components.
- DOC-SELF-CONTAINED: Make every page intelligible in isolation by defining prerequisites and context without assuming prior reading.
- DOC-ADDRESSABLE: Give every meaningful document section a stable, direct link using consistent, non-duplicate headings.
- DOC-AUDIENCE: Write every document for a specific reader with a known background and a concrete goal in mind.
- DOC-ACCURACY: Keep documentation synchronized with the system; never let it describe behavior that no longer exists.
- DOC-EXAMPLES: Accompany every abstract description with a concrete, runnable example that readers can try immediately.
- DOC-PROGRESSIVE-DISCLOSURE: Lead with the simplest case and layer increasing complexity so readers can stop when they have enough.
- CD-DEPLOY-ON-EVERY-COMMIT: Automate the pipeline so any green commit can be deployed to production without manual intervention.
- CD-FEATURE-FLAGS: Decouple deployment from release by hiding incomplete features behind runtime flags, not branches.
- CD-GITOPS: Declare all infrastructure state in git and let an automated operator continuously reconcile the live environment.
- CD-BLUE-GREEN-DEPLOYMENT: Deploy to an idle environment first, run smoke tests, then switch traffic; keep the previous environment for instant rollback.
- CD-CANARY-RELEASE: Route a small fraction of traffic to the new version and roll back automatically if metrics degrade.
- CD-DEPLOYMENT-SMOKE-TESTS: Run automated smoke tests after every deployment to verify core functionality before routing real traffic.

## Layer 3 — Risk-Elevated (server/)

- CODE-PF-PROFILE-FIRST: Always profile to identify actual bottlenecks before making any performance optimization.
- CODE-PF-MEASURE-REAL-OVERHEAD: — (not in catalog)
- CODE-PF-PREDICTABLE-LATENCY: Eliminate lock contention and GC pressure in hot paths to ensure consistent, predictable response times.
<!-- .principles: end -->

<!-- .principles: begin -->
# Code Principles — AI Coding Guidelines

When writing or reviewing code, follow the layered principle system below.

## Layer 1 — Always Active

Non-negotiable fundamentals that apply to every line of code: single responsibility, no duplication, reveal intention, fail fast, validate input, delete dead code.

## Layer 2 — Context-Dependent

Additional principles activated by what you're building. Covers API design, concurrency, domain modeling, testing, cloud-native, and infrastructure patterns.

## Layer 3 — Risk-Elevated

Extra scrutiny for high-risk areas where mistakes are costly or hard to reverse: authentication, financial transactions, personal data (PII), public APIs, performance-critical paths, and distributed systems.
<!-- .principles: end -->
