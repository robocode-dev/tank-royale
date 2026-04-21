# Event Handling Flow

This document explains how events (like `WonRoundEvent`, `ScannedBotEvent`, etc.) are generated, queued, dispatched, and handled in Tank Royale Bot APIs across Java, Python, C# (.NET), and TypeScript.

## Overview

**System:** Distributed multi-process event system  
**Platforms:** Java (reference), Python, C# (.NET), TypeScript  
**Key Concept:** Events are generated server-side, transmitted via WebSocket, queued client-side, and dispatched to bot handlers  
**Output Handling:** Handler output (print statements, log messages) is automatically captured and transmitted to the GUI console

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

Events are dispatched **after the previous tick's wait completes, before `run()` continues** — ensuring bot code always reads state that already matches the events that just fired. This matches Classic Robocode semantics. See [Event Dispatch Timing Semantics](#event-dispatch-timing-semantics) below for the full story.

There are two execution paths depending on whether the bot uses a managed thread (`Bot`) or drives the loop itself (`BaseBot`):

#### Path A — `Bot` (managed thread, `run()` loop)

```mermaid
sequenceDiagram
    participant WS as WebSocket Thread
    participant EQ as Event Queue
    participant BT as Bot Thread
    participant Recording as RecordingPrintStream

    Note over BT: blocked inside execute(N-1) → waitForNextTurn(N-1)

    WS->>EQ: addEventsFromTick(tick N)  [TickEvent(N), ScannedBotEvent(N), ...]
    WS->>WS: setTickEvent(tick N)
    WS->>BT: notifyAll()

    Note over BT: wakes — still inside execute(N-1)
    BT->>EQ: dispatchEvents(N)   ← fired INSIDE execute(), before it returns

    loop For each queued event (by priority)
        EQ->>EQ: Check: not too old? priority >= current?
        opt Ready
            EQ->>Recording: Clear previous output
            EQ->>BT: fire(event) → onTick / onScannedBot / etc.
            BT->>Recording: System.out.println()
        end
    end

    Note over BT: execute(N-1) returns → go() returns
    Note over BT: run() reads state (events for N already fired ✓)
    BT->>BT: run() logic → go() → execute(N)
    BT->>BT: sendIntent(N)
    BT->>Recording: readNext()
    Recording-->>BT: Captured output
    BT->>BT: botIntent.setStdOut(output)
    BT->>WS: Send BotIntent
    Note over BT: waitForNextTurn(N) — blocks until tick N+1
```

#### Path B — `BaseBot` (no managed thread, user calls `go()`)

```mermaid
sequenceDiagram
    participant WS as WebSocket Thread
    participant EQ as Event Queue
    participant User as User Code
    participant Recording as RecordingPrintStream

    WS->>EQ: addEventsFromTick(tick N)
    WS->>WS: setTickEvent(tick N)

    User->>User: go() called (e.g. from onTick handler)
    User->>EQ: dispatchEvents(N)   ← in go(), before execute()

    loop For each queued event (by priority)
        EQ->>User: fire(event) → onTick / onScannedBot / etc.
        User->>Recording: System.out.println()
    end

    User->>User: execute(N) → sendIntent(N)
    User->>Recording: readNext()
    Recording-->>User: Captured output
    User->>User: botIntent.setStdOut(output)
    User->>WS: Send BotIntent
    Note over User: execute() returns immediately (no managed thread)
```

---

## Event Dispatch Timing Semantics

This section explains *when* events fire relative to bot state, why Tank Royale requires extra care, and how the implementation matches Classic Robocode behavior.

### Classic Robocode — Reference Model

Classic Robocode uses a two-thread model where the **Battle Thread owns the game loop and blocks until the robot finishes its turn**. This guarantees that the robot always wakes to process exactly the turn it was woken for — there is no race.

```mermaid
sequenceDiagram
    participant BT as Battle Thread
    participant RQ as RobotPeer EventQueue
    participant RT as Robot Thread

    Note over RT: sleeping in waitForNextTurn()

    Note over BT: ── Turn N ──
    BT->>BT: currentTime = N
    BT->>BT: loadCommands()   [reads robot's intent from turn N-1]
    BT->>BT: performMove()    [moves all robots]
    BT->>BT: performScan()    [radar sweeps enemy at turn N]
    BT->>RQ: addEvent(ScannedRobotEvent(time=N))
    BT->>RT: wakeupRobots()   [notifyAll on isSleeping]
    BT->>BT: waitSleeping()   [BLOCKS — cannot advance until robot sleeps again]

    Note over RT: wakes inside executeImpl()
    RT->>RQ: readoutEvents() → [ScannedRobotEvent(time=N), ...]
    RT->>RT: EventManager.processEvents()
    RT->>RT: onScannedRobot fires  [getTurnNumber() == N ✓]
    RT->>RT: bot reacts: setFire(), setTurnRadarLeft(), etc.
    RT->>RT: go() → executeImpl() → stores new commands
    RT->>BT: waitForNextTurn()  [signals isSleeping=true, unblocks Battle Thread]

    Note over BT: ── Turn N+1 ──
    BT->>BT: loadCommands()  [reads bot's reaction from turn N ✓]
```

