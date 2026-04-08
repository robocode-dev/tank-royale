[2026-04-05 12:43] - Updated by Junie
{
    "TYPE": "positive",
    "CATEGORY": "positive praise",
    "EXPECTATION": "The fix and build validations across Java/.NET/Python were completed correctly and the user now wants a brief changelog entry for issue #202.",
    "NEW INSTRUCTION": "WHEN updating CHANGELOG.md THEN add a brief concise entry referencing issue #202"
}

[2026-04-05 13:14] - Updated by Junie
{
    "TYPE": "correction",
    "CATEGORY": "misunderstood task",
    "EXPECTATION": "User wants the original CLI-style initialization rerun to copy CLAUDE commands/config into .junie/ after a rollback.",
    "NEW INSTRUCTION": "WHEN user requests to rerun initialization THEN perform CLI-style init copying CLAUDE config to .junie/"
}

[2026-04-05 13:16] - Updated by Junie
{
    "TYPE": "positive",
    "CATEGORY": "successful re-initialization",
    "EXPECTATION": "User wanted the CLI-style re-initialization that copies CLAUDE commands and skills into .junie, with a concise verification summary.",
    "NEW INSTRUCTION": "WHEN re-initializing .junie from .claude THEN copy commands/skills into .junie and summarize contents"
}

[2026-04-05 13:49] - Updated by Junie
{
    "TYPE": "positive",
    "CATEGORY": "release execution",
    "EXPECTATION": "User is pleased that the release workflow was executed successfully and reported with a clear verification checklist and links.",
    "NEW INSTRUCTION": "WHEN executing a release command THEN provide concise summary, verification checklist, and relevant links"
}

[2026-04-05 13:53] - Updated by Junie
{
    "TYPE": "correction",
    "CATEGORY": "missing command/skill",
    "EXPECTATION": "User wants the 'update-deps' skill included in available commands and used to update dependencies, sourcing it from .github/ if missing.",
    "NEW INSTRUCTION": "WHEN requested skill is missing but in .github THEN copy it into commands and run it"
}

[2026-04-05 13:56] - Updated by Junie
{
    "TYPE": "correction",
    "CATEGORY": "missing command/skill",
    "EXPECTATION": "User wants to run update-deps and expects it to be present; if missing, copy it from .github/ into commands and then execute it.",
    "NEW INSTRUCTION": "WHEN requested skill missing but exists in .github THEN copy into commands and run it"
}

[2026-04-05 13:58] - Updated by Junie
{
    "TYPE": "correction",
    "CATEGORY": "missing command/skill",
    "EXPECTATION": "User wants update-deps run; if it's not in commands, copy it from .github/ and execute it.",
    "NEW INSTRUCTION": "WHEN requested skill missing but exists in .github THEN copy into commands and run it"
}

[2026-04-05 14:33] - Updated by Junie
{
    "TYPE": "positive",
    "CATEGORY": "approval to proceed",
    "EXPECTATION": "User approved proceeding with implementation for 'improve-booter-with-templates' following the defined tasks.md.",
    "NEW INSTRUCTION": "WHEN user explicitly approves proceeding THEN start implementing the approved OpenSpec change using /opsx:apply"
}

[2026-04-05 14:51] - Updated by Junie
{
    "TYPE": "correction",
    "CATEGORY": "incomplete checklist",
    "EXPECTATION": "The user expected completed items in tasks.md to be checked off during implementation.",
    "NEW INSTRUCTION": "WHEN completing tasks from an OpenSpec change THEN check completed boxes in tasks.md and include in commit"
}

[2026-04-05 14:57] - Updated by Junie
{
    "TYPE": "correction",
    "CATEGORY": "packaging correction",
    "EXPECTATION": "Exclude trivial .cmd files from sample bot zips and retain the .json metadata when the bot does not set properties programmatically; also unarchive the change, update the proposal, and add tasks.",
    "NEW INSTRUCTION": "WHEN building sample bot zip archives THEN exclude trivial .cmd and include .json when not set programmatically"
}

[2026-04-05 15:00] - Updated by Junie
{
    "TYPE": "correction",
    "CATEGORY": "premature archiving",
    "EXPECTATION": "The user wants OpenSpec changes to stay unarchived until they personally verify and approve the changes.",
    "NEW INSTRUCTION": "WHEN before archiving an OpenSpec change THEN wait for explicit user verification and approval"
}

