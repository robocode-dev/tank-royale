# Change: Format bullet states in console

## Why

When dumping bullet values in the bot events console, multiple `bulletState` entries were being listed directly under
the event. This made it unclear that there could be multiple bullets. Grouping them under a `bulletStates:` header
improves clarity and reflects that it's a collection.

## What Changes

- Group individual `bulletState` entries under a `bulletStates:` header in the `BotEventsPanel`.
- Indent `bulletState` entries further under the `bulletStates:` header.

## Impact

- Affected specs: `gui-bot-console`
- Affected code: `BotEventsPanel.kt`
