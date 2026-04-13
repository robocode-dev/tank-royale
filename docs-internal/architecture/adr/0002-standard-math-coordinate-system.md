# ADR-0002: Standard Mathematical Coordinate System

**Status:** Accepted  
**Date:** 2026-02-14 (Documenting historical decision)

---

## Context

The original Robocode used a non-standard coordinate system: Y-axis inverted (0° = up/north), angles measured clockwise.
This caused confusion for developers familiar with standard mathematics.

**Problem:** Should Tank Royale preserve the original Robocode coordinate system for backward compatibility, or adopt
standard math conventions?

---

## Decision

Use **standard mathematical conventions** for the coordinate system and angles:

- **Y-axis:** Positive = up (standard Cartesian, not screen coordinates)
- **0°:** Points right (east), not up (north)
- **Positive rotation:** Counter-clockwise (mathematical convention)
- **Coordinate origin:** Bottom-left of arena

This is a **deliberate break** from the original Robocode's conventions.

---

## Rationale

- ✅ Matches standard mathematics taught in schools and universities
- ✅ Standard trigonometry functions (`sin`, `cos`, `atan2`) work without mental translation
- ✅ Reduces confusion for new developers
- ✅ Aligns with the educational mission (teaching real math, not game-specific quirks)
- ❌ Incompatible with original Robocode bots (cannot port bot code without angle conversion)
- ❌ Confuses developers experienced with the original Robocode

---

## References

- [Tank Royale vs Original Robocode](/docs-build/docs/articles/tank-royale.md)
- [math.kt](/server/src/main/kotlin/dev/robocode/tankroyale/server/model/math.kt)
