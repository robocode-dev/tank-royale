---
id: CAP-024-criteria
type: criteria
status: draft
links: [CAP-024]
title: Acceptance criteria for CAP-024 (rumble-command-line)
ac-prefix: RCI
---

```gherkin
Feature: rumble-command-line — Rumble command line

  @RCI-001
  Scenario: Every command answers in text for a person or in one JSON document for a program
    Test-type: Unit
    Given any of the commands
    When it is run with and without the JSON option
    Then without it the answer is short text, and with it standard output holds exactly one JSON document with a schema version and the same facts
    And nothing else is written to standard output, and standard error holds nothing a program needs

  @RCI-002
  Scenario: Exit codes say what happened
    Test-type: Unit
    Given commands that succeed, fail, are given bad input or refuse
    When each ends
    Then 0 means done, 1 means the command failed, and 2 means it was refused or given bad input such as an unknown command, option or environment
    And the code 3 is reserved for commands that need a sign-in and is not used by any command yet

  @RCI-003
  Scenario: No command ever asks anything on the keyboard
    Test-type: Integration
    Given a command run as a real process whose standard input is closed
    When it runs
    Then it finishes with its answer and its exit code without waiting for input

  @RCI-004
  Scenario: The state folder can be chosen, and the commands that only read write nothing
    Test-type: Unit
    Given a state folder given by an option and the default folder when none is given
    When the commands that only read run
    Then they use the folder given and write nothing in it or anywhere else, and the folder with the real state is never touched when another is given

  @RCI-005
  Scenario: version tells the program, the engine and the carried worker
    Test-type: Unit
    Given the command line program
    When version is run
    Then it gives the program version, the engine behavior version, the Java version, and whether the battle worker is carried inside it with its size and SHA-256 hash

  @RCI-006
  Scenario: environments lists the known Rumble environments
    Test-type: Unit
    Given a state folder
    When environments is run
    Then every known environment is listed with its name, its repositories, whether it can be reset, whether it has a sign-in App, and whether state for it exists in the state folder

  @RCI-007
  Scenario: status says what is saved for one environment without showing a secret
    Test-type: Unit
    Given a state folder with a saved registration and a saved sign-in, or none
    When status is run for an environment
    Then it says whether a registration is saved and its client ID, and whether a sign-in is saved and whether it can be used, with the reason when it cannot
    And no token appears in the answer, and an unknown environment is refused with exit code 2

  @RCI-008
  Scenario: import-legacy previews by default and copies only when asked
    Test-type: Unit
    Given an old client's configuration file
    When import-legacy is run without and with the copy option
    Then without it only the preview is given and nothing is written, with it the import is made as the library does, and the results map to the exit codes
    And the old files are unchanged after both

  @RCI-009
  Scenario: Answers hold no secret and no path of this computer
    Test-type: Unit
    Given a state folder holding a saved sign-in with a fake token, and an old client folder
    When every command is run in both forms
    Then no answer contains the token, the home folder, the user name or an absolute path of this computer, and paths are shown relative to the folder they belong to

  @RCI-010
  Scenario: The command line is one runnable jar
    Test-type: Integration
    Given the built command line jar
    When it is run as a real process with java -jar and nothing else on the classpath
    Then version in JSON form gives a valid answer and exit code 0, and an unknown command gives exit code 2

  @RCI-011
  Scenario: The battle worker is its own runnable jar, carried inside the command line and free of controller code
    Test-type: Integration
    Given the built worker jar and the command line jar
    When they are inspected and the carried worker is used
    Then the worker jar holds no host, bot project, desktop or GitHub code, the command line carries it as a resource and not as classes on its own classpath and the carried copy is byte for byte the built worker jar
    And the carried worker, extracted into the state folder by the command line, runs a real battle through the host with the library's usual command and gives a ranked result

  @RCI-012
  Scenario: The documentation describes two programs, the contract and the condition on sign-in commands
    Test-type: Human
    Given the participation design
    When a maintainer reads about the command line
    Then it says that the controller and the worker are two programs and why, gives the output contract and exit codes, says nothing is published in this change and who decides when, and says that no command that signs in or changes GitHub may be added before it is decided whether an agent works with its own account or a participant's
```
