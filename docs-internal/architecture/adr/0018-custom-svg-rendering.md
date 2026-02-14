# ADR-0018: Custom SVG Rendering for Bot API Graphics

**Status:** Accepted  
**Date:** 2026-02-14 (Documenting historical decision)

---

## Context

Bot APIs across all platforms need a graphics/drawing API for debug visualization overlays. The API must be identical
across Java, .NET, and Python per the symmetric API requirement.

**Problem:** How to provide consistent drawing/graphics APIs across all Bot API platforms?

---

## Decision

Implement a **custom SVG-based graphics API** in each Bot API rather than wrapping platform-specific graphics libraries.

Bots build SVG strings using a cross-platform API. The SVG is transmitted to the server via the bot intent and rendered
by the GUI. This ensures all platforms produce identical output.

---

## Rationale

- ✅ SVG is a universal standard — same output regardless of platform
- ✅ Enables 1:1 identical API across Java, .NET, Python (no platform-specific dependencies)
- ✅ Transmitted as text through existing bot-intent protocol (no binary encoding)
- ✅ GUI renders SVG natively (JSVG library)
- ❌ Custom implementation to maintain per platform
- ❌ Limited to SVG capabilities (no platform-native hardware acceleration)

**Alternatives rejected:**

- **Platform-native graphics (AWT, System.Drawing, PIL):** APIs would diverge, violating symmetric API requirement
- **Third-party SVG libraries per platform:** Different APIs, different SVG output, hard to keep 1:1

---

## References

- [ADR-0002: Cross-Platform Bot API Strategy](./0002-cross-platform-bot-api-strategy.md)
- [ADR-0007: Java as Authoritative Reference](./0007-java-reference-implementation.md)
