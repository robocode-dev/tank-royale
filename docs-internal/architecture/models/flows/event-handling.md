# Event Handling Flow

This document explains how events (like `WonRoundEvent`, `ScannedBotEvent`, etc.) are generated, queued, dispatched, and handled in Tank Royale Bot APIs across Java, Python, and C#.

## Overview

**System:** Distributed multi-process event system  
**Platforms:** Java (reference), Python, C# (.NET)  
**Key Concept:** Events are generated server-side, transmitted via WebSocket, queued client-side, and dispatched to bot handlers  
**Critical Issue:** Output from event handlers must be explicitly captured and transferred

---

## Event Lifecycle (Complete Flow)

```mermaid
sequenceDiagram
    participant Server
    participant BotProcess as Bot Process<br/>(JVM/Python/.NET)
    participant WebSocket as WebSocket<br/>Handler Thread
    participant EventQueue as Event<br/>Queue
    participant BotThread as Bot Thread<br/>(run()/go())
    participant GUI as GUI Console

    Note over Server: Phase 1: Event Generation (Server)
    Server->>Server: 1. Detect event condition<br/>(e.g., bot won round)
    Server->>Server: 2. Create event object<br/>(WonRoundEvent)
    Server->>Server: 3. Add to final tick
    
    Note over Server,BotProcess: Phase 2: Transmission (WebSocket)
    Server->>WebSocket: 4. Send TickEventForBot<br/>{events: [WonRoundEvent]}
    Note over WebSocket: WebSocket thread<br/>(async)
    
    Note over EventQueue: Phase 3: Queueing (Client)
    WebSocket->>EventQueue: 5. addEventsFromTick()<br/>(WonRoundEvent queued)
    
    Note over BotThread: Phase 4: Dispatch at Turn Start
    BotThread->>EventQueue: 6. go() → execute() → dispatchEvents()
    EventQueue->>BotThread: 7. Fire onWonRound()<br/>(event handler invoked)
    BotThread->>BotThread: 8. System.out.println()<br/>(output captured)
    
    Note over WebSocket: Phase 5: Output Transfer (Round End)
    WebSocket->>BotThread: 9. handleRoundEnded()<br/>dispatchEvents()
    BotThread->>BotThread: 10. onWonRound() fires<br/>(prints to buffer)
    BotThread->>BotThread: 11. transferStdOutToBotIntent()
    BotThread->>BotThread: 12. Update BotIntent with stdout
    WebSocket->>Server: 13. Send BotIntent<br/>{stdOut: "I won!"}
    
    Note over GUI: Phase 6: Display (GUI)
    Server->>GUI: 14. Forward output to console
    GUI->>GUI: 15. Display in bot console panel
```

---

## Event Generation (Server-Side)

### Where Events Are Created

Events originate from server physics/battle logic:

```mermaid
graph TD
    A["Battle Server<br/>Game Loop"] -->|Detect Condition| B{Event Type?}
    B -->|Bot Defeated| C["Create<br/>DeathEvent"]
    B -->|Bot Scanned| D["Create<br/>ScannedBotEvent"]
    B -->|Round Won| E["Create<br/>WonRoundEvent"]
    B -->|Bullet Hit| F["Create<br/>BulletHitEvent"]
    
    C --> G["Add to Tick's<br/>Event List"]
    D --> G
    E --> G
    F --> G
    
    G -->|Final Tick| H["TickEventForBot<br/>Message"]
    H -->|WebSocket| I["Bot Process<br/>Receives"]
```

### Example: WonRoundEvent Generation

```json
{
  "type": "WonRoundEvent",
  "turnNumber": 127
}
```

This is embedded in the final tick:

```json
{
  "type": "TickEventForBot",
  "roundNumber": 1,
  "turnNumber": 127,
  "botState": { ... },
  "bulletStates": [ ... ],
  "events": [
    {
      "type": "WonRoundEvent",
      "turnNumber": 127
    }
  ]
}
```

---

## Client-Side Event Queue Management

### Event Queue Architecture

