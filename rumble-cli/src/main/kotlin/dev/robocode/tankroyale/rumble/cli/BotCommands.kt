package dev.robocode.tankroyale.rumble.cli

import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.CoreCliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import dev.robocode.tankroyale.botproject.BotStaging
import dev.robocode.tankroyale.botproject.BotValidator
import dev.robocode.tankroyale.botproject.Finding
import dev.robocode.tankroyale.botproject.StagedSubmission
import dev.robocode.tankroyale.botproject.StagingResult
import dev.robocode.tankroyale.rumble.BotProgress
import dev.robocode.tankroyale.rumble.Catalog
import dev.robocode.tankroyale.rumble.CatalogUnreadable
import dev.robocode.tankroyale.rumble.GitHubTokens
import dev.robocode.tankroyale.rumble.PrecheckReport
import dev.robocode.tankroyale.rumble.RumbleEnvironment
import dev.robocode.tankroyale.rumble.LauncherCheck
import dev.robocode.tankroyale.rumble.LauncherState
import dev.robocode.tankroyale.rumble.RumbleHead
import dev.robocode.tankroyale.rumble.RumbleRules
import dev.robocode.tankroyale.rumble.RumbleSnapshot
import dev.robocode.tankroyale.rumble.SavedRenewal
import dev.robocode.tankroyale.rumble.SignInRefused
import dev.robocode.tankroyale.rumble.SourceNotice
import dev.robocode.tankroyale.rumble.SubmissionOutcome
import dev.robocode.tankroyale.rumble.SubmissionPrecheck
import dev.robocode.tankroyale.rumble.SubmissionReadiness
import dev.robocode.tankroyale.rumble.SubmissionResult
import dev.robocode.tankroyale.rumble.signInNotAvailable
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

/** The `bot` commands: checking a bot, sending it to the Rumble and following it there. */
internal class BotGroup : CoreCliktCommand(name = "bot") {
    override fun help(context: Context) =
        "Check a bot offline, send it to the Rumble as a pull request, and follow where it stands. Sending makes the whole source public."

    override fun run() {
        // No work of its own; the commands do it.
    }
}

/** What the bot commands have in common: the answer, with the name of the command it belongs to. */
internal abstract class BotLeaf(name: String, io: Io, services: Services) : SignInLeaf(name, io, services) {
    override val answerName: String get() = "bot $commandName"

    companion object {
        /** Said by `bot check`, which is offline, so that nobody believes it also looked at the launchers. */
        const val LAUNCHER_NOTE = "The launcher scripts are compared with the Rumble's starter launchers at the dry run: rumble bot send <folder> --dry-run."
    }

    /** A folder given on the command line. */
    protected fun folder(text: String): Path = pathOf(text, "The bot folder").toAbsolutePath().normalize()

    /** Problems of a bot folder as JSON, with text from outside cleaned. */
    protected fun JsonObjectBuilder.putFindings(findings: List<Finding>) {
        put("findings", buildJsonArray {
            findings.forEach {
                add(buildJsonObject {
                    put("code", it.code)
                    put("file", io.outside(it.file))
                    put("message", io.outside(it.message))
                    put("fix", io.outside(it.fix))
                })
            }
        })
    }

    protected fun findingsText(findings: List<Finding>): String =
        findings.joinToString("\n") { io.outside("- ${it.code} in ${it.file}: ${it.message} Fix: ${it.fix}") }
}

internal class BotCheckCommand(io: Io, services: Services) : BotLeaf("check", io, services) {
    override fun help(context: Context) =
        "Checks a bot or team folder against the Rumble's rules for one bot, offline, and lists every problem with its fix. " +
            "It needs no sign-in, contacts nobody and writes nothing."

    private val botFolder by argument("FOLDER", help = "The folder of the bot or team.")

    override fun run() {
        val env = environment()
        val report = BotValidator.validate(folder(botFolder))
        val reply = if (report.passes) {
            reply(env, ExitCodes.DONE, "passes", "The bot passes the checks of the Rumble's bot repository for one bot.\n$LAUNCHER_NOTE", next = null) {
                putFindings(emptyList())
                put("notes", buildJsonArray { report.notes.forEach { add(JsonPrimitive(io.outside(it))) }; add(JsonPrimitive(LAUNCHER_NOTE)) })
            }
        } else {
            reply(
                env, ExitCodes.REFUSED, "check-failed",
                "The bot has ${report.findings.size} problem" + (if (report.findings.size == 1) "" else "s") + ":\n" + findingsText(report.findings) + "\n$LAUNCHER_NOTE",
            ) { putFindings(report.findings); put("notes", buildJsonArray { report.notes.forEach { add(JsonPrimitive(io.outside(it))) }; add(JsonPrimitive(LAUNCHER_NOTE)) }) }
        }
        answer(reply)
    }
}

