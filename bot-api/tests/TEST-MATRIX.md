### Tank Royale Bot API — Cross-language Test Matrix

This document is the single source of truth for the public Bot API test plan. It defines a parity-first,
language-agnostic matrix of semantic tests that must be implemented identically across Java, .NET, and Python.

#### Principles

- Java is the reference implementation for semantics, naming, defaults, and timing.
- Tests must be 1‑1 in intent and naming across languages (allowing idiomatic differences only).
- Public API stability: avoid breaking changes. Keep protocol/wire behavior identical.
- Prefer testing via public behavior. For complex internals, use language-appropriate mechanisms sparingly.

#### Frameworks

- Java: JUnit 5
- .NET: NUnit
- Python: pytest

#### Naming and IDs

- Canonical Test IDs: `TR-API-<AREA>-<NNN>` (e.g., `TR-API-VAL-001`). Use sub-IDs when defined (e.g.,
  `TR-API-BOT-001a`).
- Organize tests by subject or scenario, not by ID:
    - Keep files/classes named after the subject under test (e.g., `BotInfoTest`, `InitialPositionTest`) or a
      cross-cutting scenario (e.g., `BaseBotConstructorTest`).
    - It is expected and encouraged that a single `XxxTest` file contains multiple matrix IDs when they relate to the
      same subject/scenario.
- Cross-language mirroring (names should align across Java/.NET/Python idioms):
    - Java: file/class `XxxTest.java`; methods `test_<id>_<short_description>()`.
    - .NET (NUnit): file/class `XxxTest.cs`; methods `Test_<id>_<ShortDescription>()`.
    - Python (pytest): file/module `test_xxx.py`; functions `test_<id>_<short_description>()`.
- Embed the canonical ID and title via language-appropriate metadata, so reports display the matrix ID clearly:
    - Java (JUnit 5): add `@DisplayName("TR-API-<ID> <short description>")` on each test method; optionally
      `@Tag("<AREA>")` and `@Tag("TR-API-<ID>")` for filtering.
    - .NET (NUnit): add `[Description("TR-API-<ID> <short description>")]` on each test method; optionally
      `[Category("<AREA>")]` and `[Category("TR-API-<ID>")]`.
    - Python (pytest): put a docstring whose first line is `TR-API-<ID> <short description>` on each test function;
      optionally use markers for filtering (e.g., `@pytest.mark.VAL`).
- Sub-IDs and parameterization:
    - Group sub-IDs (`001a`, `001b`, …) for the same umbrella requirement in the same scenario file/class (e.g.,
      `BaseBotConstructorTest`).
    - If one ID has multiple data variations, use parameterized tests so the ID maps to a single method with multiple
      cases.

##### Example: BotInfoTest across languages (VAL)

Java (JUnit 5):

```java
class BotInfoTest {

    @Test
    @DisplayName("TR-API-VAL-001 BotInfo required fields")
    @Tag("VAL")
    @Tag("TR-API-VAL-001")
    void test_TR_API_VAL_001_required_fields() {
        // Arrange/Act/Assert
    }

    @Test
    @DisplayName("TR-API-VAL-002 BotInfo validation: invalid fields raise/throw")
    @Tag("VAL")
    @Tag("TR-API-VAL-002")
    void test_TR_API_VAL_002_invalid_fields_validation() {
        // Arrange/Act/Assert
    }
}
```

.NET (NUnit):

```csharp
[TestFixture]
public class BotInfoTest {
    [Test]
    [Category("VAL")]
    [Category("TR-API-VAL-001")]
    [Description("TR-API-VAL-001 BotInfo required fields")]
    public void Test_TR_API_VAL_001_Required_Fields() {
        // Arrange/Act/Assert
    }

    [Test]
    [Category("VAL")]
    [Category("TR-API-VAL-002")]
    [Description("TR-API-VAL-002 BotInfo validation: invalid fields raise/throw")]
    public void Test_TR_API_VAL_002_Invalid_Fields_Validation() {
        // Arrange/Act/Assert
    }
}
```

Python (pytest):

```python
def test_TR_API_VAL_001_required_fields():
    """TR-API-VAL-001 BotInfo required fields"""
    # arrange/act/assert


def test_TR_API_VAL_002_invalid_fields_validation():
    """TR-API-VAL-002 BotInfo validation: invalid fields raise/throw"""
    # arrange/act/assert
```

Notes:

- Keep tests for the same subject (`BotInfo`) together; add additional `TR-API-VAL-00x` methods as needed in the same
  file.
- For umbrella items like `TR-API-BOT-001a`..`001e`, place them in a scenario-focused file like
  `BaseBotConstructorTest` (one method per sub-ID).

---

### A. Value objects and constants (VAL)

- ✅ TR-API-VAL-001 BotInfo required fields: constructing with minimal valid data sets defaults correctly; getters return
  exact values.
- ✅ TR-API-VAL-002 BotInfo validation: invalid fields (e.g., negative energy, invalid name) raise/throw as per Java
  reference semantics.
- ✅ TR-API-VAL-003 InitialPosition defaults: unspecified fields default to spec (e.g., random vs fixed optional fields);
  mapping consistency.
- ✅ TR-API-VAL-004 InitialPosition mapping round-trip: `InitialPositionMapper` round-trips to/from schema JSON 1:1.
- ✅ TR-API-VAL-005 Constants integrity: named constants exist and do not change unexpectedly (e.g., battlefield defaults,
  max/min values).