[2026-04-05 15:01] - Updated by Junie
{
    "TYPE": "correction",
    "CATEGORY": "premature archiving",
    "EXPECTATION": "User wants to personally test and verify OpenSpec changes and only archive after their explicit approval.",
    "NEW INSTRUCTION": "WHEN considering archiving an OpenSpec change THEN wait for explicit user verification and approval"
}

[2026-04-05 15:43] - Updated by Junie
{
    "TYPE": "correction",
    "CATEGORY": "build process hygiene",
    "EXPECTATION": "The user expected trivial .cmd/.sh removal to apply to all sample bots and the build to be preceded by a clean using sample-bots:clean.",
    "NEW INSTRUCTION": "WHEN building sample-bots THEN run gradlew :sample-bots:clean before assembling"
}

[2026-04-05 15:44] - Updated by Junie
{
    "TYPE": "correction",
    "CATEGORY": "build process hygiene",
    "EXPECTATION": "Trivial .cmd/.sh removal must apply to all sample bots, and a clean build must be run with :sample-bots:clean before assembling.",
    "NEW INSTRUCTION": "WHEN building sample-bots THEN run gradlew :sample-bots:clean before assembling"
}

[2026-04-05 15:50] - Updated by Junie
{
    "TYPE": "correction",
    "CATEGORY": "regression verification",
    "EXPECTATION": "Verify and run the Java, C#, and Python sample bots using the battle runner pointed at C:\\Code\\bots\\{Java,C#,Python}, and fix any issues found.",
    "NEW INSTRUCTION": "WHEN verifying sample bots run THEN use /runner on C:\\Code\\bots\\Java,C#,Python and report fixes"
}

[2026-04-05 15:57] - Updated by Junie
{
    "TYPE": "correction",
    "CATEGORY": "regression verification",
    "EXPECTATION": "User wants the Java, C#, and Python sample bots verified and running via /runner on C:\\Code\\bots\\{Java,C#,Python}, and any failures fixed immediately.",
    "NEW INSTRUCTION": "WHEN sample-bot or booter changes are made THEN run /runner on C:\\Code\\bots\\Java,C#,Python and fix failures"
}

[2026-04-05 16:16] - Updated by Junie
{
    "TYPE": "correction",
    "CATEGORY": "insufficient verification",
    "EXPECTATION": "User wants real-life end-to-end testing performed in C:\\Code\\bots\\Java,C#,Python after clean build and deployment, not just internal checks.",
    "NEW INSTRUCTION": "WHEN verifying booter or sample-bots changes THEN reproduce build, deploy to C:\\Code\\bots and run /runner on Java,C#,Python and report results"
}

[2026-04-05 16:36] - Updated by Junie
{
    "TYPE": "preference",
    "CATEGORY": "verification status",
    "EXPECTATION": "User wants explicit confirmation whether the sample bots under C:\\Code\\bots\\Java were actually updated to the latest build.",
    "NEW INSTRUCTION": "WHEN verifying changes in C:\\Code\\bots THEN confirm file updates and describe sync method used"
}

[2026-04-05 16:41] - Updated by Junie
{
    "TYPE": "correction",
    "CATEGORY": "team bot failure",
    "EXPECTATION": "The Java team bot 'MyFirstTeam' under C:\\Code\\bots\\Java should start successfully like the other sample bots.",
    "NEW INSTRUCTION": "WHEN verifying Java sample bots THEN include MyFirstTeam and ensure it starts under C:\\Code\\bots\\Java"
}

[2026-04-05 16:42] - Updated by Junie
{
    "TYPE": "correction",
    "CATEGORY": "team bot failure",
    "EXPECTATION": "User wants the MyFirstTeam Java bot under C:\\Code\\bots\\Java to start like the others.",
    "NEW INSTRUCTION": "WHEN verifying Java sample bots THEN include MyFirstTeam and ensure it starts under C:\\Code\\bots\\Java"
}

[2026-04-05 16:50] - Updated by Junie
{
    "TYPE": "correction",
    "CATEGORY": "C# parity and verification",
    "EXPECTATION": "Fix C# sample bots to start the same way as Java and verify them under C:\\Code\\bots\\C#.",
    "NEW INSTRUCTION": "WHEN verifying C# sample bots THEN run /runner on C:\\Code\\bots\\C# and fix start failures"
}

[2026-04-05 19:10] - Updated by Junie
{
    "TYPE": "positive",
    "CATEGORY": "praise + doc request",
    "EXPECTATION": "User is happy with progress and now wants clarity on whether C# sample bots still need a .csproj under template-based booting, and for this to be documented.",
    "NEW INSTRUCTION": "WHEN changing boot requirements for C# bots THEN document .csproj necessity and update docs"
}