internal class BotSendCommand(io: Io, services: Services) : BotLeaf("send", io, services) {
    override fun help(context: Context) =
        "Sends a bot to the Rumble as one pull request from your own copy of the bot repository. EVERYTHING IN THE BOT'S FILES BECOMES PUBLIC: " +
            "anyone can read and copy the source, and it stays in the history even if the pull request is closed. " +
            "Run it first with --dry-run to see what would be sent."

    private val botFolder by argument("FOLDER", help = "The folder of the bot or team.")
    private val dryRun by option("--dry-run", help = "Do everything except sending: check, stage, and look at the rules and your setup.").flag()
    private val certify by option(
        "--certify-license",
        help = "You, the author, certify that you have the right to publish this bot under the license in its files, and that its source becomes public. " +
            "Never given by default or remembered. An agent may pass it only when its person has told it to for this bot.",
    ).flag()

    override fun run() {
        val env = environment()
        if (env.signInClientId == null) {
            answer(reply(env, ExitCodes.REFUSED, "no-sign-in-app", env.signInNotAvailable()))
            return
        }
        val folder = folder(botFolder)
        val report = BotValidator.validate(folder)
        if (!report.passes) {
            answer(
                reply(
                    env, ExitCodes.REFUSED, "check-failed",
                    "The bot has problems, so nothing was sent:\n" + findingsText(report.findings),
                    next = null,
                ) { putFindings(report.findings); putNotice(emptyList()) },
            )
            return
        }
        if (!dryRun && !certify) {
            answer(
                reply(
                    env, ExitCodes.REFUSED, "license-not-certified",
                    "Nothing was sent. The author must certify that they may publish this bot under its license, with --certify-license. ${SourceNotice.TEXT}",
                ) { putNotice(emptyList()); put("licenseCertified", false) },
            )
            return
        }

        val flow = flow(env)
        val renewal = when (val r = flow.renewSaved()) {
            is SavedRenewal.Renewed -> r
            is SavedRenewal.SignInNeeded -> {
                answer(reply(env, ExitCodes.SIGN_IN_NEEDED, outcomeOf(r), r.message, next = "login"))
                return
            }
            is SavedRenewal.Failed -> {
                answer(reply(env, ExitCodes.FAILED, "failed", r.message, next = "bot send ${quoted(botFolder)}"))
                return
            }
        }
        val sending = services.submission(env)
        val tokens = renewal.tokens
        val readiness = sending.checkReadiness(tokens)
        val ready = when (readiness) {
            is SubmissionReadiness.Ready -> readiness
            else -> {
                answer(notReady(env, flow, renewal, readiness))
                return
            }
        }

        val staged = when (val result = BotStaging.stage(folder, stagingFolder(env))) {
            is StagingResult.Staged -> result.submission
            is StagingResult.Rejected -> {
                answer(reply(env, ExitCodes.REFUSED, "check-failed", "The bot has problems, so nothing was sent:\n" + findingsText(result.findings)) { putFindings(result.findings); putNotice(emptyList()) })
                return
            }
            is StagingResult.Refused -> {
                answer(reply(env, ExitCodes.REFUSED, "refused", io.outside(result.message)) { putNotice(emptyList()) })
                return
            }
        }
        val files = staged.files.map { it.path }

        val advice = advice(env, staged, ready.account, tokens.accessToken)
        if (advice.report?.hasProblems == true) {
            val findings = advice.report.problems
            answer(
                reply(
                    env, ExitCodes.REFUSED, "precheck-failed",
                    "The Rumble's rules would refuse this bot, so nothing was sent:\n" + findings.joinToString("\n") { io.outside("- ${it.message} Fix: ${it.fix}") },
                ) {
                    put("findings", buildJsonArray {
                        findings.forEach { add(buildJsonObject { put("code", it.code); put("message", io.outside(it.message)); put("fix", io.outside(it.fix)) }) }
                    })
                    putNotice(files)
                },
            )
            return
        }
        val launchers = sending.compareLaunchers(tokens, staged)
        val facts = factsText(staged, advice, files, launchers)
        if (dryRun) {
            answer(
                reply(
                    env, ExitCodes.DONE, "dry-run",
                    "Dry run: nothing was sent. This is what would be sent.\n$facts",
                    next = "bot send ${quoted(botFolder)} --certify-license",
                ) { putStaged(staged, advice, launchers); putNotice(files) },
            )
            return
        }

        when (val result = sending.submit(tokens, staged, authorCertifiesLicense = true)) {
            is SubmissionResult.Submitted -> {
                val what = when (result.outcome) {
                    SubmissionOutcome.OPENED -> "opened"
                    SubmissionOutcome.RESUMED -> "resumed"
                    SubmissionOutcome.UNCHANGED -> "unchanged"
                    SubmissionOutcome.UPDATED -> "updated"
                }
                answer(
                    reply(
                        env, ExitCodes.DONE, what,
                        "Pull request ${result.pullRequestNumber} for ${io.outside(staged.name)} is $what: ${result.pullRequestUrl}\n" +
                            "The author certified the right to publish this source. $facts",
                        next = "bot status ${staged.name}",
                    ) {
                        put("pullRequestNumber", result.pullRequestNumber)
                        put("pullRequestUrl", result.pullRequestUrl)
                        put("licenseCertified", true)
                        putStaged(staged, advice, launchers)
                        putNotice(files)
                    },
                )
            }
            is SubmissionResult.SetupNeeded -> answer(notReady(env, flow, renewal, result.readiness))
            is SubmissionResult.Refused -> answer(reply(env, ExitCodes.REFUSED, "refused", io.outside(result.message)) { putNotice(files) })
            is SubmissionResult.SignInNeeded -> {
                flow.forgetRefused(renewal)
                answer(reply(env, ExitCodes.SIGN_IN_NEEDED, "sign-in-refused", result.message, next = "login"))
            }
            is SubmissionResult.Failed -> answer(reply(env, ExitCodes.FAILED, "failed", io.outside(result.message), next = "bot send ${quoted(botFolder)} --certify-license") { putNotice(files) })
        }
    }