**Key constraint:** `waitSleeping()` on the Battle Thread means turn N+1 **cannot start** until the robot finishes turn N. The robot always processes exactly the turn it was woken for.

---

### Tank Royale — The Race Condition (pre-fix)

Tank Royale has no equivalent lock. The server sends ticks independently via WebSocket. If the bot thread is delayed by the OS scheduler, the WebSocket thread may have already delivered tick N+1 by the time the bot calls `go()`.

```mermaid
sequenceDiagram
    participant S  as Server
    participant WS as WebSocket Thread
    participant EQ as EventQueue
    participant BT as Bot Thread

    Note over BT: sleeping in waitForNextTurn(1)

    Note over S: ── Server Turn 2 ──
    S->>WS: TickEvent(2, [ScannedBotEvent(2)])
    WS->>EQ: addEventsFromTick → TickEvent(2) + ScannedBotEvent(2)
    WS->>WS: setTickEvent(tick 2)
    WS->>BT: notifyAll()

    Note over BT: wakes — but OS scheduling delay...

    Note over S: ── Server Turn 3 ──
    S->>WS: TickEvent(3, [])
    WS->>EQ: addEventsFromTick → TickEvent(3)
    WS->>WS: setTickEvent(tick 3)  ⚠ tickEvent is now 3
    WS->>BT: notifyAll()

    Note over BT: FINALLY runs → go()
    BT->>BT: currentTick = getCurrentTickOrNull() → returns tick 3 ⚠ WRONG TICK
    BT->>EQ: dispatchEvents(3)
    EQ->>BT: TickEvent(3) → onTick(3) "Tick 3"
    EQ->>BT: ScannedBotEvent(2) — 2≤3 ✓ → onScannedBot fires at turn 3 ✗
```

**Why Classic Robocode doesn't have this:** `waitSleeping()` prevents turn N+1 from starting until the robot finishes turn N. Tank Royale has no such coordination — server and bot run fully independently.

---

### Tank Royale — Fixed Flow (post-fix, matches Classic Robocode semantics)

The fix captures the turn number **from the newly arrived tick inside `execute()`**, not from `go()`. Events fire before `execute()` returns, so `run()` always reads state that already has the corresponding events fired.

```mermaid
sequenceDiagram
    participant S  as Server
    participant WS as WebSocket Thread
    participant EQ as EventQueue
    participant BT as Bot Thread

    Note over BT: blocked in waitForNextTurn(N-1) inside execute(N-1)

    Note over S: ── Server Turn N ──
    S->>S: performScan() → ScannedBotEvent(turnNumber=N)
    S->>WS: TickEvent(turnNumber=N, events=[ScannedBotEvent(N)])

    WS->>EQ: addEventsFromTick → TickEvent(N) + ScannedBotEvent(N)
    WS->>WS: setTickEvent(tick N)
    WS->>BT: notifyAll()

    Note over BT: wakes — still inside execute(N-1)
    BT->>EQ: dispatchEvents(N)  ← turn number from newly arrived tick — no race
    EQ->>BT: TickEvent(N, pri=130) → onTick(N) fires
    EQ->>BT: ScannedBotEvent(N, pri=20) → onScannedBot(N) fires  [turnNumber=N ✓]
    Note over BT: execute(N-1) returns → go() returns
    Note over BT: run() reads radar/gun state = tick N ✓  (events already fired)
    BT->>BT: run() reacts: setFire(), setTurnRadarLeft(), etc.
    BT->>BT: go() → dispatchEvents(N) [no-op] → execute(N) → sendIntent(N)
    BT->>BT: waitForNextTurn(N)
```

### Timing comparison

