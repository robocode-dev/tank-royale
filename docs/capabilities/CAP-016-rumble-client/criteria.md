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
    And New Battle reads the bot directories again after a download only while it is open, and otherwise when it opens next, so the download screen never waits for it, and the Bot Root Directories dialog, when open, reads them again, so it never writes back an old list
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
```