```mermaid
graph LR
    A["WebSocket<br/>Message"] -->|Parse| B["EventMapper<br/>Converts to<br/>Bot Event"]
    B --> C["EventQueue<br/>In-Memory Buffer"]
    C -->|Priority Sort| D["Event<br/>Dispatcher"]
    D --> E["Bot Event<br/>Handler<br/>onWonRound"]
    E -->|Output| F["RecordingPrintStream<br/>Buffer"]
```

### Queue Implementation (All Platforms)

| Component | Java | Python | C# |
|-----------|------|--------|-----|
| Recording | `RecordingPrintStream` | `RecordingTextWriter` | `RecordingTextWriter` |
| Queue | `EventQueue` (synchronized) | `EventQueue` (threadsafe) | `EventQueue` (locked) |
| Dispatch | `dispatchEvents()` | `dispatch_events()` | `DispatchEvents()` |
| Transfer | `transferStdOutToBotIntent()` | `_transfer_std_out_to_bot_intent()` | `TransferStdOutToBotIntent()` |

---

## Event Dispatch Flow (Per Turn)

### Standard Turn Dispatch

```mermaid
sequenceDiagram
    participant BotThread as Bot Thread
    participant EventQueue as Event Queue
    participant Handler as Event Handler
    participant Recording as RecordingPrintStream
    
    BotThread->>BotThread: go() called
    BotThread->>EventQueue: dispatchEvents(turnNumber)
    
    loop For each queued event (by priority)
        EventQueue->>EventQueue: peekNextEvent()
        EventQueue->>EventQueue: Check: not too old?<br/>Check: priority >= current?
        
        opt Event is ready to dispatch
            EventQueue->>Recording: Clear previous output
            EventQueue->>Handler: fire(event)
            Handler->>Recording: System.out.println()
            Recording->>Recording: Store in ByteArrayOutputStream
        end
    end
    
    EventQueue-->>BotThread: All events dispatched
    BotThread->>BotThread: execute() → sendIntent()
    BotThread->>Recording: readNext()
    Recording-->>BotThread: Captured output
    BotThread->>BotThread: botIntent.setStdOut(output)
    BotThread->>WebSocket: Send BotIntent
```

---

## Critical: Round End Event Dispatch

### The Problem (Before Fix)

```mermaid
graph TD
    A["Final Tick<br/>Received<br/>WonRoundEvent queued"] --> B["Bot's go()<br/>Loop Iteration"]
    B --> C["execute()"]
    C --> D["sendIntent()<br/>STDOUT TRANSFERRED HERE"]
    D --> E["waitForNextTurn()"]
    E --> F["Server Sends<br/>RoundEndedEvent"]
    F --> G["handleRoundEnded()"]
    G --> H["dispatchEvents()"]
    H --> I["onWonRound() FIRES<br/>Prints to buffer"]
    I --> J["❌ sendIntent()<br/>Already Called!"]
    J --> K["Captured Output<br/>LOST"]
```

### The Solution (After Fix)

```mermaid
graph TD
    A["Final Tick<br/>Received<br/>WonRoundEvent queued"] --> B["Bot's go()<br/>Loop Iteration"]
    B --> C["execute()"]
    C --> D["sendIntent()"]
    D --> E["waitForNextTurn()"]
    E --> F["Server Sends<br/>RoundEndedEvent"]
    F --> G["handleRoundEnded()"]
    G --> H["dispatchEvents()"]
    H --> I["onWonRound() FIRES<br/>Prints to buffer"]
    I --> J["✅ transferStdOutToBotIntent()"]
    J --> K["Captured Output<br/>TRANSFERRED"]
    K --> L["Sent with Final<br/>BotIntent"]
    L --> M["GUI Displays<br/>Output"]
```

### Implementation (All 3 Platforms)

**Java:**
```java
private void handleRoundEnded(JsonObject jsonMsg) {
    ...
    baseBotInternals.dispatchEvents(turnNumber);        // Fire onWonRound
    baseBotInternals.transferStdOutToBotIntent();       // ✅ Transfer captured output
    ...
}
```

**Python:**
```python
async def handle_round_ended(self, json_msg: Dict[Any, Any]):
    ...
    self.event_queue.dispatch_events(turn_number)       # Fire on_won_round
    self._transfer_std_out_to_bot_intent()             # ✅ Transfer captured output
    ...
```

