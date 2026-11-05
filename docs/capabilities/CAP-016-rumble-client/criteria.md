---
id: CAP-016-criteria
type: criteria
status: draft
links: [CAP-016]
title: Acceptance criteria for CAP-016 (rumble-client)
ac-prefix: RCL
provenance: inferred
reversal-cost: low
---

```gherkin
Feature: rumble-client — Local ranked and practice battle client

  @RCL-001 @draft
  Scenario: Client configuration is validated before side effects
    Test-type: Integration
    Given a client configuration declares its schema version, mode, repository locations, game types, session size, local storage, and a client identity in ranked mode
    When the client starts
    Then it accepts a complete ranked configuration and a complete practice configuration without a client identity
    And it rejects an unknown schema version, unsupported value, missing ranked identity, or unsafe local path before synchronizing, running, journaling, or submitting anything

  @RCL-002 @draft
  Scenario: Ranked synchronization establishes one validated input snapshot
    Test-type: Integration
    Given the configured data repository publishes its canonical pointer, engine pin, synchronized catalog, client registration, and matchmaking advice
    When a ranked session synchronizes
    Then the client follows the canonical location and accepts one mutually consistent snapshot with supported schemas, an active registered client ID, an immutable catalog commit, and advice for every selected game type
    And it refuses ranked execution when a required document is missing, unsupported, inconsistent, or fails its declared source identity

  @RCL-003 @retired
  Scenario: Ranked selection turns published advice into a valid battle
    Test-type: Unit
    Given a validated snapshot contains active bots and matchmaking advice for `1v1`, `twinduel`, or `melee`
    When the client selects a ranked battle with a recorded random seed
    Then it selects the pinned number of distinct active participants, preferring advised pairings involving configured own bots before other high-priority advice
    And it treats advice as non-exclusive and can select a valid fallback battle when no advised pairing is available

  @RCL-010 @draft
  Scenario: Ranked selection turns published advice into a valid executable battle
    Test-type: Unit
    Given a validated snapshot contains active individual bots, active TwinDuel teams with two member slots backed by active individuals, and matchmaking advice
    When the client selects a ranked `1v1`, `twinduel`, or `melee` battle with a recorded random seed
    Then it selects the pinned number of distinct individual entries or two distinct team entries whose expanded members equal the pinned participant count
    And it prefers advised pairings involving configured own entries before selecting a valid seeded fallback

  @RCL-011 @draft
  Scenario: TwinDuel teams never face a shared member bot
    Test-type: Unit
    Given a validated snapshot contains active TwinDuel teams with immutable member identities
    When the client selects a ranked TwinDuel battle
    Then the two selected team entries have disjoint member identities
    And it rejects selection when no two teams can satisfy that condition

  @RCL-004 @draft
  Scenario: Ranked and practice modes cannot mix result state
    Test-type: Integration
    Given the client is configured for ranked or practice mode
    When it prepares and runs a battle
    Then ranked mode uses only pinned catalog sources and the pinned behavior version and records every completed result
    And practice mode may use local bot sources but never appends to the ranked journal or invokes a submission transport

  @RCL-005 @draft
  Scenario: A completed ranked battle becomes replay-bound result evidence
    Test-type: Integration
    Given a valid ranked selection and a running server whose behavior version matches the engine pin
    When Battle Runner completes the game type's full configured round count
    Then the client transcribes the complete participant results into the result-data envelope contract and binds the record to a locally retained replay by battle ID and SHA-256 hash
    And an aborted, incomplete, identity-mismatched, or behavior-incompatible battle creates no submittable result

  @RCL-006 @draft
  Scenario: The ranked journal survives submission failures without selective loss
    Test-type: Integration
    Given one or more completed ranked results have been durably appended to the local journal
    When submission succeeds, fails, or is interrupted
    Then it removes only records whose accepted facts were published before their successful ingestion receipts
    And unacknowledged records remain retryable while records from an obsolete behavior-version epoch are quarantined with a clear diagnostic

  # Retired: the credential-free browser form replaces token-based API issue creation (RCL-013).
  @RCL-007 @retired
  Scenario: A registered client submits a bounded issue-ops batch without repository write access
    Test-type: Integration
    Given the ranked journal contains one or more compatible results and the contributor supplies an Issues-only repository credential
    When the client submits the pending results
    Then it creates labelled result issues containing between one and sixty results as required by the result-data contract and correlates each post-publication ingestion receipt to its journal records
    And it neither requests nor uses permission to modify repository contents, branches, releases, packages, Pages, facts, or projections

  @RCL-013 @draft
  Scenario: An outside contributor submits ranked batches through the web form without a credential
    Test-type: Integration
    Given the ranked journal contains compatible results and the contributor holds no repository credential
    When the client prepares a submission
    Then it writes each batch as plain text in a single fenced json block, split by serialized size as well as by the limit of sixty results, and prints the result form address with only a unique title prefilled
    And it keeps each prepared batch retryable until the contributor's issue is found and its ingestion receipts are correlated to the journal records without a credential

  @RCL-008 @draft
  Scenario: The client runtime can execute every supported bot platform within its declared boundary
    Test-type: E2E
    Given the pinned Rumble engine and catalog contain Java, .NET, Python, and TypeScript bots
    When a contributor builds the primary container or follows a supported bare-metal setup
    Then the client can boot each platform with the pinned runtime versions and run a battle through Battle Runner
    And the container restricts bot networking to the local server while client egress is limited to repository synchronization and result submission

  # Retired: RCL-014 permits the contributor to paste an unchanged batch into the result form.
  @RCL-009 @retired
  Scenario: A ranked result reaches the immutable Rumble facts without manual handling
    Test-type: E2E
    Given a registered contributor starts a ranked session from a clean client installation
    When the client synchronizes, selects and completes a battle, journals its result, and submits it through issue-ops
    Then the result-data ingestion workflow accepts the record as an immutable raw fact and regenerates the affected projections
    And no person copies, edits, or grants repository-content write access to deliver the result

  @RCL-014 @draft
  Scenario: An outside contributor's form submission reaches published Rumble rankings
    Test-type: E2E
    Given a registered contributor with no organization membership or repository push access starts from a clean client installation
    When the client synchronizes, selects and completes a ranked battle, journals its result, and prepares a plain-text batch
    And the contributor opens the linked result form while signed in to the registered GitHub account and pastes the complete unchanged batch
    Then ingestion publishes the accepted record as an immutable raw fact before issuing its successful receipt and regenerates the affected rankings
    And the client finds the issue and correlates its successful receipt without a repository credential, removing only the acknowledged journal records
    And the eligible result appears in the published rankings without maintainer labeling, repair, or repository-content write access for the contributor

  @RCL-012 @draft
  Scenario: Runtime refreshes are proposed only after cross-platform container evidence
    Test-type: Integration
    Given an official release feed contains a newer runtime in one of the configured LTS-first lanes
    When the scheduled Rumble runtime refresh runs
    Then it updates the single runtime policy source and the derived user-facing runtime references
    And it opens a reviewable pull request only after the updater checks, image build, hardened checks, and Java, .NET, Python, and TypeScript one-round smoke battles pass
    And it does not automatically merge or publish the proposed refresh

  @RCL-015 @draft
  Scenario: Downloaded competitor bots are verified and kept apart
    Test-type: Integration
    Given the pinned catalog lists active bots with their content hashes and declared licenses
    When a person downloads all or selected active bots into a chosen folder
    Then each bot is placed in the folder only after its source matches the recorded hash, and its license is shown and kept with it
    And a bot whose hash differs is skipped and reported while the others complete
    And no bot outside the chosen folder is created, replaced or deleted, including a bot of the person's own with the same name

  @RCL-016 @draft
  Scenario: A downloaded bot that is no longer active is kept and marked
    Test-type: Integration
    Given a bot was downloaded earlier and the pinned catalog now lists it as retired, withdrawn or disqualified
    When the person refreshes the download folder
    Then the bot stays in the folder and is reported as no longer active
    And it is removed only when the person asks, and a new download never offers it as an active bot

  @RCL-017 @draft
  Scenario: Ranked contribution never reads the download folder
    Test-type: Integration
    Given a download folder contains bots, including one whose name and version match a catalog entry with different source
    When a ranked session selects and runs a battle
    Then it runs only the sources fetched from the pinned catalog and verified against their hashes
    And no file in the download folder is read, run or submitted as ranked evidence

  @RCL-018
  Scenario: A repository belongs to a Rumble environment only on an exact match
    Test-type: Unit
    Given the public Beta and the test environment each name their bot and result repositories under one owner
    When a repository is given as a short name or a GitHub address in any common form, in any letter case
    Then it is assigned to an environment only when its owner and name match that environment's repositories exactly
    And a lookalike, foreign or unknown repository is refused, and an installation that names no environment uses the public Beta

  @RCL-019
  Scenario: State and pending queues of one environment are never given to another
    Test-type: Unit
    Given two environments and a base folder for Rumble state
    When state folders are chosen and a pending queue is created for one environment
    Then each environment gets its own folder, never inside the other's
    And the queue can be read only by asking for the environment it was created for, and a request for another environment is refused

  @RCL-020
  Scenario: The shared Rumble library contains no desktop code
    Test-type: Unit
    Given the shared Rumble library's source and its runtime dependencies
    When the build checks its dependency rules
    Then the library depends on neither the desktop program nor the battle runner and imports no Swing or AWT class
    And a deliberate violation of any of these makes the check fail

  @RCL-021
  Scenario: The bot catalog is read into typed entries and unsupported versions are refused
    Test-type: Unit
    Given a catalog document with a schema version, generation time, commit and bot entries
    When the catalog is read
    Then each entry exposes its name, version, platform, path, source hash, owner, authors, added date, status and team members
    And fields it does not know are ignored, a missing team member list means none, and only active entries count as current
    And an unsupported schema version, malformed document or entry without a required field is refused with an actionable message

  @RCL-022
  Scenario: The source tree hash agrees with the bot repository's rule
    Test-type: Unit
    Given a bot source tree of files with paths and bytes, including Windows line endings and a cache folder
    When its hash is computed
    Then it equals the hash the bot repository's own algorithm gives for the same tree, which sorts the slash-separated relative paths and hashes each path and its bytes, each followed by a zero byte, and skips cache folders
    And changing any file's bytes, name or path changes the hash

  @RCL-023
  Scenario: A bot source is placed only when its hash matches the catalog
    Test-type: Integration
    Given a catalog commit whose archive holds several bots and a catalog entry for each
    When the chosen bots are fetched into a target folder
    Then each bot whose source tree hash matches its entry is placed in the folder, and the others complete independently
    And a bot whose hash differs, or whose files are missing from the archive, is reported by name and version and nothing of it is left in the folder
    And a bot folder that already exists in the target is never replaced

  @RCL-024
  Scenario: Fetching cannot write outside the target folder
    Test-type: Integration
    Given an archive or catalog entry whose paths are absolute or climb out of their folder
    When the bots are fetched
    Then nothing is written outside the target folder and the fetch is refused with an explanation
    And an ordinary archive with the same bots is still fetched normally

  @RCL-025
  Scenario: Signing in with a code gives a typed answer at every step
    Test-type: Unit
    Given an environment that has a sign-in App
    When the library asks GitHub for a sign-in code and then checks for approval
    Then it gives the caller the code to show, the address to open and the time the code lasts
    And each check gives exactly one of: still waiting, check more slowly, signed in, code expired, access denied, or failed with a reason in plain words
    And a request for a slower pace raises the waiting time before the next check

  @RCL-026
  Scenario: A sign-in is renewed with its refresh token and no secret
    Test-type: Unit
    Given a sign-in that holds a refresh token
    When the library renews it
    Then it sends no client secret and receives a new sign-in with a new lifetime
    And a refusal because access was revoked or the refresh token expired is reported as a refused renewal that asks the person to sign in again, and a network failure is reported as a failure that can be retried

  # Retired: RCL-030 keeps the rule that no token appears in text, messages or logs, and RCL-031 replaces the rule that nothing is written to disk with a rule that only the sign-in store writes a token, in a protected file.
  @RCL-027 @retired
  Scenario: A sign-in token never appears in text, messages or logs
    Test-type: Unit
    Given a sign-in with recognisable access, refresh and device code values
    When the sign-in objects are turned into text and when every error and failure path is run
    Then none of those values appears in any text, message, log line or console output the library produces
    And the library writes no token to disk

  @RCL-028
  Scenario: An environment without a sign-in App refuses to sign in
    Test-type: Unit
    Given an environment whose sign-in App is not set
    When the library is asked to start a sign-in
    Then it refuses before contacting GitHub with a message that says signing in is not available there yet
    And an environment with a sign-in App uses that App's public identifier and no secret

  @RCL-029
  Scenario: Waiting for approval respects the interval, the expiry and cancellation
    Test-type: Unit
    Given a sign-in code and a caller-supplied clock, waiting function and cancel signal
    When the library waits for approval
    Then it checks no more often than the interval GitHub named, including after a request to slow down
    And it stops with the expired answer when the code's lifetime has passed and with a cancelled answer as soon as the caller cancels
    And it returns as soon as GitHub reports the person signed in or denied access

  @RCL-030
  Scenario: A sign-in token never appears in text, messages or logs
    Test-type: Unit
    Given a sign-in with recognisable access, refresh and device code values
    When the sign-in objects are turned into text and when every error and failure path is run
    Then none of those values appears in any text, message, log line or console output the library produces
    And the sign-in code that talks to GitHub uses no files, console or logging

  # RCL-031 retired (CH-084): the saved file now also holds what belongs to the sign-in. Reminted as RCL-041.
  @RCL-031 @retired
  Scenario: The saved sign-in holds only the refresh token and is written only by the sign-in store
    Test-type: Unit
    Given a signed-in person and a folder for the environment's saved state
    When the sign-in is saved and then loaded by a new store object on the same folder
    Then the loaded value is the refresh token and when it stops working, and the access token was never saved
    And the file holds no readable token text, and no other part of the library writes a token to disk
    And when saving is switched off nothing is written and nothing is loaded

  @RCL-032
  Scenario: The saved file is only for its owner, and the library checks that before and after writing
    Test-type: Unit
    Given a folder on a file system
    When the sign-in is saved
    Then the file is created so that only its owner may use it, the rights are read back before any token is written and again after the file is moved into place, and the file is written by a temporary file and a single move
    And when the rights cannot be made owner-only the sign-in is not saved, nothing readable is left behind, and the reason is given in plain words

  @RCL-033
  Scenario: A saved sign-in that cannot be trusted is refused and the person signs in again
    Test-type: Unit
    Given a saved sign-in file
    When it is damaged by one byte, cut short, empty, opened by another user name or home folder, moved to another environment, or has rights that let others use it
    Then loading gives an unusable result with a reason in plain words, no token, and no crash
    And a missing file is reported as missing, which is not a problem

  @RCL-034
  Scenario: Signing out removes the saved sign-in
    Test-type: Unit
    Given a saved sign-in
    When the person signs out
    Then the file and any temporary file are removed and loading afterwards reports it as missing

  @RCL-035
  Scenario: Programs that share a saved sign-in renew it one at a time and never use an outdated refresh token
    Test-type: Unit
    Given two programs on the same computer that share one saved sign-in, where GitHub accepts each refresh token only once
    When both renew, one after the other and at the same time
    Then each renewal takes a lock on the saved file, reads the current refresh token from the file, and saves the new refresh token before using the new sign-in
    And no renewal uses a refresh token that another program has already replaced, so both stay signed in
    And a refusal by GitHub removes the saved sign-in and asks the person to sign in again

  @RCL-036
  Scenario: An account that is already registered gets its client ID without opening an issue
    Test-type: Unit
    Given a signed-in account whose registration file exists in the environment's result repository, or whose registration is already saved on this computer
    When the library registers the account
    Then it returns the client ID from the file and marks the account as already registered
    And it opens no issue, and a saved registration is used without any further request after the account is identified

  @RCL-037
  Scenario: A new account is registered through one registration issue and the result repository's file
    Test-type: Unit
    Given a signed-in account with no registration file and no open registration issue
    When the library registers the account
    Then it opens one issue titled `[register]` with the account name as the signed-in account, then checks every five seconds for up to two minutes until the registration file exists
    And it returns the client ID from the file, saves the registration, and never opens a second issue while an earlier registration issue from the same account is still open

  @RCL-038
  Scenario: Registration ends in a typed answer for every other outcome
    Test-type: Unit
    Given a signed-in account that cannot be registered at once
    When the library registers the account
    Then a registration refused by the workflow is returned as refused with the workflow's reason in one short line
    And a wait that ends without a registration is returned as not finished with the address of the issue, and a later attempt continues from the registration file or the open issue
    And GitHub not accepting the token is returned as sign in again, and every other problem, including being offline or the App not being installed, is returned as failed with a reason in plain words

  # RCL-039 retired (CH-084): the registration moved from a plain file into the sign-in's one file. Reminted as RCL-042.
  @RCL-039 @retired
  Scenario: The saved registration is kept per environment and only used for the same account
    Test-type: Unit
    Given a registration saved for an environment and an account
    When the saved registration is read back
    Then it holds the account, the client ID, the environment and when it was saved, in a plain file that is not secret
    And it is ignored when it belongs to another account or another environment or is damaged, and it is removed when the person signs out

  @RCL-040
  Scenario: The sign-in token goes only to GitHub's API address and never into text or an address
    Test-type: Unit
    Given a signed-in token and every request the registration makes
    When the requests are built and every result and failure is turned into text
    Then the token is sent only in the authorization header of requests to GitHub's API address
    And it appears in no address, no result, no message and no console output

  @RCL-041
  Scenario: The saved sign-in holds the refresh token and what belongs to it, never the access token, in one file
    Test-type: Unit
    Given a signed-in person and a folder for the environment's saved state
    When the sign-in is saved and then loaded by a new store object on the same folder
    Then the loaded value is the refresh token and when it stops working, and the access token was never saved
    And the one file holds no readable token text, and no other part of the library writes a token to disk
    And a file that cannot be read just now is left as it is, not written over
    And when saving is switched off nothing is written and nothing is loaded

  @RCL-042
  Scenario: The saved registration belongs to one sign-in in that sign-in's file
    Test-type: Unit
    Given a registration saved for a sign-in in an environment
    When the sign-in is renewed, replaced by a newly approved one or removed, or the registration is read back
    Then it holds the account, the client ID, the environment and when it was saved, and a renewal keeps it
    And it is ignored for another account and is useless in another environment, a newly approved sign-in starts without it, and it goes when the person signs out
    And a registration or an account name that arrives after its sign-in was replaced or removed saves nothing, and a name that has changed on GitHub leaves the registration behind

  @RCL-043
  Scenario: The library says where the Rumble command line is and whether it is the right version
    Test-type: Unit
    Given a user data folder and a desktop version
    When the program is looked up with nothing there, with a file of the same version, with a file of another version and with a file that does not answer
    Then the answers are missing, ready, wrong version and wrong version, each with the path, and nothing is fetched or changed by looking
    And the place is the file rumble.jar in the folder rumble below the user data folder

  @RCL-044
  Scenario: A fetch is planned from exactly the release of the desktop version, and the person can be asked first
    Test-type: Unit
    Given a desktop version and a fake GitHub with that version's release and its assets
    When a fetch is planned
    Then it reads the release for that exact version and never the latest, finds the one file for that version, and gives its name, size, the hash GitHub reports and the address the program built itself from the version and the name over HTTPS from github.com
    And nothing is downloaded or written by planning
    And a missing release, a missing file, a missing hash, a file over the size ceiling, a request limit, a bad answer and being offline each give a typed answer with a plain message and no exception

  @RCL-045
  Scenario: The file is placed only after the size, the hash and the version are right, and nothing bad is left behind
    Test-type: Unit
    Given a plan and a fake download
    When the fetch runs with a good file, a file with another hash, a file over the ceiling, a file of another version, an interrupted download and a file that is in use
    Then only the good file ends as rumble.jar, after it was downloaded under a temporary name, matched with the hash and asked for its version before it was moved
    And every other case leaves no file and no temporary file, an earlier good copy is never touched, and a file in use answers that it is in use
    And a file that is already the right version is not fetched again

  @RCL-046
  Scenario: The real command line jar passes the fetch's checks and runs from the place it was put
    Test-type: Integration
    Given the built command line jar served as the release's file by a fake GitHub
    When it is fetched into a user data folder
    Then it passes the hash and version checks, is placed as rumble.jar, and answers its version when run with java -jar from there

  @RCL-047
  Scenario: A developer can name a jar to use, and then nothing is fetched
    Test-type: Unit
    Given the system property rumble.jar naming a jar, and the same property naming nothing usable
    When the program is looked up and a fetch is asked for
    Then the named jar is the answer, it is marked as the developer's choice, and nothing is fetched or written
    And an unusable name is answered as such, with the reason, and never falls back to fetching in silence

  @RCL-048
  Scenario: The documentation says how the program reaches people and what the hash protects against
    Test-type: Human
    Given the participation design
    When a maintainer reads about how the program reaches people
    Then it says the desktop program fetches the command line at first use from the release of its own version, asks first and shows the size, checks size, hash and version before placing it, and keeps it as rumble.jar in the user's folder
    And it says that the hash protects against a broken or changed file and not against a release that has been taken over, that releases before this one hold no such file, and the developer setting

  @RCL-049
  Scenario: The desktop program never depends on the command line or the battle worker
    Test-type: Unit
    Given the desktop program's build file and its sources
    When the dependencies it declares and the packages it imports are read, with a deliberate violation added
    Then it depends on and imports neither the Rumble command line nor the battle worker, and every deliberate violation is reported with where it is
    And the desktop build fails the same rule on the real dependency graph, so a dependency added by another route is also caught

  @RCL-050
  Scenario: The documentation says the desktop program uses the Rumble libraries in its own process
    Test-type: Human
    Given the participation design and the decision record
    When a maintainer reads how the desktop program and the command line use the Rumble code
    Then they say the desktop program calls the shared libraries in its own process and the command line is a second front end over the same libraries, and that nothing but the battle worker is fetched for the desktop program
    And they say that sending a bot needs no fetch, that the desktop program holds the sign-in token in its own memory and why that is accepted, and that the desktop program never depends on the command line or the worker

  @RCL-051
  Scenario: The account screen starts from the saved sign-in and shows what it found
    Test-type: Unit
    Given the account screen's behavior with a fake library
    When it is opened with a saved sign-in that works, with none, with one that cannot be used, with one GitHub cannot be asked about, and in an environment without a sign-in App
    Then the state is signed in with the account, signed out, signed out with the reason, signed out with the library's plain message, and unavailable with the library's plain message
    And no state, message or text holds a token

  @RCL-052
  Scenario: Signing in shows the code, waits at GitHub's pace, registers the account and shows the client ID
    Test-type: Unit
    Given the account screen's behavior, a fake library and a clock the test moves
    When the person signs in and approves, and when the code expires, is denied, the check fails for good, the sign-in cannot be kept or the registration is refused, not finished or needs a new sign-in
    Then the state is waiting for the code with the code, the address and when it expires, then signed in while the account is registered and with the client ID, and in each failure the state says in plain words what happened and what the person can do
    And GitHub is asked again only after the pace it asked for, a check that fails but can still work is tried again, and a refused registration is shown as such while the person stays signed in

  @RCL-053
  Scenario: Cancelling, closing and signing out stop the waiting and remove what they should, and nothing blocks the screen
    Test-type: Unit
    Given the account screen's behavior with a library whose calls block until released
    When the person cancels, closes the dialog and signs out, and when every call to the library is blocked
    Then every call to the screen returns at once, all work runs on a thread of its own, and results reach the screen through its own queue in order
    And cancelling forgets the waiting code and stops further checks, closing stops the checks and keeps the code, and signing out removes the saved sign-in and shows signed out, or says plainly that it could not

  @RCL-054
  Scenario: The library behind the account screen signs in, registers and signs out as the command line does
    Test-type: Unit
    Given a fake GitHub and the library's real sign-in and registration
    When the account screen's library is used to open, begin, check, register, cancel and sign out
    Then it gives the same results as the command line's commands would, with the account and client ID, and each failure as the plain message of the library
    And the sign-in and registration it saves are the same files the command line reads

  @RCL-055
  Scenario: The Rumble menu is shown only when a developer switches it on, and the environment follows the command line's rule
    Test-type: Unit
    Given the switch property or variable set to a true word, to another word and not set, and the variable naming an environment, an unknown name and nothing
    When the menu switch and the environment are worked out
    Then the menu is shown only for a true word in the property or the variable, the environment is the named one, the public Rumble when none is named, and an unknown name is reported as unknown and never replaced silently
    And nothing in the switch reads or writes anything else

  @RCL-056
  Scenario: Every text the account screen shows is in the desktop program's English resource file
    Test-type: Unit
    Given the account screen's states and notices and the English resource files of the desktop program
    When every text key that a state, a notice or a registration result needs is looked up
    Then each key is in the English file with a non-empty text, and the English menu file has the Rumble menu and its account item
    And no text says "token" or shows how to read the saved sign-in

  @RCL-057
  Scenario: The desktop program shows the account screen and signs in to the test environment
    Test-type: Human
    Given the desktop program started with the menu switched on and the test environment chosen
    When the maintainer opens Rumble, GitHub account, signs in with the code, looks at the account and the client ID, closes and opens the screen again and signs out
    Then the screen shows the code and the address and opens the page, shows the account and the client ID after approval, shows the same account after reopening, and shows signed out after signing out
    And the observer, the conditions, the observation and the result are recorded in the acceptance brief, and no human observation is invented
```
