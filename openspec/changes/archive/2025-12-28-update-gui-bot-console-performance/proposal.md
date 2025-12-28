# Change: Improve GUI bot console performance for large ANSI logs

## Why

The GUI bot console panels (e.g. BotEventsPanel via BaseBotConsolePanel) can become progressively slower and may appear
to freeze when they receive large amounts of ANSI-formatted text. This makes the GUI hard to use during longer battles
or when bots log frequently.

## What Changes

- Define measurable performance requirements for the GUI bot console when appending large volumes of ANSI-formatted
  text.
- Investigate and document the real bottlenecks (e.g. StyledDocument mutation cost, ANSI parsing, repaint/layout on
  EDT).
- Specify one or more mitigations (e.g. bounded retention, batched updates, background parsing, or alternative
  rendering) with clear behavioral expectations.

## Impact

- Affected specs: `gui-bot-console` (new)
- Affected code: `gui` module, especially `BotEventsPanel`, `BaseBotConsolePanel`, `ConsolePanel`, `AnsiEditorPane`,
  `AnsiEditorKit`.
- Potential behavioral changes: log retention limits and/or different rendering strategy might truncate older output

Note: Current ANSI features are preserved. These cannot be removed, but optimizations are welcome.

