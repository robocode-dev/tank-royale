# WonRoundEvent Output Capture Bug - Complete Investigation & Fix

**Date:** 2026-02-28  
**Status:** ✅ Fixed  
**Affected Platforms:** Java, C#  
**Also Improved:** Python (consistency)

---

## Executive Summary

A subtle but critical bug in Tank Royale's Bot API prevented event handler output (specifically `onWonRound()` / `on_won_round()` / `OnWonRound()`) from appearing in the GUI console. 

The root cause was an **architectural timing mismatch** in how stdout is captured and transferred in a distributed, multi-process system. The fix ensures that output from event handlers firing at round end is explicitly captured and included in the final BotIntent message before the bot process shuts down.

---

## The Bug

### Symptoms

- **Java Bots:** `onWonRound()` prints to stdout, but messages rarely/never appear in GUI console
- **C# Bots:** Same behavior as Java
- **Python Bots:** Works correctly (by accident, due to async timing)
- **Message Count:** 2 of 10 messages per bot over 10 rounds (80% loss rate)
- **Terminal Output:** Messages sometimes appear in terminal but not in GUI

### Root Cause

Tank Royale's stdout capture mechanism relies on `RecordingPrintStream`/`RecordingTextWriter` objects that:

1. Intercept all stdout writes
2. Store output in an in-memory buffer
3. Transfer buffer to `BotIntent` message when `sendIntent()` is called

**The Problem:** `sendIntent()` is called during the bot's normal turn loop, **before** the round-end event handlers fire. By the time `onWonRound()` executes and prints output, the buffer has already been transferred. The new output sits in the buffer indefinitely because:

- No further `sendIntent()` call happens
- Round ends
- Bot process terminates
- Output is lost

```
Broken Timeline:
================
Final Tick (contains WonRoundEvent) received
    ↓
go() called
    ↓
execute() → sendIntent() [transfer stdout HERE] ← Old stdout transferred
    ↓
waitForNextTurn()
    ↓
Server sends RoundEndedEventForBot
    ↓
handleRoundEnded() → dispatchEvents() → onWonRound() [prints NEW stdout] ← New output buffered
    ↓
❌ No more sendIntent() calls
    ↓
Round ends, bot terminates
    ↓
❌ New stdout lost forever
```

---

## The Fix

### Solution: Explicit Output Transfer at Round End

Call `transferStdOutToBotIntent()` **after** `dispatchEvents()` in `handleRoundEnded()`:

```
Fixed Timeline:
================
Final Tick (contains WonRoundEvent) received
    ↓
go() called
    ↓
execute() → sendIntent() [transfer old stdout]
    ↓
waitForNextTurn()
    ↓
Server sends RoundEndedEventForBot
    ↓
handleRoundEnded():
    dispatchEvents() → onWonRound() [prints NEW stdout] ← New output buffered
    transferStdOutToBotIntent()  [✅ Transfer new stdout] ← NEW FIX
    ✅ All output captured and will be sent
    ↓
Round ends cleanly
```

### Code Changes

**Java** (`bot-api/java/src/main/java/dev/robocode/tankroyale/botapi/internal/WebSocketHandler.java`):
```java
private void handleRoundEnded(JsonObject jsonMsg) {
    var roundEndedEvent = JsonConverter.fromJson(jsonMsg, RoundEndedEvent.class);
    var mappedRoundEndedEvent = new RoundEndedEvent(
        roundEndedEvent.getRoundNumber(), 
        roundEndedEvent.getTurnNumber(), 
        roundEndedEvent.getResults());

    // Dispatch any queued events (e.g. WonRoundEvent from the last tick)
    baseBotInternals.dispatchEvents(mappedRoundEndedEvent.getTurnNumber());

    // ✅ NEW: Transfer any remaining stdout/stderr from event handlers
    baseBotInternals.transferStdOutToBotIntent();

    botEventHandlers.onRoundEnded.publish(mappedRoundEndedEvent);
    internalEventHandlers.onRoundEnded.publish(mappedRoundEndedEvent);
}
```

**Python** (`bot-api/python/src/robocode_tank_royale/bot_api/internal/websocket_handler.py`):
```python
async def handle_round_ended(self, json_msg: Dict[Any, Any]):
    schema_evt: RoundEndedEventForBot = from_json(json_msg)
    results = ResultsMapper.map(schema_evt.results)
    round_ended_event = RoundEndedEvent(
        schema_evt.round_number, schema_evt.turn_number, results
    )
    # Dispatch any queued events
    self.event_queue.dispatch_events(schema_evt.turn_number)
    
    # ✅ NEW: Transfer any remaining stdout/stderr from event handlers
    self._transfer_std_out_to_bot_intent()
    
    self.bot_event_handlers.on_round_ended.publish(round_ended_event)
    self.internal_event_handlers.on_round_ended.publish(round_ended_event)
```

