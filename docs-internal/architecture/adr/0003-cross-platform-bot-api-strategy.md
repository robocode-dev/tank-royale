# ADR-0003: Cross-Platform Bot API Strategy

**Status:** Accepted  
**Date:** 2026-02-11

---

## Context

Tank Royale needs to support multiple programming languages while ensuring fair competition and consistent bot behavior.

**Problem:** How to implement bot APIs across Java, .NET/C#, and Python that maintain behavioral consistency without creating maintenance burden?

**Requirements:**
- No language-specific advantages in competition
- Consistent bot behavior across platforms  
- Minimize API divergence over time
- Fair competition (same game logic regardless of language)

---

## Decision

Implement **symmetric APIs** across all supported languages - identical structure with language-appropriate naming conventions.

**Supported languages:**
- Java (`bot-api/java`)
- .NET/C# (`bot-api/dotnet`) 
- Python (`bot-api/python`)

**Symmetry means:**
- Same class/interface hierarchy
- Identical method signatures (adapted to language conventions)
- Same event model and lifecycle
- Identical WebSocket protocol implementation
- Same environment variables

---

## Rationale

**Why symmetric APIs:**
- ✅ **Fair competition**: No language gives gameplay advantage
- ✅ **Schema-driven**: All APIs map to same protocol schemas
- ✅ **Consistent behavior**: Same game logic across languages
- ✅ **Unified docs**: Tutorial works for all languages

**Architecture pattern (identical across languages):**
```
BaseBot (abstract class)
├── BaseBotInternals (connection management)  
└── WebSocketHandler (language-specific WebSocket library)
```

**API examples:**
```java
// Java - camelCase
public void setTurnRate(double turnRate);
public void onScannedBot(ScannedBotEvent e);
```

```csharp
// C# - PascalCase
public void SetTurnRate(double turnRate);
public virtual void OnScannedBot(ScannedBotEvent e);
```

```python
# Python - snake_case
def set_turn_rate(self, turn_rate: float):
def on_scanned_bot(self, event: ScannedBotEvent):
```

**Alternatives rejected:**
- **JVM + bindings**: JVM dependency too heavy
- **Language-specific APIs**: APIs would diverge, unfair advantages
- **Code generation**: Generated code hard to debug/customize
- **Java-only**: Excludes non-Java developers

---

## Implementation

**Common interface contract:**
```java
// Movement
void setTurnRate(double turnRate);
void setTargetSpeed(double targetSpeed);

// Weapons  
void setGunTurnRate(double gunTurnRate);
void setFire(double firepower);

// Events (same across all languages)
void onScannedBot(ScannedBotEvent e);
void onHitByBullet(HitByBulletEvent e);
void onBulletHit(BulletHitEvent e);
// ... more events
```

**Environment variables (identical):**
```bash
BOT_NAME=MyBot
BOT_VERSION=1.0  
SERVER_URL=ws://localhost:7654
SERVER_SECRET=my-secret
```

---

## Consequences

- ✅ Fair competition across languages
- ✅ Schema-driven design prevents API divergence
- ✅ Single tutorial/documentation works for all
- ✅ Consistent bot behavior regardless of language choice
- ❌ Not fully idiomatic (follows structure over language conventions)
- ❌ Maintenance burden (changes replicated across 3+ codebases)
- ❌ Cannot leverage advanced language-specific features optimally

---

## References

- [Bot API Java](/bot-api/java/)
- [Bot API .NET](/bot-api/dotnet/)
- [Bot API Python](/bot-api/python/)