    private class Advice(val report: PrecheckReport?, val note: String?)

    /** The advisory check against the Rumble's catalog and rules. A Rumble that cannot be read does not stop the send. */
    private fun advice(env: RumbleEnvironment, staged: StagedSubmission, account: String, token: String): Advice {
        val snapshot = RumbleSnapshot.read(services.api, env, services.catalog(env), token)
        val catalog = snapshot.catalog
        val rules = snapshot.rules
        if (catalog != null && rules != null) return Advice(SubmissionPrecheck.check(staged, account, catalog, rules), null)
        return Advice(null, "The Rumble's catalog and rules could not be read (${io.outside(snapshot.problem.orEmpty())}), so the name, owner and version were not checked. The bot repository checks them anyway.")
    }

    private fun factsText(staged: StagedSubmission, advice: Advice, files: List<String>, launchers: LauncherState): String = buildString {
        appendLine("Bot: ${io.outside(staged.name)} ${io.outside(staged.version.orEmpty())} (${staged.language}), license ${io.outside(staged.license ?: "none declared")}")
        appendLine("PUBLIC: ${SourceNotice.TEXT}")
        appendLine("Files (${files.size}): ${files.joinToString { io.outside(it) }}")
        advice.report?.warnings?.forEach { appendLine(io.outside("Note: ${it.message} ${it.fix}")) }
        advice.note?.let { appendLine(it) }
        when (launchers) {
            is LauncherState.Differs -> appendLine(io.outside("Launchers: differs (${launchers.files.joinToString()}). ${LauncherCheck.DIFFERS_MESSAGE} ${LauncherCheck.DIFFERS_FIX}"))
            is LauncherState.NotChecked -> appendLine(io.outside("Launchers: not checked, because ${launchers.reason}. The bot repository's moderation checks them anyway."))
            LauncherState.Same -> appendLine("Launchers: same as the starter launchers.")
        }
    }.trimEnd()

