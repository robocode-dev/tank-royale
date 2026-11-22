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
    And no English sentence is written in the behavior class or the dialog, so every text a person sees comes from the resource files, and a sentence that a lower layer said is shown only as a detail under a label from the resources

  @RCL-057
  Scenario: The desktop program shows the account screen and signs in to the test environment
    Test-type: Human
    Given the desktop program started with the menu switched on and the test environment chosen
    When the maintainer opens Rumble, GitHub account, signs in with the code, looks at the account and the client ID, closes and opens the screen again and signs out
    Then the screen shows the code and the address and opens the page, the code can be selected with the mouse and copied with Ctrl+C or with a button, shows the account and the client ID after approval, shows the same account after reopening, and shows signed out after signing out
    And the observer, the conditions, the observation and the result are recorded in the acceptance brief, and no human observation is invented

  @RCL-058
  Scenario: Choosing a folder shows what the bot is, and a folder that is no bot is told so
    Test-type: Unit
    Given the submit wizard's behavior and real bot folders
    When a bot folder, a team folder, a folder with no configuration, a file and a folder with broken configuration are chosen
    Then the first two show the bot's name, version and language read from its own configuration, and the others show a plain problem code with the file it concerns
    And choosing changes nothing on disk and starts no check by itself

  @RCL-059
  Scenario: Checking lists every problem at once and can be repeated after the person fixes the folder
    Test-type: Unit
    Given a bot folder with several problems and the same folder after they are fixed
    When the check runs, and runs again
    Then every problem is listed together with its code, its file and the library's message and fix, a passing bot shows no problems, and a repeated check reads the folder again

  @RCL-060
  Scenario: Review stages a separate copy, shows exactly what will be published and never touches the original
    Test-type: Unit
    Given a bot folder that passes the check and a state folder
    When the person goes on to review
    Then the folder is staged in the state folder, and the review shows the name, the version, the language, the license the bot declares and every file that will be published
    And the original folder is byte for byte as it was, and a copy that the library rejects returns the person to the problems

  @RCL-061
  Scenario: The person cannot go on without both confirmations, and nothing ticks them
    Test-type: Unit
    Given the review of a staged bot
    When neither, one and both of the confirmations are ticked, and when the person goes back and returns
    Then the person can go on only with both ticked, the confirmations start unticked every time the review is shown, and nothing in the program ticks, presets or remembers them
    And going on leads to the check of the person's copy on GitHub, and nothing is sent or written beyond the staged copy by going on alone

  @RCL-062
  Scenario: The wizard never blocks the screen and drops results that come too late
    Test-type: Unit
    Given the wizard's behavior with a library whose calls block until released
    When the person chooses, checks, goes on, goes back and closes
    Then every call returns at once, all disk work runs on a thread of its own, states are shown on the screen's own thread in order, and a result after the person went back or closed is thrown away

  @RCL-063
  Scenario: Every problem the library can find has a text of its own, and no English sentence is written in the wizard
    Test-type: Unit
    Given the problem codes written in the bot project library's sources and the English resource file
    When each code, each step and each notice of the wizard is looked up
    Then every code has a non-empty text and each step and notice has its text, including the notice that everything becomes public and the two confirmations
    And no English sentence is written in the wizard's behavior class or its dialog, and the library's own sentence is shown only as a labelled detail

  @RCL-064
  Scenario: A person with no GitHub account can open GitHub's own sign-up page from the account screen
    Test-type: Unit
    Given the account screen's signed-out state and the library's address for creating an account
    When the screen's texts and the address are looked at
    Then the address is GitHub's sign-up page over HTTPS and nothing else, the screen offers it only when the person is signed out, and its texts are in the resource file
    And the program opens the page and nothing more, creates no account and fills in nothing

  @RCL-065
  Scenario: The desktop program shows the first three steps of Submit or update bot
    Test-type: Human
    Given the desktop program started with the menu switched on
    When the maintainer opens Rumble, Submit or update bot, chooses a bot, fixes a problem and checks again, reviews, and tries to go on with one and with both confirmations ticked
    Then the folder chooser, the found bot, the list of problems, the review of what will be published and the notice are shown, going on is possible only with both confirmations ticked, and every text wraps
    And the observer, the conditions, the observation and the result are recorded in the acceptance brief, and no human observation is invented

  @RCL-066
  Scenario: The wizard checks that the account has its own copy of the bot repository with the App installed, and waits for the person to make it
    Test-type: Unit
    Given the wizard's behavior with a fake of the readiness check that answers ready, no copy, App not installed, not the Rumble's repository, sign-in needed and failed
    When the person goes on from the review and the fake answers each of these
    Then ready goes on to sending at once, no copy and App not installed each show their own step with a button that opens only the page the library names, the other three show their own plain state, and while a copy or the App is missing the wizard checks again by itself and goes on when both are found
    And the program never makes the copy and never installs the App

  @RCL-067
  Scenario: Waiting never blocks the screen, spends no more of GitHub's limits than the library's pace and stops when the person leaves
    Test-type: Unit
    Given the wizard's behavior with a fake whose calls block until released and a clock that is moved by the test
    When the copy check and the following of a pull request wait, the person goes back or closes the dialog, and a result arrives afterwards
    Then every call returns at once, every call to GitHub runs on a thread of its own, the repeated checks keep the library's pace, no check runs after going back or closing, and a result that arrives afterwards is thrown away

  @RCL-068
  Scenario: Only the reviewed copy is sent, and only with the author's confirmation the person ticked
    Test-type: Unit
    Given the wizard's behavior with a fake GitHub that records what it is asked to send
    When the person goes through the review with both, one or neither of the confirmations ticked and then asks to send
    Then nothing is sent unless both are ticked, the author's license confirmation handed to the library is the second box and never a default, the files handed over are exactly the staged copy that was reviewed and never the original folder, and no step of the wizard or its tests contacts a real GitHub
    And a send that is asked twice for the same files does not open a second pull request

  @RCL-069
  Scenario: Every way sending can end has its own plain result and the launcher advice never blocks
    Test-type: Unit
    Given the wizard's behavior with a fake GitHub that ends a send as opened, resumed, unchanged, updated, set-up needed, refused, sign-in needed and failed, and answers the launcher comparison as same, differs and not checked
    When the person sends
    Then each ending shows its own state with a button that opens the pull request where there is one, a launcher that differs is shown as a note and the send still happens, and a failed or refused send shows what to do and leaves the staged copy and the confirmations as they were

  @RCL-070
  Scenario: The wizard shows where the pull request stands in one plain state and checks calmly while it is open
    Test-type: Unit
    Given the wizard's behavior with a fake that reads each state a pull request can be in: waiting for the checks, being checked, sent back with a reason, waiting for a moderator, accepted, merged, in the catalog, closed, sign-in needed and failed
    When a bot has been sent and the screen is open, and when the person asks to check now
    Then each state is shown in its own plain words with the reason where there is one, checks, review, publication and visibility in the rankings are not mixed into one word, the screen checks again by itself at a calm pace and on request, and it stops when the dialog is closed

  @RCL-071
  Scenario: A bot whose content is already in the Rumble is told so by its checksum, and nothing is sent
    Test-type: Unit
    Given a staged copy whose content hash is the one the catalog records for the same bot name and version, one that differs from it, and a name and version the catalog does not hold
    When the person reaches the sending
    Then the first is told that exactly this version is already in the Rumble and nothing is offered to send, the second is told that the version exists with other content and must have a new version, and the third goes on to send
    And the hash comes from the staged copy and the catalog, and nothing is saved on the computer to remember it

  @RCL-072
  Scenario: A bot the account has already sent is found again through GitHub and shows where its pull request stands
    Test-type: Unit
    Given a fake GitHub that holds an open pull request of the signed-in account for the bot being sent, a closed one, and none
    When the person reaches the sending
    Then the open one is found and shows where it stands with the choice to update it with the new files, the closed one and none lead to a new send, and nothing about the sent bot is written to the computer

  @RCL-073
  Scenario: Every state and ending of the last steps has a text in the resources, and no text holds a token
    Test-type: Unit
    Given the resource files of the desktop program and the sources of the wizard and its dialog
    When every readiness case, sending ending and progress state is listed from the library and compared with the resource keys
    Then every one has a non-empty text in the base file and the same keys exist in every language file, no English sentence is written in the wizard classes or the dialog, no text mentions a token, and success and failure are marked in the same way as in the first steps

  @RCL-074
  Scenario: The desktop program sends a test bot to the test environment and follows it
    Test-type: Human
    Given the desktop program started with the menu switched on for the test environment, a signed-in test account and a test bot the maintainer has allowed to be sent
    When the maintainer goes through the whole wizard with the copy and the App missing and then present, sends the bot, follows the pull request, tries to send the same files again and closes the pull request afterwards
    Then the copy and App steps open the right pages and go on by themselves, the pull request appears on the test bot repository with exactly the reviewed files, the progress shows where it stands, sending the same files again opens nothing new, and every text wraps
    And the observer, the conditions, the observation and the result are recorded in the acceptance brief, and no human observation is invented

  @RCL-075
  Scenario: The Download bots screen lists the active bots of the pinned catalog and explains the Rumble's licenses in short
    Test-type: Unit
    Given a fake catalog with active, superseded, retired and disqualified bots on several platforms
    When the person opens the screen and narrows the list by platform
    Then only active bots are offered, each with name, version and platform, and the platform filter keeps only the chosen platforms
    And one button opens a small window of its own with a list box of the licenses the Rumble allows, and choosing one replaces what is shown, without moving the text, with in short what the license means for the author of a bot and what it means for a person who uses or copies the bot, with what that person may do, what they must do when they share the code and what is not promised, with a link to the official text and a link to a fuller explanation, and says that this is not legal advice and that reading a bot or practising against it asks nothing of the person
    And no license text is held by the program, no bot's configuration file is fetched before the download, and a bot that is already in the download folder shows the license its own configuration file declares

  @RCL-076
  Scenario: Each bot shows whether it is new, an update, up to date, changed here or no longer active, from a marker file and the catalog's checksum
    Test-type: Unit
    Given a download folder with a bot folder that holds the marker file and the catalog's content, one with the marker and an older version, one with the marker whose files the person changed, one without the marker that has the same name as a catalog bot, one with the marker for a bot the catalog now lists as retired, and a catalog bot with no folder
    When the screen works out the state of each bot
    Then they are shown as up to date, an update, changed here, kept apart and untouched, no longer active, and new, in that order
    And what a program leaves in a bot folder when the bot is run, in the folders and with the file endings the bot repository never accepts, does not count as a change
    And the version is read from the bot's own configuration file and the content check is the same tree checksum the catalog records, and nothing is recorded outside the download folder
    And a row shows the catalog's version, and also the version in the bot's own configuration file in the folder when the two differ, so a bot the person changed and an earlier download with a newer version in the catalog both show what is in the folder

  @RCL-077
  Scenario: Downloading places verified bots with the marker, tells the result of each, and never replaces anything that is not an unchanged earlier download
    Test-type: Unit
    Given a fake catalog and archive with several bots, one whose content does not match its checksum, one missing from the archive, and a download folder that holds a folder without the marker with the same name as a chosen bot and an earlier unchanged download of another
    When the person presses Download for all the bots
    Then each matching bot is placed with an empty marker file and the others are reported by name and version as hash differs or not in the archive, and one failing bot never stops the others
    And the folder without the marker is left exactly as it was, the unchanged earlier download is replaced by the new version, and a download the person changed is replaced only when the person asks for that bot
    And the result shows the license each placed bot's own configuration file declares, as written, and the same button opens the explanations and the links of the licenses the Rumble allows from the result screen too
    And nothing the screen placed is run, built or added to any list of bots to start, and the files of each bot are exactly the files of the catalog's source, including any license file in them
    And an earlier folder that was moved aside and cannot be put back is never deleted: it is kept whole inside the download folder and the result says where, a work folder that cannot be removed does not hide the bots that were placed, a bot whose folder cannot be read or moved is reported on its own while the others are still placed, and only one download runs at a time, so a screen opened again while a download is still running never moves the same folders at once

  @RCL-078
  Scenario: Every way a download can fail has its own plain answer and a way on
    Test-type: Unit
    Given a catalog that cannot be read, a catalog of a schema version the program does not know, a folder that cannot be written, a network that fails halfway, and an archive with an entry that leaves its folder
    When the person opens the screen or presses Download
    Then each is told in its own plain text with a way on, a folder that cannot be written is told before anything is fetched, an interrupted download keeps the bots that were placed and can be continued, and an escaping archive writes nothing outside the chosen folder
    And an ordinary archive in the same folder is still downloaded normally

  @RCL-079
  Scenario: The screen never fetches in silence, keeps the window free, and drops a result that arrives after the person has left
    Test-type: Unit
    Given a fake catalog and archive whose answers are held back
    When the screen opens, the person presses Download, goes back or closes the dialog while a call is running
    Then nothing is fetched before Download is pressed, every call to the catalog, the archive and the disk runs away from the screen's event thread and results arrive in order, and the held-back result of a call that was left is thrown away without changing the screen
    And a second press of Download while one runs starts nothing new
    And whatever a call throws, even an error that is not an exception, ends the waiting as a failure with its message shown as details

  @RCL-080
  Scenario: Every state, result, failure and allowed license of the screen has a text in the resources, only the fixed license pages can be opened, and the warning about other people's code is shown before the download
    Test-type: Unit
    Given the resource files of the desktop program and the sources of the screen and its dialog
    When every state, result, failure and license the Rumble allows is listed from the library and compared with the resource keys
    Then every one has a non-empty text in the base file and the same keys exist in every language file, no English sentence is written in the screen classes or the dialog, and success and failure are marked in the same way as in the other Rumble screens
    And every license in the Rumble's allowed list has an explanation and both links, so a newly allowed license without them fails a test, and the fuller explanation opens in the screen's language where that page exists and in English otherwise
    And the dialog opens a page in the browser from one place only, after checking that the address is one of the library's fixed license addresses, and the warning that downloaded bots are other people's code and not made safe by review is on the screen that offers the download

  @RCL-081
  Scenario: The Download bots item is in the Rumble menu and is shown only when the Rumble menu is switched on
    Test-type: Unit
    Given the Rumble menu switch off and on
    When the menu bar is built
    Then the item is in the Rumble menu only, only when the switch is on, and the menu holds no other new item
    And the dialog's title shows the environment it talks to

  @RCL-082
  Scenario: The maintainer downloads bots from the test environment with the desktop program
    Test-type: Human
    Given the desktop program started with the menu switched on for the test environment and an empty temporary folder chosen as the download folder
    When the maintainer opens Download bots, reads the explanation of each license and opens its links, picks a few bots and downloads them, changes one of the downloaded bots and opens the screen again
    Then the explanations can be read and the links open the right pages, the bots are placed with their own files and the result lists each with its declared license, the states say up to date and changed here, nothing outside the folder is touched, every text wraps, choosing a license replaces the explanation without moving the text, and the buttons at the bottom stay in view however long the list is
    And the observer, the conditions, the observation and the result are recorded in the acceptance brief, the temporary folder is removed afterwards, and no human observation is invented
  @RCL-083
  Scenario: The Download bots screen checks each platform against the Rumble's one list of supported versions
    Test-type: Unit
    Given fake answers from the Rumble client's latest release for its runtime list, and fake answers from java, dotnet, node with npm, and python for their versions, including a missing tool, an older version, output that cannot be read and a tool that does not answer in time
    When the screen opens on a download folder
    Then each platform passes only when its tool reports at least the major version the list names, and a missing, older, unreadable or silent tool fails only its own platform with its own reason
    And Python passes only when the download folder's own Python environment finished its setup, starts and holds the Python Bot API, and an environment that no longer starts, because Python was upgraded or the folder was moved, or whose setup was stopped half-way, counts as missing
    And no version is written in the program, the checks run away from the screen's event thread, and checking installs or changes nothing
    And when the runtime list cannot be read or understood, each platform passes when its tool is present at all, and the screen says that the Rumble's supported versions could not be read; the list is read once for each opening of the screen

  @RCL-084
  Scenario: Only platforms that can run are ticked, and ticking another one says what is missing
    Test-type: Unit
    Given checks where some platforms pass and others fail for each kind of reason
    When the screen shows the platforms and the person ticks a platform that failed
    Then only the passing platforms are ticked to begin with, so the bots of the others are not downloaded unless the person asks
    And ticking a failed platform opens a dialog with the reason in plain words: for Python with no environment it offers to set one up, for Python that is missing, too old or cannot make an environment it links to the official download and on Debian and Ubuntu names the python3-venv package, and for Java, .NET or Node.js it names the version needed and links to the official download
    And every dialog has Show anyway, which ticks the platform and installs nothing, and closing the dialog leaves the platform unticked
    And the dialog opens a page only after checking that it is one of the library's fixed download pages

  @RCL-085
  Scenario: Setting up Python for a download folder uses only that folder's own environment
    Test-type: Unit
    Given a download folder, a Python that passes its check, the program's own Python Bot API, and fake commands that succeed or fail at each step
    When the person asks to set up Python for the bots in that folder
    Then the screen shows the commands before running them, makes the environment in a hidden folder of the download folder with that Python, and installs the program's own Python Bot API into it with the environment's own python -m pip
    And progress and the result are shown, a failure keeps the failing step and its own details, a half-made environment is removed or, when it cannot be removed, the person is told that part of it is left and it is never used, an old environment that cannot be removed stops the setup before anything is made, and a second setup replaces an environment that no longer starts
    And the environment counts only once its setup marked it finished as the last step, the program's Python Bot API file is written to a private folder of its own for each opening of the screen, and the person's choices on the list are kept across the setup
    And nothing outside that hidden folder is created or changed, no other Python is installed into, and the hidden folder is never listed as a bot, neither on the screen nor among the bots the desktop program can start

  @RCL-086
  Scenario: Bots in a folder with that environment start with it, and every other bot starts as before
    Test-type: Unit
    Given a bot in a download folder that holds a finished environment, a bot in a folder whose environment's setup was stopped half-way, and a bot in a folder that holds none
    When the desktop program starts each bot
    Then the first bot's process has the environment's scripts folder first on its path and RUMBLE_PYTHON set to the environment's Python, so launchers that call python and launchers that use RUMBLE_PYTHON both use it
    And the other bots' process environments are exactly what they are today

  @RCL-087
  Scenario: Every reason, offer and result of the platform checks has a text in every language file
    Test-type: Unit
    Given the resource files of the desktop program and the sources of the checks, the setup and the dialogs
    When every platform, reason and setup result is listed from the library and compared with the resource keys
    Then every one has a non-empty text in the base file and the same keys exist in every language file, no English sentence is written in the sources, and success and failure are shown in the same way as on the other Rumble screens

  @RCL-088
  Scenario: The maintainer sets up Python for downloaded bots and starts them in a battle
    Test-type: Human
    Given the desktop program started with the menu switched on for the test environment, an empty temporary folder chosen as the download folder and added as a bot directory, and a computer whose Python has no Python Bot API
    When the maintainer opens Download bots, sees which platforms are ticked, ticks Python, sets it up, downloads the bots and starts a downloaded Python bot and a downloaded Java bot in a battle
    Then the ticks match what the computer has, the dialog says what was missing, the setup ends with success, and both bots start and play
    And the observer, the conditions, the observation and what was not observed are recorded in the merge commit, the temporary folder is removed afterwards, and no human observation is invented
  @RCL-089
  Scenario: Labels of bot directories are kept beside the existing setting, which is read and written as before
    Test-type: Unit
    Given a setting of bot directories, each switched on or off, and labels for some of them
    When the labels are written and read again, and the same folder is written with other slashes, a trailing slash or, on Windows and macOS, other letter case
    Then the setting of bot directories is exactly what it was, every directory and its switch kept, so an older version of the program reads the same bot directories
    And each label comes back for its folder, the same folder written another way has the same label, a comma, a semicolon or a percent sign in a path keeps every pair in place, and an entry that is not a pair of path and label is left out on its own without shifting or losing the others
    And a label whose folder was removed from the bot directories is dropped when the bot directories are saved

  @RCL-090
  Scenario: The download folder is added to the bot directories with its label once a bot is placed there, and never moves the suggested folder
    Test-type: Unit
    Given bot directories of the person's own, and a download that placed bots in a folder
    When the folder is remembered after the download
    Then a folder not yet in the bot directories is added at the end, switched on, and labelled as downloaded from the Rumble only when it holds nothing but bot folders the screen placed, and a folder already there keeps its place and its switch and gets the label only when it is labelled already or holds nothing but bot folders the screen placed, so one of the person's own bot directories never has their own bots moved into the group
    And the result says in one line whether the folder was added, with or without the label, was there already, is switched off so its bots are not in New Battle, is one of the person's own bot directories, switched on or switched off, or could not be added, and the bot directories and labels are saved in one write
    And the folder is remembered once per download, by the download itself away from the screen's thread, so the screen never waits for the disk and showing the result again changes nothing, and a reason it could not be remembered is logged
    And nothing is changed when the download placed no bot, and the result says how the folder stands, or asks the person to add it by hand when it is not in the bot directories; a folder that cannot be looked into is taken to hold the person's own bots, and is looked into only when that decides anything; a folder whose path holds a comma, which the setting of bot directories cannot hold, is not added and the result says why; when the settings cannot be read or written nothing is added, whatever goes wrong the placed bots are never shown as a failed download, and the result says the folder could not be added
    And a label is counted only for a folder among the bot directories, so a label an older version of the program left behind never comes back with a folder the person adds later
    And New Battle reads the bot directories each time it opens, and while it is open again after the settings are saved or a download places bots, also into a folder already listed, away from the screen's thread, so neither the download screen nor New Battle waits for it and New Battle never lists fewer bots than the folders hold, and the Bot Root Directories dialog, when open, reads them again, so it never writes back an old list
    And the suggested download folder is worked out from the first of the person's own switched-on bot directories, skipping labelled download folders, so it stays where it was when a download folder is added

  @RCL-091
  Scenario: New Battle shows the bots of labelled download folders as their own group after the person's own bots
    Test-type: Unit
    Given bot entries from the person's own bot directories and from a labelled download folder
    When New Battle lists them
    Then the person's own bots are listed first as before, and the downloaded bots follow under a header row with the label and their number, each shown by its own folder name, or by its whole path when another bot in the group has a folder of the same name
    And the header row sits outside the list of the person's own bots, so it is in view however that list is scrolled, the group is closed each time New Battle opens and opens or closes when the header is clicked, its bots can be booted like any other, Boot is offered for a selection in either list, only one of the two lists holds a selection at a time, and a closed group holds none, so Boot never boots a bot the person cannot see
    And without a labelled folder there is no header row and the list is exactly as before

  @RCL-092
  Scenario: Bot Info shows the license a bot declares, and links a standard license to its official text
    Test-type: Unit
    Given bots from the bot directories that declare a standard license, another license text or none, and a bot that joined the battle on its own
    When the person selects each in New Battle
    Then Bot Info shows the declared license as written, or that no license is declared, and beside it a Show button, whose text is a resource, opens the page of a standard SPDX license identifier on the SPDX license list; the button is active only when there is such a page, and it opens no other address
    And a bot that joined on its own shows the license of the bot of the same name, version and authors among all bots in the bot directories for the game type, whatever the filter shows, or that its license is not known when there is none or several that disagree, both in Joined Bots and in Selected Bots, and the page opens through the program's own browser helper, which tells the person when it cannot
    And every text of the field and of the group is in every language file

  @RCL-093
  Scenario: The maintainer finds downloaded bots in New Battle and reads their license
    Test-type: Human
    Given the desktop program started with the menu switched on for the test environment
    When the maintainer downloads bots into a new folder, opens New Battle, opens the group of downloaded bots, selects a bot, boots it, and closes and opens New Battle again
    Then the result says that the folder was added, the group shows the downloaded bots by name after the person's own bots, Bot Info shows the bot's license, the bot boots, and the group is closed again
    And the observer, the conditions, the observation and what was not observed are recorded in the proposal's human evidence, and no human observation is invented

  @RCL-094
  Scenario: The copy level is worked out from the license a bot declares
    Test-type: Unit
    Given licenses as bots declare them: permissive and copyleft licenses on the Blue Oak Council's lists, permissive licenses the council rates Lead, Creative Commons licenses, deprecated SPDX identifiers, expressions, unknown licenses and none
    When the shared library works out their copy level
    Then a permissive license rated Model, Gold, Silver or Bronze is free to copy, in any letter case; a copyleft license, a permissive license rated Lead and a Creative Commons attribution or share-alike license are copy with conditions; a deprecated identifier gets the level of the identifier or expression that replaces it; and an identifier ending in + gets the level of its version
    And an OR expression is at the level of its most permissive choice, an AND expression at the level of its most careful part, and a WITH expression at the level of its license, the operators counting only in capitals as SPDX writes them
    And no license, an empty license, a license on neither list, a Creative Commons non-commercial or no-derivatives license and an expression with parentheses are do not copy, and a license that is not known has no level

  @RCL-095
  Scenario: Bot Info shows the copy level and a team's members, and the Download bots screen shows the level
    Test-type: Unit
    Given bots from the bot directories with different licenses, a team and a bot that joined the battle on its own
    When the person selects each in New Battle, and the Download bots screen lists bots and their results
    Then Bot Info shows the copy level after the license as a shape and words, never as colour alone, with a tooltip saying that the level reads only what the bot declares and is not legal advice, and no level when the license is not known
    And a Team members row lists a team's members by name in order as its configuration file names them, a repeated member each time, or says that the bot is not a team, and a bot that joined on its own shows the members of the bot with the same name, version and authors in the bot directories, or that they are not known
    And the Download bots screen shows the same level beside each bot's license in its list and results, and every text is in every language file

  @RCL-096
  Scenario: The maintainer reads the copy levels and a team's members
    Test-type: Human
    Given the desktop program started with the menu switched on for the test environment
    When the maintainer selects a downloaded bot and bots of their own with different licenses, and a team such as MyFirstTeam, in New Battle, and opens the Download bots screen
    Then Bot Info shows each bot's copy level as a shape and words with its tooltip, the team's members, and the Download bots screen shows the level beside each license
    And the observer, the conditions, the observation and what was not observed are recorded in the proposal's human evidence, and no human observation is invented

  @RCL-097
  Scenario: Sending asks for a synced copy when it lacks a change to the Rumble's workflows, and a refused step is named
    Test-type: Unit
    Given a fake of GitHub with the account's copy, the App installed on it, and the Rumble's main branch ahead of the copy
    When the wizard checks the copy before sending, and the bot is sent
    Then a copy that lacks a change to a file under .github/workflows gets its own state with a button that opens only the copy on GitHub, the wizard looks again by itself and goes on once the copy is synced, and nothing is changed on GitHub
    And a copy that lacks only other changes, or that GitHub's comparison cannot judge, goes on at once as before
    And a branch that GitHub refuses as forbidden gives the same state and opens no pull request when the copy lacks commits of the Rumble, and otherwise the refusal is told; any refusal names its step and keeps GitHub's own message instead of pointing at the App installation, and a read while checking never says that something was sent
    And a send that finds the copy or the App not ready ends with that state and the button that opens the page to set it up, and looks again by itself and goes on once it is set up; an update of the author's open pull request needs no synced copy; a Rumble repository that cannot be read never fails the check
    And the work of the submit and account screens away from their thread ends with a failure whatever is thrown, also an error that is not an exception, as on the Download bots screen, so a screen never waits for an answer that will not come
    And the command line answers copy-behind with the copy's address, and every text is in every language file

  @RCL-098
  Scenario: The maintainer sends a bot from a copy that lacks a change to the Rumble's workflows
    Test-type: Human
    Given the desktop program started with the menu switched on for the test environment, and the maintainer's copy of the bot repository one workflow change behind
    When the maintainer sends a bot
    Then the fourth step asks to sync the copy, after Sync fork on GitHub the window goes on by itself, and the bot is sent and its pull request followed
    And the observer, the conditions, the observation and what was not observed are recorded in the proposal's human evidence, and no human observation is invented

  @RCL-099
  Scenario: The fifth step of Submit or update bot checks the Rumble's rules for the bot before sending
    Test-type: Unit
    Given a fake Rumble with its catalog and its owner and ban lists at one commit, and a staged bot
    When the fifth step prepares the send
    Then a banned account, a disqualified bot and a name that belongs to another owner each give their own text with the account, the bot and the owner, and nothing is offered to send, so pressing send sends nothing, and the line about a new version is then not shown as a success
    And the owner's own bot and a new name are offered to send, a version lower than the active one gives a note and is offered, a version already published is told by the content line, and rules that cannot be read stop nothing, while the screen says that the name, owner and version were not checked
    And the Rumble's newest commit is looked up with the sign-in, because GitHub allows only a few requests an hour without one, and again without it when GitHub refuses the sign-in; owners and bans are checked also when the catalog cannot be read; only who may submit which name stops the send, so the button is never greyed out without a reason; and the screen says when the rules alone could not be read
    And the pre-check gives codes and the values in them to the screen, and every text is in every language file

  @RCL-100
  Scenario: A pull request sent back by the moderation is said on a line of its own, with its reason on the next
    Test-type: Unit
    Given a pull request that the moderation has sent back with a reason
    When the sixth step shows where it stands
    Then the text for a sent-back pull request holds no value, and the reason is shown on a line of its own as plain text, without the code marks, emphasis and line breaks of GitHub's comments and with a link kept as its text and address

  @RCL-101
  Scenario: The maintainer is stopped before sending a bot that belongs to another account
    Test-type: Human
    Given the desktop program started with the menu switched on for the test environment, and RumbleOnboardingScout owned by the outside test account
    When the maintainer stages RumbleOnboardingScout and goes on to the fifth step
    Then the step says that the name belongs to another owner, names the owner, and offers nothing to send
    And the observer, the conditions, the observation and what was not observed are recorded in the proposal's human evidence, and no human observation is invented

  @RCL-102
  Scenario: All bots ignores the selection when a contribution session chooses its next battle
    Test-type: Unit
    Given a session in the all-bots mode with some entries selected, active entries in the catalog, and entries that are not active
    When the session chooses its next battle
    Then every participant is an active entry, the selection does not decide the choice, and entries that are not active are never used
    And a session with no game format chosen gives a wait with the reason code for no format and no battle

  @RCL-103
  Scenario: Prefer selected bots takes a selected entry when a valid battle holds one and falls back otherwise
    Test-type: Unit
    Given a session in the prefer mode with selected entries
    When the session chooses its next battle
    Then the battle holds a selected entry whenever a valid battle with one exists in a chosen format
    And when no valid battle holds a selected entry, or the selected entries are no longer active, the battle is any valid battle of the whole catalog, and a selection of nothing acts as all bots

  @RCL-104
  Scenario: Only selected bots never picks a battle without a selected entry and waits with a reason when none is valid
    Test-type: Unit
    Given a session in the only-selected mode
    When the session chooses its next battle
    Then every battle holds a selected entry
    And when nothing is selected, the selected entries are not active, or no valid battle holds a selected entry, the answer is a wait with its own reason code and the values that explain it, never a battle with other entries, and a wait that the whole catalog could not fill is told apart from one that only the selection could not fill

  @RCL-105
  Scenario: Selected entries and game formats take turns, and every eligible opponent is eventually used
    Test-type: Unit
    Given a session with several selected entries, several game formats and more eligible opponents than a battle holds
    When the session runs battle after battle, recording each in its history
    Then the selected entries take turns, the formats take turns, and over the session every eligible opponent has been used
    And the same request, history and seed give the same battle, and a different seed may give another

  @RCL-106
  Scenario: A battle count ends a session, no count never does, and missing advice blocks nothing
    Test-type: Unit
    Given a session with a battle count, a session without one, and sessions whose published advice is missing, empty or names entries that are not eligible
    When the session chooses its next battle
    Then the session is finished once its history holds the battle count and never finishes when it has no count
    And advice that is missing, empty or unusable never makes an otherwise valid battle impossible, and usable advice is followed

  @RCL-107
  Scenario: A TwinDuel team counts as one selected entry and the two teams never share a member bot
    Test-type: Unit
    Given active TwinDuel teams whose members are active individual bots, some teams sharing a member, and a team whose member is not active
    When the session chooses its next TwinDuel battle with a team selected
    Then the battle holds exactly two teams, one of them the selected team, with disjoint members that together make the pinned number of bots
    And a team whose member is not active is never used, and a selected team with no disjoint partner gives no battle in the only-selected mode

  @RCL-108
  Scenario: A completed ranked result is durable before it is reported kept, and reading it back gives the same record
    Test-type: Integration
    Given a ranked journal of one Rumble environment and a process that adds completed ranked records and reports each one as kept
    When that process is stopped without warning in the middle of adding records, and when a write or its flush fails half way
    Then a failed write is taken back and never leaves bytes that a later line could be glued onto, a record whose write failed is not reported kept, and every record it reported as kept is read back from the file as exactly the record that was added, and the journal opened again lists them in the order they were added

  @RCL-109
  Scenario: A torn last line is ignored and reported, and never costs an earlier record
    Test-type: Unit
    Given a journal whose last line is cut off at any position, as an interrupted write leaves it
    When the journal is opened and read
    Then every earlier record is read, the cut line is never taken for a record unless it is whole, and the read report says that the end is torn and which line cannot be read, and a header or a move line cut at any position costs no record and leaves every state as it was
    And the next record added afterwards is read back on a line of its own, never glued onto the torn bytes, and a file that does not start like a Rumble journal is refused with a plain message

  @RCL-110
  Scenario: A ranked journal belongs to one Rumble environment
    Test-type: Unit
    Given a journal file made for one Rumble environment
    When the program opens it for the other environment
    Then it is refused with a message naming both environments, nothing is read from it or written to it, and a journal made for a schema version this program does not know is refused with the instruction to update the program

  @RCL-111
  Scenario: The same battle is kept once
    Test-type: Unit
    Given a journal that holds a record of one battle
    When a record with the same battle ID is added again, in the same run or after the journal is opened again
    Then nothing is written, the answer says that it was kept before, and the journal still holds one record of that battle

  @RCL-112
  Scenario: A record moves on by a line that points at it, and the lines before it are never changed
    Test-type: Unit
    Given a journal with kept records
    When a record is handed over, accepted, rejected or returned to waiting
    Then a move line is added after the existing lines, the lines written before stay byte for byte as they were, the state is read back after the journal is opened again, a rejected record stays available with its reason code, and a move that does not fit the record's state, or names a record that is not in the journal, is refused

  @RCL-113
  Scenario: A record made on another engine epoch is set aside with its reason and stays available
    Test-type: Unit
    Given waiting records made on different behavior versions and the Rumble's current pin
    When the journal sets aside other epochs
    Then every waiting record made on another behavior version is set aside with the engine-epoch reason and both versions as its details, records of the current epoch keep waiting, nothing is removed, and the set-aside records are still listed

  @RCL-114
  Scenario: The journal offers no way to keep or drop a result by its outcome
    Test-type: Unit
    Given the public interface of the ranked journal
    When it is inspected
    Then its operations are exactly adding a record, reading the records and the read report, the moves tied to batches, receipts and the engine epoch, and opening a journal; none of them removes, hides or filters a record, none takes a function, predicate or comparator, and none takes anything but a battle ID, a reason code with its details, a version number or a whole record to add, so a record can only be chosen by its identity and never by how well the battle went

  @RCL-115
  Scenario: A completed battle of a contribution session becomes exactly one record in the journal, made from what was run
    Test-type: Unit
    Given a session with a chosen battle, its bots placed from the catalog commit, and a worker that completes the battle
    When the session runs the battle
    Then the journal holds exactly one new record with the battle's identity, the game type, the rounds the worker reported, the client, the engine pin, the catalog commit, and each participant's result with the source hash of the bot that was run, and the session reports that the battle started and was kept
    And a result that names other bots than the battle ran, or holds another number of rounds than the battle asked for, is not kept and is reported as not matching, and a battle identity the journal already holds is not counted as kept and ends the session as a journal failure

  @RCL-116
  Scenario: A battle that did not complete becomes no record, and the session goes on until too many in a row fail
    Test-type: Unit
    Given a worker that refuses the job, fails, ends without finishing, exits with an error, writes something that is not a message, or does not start
    When the session runs battles
    Then no record is kept for any of them, each is reported as not kept with its own reason code, the next battle is run, and a completed battle after a failure is kept
    And a battle that did not complete makes the next choice another battle where there is one, and advice that is all or part of it is not taken again in the session, without counting towards the battle count
    And a session set to end when no battle can be chosen ends with the planner's reason instead of waiting
    And after the configured number of battles in a row that were not kept the session ends as given up, without spinning, and a battle for a game format with no rounds set is not run and is reported

  @RCL-117
  Scenario: A bot that cannot be placed is not run and the session chooses another battle instead of stopping
    Test-type: Unit
    Given a catalog whose source for one bot does not match its recorded hash, is missing from the archive, cannot be reached, or is a team with a member of its own folder name
    When the session chooses a battle that holds that bot
    Then the bot is not run and the session reports why it could not be placed; a bot that can never be placed from this commit is left out with every team that holds it from then on, and the session goes on with battles that hold only bots that were placed
    And a source that cannot be reached is tried again after the poll time, is never left out for it, and counts towards giving up like a battle that did not complete
    And a team's members are placed beside the team, two bots whose folders end in the same name are both placed in one battle, a bot already in the cache with a matching hash is reused without fetching, a cached folder that no longer matches its hash is fetched again, and when too few bots remain the session waits with the planner's reason

  @RCL-118
  Scenario: Pause finishes the active battle and waits, finish-and-stop ends after it, and cancel-now ends the worker and keeps nothing
    Test-type: Integration
    Given a running session and, for cancel-now, a real worker process that has started a child process of its own
    When the contributor pauses, asks to finish and stop, or cancels now
    Then pause lets the active battle finish and be kept, reports the pause, runs no further battle until resumed, and reports the resumption
    And finish-and-stop keeps the active battle's result and then ends the session, and cancel-now ends the worker and everything it started, keeps nothing of the battle and ends the session as cancelled, also when the worker has closed its output but is still running
    And cancel-now or finish-and-stop during a pause ends the session without reporting a resumption, and either one asked for while bots are being fetched ends the session before a battle starts

  @RCL-119
  Scenario: A battle count ends the session and the planner's wait is reported once with its reason
    Test-type: Unit
    Given a session with a battle count, and a session whose planner has no valid battle
    When the session runs
    Then it ends as finished after exactly that many kept battles, and counts only kept battles
    And a wait is reported once for each reason with its values instead of on every look, the session looks again after the poll time without running anything, and a request that changes while it waits is used at the next look

  @RCL-120
  Scenario: A real battle through the real worker is kept in the journal and read back
    Test-type: Integration
    Given two sample bots placed from a catalog source, the real battle worker and a journal
    When a session with a battle count of one runs
    Then the journal holds one record whose participants are the two bots with the hashes of the sources that were run, and opening the journal again reads the same record

  @RCL-121
  Scenario: The Rumble's engine pin is read from the result repository and a pin that cannot be read or understood refuses the session
    Test-type: Unit
    Given an environment's result repository whose `engine.json` is valid, missing, unreachable, not the expected shape, of another schema version, holds a value that cannot be for a chosen format, or pins a rule for a chosen format that this program does not run
    When a session prepares its engine pin
    Then a valid pin gives the behavior version and each pinned format's rounds, battlefield and participant count
    And every other case refuses the session before any battle with its own reason code and the values that explain it, a newer schema version is reported as such whatever else it changed, and a format nobody chose is not read and never refuses a session

  @RCL-122
  Scenario: A session is refused before any battle when this program cannot run under the pin, and otherwise runs exactly what the pin says
    Test-type: Unit
    Given a pin whose behavior version differs from this program's, that does not name a chosen format, or whose battlefield or participant count for a chosen format is not what the battle worker runs for it
    When a session prepares its engine pin
    Then the pin is refused with a reason code naming the pin's value and this program's, so the session can end before its first battle, and a different behavior version is reported as such whatever else the pin changed
    And with a pin this program can run, the session runs each battle with the pin's rounds and battle size and keeps the pin's behavior version in each record

  @RCL-123
  Scenario: The published advice is read for each format and is optional
    Test-type: Unit
    Given an advice file of needed battles for a format, a missing one, one that cannot be fetched, and one of another schema or game type
    When a session asks for its advice
    Then the advised groups are given best first and an advised battle is run before others, a missing or unknown file gives no advice and the session runs by coverage
    And the advice is read again only after the refresh time, a read that fails keeps the last advice of that format and only that format is tried again at the next look, and an interrupted read counts as failed and keeps the interruption

  @RCL-124
  Scenario: The real public engine pin and advice files are understood
    Test-type: Integration
    Given the public result repository on the network
    When the engine pin and the advice of each format are read with the readers
    Then the pin is understood with a positive behavior version and the three ranked formats, and each advice file is either understood or absent, never misread

  @RCL-125
  Scenario: Batches hold every waiting record of the current epoch in the shape the result data validates
    Test-type: Unit
    Given a journal with waiting records of several formats and clients, records of another engine epoch, and more records than one batch may hold
    When the program makes batches to send
    Then every waiting record of the current epoch is in exactly one batch, in the order kept, with no selection, each batch holding one to sixty records of one client and staying within the size bound
    And each record has the battle identity, completion time, client identity, behavior version, game type, the rounds the battle ran and the pin's battlefield, and every participant's result marked as a team only in TwinDuel, while records of another epoch are set aside and never sent

  @RCL-126
  Scenario: A batch is sent only after its records are handed over, and a failed send or a crash never loses a record
    Test-type: Unit
    Given a signed-in contributor and waiting records
    When a batch is sent, GitHub refuses or cannot be reached, or the program stopped after handing records over and before the issue was recorded
    Then the issue has a `[result]` title unique to the batch and exactly one fenced json envelope, and its records were handed over in the journal before it was opened
    And a send GitHub refuses returns its records to waiting with a reason code, a send with no clear answer (no answer, a server error or an answer that cannot be read) keeps the batch handed over and sends it again under its own identity, a batch handed over but never recorded as an issue is sent again with its records that have not moved on, except those of another engine epoch, which are set aside, and a torn last line of the list of sent batches is never read

  @RCL-127
  Scenario: Receipts acknowledge each record only by its own line
    Test-type: Unit
    Given sent batches whose issues have a processed receipt, a refused-batch receipt, no receipt yet, or comments by other accounts
    When the program collects receipts
    Then a record the receipt accepted is accepted, a record it rejected is rejected with the receipt's reason and not sent again by itself, a record the receipt does not name waits again, and a batch with no receipt yet stays handed over
    And a batch refused before its records could be read returns them to waiting and stops sending, also after a restart, until the contributor lifts the stop, only the receiving workflow's own comments are read as receipts, also past the first hundred comments, and an issue that is gone returns its records to waiting without holding up the receipts of other batches

  @RCL-128
  Scenario: Without a usable sign-in or client ID nothing is sent
    Test-type: Unit
    Given no saved sign-in, a sign-in that cannot be renewed, a token GitHub no longer accepts, a pin that refuses this program or no longer names a record's format, or a waiting record with no client ID
    When the program tries to send
    Then nothing of that is sent, the records keep waiting, and a reason code says why, a refused token is not used again, and a format the pin no longer names holds back only its own records

  @RCL-129
  Scenario: Sending and receipts run between a session's battles
    Test-type: Unit
    Given a session with automatic sending
    When battles complete and the session waits
    Then results are sent when enough wait or after the set time, receipts are collected at their own interval, a failed send is not tried again sooner than the least gap, a journal that cannot be written ends the session as a journal failure, and any other failure of sending is reported without ending the session

  @RCL-130
  Scenario: One real batch is sent and receipted in the test Rumble
    Test-type: Integration
    Given a saved sign-in to the test Rumble's App for a registered account and a waiting record of two active catalog bots in the shape a completed battle gives
    When the program sends it and collects receipts until the receiving workflow answers
    Then the issue is opened in the test result repository and the record is accepted or rejected by its own receipt line

  @RCL-131
  Scenario: A session's Python Bot API is installed fresh with the supported Python, its hash kept in memory, and an unusable Python leaves the Python bots out
    Test-type: Unit
    Given the Python on the contributor's path, the version the Rumble supports, and the program's own Python Bot API
    When a session that may run a Python bot sets up Python
    Then the program's own Bot API and only what it imports when a bot runs are installed into a new folder of the session's own with that Python, the wheel's file name and those requirements' version bounds taken from the wheel's own metadata and pip's own cache not used, the digest is kept in memory and in no file, and the folder is removed when the session ends
    And the folders of sessions that ended without closing are removed by the next session, while a session that still runs or is just starting keeps its folder, no other session's lock file is removed, a folder that could not be removed keeps its lock file so a later session finds it, and without the Rumble's list of versions the wheel's own lowest Python is required
    And a Python that is missing or older than the Rumble supports, a failed install or a program without its Python Bot API gives a reason with the version needed and found, and leaves nothing behind

  @RCL-132
  Scenario: A battle with a Python bot gets its own checked copy through the worker's environment
    Test-type: Unit
    Given a session whose Python is set up
    When a battle runs
    Then a battle that holds a Python bot gets a copy of the session's Bot API in a new folder of the battle's own that is not the bots' temporary folder, named to the worker on Python's module path with Python told not to write compiled files nor read the user's own packages and `RUMBLE_PYTHON` naming the contributor's Python, and no folder of the Rumble's state is named to it
    And a battle without a Python bot gets none of it, and a battle whose copy cannot be made does not start and keeps the reason, whatever kind of failure it was

  @RCL-133
  Scenario: A change to one battle's copy or to the session's folder never reaches another battle
    Test-type: Unit
    Given a session whose Python is set up and battles that ran with it
    When a bot changes its battle's copy, or something changes the session's folder
    Then the next battle's copy is the session's Bot API as installed, a session's folder that no longer matches its hash is installed again before a battle gets a copy, two sessions never share a folder, and the runtime dependencies installed are exactly what the Python Bot API imports from outside the standard library

  @RCL-134
  Scenario: One real session in the test Rumble runs a Python catalog bot and its result is accepted
    Test-type: Integration
    Given the maintainer's saved sign-in and registration for the test Rumble, the supported Python, and the carried battle worker and Python Bot API
    When `rumble contribute -e test --battles 1 --format 1v1 --mode only --bot "Orbit 1.0.0"` runs
    Then Python is set up, a battle holding the Python bot is kept and sent, and the receiving workflow accepts it by its own receipt line

  @RCL-135
  Scenario: The desktop program fetches the command line program only after asking
    Test-type: Unit
    Given a desktop program without the command line program of its own version
    When the Contribute battles screen needs it to start a session
    Then the screen says what it will fetch and how large it is and asks first, fetches `rumble.jar` of its own version with the checked fetch only when the person agrees, starts nothing when the person declines or the fetch fails, and says why

  @RCL-136
  Scenario: The Contribute battles screen starts a session without a window and follows and controls it
    Test-type: Unit
    Given the screen's choices of mode, bots, formats and a battle count or keep running
    When the person starts the session and then pauses, resumes, stops or stops it now
    Then the screen starts `rumble contribute` with those choices as a process without a console window, with the desktop program as owner unless the setting says to keep contributing, follows its status through the session folder and shows the battle running, kept and not kept, why it waits, whether Python is ready and the results, and puts each control request in the session folder
    And every change reaches the screen through its event queue in order, every text comes from the resource files with the same keys in every language file, and a missing sign-in, registration, a pin this program cannot run under, or a missing Python is shown plainly with the way to fix it where there is one

  @RCL-137
  Scenario: The desktop program finds a running session again and shows the summary of one that ended
    Test-type: Unit
    Given a session of the environment that runs, ended while the desktop program was closed, or ended without closing
    When the desktop program starts, or the battles of the session it follows are done
    Then the screen shows the running session again with its controls, or the summary of the last one, or that it ended unexpectedly with what it kept still in the journal, and when the battles are done it shows the summary, and while the screen is closed the desktop program gives a short notice that does not have to be clicked away

  @RCL-138
  Scenario: Closing the desktop program while a session runs asks the first time and keeps the answer
    Test-type: Unit
    Given a session that the desktop program started and follows
    When the desktop program closes
    Then the first time it asks whether to keep contributing in the background or to stop after the running battle and keeps the answer as a setting the person can change, releases the session from its owner before it closes to keep contributing, and otherwise closes so the session stops after its battle; with no session running it asks nothing

  @RCL-139
  Scenario: One real session started from the screen in the test Rumble is followed, paused, stopped and found again after a restart
    Test-type: Integration
    Given the maintainer's saved sign-in and registration for the test Rumble and the desktop program with the developer switch on
    When the maintainer starts a session from the Contribute battles screen, pauses and resumes it, closes the desktop program to keep contributing, opens it again, stops the session, and once more closes the desktop program choosing to stop
    Then the screen finds the session again after the restart, the result is accepted by the receiving workflow, and the session started the second time ends after its battle when the desktop program closes

  @RCL-140
  Scenario: One real TwinDuel session in the test Rumble runs Rumble teams and its result is accepted
    Test-type: Integration
    Given the maintainer's saved sign-in and registration for the test Rumble, whose teams name their members as `<name> <version>`
    When `rumble contribute -e test --battles 1 --format twinduel` runs
    Then a TwinDuel battle of two catalog teams is kept and sent, and the receiving workflow accepts it by its own receipt line

  @RCL-141
  Scenario: Get started is the first entry of the Rumble menu and its texts are in every language
    Test-type: Unit
    Given the desktop program's Rumble menu and its resource files in English, Danish, Spanish and Catalan
    When the menu's source and the screen's texts are read
    Then Get started is the Rumble menu's first entry and is added only once
    And every text of the screen, its three cards (title, what it needs and its button), its Beta line, its window title and its menu entry is in every language's resource file, and none of them holds a long dash

  @RCL-142
  Scenario: Each Get started button leads to the right page or screen, and signing in comes first when it is needed
    Test-type: Unit
    Given the Get started screen for a Rumble environment
    When a button is pressed with and without a saved sign-in for that environment on this computer
    Then Make a bot opens the "My first bot" tutorial on robocode.dev and the feedback button opens the repository's Rumble feedback form, whether signed in or not
    And Submit a bot opens Submit or update bot and Contribute battles opens Contribute battles when a saved sign-in exists, and both open the GitHub account screen when none exists
    And a saved sign-in for another environment does not count, signing out removes it, and a saved file that cannot be read still counts as one, because only whether the file is there is looked at

  @RCL-143
  Scenario: The maintainer tries the Get started screen in a light and a dark theme
    Test-type: Human
    Given the desktop program with the Rumble menu, in a light and in a dark theme
    When the maintainer opens Rumble, Get started and presses each button
    Then the cards are readable in both themes and every button leads where the buttons' criterion says

  @RCL-144
  Scenario: Rankings opens the selected environment's dashboard and Help opens the Rumble guide
    Test-type: Unit
    Given the environment list with the public Beta and the development Rumble
    When the Rumble menu's Rankings or Help decides which page to open
    Then Rankings opens the dashboard of the environment the menu talks to, https://robocode-dev.github.io/rumble-data/ for the public Beta, which is used unless a developer names the development Rumble, and https://robocode-dev.github.io/rumble-data-test/ only for the development Rumble, and Help opens https://robocode.dev/rumble/
    And a program whose environment variable is unset or blank opens the public Beta's rankings and never a test page, and a dashboard address that is not an https page with a host and no user part is never opened

  @RCL-145
  Scenario: The Rumble menu lists its entries in the mockups' order
    Test-type: Unit
    Given the desktop program's Rumble menu and its menu texts in English, Danish, Spanish and Catalan
    When the menu's source is read
    Then the built entries follow the mockups' order, Get started, Submit or update bot, My bots, Download bots, Contribute battles, Results and queue, Rankings, GitHub account and Help, with the entries not yet built left out, and the last three are Rankings, GitHub account and Help
    And no entry is outside that order or listed twice, and every entry has its text in every language

  @RCL-146
  Scenario: The maintainer opens Rankings and Help from the Rumble menu
    Test-type: Human
    Given the desktop program with the Rumble menu
    When the maintainer chooses Rumble, Rankings and then Rumble, Help
    Then the browser opens the selected environment's rankings and then the Rumble guide

  @RCL-147
  Scenario: My bots lists one row per bot of the signed-in account and nothing of another account
    Test-type: Unit
    Given a catalog with entries owned by the signed-in account and by another account, and pull requests the account sent with the program's branch names, other branches and pull requests of another account
    When My bots builds its rows
    Then there is one row per bot name of the account, from the catalog entries owned by an owner record that lists the login, or by the login itself unless another owner's record has that id without listing the login, and from the pull requests it sent, read by their title `Add <name> <version>` or `Update <name> to <version>` on the branch the program names for them, sorted by name, with the newest version, the language of the catalog entry added last when the catalog knows the bot, and Open's page as the pull request shown or else the bot's folder in the bot repository
    And entries and pull requests of another account, a pull request whose title or branch is not the program's, and a pull request page that is not on github.com are never used, and an answer from GitHub that is not a list is a failure, not an empty list

  @RCL-148
  Scenario: Each row of My bots says where the bot stands, and a failed reading is said
    Test-type: Unit
    Given bots in the catalog, sent and still open, merged, closed, and no longer active
    When My bots builds its rows and shows the reading
    Then an open pull request, also an older one than a closed one, shows its state, such as sent back with the reason, before the version in the Rumble, which the row then also names, a merged pull request newer than the catalog shows that it is merged, an active catalog entry shows that it is in the rankings, an entry that is not active shows its status, and a closed pull request shows that it was closed
    And retired bots and sent bots whose pull request was closed are hidden unless the person asks to see them, while every other row, a disqualified bot too, always shows
    And a reading that failed, or that needs a sign-in, is shown as such and never as an empty list, and a reading that arrives after the screen is closed is thrown away

  @RCL-149
  Scenario: My bots follows Submit or update bot in the menu, needs a sign-in, and opens only GitHub pages
    Test-type: Unit
    Given the desktop program's Rumble menu, the My bots screen's source and its resource files in English, Danish, Spanish and Catalan
    When they are read
    Then My bots is the entry right after Submit or update bot, and every text of the screen, its title and its menu entry is in every language without a long dash
    And Open opens a row's page only when it is an https page on github.com and is switched off otherwise, its buttons show the program's own tooltip with texts from the hint files in every language, and the screen's code offers the GitHub account screen when a sign-in is needed

  @RCL-150
  Scenario: The maintainer opens My bots against the test Rumble
    Test-type: Human
    Given the desktop program set to the development Rumble with the maintainer's saved sign-in
    When the maintainer opens Rumble, My bots and presses Open and Update on a row
    Then the screen lists the account's bots with where each stands, Open opens the bot's page on GitHub and Update opens Submit or update bot
```