**C#** (`bot-api/dotnet/api/src/internal/BaseBotInternals.cs`):
```csharp
private void HandleRoundEnded(string json)
{
    var roundEndedEventForBot = JsonConverter.FromJson<S.RoundEndedEventForBot>(json);
    var botResults = ResultsMapper.Map(roundEndedEventForBot.Results);
    var mappedRoundEndedEvent = new E.RoundEndedEvent(
        roundEndedEventForBot.RoundNumber,
        roundEndedEventForBot.TurnNumber, 
        botResults);

    // Dispatch any queued events
    DispatchEvents(mappedRoundEndedEvent.TurnNumber);

    // ✅ NEW: Transfer any remaining stdout/stderr from event handlers
    TransferStdOutToBotIntent();

    BotEventHandlers.OnRoundEnded.Publish(mappedRoundEndedEvent);
    InternalEventHandlers.OnRoundEnded.Publish(mappedRoundEndedEvent);
}
```

---

## Why This Works

The fix ensures that:

1. **Event handlers fire** in correct sequence (via `dispatchEvents()`)
2. **Output is captured** in the RecordingStream buffer
3. **Buffer is explicitly transferred** to BotIntent before round ends
4. **BotIntent contains all output** when sent via WebSocket
5. **GUI receives complete output** via server forwarding

---

## Verification & Testing

### Test Cases Added

Modified RamFire sample bots (Java, Python, C#) with:

```java
@Override
public void onWonRound(WonRoundEvent e) {
    System.out.println("I won the round!");
}
```

**Expected Behavior:**
- Message prints 10/10 times over 10 rounds (100% consistency)
- Appears in both terminal and GUI console
- Same behavior across all three platforms

### Test Results

✅ **Java:** All print statements from `onWonRound()` now appear in GUI  
✅ **C#:** All print statements from `OnWonRound()` now appear in GUI  
✅ **Python:** Behavior improved for consistency  

---

## Comparison with Classic Robocode

### Classic Robocode (Single JVM)

In classic Robocode, events fire in the same process:

```
onWinEvent fires
    ↓
robot.out.println() 
    ↓
Direct access to ConsoleGUI
    ↓
Output appears immediately
```

**No buffering problem** because everything runs in the same JVM with direct stdout access.

### Tank Royale (Multi-Process)

Bot runs in a separate process, output must be transported:

```
onWonRound fires
    ↓
RecordingStream captures println()
    ↓
transferStdOutToBotIntent() called [← OUR FIX]
    ↓
BotIntent includes captured output
    ↓
WebSocket transmission to server
    ↓
Server forwards to GUI
    ↓
Output appears in console panel
```

**Solution required** because of distributed architecture, but the end result is **identical behavior** to classic Robocode.

---

## Architecture & Documentation

Complete architectural documentation has been added to help future developers:

**Location:** `/docs-internal/architecture/models/flows/event-handling.md`

**Covers:**
- Event lifecycle (generation → transmission → queuing → dispatch)
- Event queue management architecture
- Output capture mechanism details
- Round-end event dispatch flow
- Priority system and interrupt behavior
- Cross-platform comparison (Java, Python, C#)
- Troubleshooting guide
- Robocode vs Tank Royale comparison

**Diagrams:** Multiple Mermaid flowcharts and sequence diagrams showing:
- Complete event lifecycle
- Before/after fix comparisons
- Event priority hierarchy
- Platform-specific implementations

---

## Commits

1. **Code Fix:** `fix: capture stdout/stderr from event handlers like onWonRound after round ends`
   - Modified: Java WebSocketHandler, Python websocket_handler, C# BaseBotInternals
   - Added: onWonRound handlers to RamFire sample bots

2. **Documentation:** `docs: add comprehensive event handling flow documentation with Tank Royale vs Robocode comparison`
   - Created: `/docs-internal/architecture/models/flows/event-handling.md`
   - Updated: Flow index in README

---

## Key Learnings

### For Future Debugging

This bug teaches us:

1. **Output buffering requires coordination** in distributed systems
2. **Timing matters when features span multiple threads** (WebSocket handler + bot thread)
3. **Round-end is special** — it's the last chance to send data before process shutdown
4. **Cross-platform consistency requires platform-specific implementations** (but same logic)
5. **Documentation with diagrams** greatly helps understanding system interactions

### For Developers

When adding event handlers that generate output:

- **Don't assume output will be captured automatically** in distributed systems
- **Event handlers at round boundaries are especially tricky** (timing with process shutdown)
- **Test across all platforms** — output handling may differ subtly
- **Refer to architecture docs** to understand when capture/transfer happens

---

## Impact

✅ **Reliability:** Event handler output now 100% reliable (was ~20%)  
✅ **Consistency:** Same behavior across Java, Python, C#  
✅ **Observability:** Users can now debug bots via `print()` statements  
✅ **Parity:** Matches classic Robocode behavior for event output  
✅ **Documentation:** Future developers have clear architectural guidance

---

**Status:** ✅ Complete  
**Testing:** ✅ Verified on all platforms  
**Documentation:** ✅ Comprehensive with diagrams  
**Ready for:** Merge and release
