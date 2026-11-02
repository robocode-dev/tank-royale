---
id: CAP-014-criteria
type: criteria
status: draft
links: [CAP-014]
title: Acceptance criteria for CAP-014 (rumble-bot-catalog)
ac-prefix: RBC
provenance: inferred
reversal-cost: low
---

```gherkin
Feature: rumble-bot-catalog — Rumble bot catalog

  @RBC-011 @retired
  Scenario: Moderators distinguish strategic contributions from cosmetic near-duplicates
    Test-type: Human
    Given a submission is reported as a near-duplicate of an existing catalog bot
    When an impartial moderator compares the relevant source and battle strategy and considers the author's explanation
    Then the moderator may reject a near-duplicate whose differences are primarily cosmetic and make no substantial contribution to battle strategy
    And licensed reuse with a substantial strategic contribution is eligible under this rule
    And similar behavior alone is insufficient grounds for rejection
    And the decision identifies the compared entry, evidence and reasoning and allows reconsideration by a moderator without a conflict of interest

  @RBC-012
  Scenario: Rewritten derivatives need a substantial independent strategic contribution
    Test-type: Human
    Given a submission is reported as derived from an existing catalog bot
    When an impartial moderator compares the underlying implementation and strategic decisions using source and battle behavior and considers the author's explanation
    Then the moderator may reject a derivative that retains substantially the same strategy without a substantial independent strategic contribution even if its source is extensively rewritten, reorganized or translated
    And cosmetic changes or small parameter adjustments do not qualify when the underlying strategy remains substantially unchanged
    And licensed reuse with a substantial strategic contribution is eligible under this rule whether or not the bot's score improves
    And similar behavior alone is insufficient evidence of derivation from the compared bot
    And the decision records the compared entry, evidence of derivation and reasoning about the strategic contribution and allows reconsideration by a moderator without a conflict of interest

  @RBC-001 @draft
  Scenario: A valid source-only submission becomes an active catalog entry
    Test-type: Integration
    Given a pull request contains a booter-convention bot directory with an allowed SPDX license and official Bot API dependency
    When the submission validator and source-run smoke check succeed and the pull request is merged
    Then the generated `bots/index.json` contains one active entry with its name, version, platform, path, source hash, owner, and authors
    And the catalog declares its schema version and generating commit

  @RBC-002 @draft
  Scenario: An invalid submission cannot enter the catalog
    Test-type: Integration
    Given a pull request contains a bot with an invalid structure, disallowed executable artifact, missing or disallowed license, unapproved dependency, or failed smoke boot
    When the submission validator runs
    Then the validation check fails with a diagnostic that identifies the violated rule
    And neither the bot nor an altered generated catalog is merged

  @RBC-003 @draft
  Scenario: Ownership and version rules protect the active catalog
    Test-type: Integration
    Given a bot name is already owned and represented by an active catalog entry
    When an unregistered account submits that name or an owner changes its source without increasing its version
    Then validation rejects the submission
    And an approved version increase supersedes the old active version without deleting its historical record

  @RBC-004 @draft
  Scenario: A TwinDuel team is published with immutable member identities
    Test-type: Integration
    Given a valid TwinDuel team entry names exactly two active member bots
    When the generated catalog is synchronized for ranked clients
    Then the team entry carries both member identities in `teamMembers` while individual entries carry an empty list
    And generation rejects a missing, inactive, unknown, or nested team member

  @RBC-005 @draft
  Scenario: Hourly catalog polling publishes only changed reviewed content
    Test-type: Integration
    Given the normalized local catalog and the reviewed source catalog
    When the scheduled synchronization checks the source
    Then a changed bot or version is stored and regenerated into current rankings
    And identical content is not rewritten or aggregated
    And, when no independent publication state changed, no commit or deployment occurs
    And catalog and result writers cannot overlap their publication sections

  @RBC-006 @draft
  Scenario: TypeScript API runtime peers are distinguished from unrelated bot libraries
    Test-type: Integration
    Given a source-only TypeScript bot declares the official Tank Royale Bot API and its API-declared runtime dependencies
    When the submission validator checks its dependency declarations and committed lockfile where required
    Then the official API and its required runtime peers are accepted
    And an unrelated declared bot library is rejected with an actionable diagnostic
    And generated dependency directories remain outside the submitted bot source tree

  @RBC-007 @retired
  Scenario: Every incoming pull request receives a first moderation decision
    Test-type: Integration
    Given an incoming pull request from any GitHub contributor
    When trusted moderation checks its exact head commit
    Then a single-bot source submission is assessed without giving its code write credentials
    And a team, mixed-file, or non-bot pull request is held for Flemming with the specific reason

  @RBC-010 @draft
  Scenario: Every incoming pull request receives a first decision and exceptions reach the moderator team
    Test-type: Integration
    Given an incoming pull request from any GitHub contributor
    When trusted moderation checks its exact head commit
    Then a single-bot source submission is assessed without giving its code write credentials
    And a team, mixed-file, suspicious, or non-bot pull request stays pending with a reason for the Tank Royale Rumble moderators
    And only an active member of the moderator team may clear a pending moderation status with a recorded reason for that commit
    And a non-member, pending member, or failed membership check cannot clear the status
    And a human-cleared status does not merge the pull request or bypass its required review

  @RBC-008 @draft
  Scenario: A bot must safely complete a real battle before approval
    Test-type: Integration
    Given a bot-only pull request with permitted source, dependencies, and launchers
    When its bot runs for one complete round against a trusted reference in an isolated container
    Then successful source, security, and battle checks approve its exact commit
    And an unstartable or failing bot requests changes with an actionable diagnostic
    And a suspicious source pattern or unavailable test infrastructure is held for human judgment

  @RBC-009 @draft
  Scenario: Only clean bot submissions merge and publish automatically
    Test-type: Integration
    Given a clean moderation verdict and required status for the current pull request head
    When the trusted moderation App merges the bot-only pull request
    Then the post-merge publisher regenerates the catalog from trusted main without running bot source
    And generated catalog files enter main only through a pull request whose exact contents match trusted regeneration and whose head the moderator App approves
    And a newer commit, failing or pending moderation status, or changes outside one bot directory cannot auto-merge

  @RBC-013
  Scenario: An author's bot folder is recognised before submission
    Test-type: Unit
    Given a folder on the author's computer that holds a bot or team configuration file named after the folder
    When the folder is inspected
    Then the bot's name, version, authors, license, description, platform and team members are read from that file
    And the language is taken from the declared platform, and a team folder is told apart from an individual bot
    And a folder with no configuration file named after it, or with an unreadable one, is reported as not a bot project with the reason

  @RBC-014
  Scenario: The author gets the repository's metadata verdict before submitting
    Test-type: Unit
    Given a bot or team folder with its configuration file
    When it is validated
    Then it passes when name, version, authors and an allowed license are present, the folder is named as the bot, the platform matches the language and a team lists exactly two members and nothing else
    And each missing, mismatched or disallowed value is reported with the file and the change that fixes it

  @RBC-015
  Scenario: Launchers and source are checked as the bot repository checks them
    Test-type: Unit
    Given an individual bot folder in one of the four official languages
    When it is validated
    Then it passes when both launcher scripts exist and a source file of the language refers to the official Bot API
    And a missing launcher, missing source file or missing API reference is reported with the change that fixes it
    And the Unix launcher's executable bit is not checked on the author's computer, and the report says it will be set when the submission is staged

  @RBC-016
  Scenario: Generated, binary and restricted content is reported
    Test-type: Unit
    Given a bot folder that holds generated dependency or build folders, binary or archive files, or source using a construct the repository sends to a moderator
    When it is validated
    Then each such folder, file and construct is reported by name with the change that removes it
    And a folder without them passes, and cache folders are not reported

  @RBC-017
  Scenario: TypeScript dependencies are limited to the official API and its runtime peer
    Test-type: Unit
    Given a TypeScript bot folder with its package manifest and lockfile
    When it is validated
    Then it passes when only the official API and its declared runtime peer are declared and locked at registry versions with integrity, and the lockfile matches the manifest
    And an unrelated, path, URL or alias dependency, a lockfile that disagrees with the manifest, or a missing runtime peer is reported with the change that fixes it

  @RBC-018
  Scenario: Every problem is reported at once with a stable code and a fix
    Test-type: Unit
    Given a bot folder with several different problems
    When it is validated
    Then the report lists every problem, each with a stable code, the file it concerns and a plain-word change that fixes it
    And a folder with no problems has an empty list of problems

  @RBC-019
  Scenario: The bot project library contains no desktop or GitHub code
    Test-type: Unit
    Given the bot project library's source and its project dependencies
    When the build checks its dependency rules
    Then the library depends on neither the desktop program, the battle runner nor the shared Rumble library, and imports no Swing, AWT or network client class
    And a deliberate violation makes the check fail

  @RBC-020
  Scenario: A staged copy holds only the files a bot is made of
    Test-type: Unit
    Given a bot project folder that also holds build output, dependency, version-control, editor and cache folders, symbolic links and unrelated files
    When the project is staged
    Then the copy holds the bot's configuration file, its two launcher scripts, the source files of its language, the dependency declaration files of its language and an optional license file, and nothing else
    And everything left out is listed as not included, grouped by folder with a file count and a reason, and a symbolic link is never followed
    And source-looking files inside build, dependency, version-control, editor and cache folders are not taken

  @RBC-021
  Scenario: The staged copy is laid out like the bot repository and the original project is never changed
    Test-type: Unit
    Given a valid bot or team project folder and a staging folder outside it
    When the project is staged
    Then the copy is at `bots/<language>/<Name>/` below the staging folder, a team holds only its configuration file, and the project folder is byte for byte as it was before
    And a staging folder inside the project, a project inside the staging folder, and a folder that is not empty and was not made by staging are refused before anything is written

  @RBC-022
  Scenario: Fixes are made only in the copy and are listed
    Test-type: Unit
    Given a project whose files have Windows line endings
    When the project is staged
    Then every included file has Unix line endings in the copy, nothing else in a file is changed, and the files whose bytes changed are listed
    And the Unix launcher script is recorded as executable in the description, and the original files keep their line endings

  @RBC-023
  Scenario: A staged submission is a copy that passes the checks, described exactly
    Test-type: Unit
    Given a project that passes the checks once the left-out files are removed
    When the project is staged
    Then the result describes the name, version, language, license, authors, staging folder, every file with its size and whether it is executable, the files not included, the fixes made and the content hash of the copy
    And staging the same project twice gives the same files and the same hash, and the hash equals the one the bot repository's own rule gives for that folder
    And a project whose copy fails the checks gives the problems in plain words and no usable copy

  @RBC-024
  Scenario: Every active published bot stages to the hash the bot repository recorded for it
    Test-type: Integration
    Given the active bots of the public Rumble fetched and verified against the catalog
    When each fetched bot is staged
    Then the staged copy passes the checks and its hash equals the hash recorded in the catalog

  @RBC-025
  Scenario: The program tells whether the account has its own copy of the bot repository that it can send to
    Test-type: Unit
    Given a signed-in account and an environment
    When the account is checked for readiness to send a bot
    Then the answer is ready only when the account has a copy of that environment's bot repository that really is a copy of the Rumble's repository and the Rumble sign-in App is installed on it
    And otherwise the answer says which of these is missing: no copy yet with the address of the page that makes one, the App not installed on the copy with the address of the page that installs it, or a repository of the same name that is not a copy of the Rumble's
    And the check only reads and changes nothing on GitHub

  @RBC-026
  Scenario: A staged bot is committed exactly as staged, with the launcher executable and the old folder replaced
    Test-type: Unit
    Given a staged submission and an account that is ready
    When the bot is sent
    Then one commit on a new branch built from the Rumble repository's current head holds the staged files under `bots/<language>/<Name>/`, the Unix launcher with mode `100755` and every other file with mode `100644`
    And when the bot's folder already exists upstream, files that are no longer in the staged copy are removed from the folder, and nothing outside the folder is changed
    And before a pull request is opened the identifier GitHub gives each committed file is compared with the one computed locally from the staged file, and any difference stops the sending with no pull request opened

  @RBC-027
  Scenario: The pull request states the facts of the submission without exposing the author's private file names
    Test-type: Unit
    Given a staged submission that left out files and converted line endings in some
    When the bot is sent
    Then the pull request is opened from the account's branch to the Rumble repository with the title `Add <Name> <version>`, or `Update <Name> to <version>` when the bot's folder already exists upstream
    And the text gives the name and version in code format, the folder, the license, the number of files, the content hash, the number of converted files and the executable launcher, what the program checked and what it did not, and that the author confirmed the right to publish the source
    And no name of a file that was left out of the copy appears in the title, the text or the branch name
    And a name or version with any character other than letters, digits, dots, hyphens and underscores is refused before anything is sent

  @RBC-028
  Scenario: Nothing is sent without the author's confirmation of the license
    Test-type: Unit
    Given a staged submission and an account that is ready
    When the bot is sent without the confirmation that the author agrees to publish the source under its declared license
    Then the sending is refused with a plain reason and no request that changes anything on GitHub is made

  @RBC-029
  Scenario: Each bot has at most one open pull request per account, and an interrupted sending never duplicates it
    Test-type: Unit
    Given an account that may already have a branch or an open pull request for the bot
    When the same bot is sent again
    Then an open pull request for the bot whose branch holds exactly the staged files is returned unchanged
    And an open pull request whose branch holds other files gets one new commit on that branch and an updated title and text, and no second pull request is opened
    And with no open pull request the branch is `submit/<Name>-<version>`, an existing branch holding exactly the staged files is used for the pull request, and a name already used by a closed or merged pull request is followed by a number so that earlier pull request stays as history
    And no branch is ever forced, overwritten or deleted

  @RBC-030
  Scenario: A sending that cannot finish says why in plain words and leaks no token
    Test-type: Unit
    Given an account that is ready
    When GitHub does not accept the sign-in, cannot be reached, limits the requests, refuses the branch or answers unexpectedly
    Then the result says which of these happened in plain words, tells the person to sign in again when the sign-in was not accepted, and no pull request is reported as opened
    And the token appears in no address, result text, request body or console output

  @RBC-031
  Scenario: The Rumble's owner and ban lists are read beside its catalog at a given commit
    Test-type: Unit
    Given a commit of a Rumble's bot repository with its owner list and ban list
    When the rules are read
    Then each owner's account names and bot names, the banned accounts and the disqualified bot names are available
    And a missing or malformed list, or a schema version the program does not know, is reported in plain words and no partial rules are returned

  @RBC-032
  Scenario: The pre-check names what the Rumble would refuse, with the change that fixes it
    Test-type: Unit
    Given a staged bot, the sending account, the catalog and the rules
    When the bot is checked before sending
    Then a banned account, a disqualified bot, a bot that belongs to another owner, the same version already published with different source and the identical version already active are each reported as a problem in plain words with the change that fixes it
    And a bot with a new name, and a new version of a bot that the account's owner already holds, have no problem

  @RBC-033
  Scenario: A lower version than the active one is a warning, never a problem
    Test-type: Unit
    Given a staged bot whose version is lower than the active version of the same bot
    When the bot is checked before sending
    Then the report holds a warning that the Rumble accepts it but the older version would replace the newer, and no problem
    And versions that are not plain dotted numbers are not compared and give no warning

  @RBC-034
  Scenario: The pre-check only advises and the sending never depends on it
    Test-type: Unit
    Given the sending code
    When it sends a bot that the pre-check would report a problem for
    Then the sending behaves exactly as it does without the pre-check, and the source of the sending code refers to neither the pre-check nor the rules

  @RBC-035
  Scenario: An open pull request is reported in one plain state, read-only
    Test-type: Unit
    Given an open pull request for a bot and the statuses and reviews the moderation workflow wrote for it
    When its progress is read
    Then the state is waiting for the checks when no moderation status exists, being checked while the status is pending, sent back with the moderator's reason when the status failed or changes were requested, waiting for a human moderator when that is the pending reason, and accepted when moderation passed
    And no request that changes anything on GitHub is made and the token appears in no address, result text or console output

  @RBC-036
  Scenario: A finished pull request is reported as merged, in the catalog or closed, and unknown wording is not guessed
    Test-type: Unit
    Given a pull request that is merged or closed, and a catalog
    When its progress is read
    Then a merged pull request is reported as merged while the catalog lacks its version and as in the catalog with the published version once the catalog holds it, and a closed pull request that was not merged is reported as closed
    And a pending status with wording the program does not know is reported as being checked with a note that the wording was not understood

  @RBC-037
  Scenario: Every active public bot passes the pre-check as its owner and is refused as another account
    Test-type: Integration
    Given the active bots of the public Rumble with its owner and ban lists
    When each bot is checked as its own owner and as an unrelated account
    Then as its owner it has no problem other than the identical version already being active, and as the unrelated account it is reported as belonging to another owner
```
