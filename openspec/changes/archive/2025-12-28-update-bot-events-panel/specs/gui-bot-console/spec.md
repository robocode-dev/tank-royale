## ADDED Requirements

### Requirement: Bot-specific event logging optimization

The bot events console SHALL only log events that occur in the current game turn to avoid redundant processing and
maintain clarity.

#### Scenario: Multiple turns events received

- **WHEN** a TickEvent is received containing events from the current turn
- **THEN** the console SHALL only process and log those events
- **AND** it SHALL skip any events that do not belong to the current turn if they were somehow included

### Requirement: Comprehensive bot event dumping

The bot events console SHALL dump all events relevant to the specific bot being monitored, including all their relevant
fields.

#### Scenario: Bot involved in multiple events

- **WHEN** events like hits, scans, or firing occur involving the monitored bot
- **THEN** all such events SHALL be displayed in the console
- **AND** each event log SHALL include all its mandatory and relevant fields (e.g., bullet states, coordinates, energy
  levels)

### Requirement: Detailed TickEvent field logging

The bot events console SHALL log relevant fields from the TickEvent itself, specifically including bullet states.

#### Scenario: TickEvent contains bullet states

- **WHEN** a TickEvent is processed
- **THEN** the console SHALL list all bullet states relevant to the monitored bot from that tick

### Requirement: Consistent indentation and formatting

The bot events console SHALL use a consistent and clear indentation style for nested event data to ensure readability.

#### Scenario: Nested data in event dump

- **WHEN** an event contains complex objects like BulletState
- **THEN** the nested fields SHALL be indented relative to the parent event
- **AND** the formatting SHALL follow a standardized pattern for all fields