- ✅ TR-API-VAL-006 Graphics Color: constructors, from-hex, to-hex, clamping, equality/hash.
- ✅ TR-API-VAL-007 Graphics Point: creation, equality/hash, basic vector ops if exposed.

### B. Utilities (UTL)

- ✅ TR-API-UTL-001 ColorUtil conversions: RGB↔HSV (and others if present) within tolerance; invalid input handling.
- ✅ TR-API-UTL-002 JsonUtil serialization: canonical formatting for API DTOs; schema compliance for all message types used
  by the API.
- ✅ TR-API-UTL-003 CountryCode utility: detection/normalization per ISO list; error handling.

### C. Graphics API (GFX)

- ✅ TR-API-GFX-001 SvgGraphics path basics: move/line/curve commands produce expected SVG fragments.
- ✅ TR-API-GFX-002 SvgGraphics styles: stroke/fill/alpha apply as expected; state resets between shapes if defined by API.
- TR-API-GFX-003 SvgGraphics text/images (if applicable): correct attributes and escaping.
- TR-API-GFX-004 IGraphics contract: drawing primitives map to the same resulting SVG across languages for identical
  sequences.

### D. Bot lifecycle and configuration (BOT)

- TR-API-BOT-001 BaseBot constructor (umbrella): configuration via environment variables and (Java-only) System
  properties; explicit args where applicable.
    - TR-API-BOT-001a ENV read & defaults: with required vars present, constructor reads values; when absent, defaults (
      server URL/port) are applied per Java reference. [Parity: Java/.NET/Python]
    - TR-API-BOT-001b ENV validation: missing/invalid values produce clear errors/exceptions identical to Java
      semantics (e.g., non-numeric or out-of-range ports, empty/whitespace-only values). [Parity: Java/.NET/Python]
    - TR-API-BOT-001c Precedence: explicit args > Java System properties > ENV (confirm against Java reference;
      .NET/Python verify explicit args > ENV). When multiple sources define the same key, the higher-precedence source
      wins. [Parity: Java (full chain), .NET/Python (no Java properties)]
    - TR-API-BOT-001d Type parsing/normalization: ints/bools parsed consistently; trimming/whitespace handling and case
      normalization where applicable; deterministic error messages. [Parity: Java/.NET/Python]
    - TR-API-BOT-001e Java System properties facet (Java-only): `-D` properties mirror ENV keys with the same
      defaults/validation and precedence relative to ENV. [Parity: Java only]
- TR-API-BOT-002 Connect/Disconnect: bot opens and closes connection; handshake messages are valid per schema.
- TR-API-BOT-003 Start/Stop/Pause/Resume: correct state flags, idempotence, and event firing order.
- TR-API-BOT-004 Error handling: on protocol error or server disconnect, bot transitions to safe state and surfaces
  error appropriately.

### E. Commands (CMD)

- TR-API-CMD-001 Movement commands: setting target speed/turn rates updates the next tick’s intent payload exactly as
  per spec.
- TR-API-CMD-002 Fire command: power bounds enforced; resulting intent serializes correctly; cooldown respected if
  applicable.
- TR-API-CMD-003 Radar/Scan commands: rescan/locking behavior produces expected intents/events.
- TR-API-CMD-004 Graphics frame emission: drawings are emitted in the tick where added and cleared/reset according to
  API semantics.

### F. Events (EVT)

- TR-API-EVT-001 GameStartedEvent: populated fields match handshake/game setup; timing at round 1, tick 1.
- TR-API-EVT-002 RoundStartedEvent: round counter increments, initial state matches spec.
- TR-API-EVT-003 ScannedBotEvent: geometry fields (bearing, distance, heading) computed consistently from world state.
- TR-API-EVT-004 SkippedTurnEvent: triggered when bot exceeds time budget; state unaffected except documented fields.
- TR-API-EVT-005 Event interruption semantics: handler interruption rules match Java reference and do not deadlock.

### G. Ticks and state (TCK)

- TR-API-TCK-001 Tick progression: per-tick state snapshots are immutable/read-only to consumer.
- TR-API-TCK-002 Intent apply/reset: intents apply for the next tick and reset according to spec.
- TR-API-TCK-003 Timeouts: command submission after deadline triggers skipped turn and no partial effects.

### H. Protocol and schema (PRT)

- TR-API-PRT-001 All outgoing messages validate against `/schema` examples.
- TR-API-PRT-002 All incoming messages parse to API DTOs and back to JSON losslessly for known fields.
- TR-API-PRT-003 Backward compatibility: unknown fields are ignored/preserved in pass-through where applicable.

### I. Internal large methods (INT)

- TR-API-INT-001 Internal mapping helpers: heavy mappers/validators (non-public) validated via reflection or public
  effects.
- TR-API-INT-002 Event dispatch loop: large private/protected dispatch logic validated with controlled inputs and
  spy/observer hooks.

---

### Parity execution plan

1) Establish spec and matrix (this file). ✅
2) Build/refine test scaffolding (MockedServer & intent capture). ✅
3) Implement VAL/UTL/GFX. (✅)
4) Lifecycle & protocol.
5) Command & tick semantics.
6) Events.
7) Internals if needed.
8) Keep names/IDs synchronized.
9) Run per language.
10) Track coverage.

### Run commands

- Java: `./gradlew :bot-api:java:test`
- .NET: `./gradlew :bot-api:dotnet:test`
- Python: `./gradlew :bot-api:python:test`

### Cross-language scaffolding requirements

- Java/.NET: `MockedServer` must serve handshake, inject events, and capture intents; extend if needed.
- Python: add a simple mock server/harness mirroring Java/.NET behavior so scenarios can run identically.
