## ADDED Requirements

### Requirement: Boot Progress Dialog

The GUI SHALL display a modal progress dialog between the "Start Battle" action and the game start,
showing which bots have connected to the server and which are still pending.

#### Scenario: Dialog shown on battle start
- **WHEN** the user clicks "Start Battle" in the New Battle dialog
- **THEN** a modal progress dialog SHALL appear showing all expected bot identities
- **AND** each bot SHALL be displayed with its name and version from `bot.json`

#### Scenario: Bot connects during wait
- **WHEN** a bot connects to the server while the progress dialog is open
- **THEN** the dialog SHALL update that bot's status from pending to connected
- **AND** the connected count SHALL increment

#### Scenario: All bots connected
- **WHEN** all expected bot identities have connected
- **THEN** the progress dialog SHALL auto-close
- **AND** the battle SHALL start automatically

#### Scenario: Team identity expansion
- **WHEN** a team directory is among the selected bots
- **THEN** the dialog SHALL show each team member's identity individually
- **AND** duplicate members (e.g., multiple droids) SHALL show aggregated counts like "2/4 connected"

### Requirement: Boot Progress Timing Display

The boot progress dialog SHALL display elapsed time and timeout information so the user knows how
long the system has been waiting and when it will give up.

#### Scenario: Elapsed time updates
- **WHEN** the progress dialog is open
- **THEN** an elapsed time indicator SHALL update at least every second
- **AND** the configured timeout SHALL be displayed alongside the elapsed time

#### Scenario: Timeout reached
- **WHEN** the boot timeout expires while bots are still pending
- **THEN** the dialog SHALL display an error message listing the pending bot identities
- **AND** the dialog SHALL offer "Retry" and "Cancel" options

### Requirement: Boot Progress Cancellation

The boot progress dialog SHALL provide a way to cancel the boot process and return to bot selection.

#### Scenario: User cancels during boot
- **WHEN** the user clicks "Cancel" on the progress dialog
- **THEN** the progress dialog SHALL close
- **AND** all booted bot processes SHALL be terminated
- **AND** the user SHALL be returned to the New Battle dialog with their bot selection intact

#### Scenario: User retries after timeout
- **WHEN** the user clicks "Retry" after a boot timeout
- **THEN** the timeout timer SHALL reset
- **AND** the dialog SHALL continue waiting for pending bots to connect

### Requirement: Pre-existing Bot Filtering in GUI

The boot progress dialog SHALL exclude bots that were already connected to the server before the
boot process started, so that only freshly booted bots are tracked.

#### Scenario: External server with existing bots
- **WHEN** a battle is started on an external server that has pre-existing bots
- **THEN** the progress dialog SHALL only track bots that were booted for this battle
- **AND** pre-existing bots SHALL NOT appear in the progress display