**C#:**
```csharp
private void HandleRoundEnded(string json)
{
    ...
    DispatchEvents(turnNumber);                        // Fire OnWonRound
    TransferStdOutToBotIntent();                       // ✅ Transfer captured output
    ...
}
```

---

## Comparison: Tank Royale vs Classic Robocode

### Classic Robocode (Single Process)

```mermaid
sequenceDiagram
    participant EventManager
    participant Robot as Robot<br/>Event Handler
    participant ConsoleGUI as GUI<br/>Console
    
    EventManager->>Robot: dispatch(onWinEvent)
    Robot->>Robot: robot.out.println()
    Robot-->>ConsoleGUI: Direct stdout access
    ConsoleGUI->>ConsoleGUI: Display immediately
    
    Note over EventManager,ConsoleGUI: ✅ No buffering needed<br/>✅ Output appears immediately<br/>✅ Same JVM process
```

### Tank Royale (Multi-Process)

```mermaid
sequenceDiagram
    participant WebSocketThread as WebSocket<br/>Thread<br/>(Server)
    participant BotProcess as Bot Process<br/>(Separate JVM)
    participant RecordingStream as Recording<br/>Stream
    participant Server as Server
    participant GUI as GUI
    
    WebSocketThread->>BotProcess: handleRoundEnded()
    BotProcess->>BotProcess: dispatchEvents()
    BotProcess->>BotProcess: onWonRound() fires
    BotProcess->>RecordingStream: println()
    RecordingStream->>RecordingStream: Store in buffer
    BotProcess->>RecordingStream: readNext()
    RecordingStream-->>BotProcess: Return captured output
    BotProcess->>BotProcess: botIntent.stdOut = output
    BotProcess->>Server: Send BotIntent<br/>(WebSocket)
    Server->>GUI: Forward stdOut
    GUI->>GUI: Display in console
    
    Note over WebSocketThread,GUI: ✅ Output captured<br/>✅ Sent via WebSocket<br/>✅ Displayed in GUI<br/>✅ Cross-process compatible
```

### Behavioral Parity

| Aspect | Classic | Tank Royale | Match? |
|--------|---------|-------------|--------|
| Event fires | ✅ onWinEvent | ✅ onWonRound | ✅ Yes |
| Output captured | ✅ Direct stdout | ✅ RecordingStream | ✅ Yes |
| Output location | GUI console | GUI console | ✅ Yes |
| Consistency | 100% | 100% (after fix) | ✅ Yes |
| Timing | Synchronous | Async via WebSocket | ⚠️ Different mechanism |

---

## Event Priority System

All events have numeric priorities (0-100). Higher priority events interrupt lower priority ones:

### Standard Priorities

```mermaid
graph TB
    A["Event Priority<br/>Hierarchy"] --> B["99: ScannedBotEvent"]
    A --> C["80: DeathEvent"]
    A --> D["75: WonRoundEvent"]
    A --> E["60: BulletMissedEvent"]
    A --> F["50: HitWallEvent"]
    A --> G["40: HitRobotEvent"]
    A --> H["30: BulletHitEvent"]
    A --> I["0: Custom Events"]
    
    J["Critical Events"] -.->|Always delivered| K["Even if bot stops"]
    J -->|Examples:| L["DeathEvent,<br/>GameEndedEvent"]
```

### Interrupt Behavior

When a higher-priority event fires during a lower-priority handler:

```mermaid
sequenceDiagram
    participant EventQueue
    participant Handler99 as ScannedBot Handler
    participant Handler30 as BulletHit Handler
    
    Handler99->>Handler99: Executing onScannedBot()
    EventQueue->>EventQueue: Higher priority event ready?
    alt Higher priority found
        EventQueue->>Handler99: Throw ThreadInterruptedException
        Handler99-->>EventQueue: Exit handler
        EventQueue->>Handler30: Dispatch BulletHitEvent
        Handler30->>Handler30: onBulletHit() runs
    else No higher priority
        Handler99->>Handler99: Continue normally
    end
```

---

## Output Capture Mechanism

### RecordingPrintStream (Java)