[2026-04-05 19:21] - Updated by Junie
{
    "TYPE": "positive",
    "CATEGORY": "commit guidance",
    "EXPECTATION": "User is pleased with the change proposal and wants a local commit with a clear, descriptive message explaining what the feature fixes, without pushing yet.",
    "NEW INSTRUCTION": "WHEN committing this change THEN write a descriptive fix-focused message and do not push"
}

[2026-04-05 19:33] - Updated by Junie
{
    "TYPE": "correction",
    "CATEGORY": "config redundancy",
    "EXPECTATION": "Do not add a main field to bot .json files; derive the main from the bot directory name and only use it internally if needed.",
    "NEW INSTRUCTION": "WHEN determining bot main for boot THEN derive it from the bot directory name"
}

[2026-04-05 19:36] - Updated by Junie
{
    "TYPE": "correction",
    "CATEGORY": "config redundancy + documentation",
    "EXPECTATION": "Do not add a main field to bot JSON; derive main from the parent directory name, and document this convention via an ADR.",
    "NEW INSTRUCTION": "WHEN changing or clarifying boot conventions THEN create an ADR documenting the decision"
}

[2026-04-05 19:42] - Updated by Junie
{
    "TYPE": "correction",
    "CATEGORY": "documentation scope",
    "EXPECTATION": "ADR 30 should not mention renaming from main to base because main was never officially released; treat it as ongoing work.",
    "NEW INSTRUCTION": "WHEN drafting ADR 30 THEN omit any mention of main-to-base renaming"
}

[2026-04-05 19:44] - Updated by Junie
{
    "TYPE": "correction",
    "CATEGORY": "changelog reference",
    "EXPECTATION": "Remove the '#202:' reference from the 0.38.4 changelog because that issue was fixed in 0.38.3.",
    "NEW INSTRUCTION": "WHEN editing 0.38.4 CHANGELOG.md THEN remove '#202' from all entries"
}

[2026-04-05 19:47] - Updated by Junie
{
    "TYPE": "correction",
    "CATEGORY": "model field order",
    "EXPECTATION": "User requires a 'main' attribute and it must appear first in BootEntry, DirBootEntry, and IBootEntry.",
    "NEW INSTRUCTION": "WHEN editing these model classes THEN declare 'main' as the first property"
}

[2026-04-05 19:47] - Updated by Junie
{
    "TYPE": "preference",
    "CATEGORY": "documentation comment",
    "EXPECTATION": "The key field in the boot entry models should have an inline comment explaining its purpose.",
    "NEW INSTRUCTION": "WHEN editing BootEntry/DirBootEntry/IBootEntry fields THEN add a concise purpose comment on the key field"
}

[2026-04-05 19:54] - Updated by Junie
{
    "TYPE": "correction",
    "CATEGORY": "documentation inconsistency",
    "EXPECTATION": "User wants all README files updated to remove references to the deprecated 'main' property and use the 'base' convention instead.",
    "NEW INSTRUCTION": "WHEN README/ReadMe mentions 'main' property THEN replace with 'base' convention and note dir-derived default"
}

[2026-04-05 19:58] - Updated by Junie
{
    "TYPE": "correction",
    "CATEGORY": "ADR placement/format",
    "EXPECTATION": "The ADR for template-based booting must use MADR, have the correct sequential number, and be placed under docs-internal/architecture/adr.",
    "NEW INSTRUCTION": "WHEN creating or modifying an ADR THEN place under docs-internal/architecture/adr using MADR and next sequential number"
}

[2026-04-05 20:04] - Updated by Junie
{
    "TYPE": "correction",
    "CATEGORY": "released changelog immutability",
    "EXPECTATION": "The user does not want any changes made to entries of already released versions like 0.38.3; edits should target the current or unreleased version only.",
    "NEW INSTRUCTION": "WHEN editing CHANGELOG.md for released versions THEN do not modify past entries"
}

[2026-04-05 20:10] - Updated by Junie
{
    "TYPE": "correction",
    "CATEGORY": "inaccurate documentation",
    "EXPECTATION": "Update all my-first-bot--for-xxx.md files to remove the false claim about scripts being optional if the 'main' property is provided, and instead explain scriptless booting via template-based booting with 'base' derived from the directory name.",
    "NEW INSTRUCTION": "WHEN docs mention scripts optional due to 'main' in JSON THEN replace with template-based booting and dir-derived 'base'"
}