    private fun JsonObjectBuilder.putStaged(staged: StagedSubmission, advice: Advice, launchers: LauncherState) {
        put("bot", buildJsonObject {
            put("name", io.outside(staged.name))
            put("version", io.outside(staged.version.orEmpty()))
            put("language", staged.language)
            put("license", io.outside(staged.license.orEmpty()))
            put("treeHash", staged.treeHash)
        })
        put("launchers", buildJsonObject {
            when (launchers) {
                LauncherState.Same -> put("state", "same")
                is LauncherState.Differs -> {
                    put("state", "differs")
                    put("files", buildJsonArray { launchers.files.forEach { add(JsonPrimitive(io.outside(it))) } })
                    put("message", LauncherCheck.DIFFERS_MESSAGE)
                    put("fix", LauncherCheck.DIFFERS_FIX)
                }
                is LauncherState.NotChecked -> {
                    put("state", "not-checked")
                    put("reason", io.outside(launchers.reason))
                }
            }
        })
        put("catalogChecked", advice.report != null)
        advice.note?.let { put("catalogNote", it) }
        put("warnings", buildJsonArray {
            advice.report?.warnings?.forEach { add(buildJsonObject { put("code", it.code); put("message", io.outside(it.message)); put("fix", io.outside(it.fix)) }) }
        })
    }

    /** Says in every answer that the source becomes public, and which files. */
    private fun JsonObjectBuilder.putNotice(files: List<String>) {
        put("sourceIsPublic", true)
        put("notice", SourceNotice.TEXT)
        put("publicFiles", buildJsonArray { files.forEach { add(JsonPrimitive(io.outside(it))) } })
    }

    private fun notReady(env: RumbleEnvironment, flow: dev.robocode.tankroyale.rumble.SignInFlow, renewal: SavedRenewal.Renewed, readiness: SubmissionReadiness): Reply =
        when (readiness) {
            is SubmissionReadiness.Ready -> error("ready")
            is SubmissionReadiness.NoCopy -> reply(
                env, ExitCodes.REFUSED, "setup-needed",
                "You need your own copy of the Rumble's bot repository first. Open ${readiness.pageUrl}, make the copy, then run this command again. Nothing was changed.",
            ) { put("setup", "no-copy"); put("setupUrl", readiness.pageUrl); putNotice(emptyList()) }
            is SubmissionReadiness.AppNotInstalled -> reply(
                env, ExitCodes.REFUSED, "setup-needed",
                "The Rumble sign-in App must be installed on your copy. Open ${readiness.installUrl}, install it on your copy of the bot repository, then run this command again. Nothing was changed.",
            ) { put("setup", "app-not-installed"); put("setupUrl", readiness.installUrl); putNotice(emptyList()) }
            is SubmissionReadiness.NotRumblesRepository -> reply(
                env, ExitCodes.REFUSED, "setup-needed",
                "Your repository ${readiness.repositoryUrl} has the name of the Rumble's bot repository but is not a copy of it. Rename or remove it, make a real copy, then run this command again. Nothing was changed.",
            ) { put("setup", "not-a-copy"); put("setupUrl", readiness.repositoryUrl); putNotice(emptyList()) }
            is SubmissionReadiness.CopyBehind -> reply(
                env, ExitCodes.REFUSED, "setup-needed",
                "Your copy lacks a change to the Rumble's workflows, which the sign-in App may not bring into it. Open ${readiness.copyUrl}, press Sync fork to the right above the list of files, then Update branch, and run this command again. Nothing was sent.",
            ) { put("setup", "copy-behind"); put("setupUrl", readiness.copyUrl); putNotice(emptyList()) }
            is SubmissionReadiness.SignInNeeded -> {
                flow.forgetRefused(renewal)
                reply(env, ExitCodes.SIGN_IN_NEEDED, "sign-in-refused", readiness.message, next = "login")
            }
            is SubmissionReadiness.Failed -> reply(env, ExitCodes.FAILED, "failed", io.outside(readiness.message), next = "bot send ${quoted(botFolder)} --certify-license")
        }

    private fun stagingFolder(env: RumbleEnvironment): Path = env.stateRoot(base()).resolve("staging")

    /**
     * The folder as it can be typed again. A folder given as an absolute path cannot be written in an answer, because no
     * answer holds an absolute path of this computer, so the person puts it in place of `<folder>`.
     */
    private fun quoted(text: String): String {
        val relative = try { !Path.of(text).isAbsolute } catch (e: java.nio.file.InvalidPathException) { false }
        return when {
            !relative -> "<folder>"
            text.any { it.isWhitespace() } -> "\"$text\""
            else -> text
        }
    }
}

internal class BotStatusCommand(io: Io, services: Services) : BotLeaf("status", io, services) {
    override fun help(context: Context) =
        "Shows where the pull request you sent for a bot stands: waiting for the checks, sent back, accepted, merged or in the catalog. It only reads."

    private val botName by argument("NAME", help = "The name of the bot, as in its configuration file.")