| Aspect | Classic Robocode | Tank Royale (fixed) |
|--------|-----------------|---------------------|
| Events fire | Before bot reads new state ✓ | Before bot reads new state ✓ |
| Turn number in handler | Always correct ✓ | Always correct ✓ |
| Race on tick advance | Impossible (waitSleeping blocks) | Eliminated (turn# from tick, not go()) |
| `ScannedBotEvent(N)` visible in `run()` at turn N | ✓ | ✓ |

> **Investigation log:** Full root cause analysis (theses A–D, measurement data, bug #210 log reconstruction) is in [`/scan-event-timing.md`](/scan-event-timing.md) at the repository root.

---

## Critical: Round End Event Dispatch

### How Round End Events Work

At the end of a round, the server sends a `RoundEndedEventForBot` message. This triggers the client to:

1. **Dispatch any queued events** that arrived in the final tick (e.g., `WonRoundEvent`)
2. **Transfer captured output** from those event handlers
3. **Notify the bot** that the round has ended

This two-step process (dispatch + output transfer) ensures that print statements and logs from event handlers appear in the GUI console.

```mermaid
graph TD
    A["Final Tick<br/>Received<br/>WonRoundEvent queued"] --> B["Bot's go()<br/>Loop Iteration"]
    B --> C["execute()"]
    C --> D["sendIntent()"]
    D --> E["waitForNextTick()"]
    E --> F["Server Sends<br/>RoundEndedEvent"]
    F --> G["handleRoundEnded()"]
    G --> H["dispatchEvents()"]
    H --> I["onWonRound() FIRES<br/>Output captured"]
    I --> J["transferStdOutToBotIntent()"]
    J --> K["Output included in<br/>Final BotIntent"]
    K --> L["GUI Displays<br/>Handler Output"]
```

### Implementation (All 3 Platforms)

**Java:**
```java
private void handleRoundEnded(JsonObject jsonMsg) {
    ...
    baseBotInternals.dispatchEvents(turnNumber);        // Fire onWonRound
    baseBotInternals.transferStdOutToBotIntent();       // Transfer captured output
    ...
}
```

**Python:**
```python
async def handle_round_ended(self, json_msg: Dict[Any, Any]):
    ...
    self.event_queue.dispatch_events(turn_number)       # Fire on_won_round
    self._transfer_std_out_to_bot_intent()             # Transfer captured output
    ...
```

**C#:**
```csharp
private void HandleRoundEnded(string json)
{
    ...
    DispatchEvents(turnNumber);                        // Fire OnWonRound
    TransferStdOutToBotIntent();                       // Transfer captured output
    ...
}
```

---

## Comparison: Tank Royale vs Classic Robocode

> **Note:** This section covers **output capture** semantics (how `System.out.println()` reaches the GUI).  
> For **event dispatch timing** (when events fire relative to turn state), see [Event Dispatch Timing Semantics](#event-dispatch-timing-semantics) above.

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

## Round Start Initialization (v0.40.1 / v0.40.2)

### Correct Event Ordering for Round Start

When `RoundStartedEvent` is received by the bot, **internal handlers must fire before bot-facing handlers**. This ensures internal state is fresh before any user code runs.

```mermaid
sequenceDiagram
    participant WebSocket as WebSocket Thread
    participant Internal as Internal Handlers<br/>(P100)
    participant BotHandlers as Bot Handlers<br/>(user code)
    participant BotThread as Bot Thread

    Note over WebSocket: RoundStartedEvent received

    WebSocket->>Internal: 1. internalEventHandlers.onRoundStarted()
    Internal->>Internal: Reset tickEvent = null<br/>(stale state from previous round cleared)
    Internal->>BotThread: Start / restart bot thread<br/>(pre-warm: ready before first tick)

    WebSocket->>BotHandlers: 2. botEventHandlers.onRoundStarted()
    BotHandlers->>BotHandlers: User onRoundStarted() fires<br/>(internal state already fresh)
```

**Why ordering matters:** If the bot handler fired first, user code could call `go()` or inspect state while `tickEvent` still holds the stale last-tick from the previous round — bypassing `waitUntilFirstTickArrived()` and potentially sending an intent for the wrong round.

### Pre-Warm Thread and First-Tick Intent (Fix for Issue #202)

```mermaid
sequenceDiagram
    participant Internal as Internal Handler
    participant BotThread as Bot Thread
    participant WebSocket as WebSocket Thread
    participant Server

    Internal->>BotThread: Start thread (pre-warm on round start)
    Note over BotThread: Blocks in waitUntilFirstTickArrived()

    Server->>WebSocket: tick-event-for-bot (turn 1)
    WebSocket->>BotThread: tickEvent set, notifyAll()

    Note over BotThread: Unblocks

    BotThread->>Server: sendIntent() ← default intent sent BEFORE bot.run()
    Note over BotThread: Ensures turn 1 intent arrives even under<br/>OS scheduling pressure (e.g., 50ms+ delay)

    BotThread->>BotThread: bot.run() called
    loop Each turn
        BotThread->>BotThread: go() → dispatchEvents() → user logic
        BotThread->>Server: sendIntent()
    end
```

See [Turn Execution Flow: Turn 1 Initialization](./turn-execution.md#turn-1-initialization-pre-warm-thread) for the complete sequence.

---

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

**Cause:** Output from event handlers must be explicitly transferred to the GUI via `transferStdOutToBotIntent()`.

**Solution:** Verify that `handleRoundEnded()` calls both:
```java
dispatchEvents(turnNumber);                    // Fire handlers (output captured)
transferStdOutToBotIntent();                   // Transfer their output to BotIntent
```

Both calls must happen in sequence for handler output to appear in the GUI.

### Symptom: Output appears in terminal but not GUI

**Cause:** Terminal shows raw stdout (inherited from bot process), but GUI receives output via WebSocket BotIntent messages.

**Solution:** Ensure `transferStdOutToBotIntent()` captures the RecordingStream buffer and includes it in BotIntent before the round ends.

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
5. **Round-end has special handling** — events fire after last `sendIntent()`, so output must be explicitly transferred
6. **Platform implementations are consistent** across Java, Python, C#, and TypeScript
7. **Internal handlers fire before bot handlers** for `onRoundStarted` — ensures `tickEvent = null` is reset before user code runs
8. **Default intent is sent before `run()`** on round start — prevents turn-1 skip under OS scheduling pressure (fix for issue #202)

---

**Last Updated:** 2026-05-11
