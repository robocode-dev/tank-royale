## ADDED Requirements

### Requirement: Identity-Based Bot Matching

The Battle Runner SHALL match booted bot processes to their connected bot instances using a multiset
of `(name, version)` identities read from bot configuration files, rather than counting the total
number of connected bots.

#### Scenario: Two distinct bots connect successfully
- **WHEN** a battle is started with two bot directories containing different `(name, version)` pairs
- **THEN** the runner SHALL wait until both specific identities appear in `BotListUpdate`
- **AND** the runner SHALL return the `BotAddress` for each matched identity

#### Scenario: Same bot directory used twice (duplicate identity)
- **WHEN** a battle is started with the same bot directory listed twice
- **THEN** the runner SHALL expect two connections with the same `(name, version)`
- **AND** the runner SHALL wait until two distinct `BotAddress` entries with that identity appear

#### Scenario: Team directory expands to member identities
- **WHEN** a battle is started with a team directory containing `teamMembers` in its JSON config
- **THEN** the runner SHALL read each member's `bot.json` from sibling directories
- **AND** the expected identity multiset SHALL contain one entry per team member (including duplicates)

#### Scenario: Stray bot on external server is ignored
- **WHEN** a battle is started on an external server that has pre-existing bots with different identities
- **THEN** the stray bots SHALL NOT count toward the expected identity match
- **AND** the runner SHALL wait for the correct identities to connect

#### Scenario: Pre-existing bots are filtered
- **WHEN** a battle is started on an external server with bots already connected before boot
- **THEN** the pre-existing bots SHALL be excluded from identity matching
- **AND** only freshly connected bots SHALL be matched

#### Scenario: Bot config file missing or malformed
- **WHEN** a bot directory lacks a valid JSON config file or the file is missing `name`/`version`
- **THEN** the runner SHALL throw a `BattleException` with a descriptive message before booting

#### Scenario: Team member directory missing
- **WHEN** a team config references a member whose directory does not exist
- **THEN** the runner SHALL throw a `BattleException` naming the missing member and expected path

### Requirement: Configurable Boot Timeout

The Battle Runner builder SHALL accept a `botConnectTimeout(Duration)` option that controls how long
`waitForBots()` waits for all expected identities to connect. The default SHALL remain 30 seconds
for backward compatibility.

#### Scenario: Custom timeout via builder
- **WHEN** a user creates a `BattleRunner` with `botConnectTimeout(Duration.ofSeconds(120))`
- **THEN** the runner SHALL wait up to 120 seconds for bots to connect

#### Scenario: Default timeout preserved
- **WHEN** a user creates a `BattleRunner` without specifying `botConnectTimeout`
- **THEN** the runner SHALL use the default 30-second timeout

#### Scenario: Timeout with identity-aware error
- **WHEN** the boot timeout expires before all expected identities connect
- **THEN** the runner SHALL throw a `BattleException` listing which identities are still pending

### Requirement: Boot Progress Reporting

The Battle Runner SHALL report boot progress with identity information so callers can display which
bots have connected and which are still pending.

#### Scenario: Progress event on bot connection
- **WHEN** a `BotListUpdate` arrives during the boot wait phase
- **THEN** the runner SHALL fire an `onBootProgress` event with connected and pending identity maps

#### Scenario: Periodic progress during wait
- **WHEN** the runner is waiting for bots to connect
- **THEN** the runner SHALL fire `onBootProgress` periodically (at most every 500ms) with elapsed
  time and timeout remaining

#### Scenario: Progress includes timing
- **WHEN** an `onBootProgress` event fires
- **THEN** it SHALL include `elapsedMs` (time since boot started) and `timeoutMs` (configured timeout)

## MODIFIED Requirements

### Requirement: Bot Selection

The system SHALL allow users to specify which bots participate in a battle by providing file system
paths to bot directories (containing bot configuration files).

#### Scenario: Select bots by path
- **WHEN** a user provides a list of bot directory paths
- **THEN** the runner SHALL resolve bot entries from those directories
- **AND** the runner SHALL validate that each path contains a valid bot configuration
- **AND** the runner SHALL pre-read bot identities from the configuration files

#### Scenario: Invalid bot path
- **WHEN** a user provides a path that does not contain a valid bot
- **THEN** the runner SHALL throw a descriptive error before starting the battle

#### Scenario: Team bot path with missing members
- **WHEN** a user provides a team directory path where a team member's directory is missing
- **THEN** the runner SHALL throw a descriptive error naming the missing member before starting the battle