    override fun run() {
        val env = environment()
        if (env.signInClientId == null) {
            answer(reply(env, ExitCodes.REFUSED, "no-sign-in-app", env.signInNotAvailable()))
            return
        }
        val flow = flow(env)
        val renewal = when (val r = flow.renewSaved()) {
            is SavedRenewal.Renewed -> r
            is SavedRenewal.SignInNeeded -> {
                answer(reply(env, ExitCodes.SIGN_IN_NEEDED, outcomeOf(r), r.message, next = "login"))
                return
            }
            is SavedRenewal.Failed -> {
                answer(reply(env, ExitCodes.FAILED, "failed", r.message, next = "bot status $botName"))
                return
            }
        }
        val tokens = renewal.tokens
        val progress = services.progress(env)
        val account = renewal.account ?: flow.identify(renewal)
        if (account == null) {
            answer(reply(env, ExitCodes.FAILED, "failed", "GitHub would not say which account is signed in. Try again.", next = "bot status $botName"))
            return
        }
        val sent = try {
            progress.find(tokens, account, botName)
        } catch (e: SignInRefused) {
            flow.forgetRefused(renewal)
            answer(reply(env, ExitCodes.SIGN_IN_NEEDED, "sign-in-refused", e.message.orEmpty(), next = "login"))
            return
        } catch (e: IOException) {
            answer(reply(env, ExitCodes.FAILED, "failed", io.outside(e.message.orEmpty()), next = "bot status $botName"))
            return
        }
        if (sent == null) {
            answer(reply(env, ExitCodes.REFUSED, "not-sent", "No pull request from this account was found for a bot called ${io.outside(botName)}.", next = "bot send <folder> --dry-run"))
            return
        }
        var catalogNote: String? = null
        val catalog = try {
            Catalog.parse(services.catalog(env).catalogText(RumbleHead.commit(services.api, env, tokens.accessToken)))
        } catch (e: CatalogUnreadable) {
            catalogNote = "The Rumble's catalog could not be read, so a merged bot cannot be told from one that is in the Rumble."
            null
        } catch (e: IOException) {
            catalogNote = "The Rumble's catalog could not be read, so a merged bot cannot be told from one that is in the Rumble."
            null
        }
        val state = progress.read(tokens, sent.pullRequestNumber, botName, sent.version, catalog)
        answer(stateReply(env, flow, renewal, sent.pullRequestNumber, sent.version, state, catalogNote))
    }

    private fun stateReply(env: RumbleEnvironment, flow: dev.robocode.tankroyale.rumble.SignInFlow, renewal: SavedRenewal.Renewed, number: Int, version: String, state: BotProgress, note: String?): Reply {
        fun done(outcome: String, message: String, extra: JsonObjectBuilder.() -> Unit = {}) =
            reply(env, ExitCodes.DONE, outcome, message + (note?.let { " $it" } ?: "")) {
                put("pullRequestNumber", number)
                put("version", io.outside(version))
                note?.let { put("catalogNote", it) }
                extra()
            }
        return when (state) {
            BotProgress.WaitingForChecks -> done("waiting-for-checks", "Waiting for the checks. For a first-time contributor GitHub holds them until a moderator approves the run.")
            is BotProgress.BeingChecked -> done("being-checked", "The checks are running." + (state.note?.let { " $it" } ?: ""))
            is BotProgress.SentBack -> {
                val reason = io.outside(state.reason)
                done("sent-back", "The bot was sent back: $reason Push a fix to the same branch and run bot send again.") { put("reason", reason) }
            }
            BotProgress.WaitingForModerator -> done("waiting-for-moderator", "The checks could not decide, so a human moderator must look at it.")
            BotProgress.Accepted -> done("accepted", "The checks passed. The pull request is waiting to be merged.")
            BotProgress.Merged -> done("merged", "The pull request is merged. The catalog does not hold this version yet; it is updated every hour or so.")
            is BotProgress.InCatalog -> done("in-catalog", "Version ${io.outside(state.version)} is in the Rumble.")
            BotProgress.Closed -> done("closed", "The pull request was closed without being merged.")
            is BotProgress.SignInNeeded -> {
                flow.forgetRefused(renewal)
                reply(env, ExitCodes.SIGN_IN_NEEDED, "sign-in-refused", state.message, next = "login")
            }
            is BotProgress.Failed -> reply(env, ExitCodes.FAILED, "failed", io.outside(state.message), next = "bot status $botName")
        }
    }
}
