---
id: CAP-006-design
type: design
status: draft
links: [CAP-006]
title: Design notes for CAP-006 (protocol)
provenance: inferred
reversal-cost: low
---

# CAP-006 design

## Team-message batches

Each Bot API validates its pending messages before enqueueing. Ordinary messages count as one logical payload; a batch counts each contained value. The server validates the complete compact UTF-8 `teamMessages` array before it merges an intent into the turn, then schedules each packet for delivery on the next turn. A batch remains one packet and one recipient event, with its values decoded in send order.

The protocol allows 64 packets, 128 logical payloads, 49,152 UTF-8 bytes per encoded packet, and 262,144 UTF-8 bytes for the compact array per bot per turn. Bot handshakes advertise batch protocol version 1; batch packets are accepted only when every intended recipient supports version 1. The schema and wire shape are documented in [Intent Messages](../../architecture/models/message-schema/intents.md).
