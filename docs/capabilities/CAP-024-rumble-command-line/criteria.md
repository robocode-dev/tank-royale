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

  # RCI-001 retired (CH-084): `login --wait` has no JSON form and bad input now answers too. Reminted as RCI-014.
  @RCI-001 @retired
  Scenario: Every command answers in text for a person or in one JSON document for a program
    Test-type: Unit
    Given any of the commands
    When it is run with and without the JSON option
    Then without it the answer is short text, and with it standard output holds exactly one JSON document with a schema version and the same facts
    And nothing else is written to standard output, and standard error holds nothing a program needs

  # RCI-002 retired (CH-084): code 3 is now used and code 4 is new. Reminted as RCI-015.
  @RCI-002 @retired
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

  # RCI-007 retired (CH-084): a registration of another account is no longer shown, and status gives the next step. Reminted as RCI-016.
  @RCI-007 @retired
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

  # RCI-009 retired (CH-084): the account name and web addresses are now kept in answers. Reminted as RCI-017.
  @RCI-009 @retired
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

  # RCI-012 retired (CH-083): its condition, that no sign-in command may be added before it is
  # decided whose GitHub account an agent uses, is met, and the claim about the two programs, the
  # contract and publication is carried over. Reminted as RCI-013.
  @RCI-012 @retired
  Scenario: The documentation describes two programs, the contract and the condition on sign-in commands
    Test-type: Human
    Given the participation design
    When a maintainer reads about the command line
    Then it says that the controller and the worker are two programs and why, gives the output contract and exit codes, says nothing is published in this change and who decides when, and says that no command that signs in or changes GitHub may be added before it is decided whether an agent works with its own account or a participant's

  # RCI-013 retired (CH-084): the decision was refined and the way the program reaches people was settled. Reminted as RCI-018.
  @RCI-013 @retired
  Scenario: The documentation describes two programs, the contract, and the decision on an agent's account with the rules for the sign-in commands
    Test-type: Human
    Given the participation design
    When a maintainer reads about the command line
    Then it says that the controller and the worker are two programs and why, gives the output contract and exit codes, and says nothing is published yet and who decides when
    And it says that an agent works with the participant's own GitHub account and why, and that an agent with an account of its own is not supported and would need its own rules
    And it says that the person approves the sign-in at GitHub and an agent never approves it or creates or registers an account, that a command without a usable sign-in ends with exit code 3 and says so in its JSON answer without asking anything, and that no answer shows a token
    And it says plainly that an agent running as the participant's operating system user can read the saved sign-in, as a bot could, and that the battle worker is what keeps bots away from it

  @RCI-014
  Scenario: Every command answers in text for a person or in one JSON document for a program
    Test-type: Unit
    Given any command, including one given bad input
    When it is run with and without the JSON option
    Then without it the answer is short text, and with it standard output holds exactly one JSON document with a schema version and the same facts, and what the command does is the same in both
    And nothing else is written to standard output, and standard error holds nothing a program needs
    And the one exception is signing in and waiting in one call, which exists in text only and is refused with the JSON option

  @RCI-015
  Scenario: Exit codes say what happened
    Test-type: Unit
    Given commands that succeed, fail, are refused, need a sign-in or are not finished
    When each ends
    Then 0 means done, 1 means failed with the answer saying what to do next, 2 means refused or bad input, 3 means a sign-in is needed, and 4 means not finished so the same command should be run again
    And every answer of login, logout and register carries an outcome word that names which case it is

  @RCI-016
  Scenario: status says what is saved and what to do next without contacting anyone or showing a secret
    Test-type: Unit
    Given a state folder with or without a sign-in, an account name, a waiting code and a registration
    When status is run for an environment
    Then it names the environment and says whether a sign-in is saved, with the reason when it cannot be used, that a saved sign-in has not been checked with GitHub, the signed-in account when known, whether a code is waiting, and whether all is ready
    And a registration is reported only when it belongs to the signed-in account or no account is known, and no GitHub request is made and nothing is written

  @RCI-017
  Scenario: Answers hold no secret and no path of this computer, and web addresses and the account name stay whole
    Test-type: Unit
    Given a saved sign-in with a fake token, a waiting code, an old client folder, and a GitHub account named like the operating system user
    When every command is run in both forms
    Then no answer contains a token, a device code, the home folder or an absolute path of this computer, and paths are shown relative to the folder they belong to
    And the address to open and the address of a registration issue are whole, and the account name is shown; the operating system user name appears nowhere else, even when a word of the answer is part of it

  @RCI-018
  Scenario: The documentation describes the programs, the contract, the account decision and how the program reaches people
    Test-type: Human
    Given the participation design
    When a maintainer reads about the command line
    Then it says that the controller and the worker are two programs and why, and gives the output contract and all the exit codes
    And it says that an agent works with the participant's own GitHub account and why, that an agent may start a sign-in and register the account its person approved, and never approves a sign-in or creates an account, and that an agent with an account of its own is not supported
    And it says plainly that an agent running as the participant's operating system user can read the saved sign-in, as a bot could, and that the battle worker is what keeps bots away from it
    And it says that the desktop program fetches the program from the release with its own version at first use, asking first and checking the hash GitHub reports, and what that check does not protect against

  @RCI-019
  Scenario: login gives the code and the address, once it knows the sign-in can succeed
    Test-type: Unit
    Given an environment and a state folder
    When login is run
    Then it first refuses an environment without a sign-in App and a folder that cannot be kept private, before asking GitHub for a code
    And otherwise it answers at once with the short code, the address and when the code runs out, and saves the waiting code protected so the secret device code is never shown
    And a second login while the code is valid gives the same code, a saved sign-in that still renews is answered as already signed in, and one that GitHub refuses is removed and a new code is given

  @RCI-020
  Scenario: login with the finish option checks at the pace GitHub allows and waits no longer than asked
    Test-type: Unit
    Given a waiting code
    When login is run with the finish option, with or without a time to wait
    Then without a time it checks once, and within the pace it answers how many seconds to wait without contacting GitHub
    And a slower pace from GitHub is kept between runs, a wait never ends later than the time asked for, and not approved yet is answered as waiting with exit code 4
    And a code that ran out or was denied is removed and answered with exit code 3, as is finishing when no code is waiting

  @RCI-021
  Scenario: An approved sign-in is saved with its account, and every way that can go wrong has a way out
    Test-type: Unit
    Given a person approves the code
    When login finishes
    Then the sign-in and the account name are saved and the account is answered, and a registration of another account is removed
    And a sign-in that cannot be saved is answered as not saved with exit code 1 and login as the next step, with the used code removed
    And a sign-in saved when the account name cannot be fetched is answered as signed in with the account unknown, and the next login or register fills the name in

  @RCI-022
  Scenario: Two waiting runs and a sign-out cannot leave a wrong result
    Test-type: Unit
    Given a run waiting for approval
    When a second finish runs at the same time, or logout runs while it waits
    Then the code is used once and the later run answers from what was saved, and logout does not wait for the run to end
    And after logout has answered no sign-in appears: the waiting run finds its code gone, saves nothing and answers that no code is waiting

  @RCI-023
  Scenario: A person can sign in with one command
    Test-type: Unit
    Given a person at a terminal
    When login is run with a time to wait and without the finish option
    Then the code and the address are shown first and the command then waits and ends as finishing does
    And with the JSON option it is refused with exit code 2 and no code is requested

  @RCI-024
  Scenario: logout removes what is saved for one environment and is honest about what remains
    Test-type: Unit
    Given saved state in two environments, or none
    When logout is run for one environment
    Then its sign-in, waiting code, account name and registration are gone and the other environment is untouched, and the answer says that access at GitHub stops only when the person revokes the App there
    And with nothing saved it succeeds and creates nothing, and a file that cannot be removed is answered as not removed with exit code 1

  @RCI-025
  Scenario: register registers the signed-in account and says how it went
    Test-type: Unit
    Given a saved sign-in, or none
    When register is run
    Then it registers the account and saves the client ID, and running it again answers the same account and client ID as already registered
    And an environment without a sign-in App is refused first with exit code 2, a missing or refused sign-in is exit code 3 with login as the next step, and a registration not finished in time is exit code 4 with the address of the issue
    And a registration or a renewed sign-in that could not be saved on this computer is said so in the answer
    And a token GitHub's API refuses removes the saved sign-in, so the next step of signing in gives a new code

  @RCI-026
  Scenario: A refusal counts only when the workflow wrote it, and it does not open new issues
    Test-type: Unit
    Given an account's registration issues in the public result repository
    When register is run
    Then a refusal on the account's newest closed registration issue is answered as refused without opening an issue, and the retry option opens one new issue
    And a comment from anyone but the workflow is ignored, whatever it says, both while waiting and on closed issues
    And when it cannot be read how the newest closed issue ended, the run fails and no new issue is opened

  @RCI-027
  Scenario: The next step can be run as it stands
    Test-type: Unit
    Given an answer that gives a next step
    When the command is read
    Then it names the environment, it tells that the same state folder must be added when one was chosen, and it never includes the retry option after a pending registration
    And on an environment without a sign-in App no refused command is given as the next step; the answer says that signing in is not available yet

  @RCI-028
  Scenario: Bad input answers in the form asked for
    Test-type: Unit
    Given an unknown command, option or environment
    When the program is run with and without the JSON option
    Then with it standard output holds one JSON document with the outcome bad-input and the real message, and without it standard error holds one line and not the whole help text, and the exit code is 2

  @RCI-029
  Scenario: The environment is chosen the same way everywhere
    Test-type: Unit
    Given the commands that use an environment
    When none is named, or it is named by the short option, the long option or the environment variable
    Then the public Rumble is used when none is named, the option wins over the variable, and every answer names the environment, in text too
    And an unknown name, also from the variable, is bad input

  @RCI-030
  Scenario: The program tells its version and where to begin
    Test-type: Unit
    Given the program
    When it is run with the version option, short or long, or with the help option
    Then the version option gives the program version and exit code 0
    And the help says where to begin, shows the sign-in steps and explains all the exit codes

  @RCI-031
  Scenario: The real program keeps standard error empty and needs no keyboard for a sign-in command
    Test-type: Integration
    Given the built command line jar run as a real process with standard input closed
    When a command that needs a sign-in, the help and bad input are run
    Then the sign-in command ends with exit code 3 and one JSON document, and in normal use standard error holds no warning from Java
```
