---
id: CAP-005-criteria
type: criteria
status: draft
links: [CAP-005]
title: Acceptance criteria for CAP-005 (gui-bot-console)
ac-prefix: GBC
provenance: inferred
---

```gherkin
Feature: gui-bot-console — Define behavior and quality requirements for GUI panels that display bot-related console output (events, logs,

  # Requirement: Responsive bot console rendering under heavy logging
  # The GUI bot console (e.g. events/log output panels) MUST remain responsive while appending large volumes of
  # ANSI-formatted text.

  @GBC-001
  Scenario: Continuous high-volume ANSI log append
    When the GUI receives a continuous stream of bot log/event output containing ANSI styling
    And the total retained console text grows large
    Then the GUI MUST remain interactive (no sustained UI freezes)
    And appending new content MUST NOT cause progressively worse slowdowns over time

  @GBC-002
  Scenario: User scrolls while logs are streaming
    When the user scrolls the bot console while new ANSI-formatted lines are still being appended
    Then scrolling MUST remain usable without multi-second stalls

  # Requirement: Bounded resource usage for bot console logs
  # The GUI bot console MUST avoid unbounded resource consumption when bots produce large amounts of console output.

  @GBC-003
  Scenario: Console output exceeds retention threshold
    When the retained bot console output exceeds an implementation-defined threshold
    Then the console MUST apply an implementation-defined retention policy to keep CPU and memory use bounded
    And the GUI MUST continue to render new output without freezing

  # Requirement: Preserve common ANSI styling
  # The GUI bot console MUST preserve common ANSI styling sequences used for colored log output.

  @GBC-004
  Scenario: Colored output using standard SGR sequences
    When the console receives ANSI SGR sequences for foreground/background colors and reset
    Then the console MUST render the corresponding styling correctly

  # Requirement: Bot-specific event logging optimization
  # The bot events console SHALL only log events that occur in the current game turn to avoid redundant processing and
  # maintain clarity.

  @GBC-005
  Scenario: Multiple turns events received
    When a TickEvent is received containing events from the current turn
    Then the console SHALL only process and log those events
    And it SHALL skip any events that do not belong to the current turn if they were somehow included

  # Requirement: Comprehensive bot event dumping
  # The bot events console SHALL dump all events relevant to the specific bot being monitored, including all their relevant
  # fields.

  @GBC-006
  Scenario: Bot involved in multiple events
    When events like hits, scans, or firing occur involving the monitored bot
    Then all such events SHALL be displayed in the console
    And each event log SHALL include all its mandatory and relevant fields (e.g., bullet states, coordinates, energy
    # levels)

  # Requirement: Detailed TickEvent field logging
  # The bot events console SHALL log relevant fields from the TickEvent itself, specifically including bullet states grouped
  # under a `bulletStates` header.

  @GBC-007
  Scenario: TickEvent contains bullet states
    When a TickEvent is processed
    Then the console SHALL list all bullet states relevant to the monitored bot from that tick
    And they SHALL be grouped under a `bulletStates` field
    And each bullet state SHALL be listed as a `bulletState` entry indented under `bulletStates`

  # Requirement: Consistent indentation and formatting
  # The bot events console SHALL use a consistent and clear indentation style for nested event data to ensure readability.

  @GBC-008
  Scenario: Nested data in event dump
    When an event contains complex objects like BulletState
    Then the nested fields SHALL be indented relative to the parent event
    And the formatting SHALL follow a standardized pattern for all fields
```
