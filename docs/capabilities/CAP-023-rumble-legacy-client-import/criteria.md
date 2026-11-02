---
id: CAP-023-criteria
type: criteria
status: draft
links: [CAP-023]
title: Acceptance criteria for CAP-023 (rumble-legacy-client-import)
ac-prefix: RLI
---

```gherkin
Feature: rumble-legacy-client-import — Rumble legacy client import

  @RLI-001
  Scenario: The old client's configuration is read strictly
    Test-type: Unit
    Given the old client's configuration file
    When it is read
    Then the schema version, the two repository addresses, the client ID, the own-bot list, the game types, the battles per session, the mode and the work folder are found, and a work folder that is a relative path is taken relative to the folder of the configuration file
    And an unknown schema version, a file that is not valid, a missing required field and a missing work folder are refused with a plain message before anything is copied, and a client ID that is empty or still the example's placeholder is treated as not set

  @RLI-002
  Scenario: The two repository addresses decide the Rumble environment, and a mix is refused
    Test-type: Unit
    Given configuration files whose repository addresses are public, test, one of each, unknown or written in another form
    When the environment is worked out
    Then two public addresses give the public environment and two test addresses the test environment, in every form the shared library accepts
    And one of each, an unknown address and a bot address paired with the wrong data address are refused before anything is copied, and the refusal names which address was the problem

  @RLI-003
  Scenario: The preview lists what would be copied and writes nothing
    Test-type: Unit
    Given an old work folder with journal files, replay evidence, a bot cache and other files
    When the preview is made
    Then it lists every journal file and every file of the replay evidence with its size, lists everything else as not copied with a reason, names the folder the files would go to, and writes nothing anywhere
    And a work folder with nothing to copy gives a nothing-to-import answer and a work folder that does not exist is refused in plain words

  @RLI-004
  Scenario: Each file is copied byte for byte and checked before it gets its final name
    Test-type: Unit
    Given the files of an old work folder
    When the import runs
    Then each file is written under a temporary name, compared with the source by a SHA-256 hash and only then given its final name below the imported-old-state folder of the right environment, with the same relative path, and the copy is byte for byte the source
    And a copy that does not match the source is removed, reported, and never given a final name

  @RLI-005
  Scenario: The old folder is never changed
    Test-type: Unit
    Given an old work folder and its configuration file
    When the import runs, runs again, is interrupted and fails
    Then every file of the old folder and the configuration file is byte for byte the same, has the same modification time, and no file or folder is added to, moved in or removed from the old folder
    And the imported-old-state folder is never inside the old folder

  @RLI-006
  Scenario: The record is written last and is the proof that the import is complete
    Test-type: Unit
    Given an import
    When every file has been copied and checked
    Then a record is written last, holding for every file its source path, size and hash, the time, the program version and the environment, and it is the only thing that says the import is complete
    And while an import has not completed, an earlier record is not left behind to say it has

  @RLI-007
  Scenario: Running the import again completes an interrupted one and repeats nothing
    Test-type: Unit
    Given an import that ended halfway, leaving some files copied and a temporary file
    When the import runs again
    Then files already copied and unchanged are not copied again, missing and changed files are copied, the leftover temporary file is removed, and the record is then written
    And a second run after a complete import changes nothing and reports that nothing was copied

  @RLI-008
  Scenario: A file that changes while it is being read is not recorded as copied
    Test-type: Unit
    Given a journal file that grows while the import reads it
    When the import checks the copy
    Then the file is reported as changed during the import, its copy is not given a final name, no complete record is written, and the next run copies it

  @RLI-009
  Scenario: The client ID and the own-bot preferences are kept in a typed record in the right environment
    Test-type: Unit
    Given the old configuration of one environment
    When the import runs
    Then a preferences record with the client ID, the own-bot list, the game types, the battles per session and the mode is saved in the state folder of that environment and can be read back
    And nothing is ever saved in the other environment's state folder, and a client ID that is not a valid identifier is refused and not saved

  @RLI-010
  Scenario: Every ending is a typed result in plain words that shows no file content
    Test-type: Unit
    Given imports that complete, find nothing, are refused, hit a file that cannot be read, or find a full disk
    When each ends
    Then each gives a result of its own with counts and a plain message
    And no message or result shows the content of a copied file

  @RLI-011
  Scenario: The documentation says what is copied, what is only kept and what is left alone
    Test-type: Human
    Given the participation design and this capability's design notes
    When a maintainer reads what the import does
    Then they say that the journal and the replay evidence are copied unchanged and not converted, that the old files are never moved or deleted, that the bot cache is not copied, that a journal copied while the old client runs can end in a broken last line which the readers of the copy must handle, and that removing the old files is a separate later decision
```
