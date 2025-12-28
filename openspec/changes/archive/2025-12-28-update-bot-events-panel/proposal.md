# Change: Update Bot Events Panel

## Why
The Bot Events Panel in the GUI has some TODOs and missing functionality:
- It currently logs events that might not be for the current turn if not optimized.
- `TickEvent` fields like bullet values (bullet states) are not being listed.
- Not all relevant events for a specific bot are being dumped.
- Some relevant fields of events are missing from the log.
- Indentation and formatting of dumped events can be improved and simplified.

## What Changes
- Optimize `BotEventsPanel` to only log events for the current turn.
- Include bullet values (bullet states) from `TickEvent` in the console.
- Ensure all events relevant to the bot are dumped.
- Ensure all relevant fields of events are logged.
- Refactor formatting methods for better indentation and readability.

## Impact
- Affected specs: `specs/gui-bot-console/spec.md`
- Affected code: `BotEventsPanel.kt`
