# ADR-0015: Two-Tier Shared-Secret Authentication

**Status:** Accepted  
**Date:** 2026-02-14 (Documenting historical decision)

---

## Context

Multiple client types connect to the server. Bots and controllers/observers have different trust levels.

**Problem:** How to authenticate clients and prevent unauthorized access?

---

## Decision

Use **shared-secret authentication** with two separate secret pools:

- **Bot secrets** — for bot clients
- **Controller secrets** — shared by controllers and observers

Combined with **session ID binding**: server generates a UUID session ID on WebSocket connect, client must echo it in
their handshake. Security is **opt-in** — if no secrets configured, authentication is disabled.

---

## Rationale

- ✅ Simple to implement across all languages
- ✅ Two trust tiers (bots can't control games, controllers can't impersonate bots)
- ✅ Session binding prevents replay/hijack attacks
- ✅ Opt-in allows easy local development (no auth needed)
- ❌ Secrets sent in plaintext (relies on wss:// for encryption)
- ❌ No per-user identity (shared secrets, not individual credentials)

---

## References

- [ClientWebSocketsHandler.kt](/server/src/main/kotlin/dev/robocode/tankroyale/server/connection/ClientWebSocketsHandler.kt)
- [ADR-0006: Session ID for Bot Process Identification](./0006-session-id-bot-process-identification.md)
- [ADR-0010: Client Role Separation](./0010-client-role-separation.md)
