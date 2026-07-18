---
id: ARCH-021
type: architecture
status: draft
links: [ADR-0009, CAP-006]
title: WebSocket Protocol — Design Specification
provenance: inferred
---

# WebSocket Protocol — Design Specification

This document provides the concrete design and specification details for the WebSocket-based communication protocol used between the Tank Royale server and its clients (bots, observers, controllers). It complements ADR-0009 and is the canonical place for flows, examples, and implementation-oriented material.

## Overview

- Transport: WebSocket (RFC 6455)
- Endpoint: `ws://localhost:7654`
- Message format: JSON, validated against repository schemas
- Full-duplex: server pushes events; clients send intents

## Connection and Messaging Flow

```
Bot → Server: WebSocket Connect
Server → Bot: server-handshake {sessionId}
Bot → Server: bot-handshake {sessionId, name}
Server → Bot: game-started-event
Loop: Server → Bot: tick-event, Bot → Server: bot-intent
```

## Example Messages

```json
// Server → Bot
{
  "type": "tick-event-for-bot",
  "turnNumber": 42,
  "botState": {
    
  },
  "events": [
    
  ]
}

// Bot → Server
{
  "type": "bot-intent",
  "turnRate": 5,
  "targetSpeed": 8,
  "firepower": 3
}
```

## References

- WebSocket RFC 6455: https://tools.ietf.org/html/rfc6455
- Message Schemas: `/schema/schemas/README.md` (raw YAML schema files)
- Protocol Flows: `/docs/architecture/models/flows/README.md` — sequence diagrams for protocol flows
