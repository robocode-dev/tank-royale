## ADDED Requirements

### Requirement: Pythonic Property Accessors

The Python Bot API SHALL use Python property decorators for all state accessors instead of Java-style getter methods.

#### Scenario: Read-only state access via properties

- **WHEN** a bot accesses state like energy, position, or direction
- **THEN** the bot uses property syntax (e.g., `self.energy`, `self.x`, `self.direction`)
- **AND** the API does not provide `get_*()` methods for these accessors

#### Scenario: Boolean state access via properties

- **WHEN** a bot checks boolean state like disabled, stopped, or running
- **THEN** the bot uses property syntax (e.g., `self.disabled`, `self.running`)
- **AND** the API does not provide `is_*()` methods for parameterless boolean checks

#### Scenario: Read-write properties for adjustment flags

- **WHEN** a bot reads or writes adjustment flags (gun/radar compensation)
- **THEN** the bot uses property syntax for both reading and writing
- **AND** the property supports both get and set operations (e.g., `self.adjust_gun_for_body_turn = True`)

### Requirement: Semantic Equivalence with Java API

The Python Bot API SHALL maintain 1:1 semantic equivalence with the Java reference implementation while using Python
idioms.

#### Scenario: Property behavior matches Java getter

- **WHEN** a Python property is accessed
- **THEN** the behavior is identical to calling the corresponding Java getter method
- **AND** the return type is equivalent (accounting for language differences)

#### Scenario: Parameterized methods remain as methods

- **WHEN** a Java method takes parameters (e.g., `isTeammate(int botId)`)
- **THEN** the Python equivalent remains a method with the same signature
- **AND** does not become a property

## REMOVED Requirements

### Requirement: Java-style Getter Methods

The Python Bot API previously provided Java-style `get_*()` and `is_*()` methods for state access.

**Reason**: These are not idiomatic Python. Properties provide the same functionality with cleaner syntax.

**Migration**: Replace `get_x()` with `x`, `is_disabled()` with `disabled`, etc. See proposal.md for full mapping.