[2026-04-05 20:12] - Updated by Junie
{
    "TYPE": "correction",
    "CATEGORY": "documentation clarity",
    "EXPECTATION": "Clarify that scripts are optional because template-based booting is used, and that the 'base' value (not the scripts) defaults to the bot's parent directory name.",
    "NEW INSTRUCTION": "WHEN documenting script files optionality THEN state base defaults to dir name and scripts are optional via templates"
}

[2026-04-05 20:13] - Updated by Junie
{
    "TYPE": "correction",
    "CATEGORY": "documentation clarity",
    "EXPECTATION": "User wants it clear that scripts are optional due to template-based booting; the 'defaults to the bot's parent directory name' refers to the base value, not to scripts.",
    "NEW INSTRUCTION": "WHEN mentioning script optionality THEN state templates enable it and base defaults to dir name"
}

[2026-04-05 20:18] - Updated by Junie
{
    "TYPE": "preference",
    "CATEGORY": "commit workflow",
    "EXPECTATION": "User wants the latest changes saved as an amended commit and not pushed yet to allow manual verification.",
    "NEW INSTRUCTION": "WHEN asked to commit changes now THEN create an amended commit and do not push"
}

[2026-04-05 20:20] - Updated by Junie
{
    "TYPE": "correction",
    "CATEGORY": "commit granularity",
    "EXPECTATION": "User wants fixes and features separated into distinct atomic commits, not mixed together.",
    "NEW INSTRUCTION": "WHEN changes include both fixes and features THEN split into separate commits with clear messages"
}

[2026-04-05 20:33] - Updated by Junie
{
    "TYPE": "correction",
    "CATEGORY": "commit hygiene",
    "EXPECTATION": "After your commit/amend steps, the working tree should be clean with no uncommitted changes, or you should clearly explain what is intentionally left unstaged.",
    "NEW INSTRUCTION": "WHEN finishing a commit or amend THEN run git add -A, commit, then verify git status is clean and list leftovers"
}

[2026-04-05 21:00] - Updated by Junie
{
    "TYPE": "correction",
    "CATEGORY": "destructive behavior + insufficient e2e testing",
    "EXPECTATION": "User’s bots under C:\\Code\\bots must never be deleted and bot discovery must work; verification must be done end-to-end with the real bots.",
    "NEW INSTRUCTION": "WHEN syncing or modifying C:\\Code\\bots THEN use non-destructive copy and verify with /runner"
}

[2026-04-05 21:11] - Updated by Junie
{
    "TYPE": "correction",
    "CATEGORY": "reuse shared utility",
    "EXPECTATION": "Use the shared /lib OS detection functionality instead of manual System.getProperty checks in Command.kt.",
    "NEW INSTRUCTION": "WHEN detecting OS in codebase THEN use shared /lib OS detection utility"
}

[2026-04-05 21:14] - Updated by Junie
{
    "TYPE": "correction",
    "CATEGORY": "template variable syntax",
    "EXPECTATION": "Use the $$-prefixed interpolation to defer expansion (e.g., $${classPath}) instead of the old ${classPath}.",
    "NEW INSTRUCTION": "WHEN editing boot templates or scripts THEN use $$-prefixed placeholders like $${var}"
}

[2026-04-05 21:18] - Updated by Junie
{
    "TYPE": "correction",
    "CATEGORY": "over-rollback scope",
    "EXPECTATION": "User did not want a full rollback; they want the prior state restored and only targeted changes reverted.",
    "NEW INSTRUCTION": "WHEN requested to roll back widespread changes THEN confirm target files or commit scope before executing"
}

[2026-04-05 21:22] - Updated by Junie
{
    "TYPE": "correction",
    "CATEGORY": "over-rollback + restore request",
    "EXPECTATION": "The user did not want a full rollback; they wanted only targeted changes reverted and now want the pre-rollback state restored.",
    "NEW INSTRUCTION": "WHEN an over-rollback is detected THEN restore to the commit before rollback and confirm"
}

[2026-04-05 21:23] - Updated by Junie
{
    "TYPE": "correction",
    "CATEGORY": "over-rollback restore",
    "EXPECTATION": "The user did not want everything rolled back; only targeted changes should have been reverted, and they now want the pre-rollback state restored.",
    "NEW INSTRUCTION": "WHEN an over-rollback is detected and user requests restore THEN restore to pre-rollback commit and confirm"
}

