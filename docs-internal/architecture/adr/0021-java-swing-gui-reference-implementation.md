# ADR-0021: Java Swing as GUI Reference Implementation

**Status:** Accepted  
**Date:** 2026-02-15 (Documenting historical decision)

---

## Context

Tank Royale needs a graphical user interface for battle visualization, server control, and bot management. The GUI should be accessible to users, administrators, and developers without complex installation requirements.

The GUI is **not** intended to be _the_ definitive GUI, but rather a **reference implementation** that demonstrates the Observer and Controller protocol capabilities. Other implementations (web-based, competition-specific, custom visualizers) are explicitly supported and encouraged.

**Problem:** Which GUI framework should be used for the reference implementation bundled with Tank Royale?

---

## Decision

Use **Java Swing** with **MigLayout** for the reference GUI implementation, written in **Kotlin** with extensive use of extension functions.

**Key choices:**

- **Java Swing** — Bundled with standard Java (non-headless), no separate installation required
- **MigLayout** — Powerful constraint-based layout manager with readable string-based syntax
- **Kotlin** — Extension functions, property delegates, and modern language features make Swing development practical
- **AI-assisted development** — Modern AI tools (GitHub Copilot, ChatGPT) excel at generating Swing code, offsetting verbosity concerns

The GUI is explicitly a **reference implementation**, not a mandatory component. Users may develop:
- Web-based GUIs using JavaScript/TypeScript
- Competition-specific displays with custom visualizations
- Streaming overlays for broadcasts
- Alternative desktop GUIs using other frameworks

---

## Rationale

**Why Swing over JavaFX:**

- ✅ **Bundled with standard Java** — No separate installation or dependency management
- ✅ **Corporate/educational friendly** — Students and employees can run Tank Royale without administrator privileges to install JavaFX
- ✅ **Firewall/airgap compatible** — Works behind firewalls without external downloads
- ✅ **Cross-platform support** — Works on Windows, macOS, Linux without platform-specific installers
- ✅ **Mature and stable** — Battle-tested API with extensive documentation
- ❌ **Dated look-and-feel** — Swing appears less modern than JavaFX (mitigated with custom themes)
- ❌ **More verbose** — Requires more boilerplate (mitigated by Kotlin extensions and AI assistance)

**Why Kotlin + extension functions:**

- ✅ **Reduced boilerplate** — Extension functions eliminate repetitive listener registration
- ✅ **Type-safe builders** — DSL-style component construction
- ✅ **Null safety** — Prevents common Swing NPE patterns
- ✅ **Interop with Java** — Seamless integration with Swing APIs

**Why MigLayout:**

- ✅ **Readable constraints** — `"insets 0, fill"` is clearer than nested layout managers
- ✅ **Powerful and flexible** — Handles complex layouts without manual calculation
- ✅ **Cross-platform consistency** — Same layout behavior on all operating systems
- ✅ **Works with visual designers** — JFormDesigner and other tools support MigLayout

**Why AI assistance matters:**

- Modern AI tools generate high-quality Swing code from natural language descriptions
- Offsets traditional criticism of Swing verbosity
- Accelerates development of dialogs, forms, and complex layouts
- Makes Swing competitive with modern frameworks for greenfield development

**Alternatives rejected:**

- **JavaFX:** Requires separate installation alongside Java, problematic in corporate/educational environments and behind firewalls
- **Web-based (Electron, Tauri):** Would require bundling browser engine, increasing artifact size and complexity
- **Native GUI frameworks:** Would require platform-specific builds, violating cross-platform requirement

---

## Consequences

### Positive

- Users can run the GUI immediately after installing Java 11+ (no additional setup)
- Administrators can deploy Tank Royale in restricted environments
- Developers can contribute to GUI using familiar Java ecosystem tools
- AI-assisted development makes Swing productive despite its age
- Reference implementation demonstrates protocol capabilities for alternative GUI developers

### Negative

- Swing's appearance may seem dated compared to modern frameworks
- Limited built-in animation and effects compared to JavaFX
- Custom components require more effort than modern declarative frameworks

### Neutral

- GUI is explicitly a reference implementation — users building custom GUIs can use any framework
- Protocol-driven architecture means GUI replacement is a supported use case

---

## References

- [GUI README](/gui/README.md)
- [ADR-0007: Client Role Separation](./0007-client-role-separation.md) — Observer + Controller protocol
- [ADR-0009: WebSocket Communication Protocol](./0009-websocket-communication-protocol.md)
- [MigLayout Documentation](https://github.com/mikaelgrev/miglayout)
- [JFormDesigner MigLayout Support](https://www.formdev.com/jformdesigner/doc/layouts/miglayout/)

---

## Future Article

The combination of Kotlin extension functions and AI assistance for Swing development may warrant a separate technical article demonstrating modern Swing development practices.

