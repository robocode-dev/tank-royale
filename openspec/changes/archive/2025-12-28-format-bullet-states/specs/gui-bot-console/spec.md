## MODIFIED Requirements

### Requirement: Detailed TickEvent field logging

The bot events console SHALL log relevant fields from the TickEvent itself, specifically including bullet states grouped
under a `bulletStates` header.

#### Scenario: TickEvent contains bullet states

- **WHEN** a TickEvent is processed
- **THEN** the console SHALL list all bullet states relevant to the monitored bot from that tick
- **AND** they SHALL be grouped under a `bulletStates` field
- **AND** each bullet state SHALL be listed as a `bulletState` entry indented under `bulletStates`