[2026-04-05 21:39] - Updated by Junie
{
    "TYPE": "positive",
    "CATEGORY": "approval + artifact request",
    "EXPECTATION": "User approved the proposal and wants the remaining OpenSpec artifacts (specs, design, tasks) added before implementation.",
    "NEW INSTRUCTION": "WHEN user approves a proposal THEN create specs, design, and tasks artifacts before implementing"
}

[2026-04-05 21:47] - Updated by Junie
{
    "TYPE": "preference",
    "CATEGORY": "runner usage",
    "EXPECTATION": "User wants to use the BattleRunner as-is without redesign and seeks justification for test-bot placement.",
    "NEW INSTRUCTION": "WHEN planning test execution with BattleRunner THEN avoid redesign and use it as-is"
}

[2026-04-05 21:50] - Updated by Junie
{
    "TYPE": "correction",
    "CATEGORY": "premature implementation",
    "EXPECTATION": "User expects no implementation work to start before they explicitly approve the change proposal.",
    "NEW INSTRUCTION": "WHEN change proposal not explicitly approved THEN pause implementation and request explicit approval"
}

[2026-04-05 21:52] - Updated by Junie
{
    "TYPE": "preference",
    "CATEGORY": "implementation approval",
    "EXPECTATION": "Do not start implementing without explicit user instruction; ask before beginning any implementation.",
    "NEW INSTRUCTION": "WHEN no explicit user approval to implement THEN ask for confirmation and wait"
}

[2026-04-05 21:57] - Updated by Junie
{
    "TYPE": "preference",
    "CATEGORY": "documentation rule",
    "EXPECTATION": "Documentation/ADR should state: if a bot .json exists, it is authoritative; if absent, the bot must set all required properties; otherwise throw BotException listing missing fields and indicate a .json can be provided.",
    "NEW INSTRUCTION": "WHEN documenting bot discovery/validation THEN state .json precedence and BotException with missing fields"
}

[2026-04-05 22:04] - Updated by Junie
{
    "TYPE": "correction",
    "CATEGORY": "rollback restraint",
    "EXPECTATION": "User wants current changes preserved for their review and no rollbacks; also no implementation should start without explicit approval and responses should stay in sparring/answer mode when asked.",
    "NEW INSTRUCTION": "WHEN user says keep changes or do not roll back THEN do not revert any files"
}

[2026-04-05 22:06] - Updated by Junie
{
    "TYPE": "correction",
    "CATEGORY": "rollback restraint + approval gating",
    "EXPECTATION": "User wants current changes preserved for their review with no rollbacks and no further implementation without explicit approval.",
    "NEW INSTRUCTION": "WHEN no explicit user approval to implement THEN do not modify files or revert anything"
}

[2026-04-05 22:09] - Updated by Junie
{
    "TYPE": "correction",
    "CATEGORY": "incomplete checklist",
    "EXPECTATION": "User wants completed (sub)tasks checked off in tasks.md and clear indication of which tasks are new and still unimplemented; no implementation should begin.",
    "NEW INSTRUCTION": "WHEN updating OpenSpec tasks.md THEN check completed items and mark new items as pending"
}

[2026-04-05 22:13] - Updated by Junie
{
    "TYPE": "correction",
    "CATEGORY": "missing docs in proposal",
    "EXPECTATION": "User wants the OpenSpec change proposal updated to include VitePress (/docs-build) documentation about the .json file’s role and updates to all MyFirstBot examples (Java, Python, C#) to reflect the new requirements, without starting any implementation.",
    "NEW INSTRUCTION": "WHEN updating this OpenSpec change THEN add docs-build VitePress and MyFirstBot example updates"
}

[2026-04-05 22:17] - Updated by Junie
{
    "TYPE": "positive",
    "CATEGORY": "release prep request",
    "EXPECTATION": "User likes the progress and wants VERSION and CHANGELOG.md prepared for 0.39.0 without starting implementation of the proposal.",
    "NEW INSTRUCTION": "WHEN preparing a new version like 0.39.0 THEN update VERSION and draft concise CHANGELOG; avoid implementation"
}

[2026-04-05 22:25] - Updated by Junie
{
    "TYPE": "positive",
    "CATEGORY": "approval to proceed",
    "EXPECTATION": "The change proposal is approved and the user wants implementation to begin now.",
    "NEW INSTRUCTION": "WHEN proposal is explicitly approved THEN start implementation following tasks.md and /opsx:apply"
}