```java
public class RecordingPrintStream extends PrintStream {
    private final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    private final PrintStream printStream = new PrintStream(byteArrayOutputStream);

    @Override
    public void write(byte[] buffer, int offset, int length) {
        synchronized (this) {
            super.write(buffer, offset, length);           // Write to original stdout
            printStream.write(buffer, offset, length);     // Also record in memory
        }
    }

    public String readNext() {
        synchronized (this) {
            String output = byteArrayOutputStream.toString(UTF_8);
            byteArrayOutputStream.reset();                 // Clear for next transfer
            return output;
        }
    }
}
```

### RecordingTextWriter (Python/C#)

Similar pattern:
1. Writes go to **both** original stdout AND in-memory buffer
2. `read_next()` / `ReadNext()` returns and clears buffer
3. Thread-safe synchronization prevents race conditions

---

## Event Queue Lifecycle

### Full Lifecycle Diagram

```mermaid
graph LR
    A["Event Created<br/>(Server)"] -->|1. Include in Tick| B["TickEventForBot<br/>WebSocket Message"]
    B -->|2. Transmit| C["Received by<br/>WebSocket Handler"]
    C -->|3. Parse & Map| D["Bot Event Object<br/>(WonRoundEvent)"]
    D -->|4. Queue| E["EventQueue<br/>In-Memory Buffer"]
    E -->|5. Sort by Priority| F["Ordered Queue<br/>High → Low"]
    F -->|6. Dispatch| G["Bot Event Handler<br/>onWonRound"]
    G -->|7. Execute Handler| H["User Code Runs<br/>System.out.println"]
    H -->|8. Capture| I["RecordingStream<br/>Buffer"]
    I -->|9. Transfer| J["BotIntent Message"]
    J -->|10. Send| K["WebSocket to Server"]
    K -->|11. Forward| L["GUI Console<br/>Display"]
```

---

## Troubleshooting Guide

### Symptom: Event handler not called

**Check:** Is event in the final tick?
```json
{
  "type": "TickEventForBot",
  "events": [
    { "type": "WonRoundEvent" }  // ← Check this is present
  ]
}
```

**Check:** Is EventQueue receiving the event?
```java
EventQueue.addEvent(wonRoundEvent);  // Verify called
```

**Check:** Is dispatchEvents() being called after round ends?
```java
// Should be called in handleRoundEnded()
baseBotInternals.dispatchEvents(turnNumber);
```

### Symptom: Output missing from GUI

**Before Fix:** Output was lost because `transferStdOutToBotIntent()` wasn't called after event handlers fired.

**After Fix:** Output should be transferred in `handleRoundEnded()`:
```java
dispatchEvents(turnNumber);                    // Fire handlers
transferStdOutToBotIntent();                   // ← Transfer their output
```

### Symptom: Output appears in terminal but not GUI

**Cause:** Terminal shows raw stdout (inherited from bot process), but GUI receives output via WebSocket BotIntent.

**Solution:** Ensure `transferStdOutToBotIntent()` captures RecordingStream buffer and includes it in BotIntent.

---

## Related Documentation

- **[Turn Execution Flow](./turn-execution.md)** — Per-turn game loop (when events are dispatched)
- **[Battle Lifecycle Flow](./battle-lifecycle.md)** — When round ends and events fire
- **[Bot Connection Flow](./bot-connection.md)** — Event handler setup during initialization
- **[Message Schema: Events](../message-schema/events.md)** — Event message contracts
- **[ADR-0011: Realtime Game Loop](../../adr/0011-realtime-game-loop-architecture.md)** — Design decisions about event timing
- **[ADR-0012: Turn Timing Semantics](../../adr/0012-turn-timing-semantics.md)** — Exact timing semantics

---

## Key Takeaways

1. **Events are generated server-side** and transmitted as part of TickEventForBot
2. **Client-side queue** handles ordering, priority, and interruption logic
3. **Output is buffered** by RecordingPrintStream/RecordingTextWriter
4. **Transfer happens per-turn** in `sendIntent()` during normal turns
5. **Round-end requires special handling** — events fire after last `sendIntent()`
6. **Fix ensures consistency** across Java, Python, and C#

---

**Last Updated:** 2026-02-28  
**Status:** ✅ Complete (includes WonRoundEvent stdout capture fix)