[2026-04-05 22:30] - Updated by Junie
{
    "TYPE": "positive",
    "CATEGORY": "approval to proceed",
    "EXPECTATION": "User approved the change proposal and wants implementation to begin now.",
    "NEW INSTRUCTION": "WHEN proposal is explicitly approved THEN start implementation following tasks.md and /opsx:apply"
}

[2026-04-05 22:32] - Updated by Junie
{
    "TYPE": "positive",
    "CATEGORY": "approval to proceed",
    "EXPECTATION": "User explicitly approved the change proposal and wants implementation to begin now.",
    "NEW INSTRUCTION": "WHEN proposal is explicitly approved THEN start implementation following tasks.md and /opsx:apply"
}

[2026-04-05 22:37] - Updated by Junie
{
    "TYPE": "positive",
    "CATEGORY": "approval to proceed",
    "EXPECTATION": "User approved the change proposal and wants implementation to begin now.",
    "NEW INSTRUCTION": "WHEN proposal is explicitly approved THEN start implementation following tasks.md and /opsx:apply"
}

[2026-04-05 22:40] - Updated by Junie
{
    "TYPE": "positive",
    "CATEGORY": "approval to proceed",
    "EXPECTATION": "The change proposal is approved and the user wants implementation to begin now.",
    "NEW INSTRUCTION": "WHEN proposal explicitly approved THEN begin implementation per tasks.md and report status"
}

[2026-04-05 22:41] - Updated by Junie
{
    "TYPE": "positive",
    "CATEGORY": "approval to proceed",
    "EXPECTATION": "User explicitly approved the change proposal and wants implementation to begin now.",
    "NEW INSTRUCTION": "WHEN proposal is explicitly approved THEN start implementation per tasks.md and report status"
}

[2026-04-05 22:58] - Updated by Junie
{
    "TYPE": "positive",
    "CATEGORY": "ADR approval + format",
    "EXPECTATION": "ADR-0030 and ADR-0031 are approved and must adhere to MADR conventions.",
    "NEW INSTRUCTION": "WHEN creating or editing any ADR THEN follow MADR structure and guidelines"
}

[2026-04-05 23:31] - Updated by Junie
{
    "TYPE": "correction",
    "CATEGORY": "failing unit test",
    "EXPECTATION": "In Java Bot API, a bot with missing required properties should still be constructible; validation must fail at handshake time, matching BaseBotConstructorTest expectations.",
    "NEW INSTRUCTION": "WHEN enforcing Java runtime validation THEN allow construction and fail during handshake validation"
}

[2026-04-05 23:35] - Updated by Junie
{
    "TYPE": "correction",
    "CATEGORY": "failing unit test",
    "EXPECTATION": "In Java Bot API, bots missing required properties must still construct successfully; validation should fail at handshake time so BaseBotConstructorTest passes.",
    "NEW INSTRUCTION": "WHEN validating required properties in Java THEN defer failure to handshake phase"
}

[2026-04-06 14:16] - Updated by Junie
{
    "TYPE": "positive",
    "CATEGORY": "approval to proceed",
    "EXPECTATION": "The user confirms both the ADR and the proposal are accepted and expects the next steps to begin.",
    "NEW INSTRUCTION": "WHEN ADR and proposal are accepted THEN create specs/design/tasks then start implementation and report status"
}

[2026-04-06 14:22] - Updated by Junie
{
    "TYPE": "positive",
    "CATEGORY": "approval to proceed",
    "EXPECTATION": "User confirms the ADR and the proposal are accepted and wants the next steps to begin.",
    "NEW INSTRUCTION": "WHEN ADR and proposal are accepted THEN create specs/design/tasks then start implementation and report status"
}

[2026-04-06 14:39] - Updated by Junie
{
    "TYPE": "preference",
    "CATEGORY": "terminology naming",
    "EXPECTATION": "User prefers the label 'Bot Native' over 'Bot Defined' for colors set by the bot itself.",
    "NEW INSTRUCTION": "WHEN labeling tank color option THEN use 'Bot Native' instead of 'Bot Defined'"
}

[2026-04-08 22:27] - Updated by Junie
{
    "TYPE": "correction",
    "CATEGORY": "update check inconsistency",
    "EXPECTATION": "The user wants a clear, accurate statement about whether a new Junie version is actually available, without contradictory claims when the update check fails.",
    "NEW INSTRUCTION": "WHEN update check errors or permissions block validation THEN do not claim an update; explain the blocker and next steps"
}

