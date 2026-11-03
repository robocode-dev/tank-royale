package dev.robocode.tankroyale.rumble.cli

import com.github.ajalt.clikt.core.CoreCliktCommand
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.PrintHelpMessage
import com.github.ajalt.clikt.core.PrintMessage
import com.github.ajalt.clikt.core.ProgramResult
import com.github.ajalt.clikt.core.UsageError
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.core.parse
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.output.Localization
import com.github.ajalt.clikt.output.ParameterFormatter
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.versionOption
import com.github.ajalt.clikt.parameters.types.choice
import dev.robocode.tankroyale.common.rules.CURRENT_BEHAVIOR_VERSION
import dev.robocode.tankroyale.rumble.LegacyClientImport
import dev.robocode.tankroyale.rumble.LegacyImportResult
import dev.robocode.tankroyale.rumble.LegacyPreview
import dev.robocode.tankroyale.rumble.LoadResult
import dev.robocode.tankroyale.rumble.RumbleEnvironment
import dev.robocode.tankroyale.rumble.RumbleEnvironments
import dev.robocode.tankroyale.rumble.signInNotAvailable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.io.PrintStream
import java.nio.file.Files
import java.nio.file.InvalidPathException
import java.nio.file.Path
import java.util.stream.Collectors

private const val SCHEMA_VERSION = 1
private const val ENVIRONMENT_VARIABLE = "RUMBLE_ENVIRONMENT"

/** Input that the option parser accepts but no command can act on. */
internal class BadInput(message: String) : Exception(message)

/** The program version, from the resource the build fills in. */
internal fun programVersion(): String {
    val text = RumbleCli::class.java.getResourceAsStream("/rumble-cli.properties")?.bufferedReader()?.use { it.readText() }.orEmpty()
    val version = text.lineSequence().firstOrNull { it.startsWith("version=") }?.removePrefix("version=")?.trim().orEmpty()
    return if (version.isEmpty() || version.startsWith("$")) "unknown" else version
}

/**
 * The Rumble command line: a controller that a person, a script or an agent can use. Every command answers as short text
 * or, with `--json`, as one JSON document, never asks anything, and ends with an exit code: 0 done, 1 failed, 2 refused
 * or given bad input, 3 a sign-in is needed, 4 not finished so run the same command again.
 */
object RumbleCli {
    /** Runs the command line with [args] and returns the exit code. */
    fun run(args: Array<String>, io: Io, services: Services = Services()): Int {
        val root = Root().subcommands(
            VersionCommand(io), EnvironmentsCommand(io), StatusCommand(io, services), LoginCommand(io, services),
            LogoutCommand(io, services), RegisterCommand(io, services), ImportLegacyCommand(io),
        ).context { readEnvvar = services.readEnv }
        val commands = root.registeredSubcommandNames()
        val first = args.firstOrNull()
        if (first != null && !first.startsWith("-") && first !in commands) {
            return badInput(args, io, commands, "There is no command called $first. The commands are ${commands.joinToString()}.")
        }
        return try {
            root.parse(args.toList())
            ExitCodes.DONE
        } catch (done: ProgramResult) {
            done.statusCode
        } catch (help: PrintHelpMessage) {
            io.out.println(help.context?.command?.getFormattedHelp(help).orEmpty())
            if (help.error) ExitCodes.REFUSED else ExitCodes.DONE
        } catch (message: PrintMessage) {
            io.out.println(message.message.orEmpty())
            ExitCodes.DONE
        } catch (bad: BadInput) {
            badInput(args, io, commands, bad.message.orEmpty())
        } catch (usage: UsageError) {
            badInput(args, io, commands, usage.formatMessage(object : Localization {}, ParameterFormatter.Plain).ifBlank { "Bad input." })
        } catch (error: CliktError) {
            badInput(args, io, commands, error.message ?: "Bad input.")
        } catch (e: Exception) {
            // A failure no command could answer, for example a state folder that cannot be used.
            problem(args, io, commands, "failed", "The command failed unexpectedly. Check that the state folder can be written to, and try again.")
            ExitCodes.FAILED
        }
    }

    private fun badInput(args: Array<String>, io: Io, commands: Collection<String>, message: String): Int {
        problem(args, io, commands, "bad-input", "$message Run rumble --help for what is possible.")
        return ExitCodes.REFUSED
    }

    /** Answers a problem no command answered in the form asked for: one JSON document, or one line on standard error. */
    private fun problem(args: Array<String>, io: Io, commands: Collection<String>, outcome: String, message: String) {
        if ("--json" in args) {
            io.answerProblem(
                buildJsonObject {
                    put("schemaVersion", SCHEMA_VERSION)
                    put("command", args.firstOrNull { it in commands } ?: "rumble")
                    put("outcome", outcome)
                    put("message", message)
                },
            )
        } else {
            io.err.println(io.cleaned("rumble: $message"))
        }
    }

    private class Root : CoreCliktCommand(name = "rumble") {
        init {
            versionOption(programVersion(), names = setOf("-v", "--version"), help = "Show the program version and exit", message = { "rumble $it" })
        }

        override fun help(context: Context): String =
            "Tool for taking part in the Tank Royale Rumble. Every command answers in text, or with --json in one JSON " +
                "document, and never asks anything. When in doubt, run rumble status: it says what to do next."

        override fun helpEpilog(context: Context): String =
            "To sign in, run rumble login, open the address it shows and type the code yourself, then run rumble login --finish and rumble register." +
                "\n\nExit codes:" +
                "\n  0  done" +
                "\n  1  failed; the answer says what to do next" +
                "\n  2  refused or bad input" +
                "\n  3  a sign-in is needed" +
                "\n  4  not finished; run the same command again"

        override fun run() {
            // No work of its own; the commands do it.
        }
    }

    /** What every command has in common: how the answer is written and where state lives. */
    internal abstract class Leaf(name: String, protected val io: Io) : CoreCliktCommand(name = name) {
        protected val json by option("--json", help = "Answer with one JSON document instead of text.").flag()
        protected val stateFolder by option("--state-folder", help = "Where the Rumble state lives. Defaults to the Tank Royale user folder.")

        protected fun base(): Path =
            try {
                stateFolder?.let { Path.of(it).toAbsolutePath().normalize() } ?: io.defaultStateFolder()
            } catch (e: InvalidPathException) {
                throw BadInput("The state folder is not a valid path on this computer.")
            }

        protected fun answer(reply: Reply) = io.answer(reply, json)

        protected fun head(command: String, build: JsonObjectBuilder.() -> Unit): JsonObject = buildJsonObject {
            put("schemaVersion", SCHEMA_VERSION)
            put("command", command)
            build()
        }
    }

    /** A command that works on one environment, chosen the same way everywhere. */
    internal abstract class EnvironmentLeaf(name: String, io: Io) : Leaf(name, io) {
        private val environmentName by option("-e", "--environment", envvar = ENVIRONMENT_VARIABLE, help = "The Rumble environment. Defaults to the public Rumble.")
            .choice(*RumbleEnvironments.all.map { it.name }.toTypedArray()).default(RumbleEnvironments.PUBLIC.name)

        protected fun environment(): RumbleEnvironment = RumbleEnvironments.all.first { it.name == environmentName }

        /** The whole command for [step], naming the environment so it stays in the same place when run as it stands. */
        protected fun nextCommand(step: String): String = "rumble $step -e $environmentName"

        /** Puts the next step in a JSON answer. A chosen state folder cannot be written in it, since no answer holds a path. */
        protected fun JsonObjectBuilder.putNext(step: String) {
            put("next", buildJsonObject {
                put("command", nextCommand(step))
                if (stateFolder != null) put("addStateFolder", true)
            })
        }

        /** The next step as a line of text. */
        protected fun nextLine(step: String): String =
            "Next: " + nextCommand(step) + if (stateFolder != null) " (add the same --state-folder)" else ""
    }

    private class VersionCommand(io: Io) : Leaf("version", io) {
        override fun help(context: Context) = "Shows the program, the engine behavior version, Java and the battle worker the program carries."

        override fun run() {
            val worker = EmbeddedWorker.info()
            val json = head("version") {
                put("program", "rumble")
                put("version", programVersion())
                put("behaviorVersion", CURRENT_BEHAVIOR_VERSION)
                put("java", System.getProperty("java.version").orEmpty())
                put("worker", buildJsonObject {
                    put("embedded", worker != null)
                    worker?.let { put("size", it.size); put("sha256", it.sha256) }
                })
            }
            val text = buildString {
                appendLine("rumble ${programVersion()}")
                appendLine("Engine behavior version: $CURRENT_BEHAVIOR_VERSION")
                appendLine("Java: ${System.getProperty("java.version")}")
                append(worker?.let { "Battle worker: carried, ${it.size} bytes, sha256 ${it.sha256}" } ?: "Battle worker: not carried")
            }
            answer(Reply(ExitCodes.DONE, json, text))
        }
    }

    private class EnvironmentsCommand(io: Io) : Leaf("environments", io) {
        override fun help(context: Context) = "Lists the known Rumble environments and whether state for them exists."

        override fun run() {
            val base = base()
            val rows = RumbleEnvironments.all.map { it to hasState(it.stateRoot(base)) }
            val json = head("environments") {
                put("environments", buildJsonArray {
                    rows.forEach { (env, exists) ->
                        add(buildJsonObject {
                            put("name", env.name)
                            put("displayName", env.displayName)
                            put("botsRepository", env.botsRepository)
                            put("dataRepository", env.dataRepository)
                            put("resettable", env.resettable)
                            put("development", env.development)
                            put("signInApp", env.signInClientId != null)
                            put("stateExists", exists)
                        })
                    }
                })
            }
            val text = rows.joinToString("\n") { (env, exists) ->
                "${env.name}: ${env.displayName}, ${env.botsRepository} and ${env.dataRepository}" +
                    (if (env.resettable) ", can be reset" else "") + (if (env.signInClientId != null) ", sign-in App" else ", no sign-in App yet") +
                    (if (exists) ", has saved state" else ", no saved state")
            }
            answer(Reply(ExitCodes.DONE, json, text))
        }

        /** A folder holding only the lock left by signing in and out has no state in it. */
        private fun hasState(folder: Path): Boolean {
            if (!Files.isDirectory(folder)) return false
            val names = Files.list(folder).use { list -> list.map { it.fileName.toString() }.collect(Collectors.toList()) }
            return names.any { it != "signin.lock" }
        }
    }

    private class StatusCommand(io: Io, private val services: Services) : EnvironmentLeaf("status", io) {
        override fun help(context: Context) =
            "Shows what is saved for one environment and what to do next. It only reads, contacts nobody and shows no secret."

        override fun run() {
            val env = environment()
            val facts = StatusFacts(env, base(), services)
            val registration = facts.registration
            val (signInState, signInReason) = when (val signIn = facts.signIn) {
                is LoadResult.Found -> "saved" to null
                LoadResult.Missing -> "missing" to null
                LoadResult.Disabled -> "disabled" to null
                is LoadResult.Unusable -> "unusable" to signIn.reason
            }
            val noApp = env.signInClientId == null
            val waiting = facts.waiting
            val json = head("status") {
                put("environment", env.name)
                put("ready", facts.ready)
                put("registration", buildJsonObject {
                    put("saved", registration != null)
                    registration?.let { put("account", it.account); put("clientId", it.clientId) }
                })
                put("signIn", buildJsonObject {
                    put("state", signInState)
                    put("checkedWithGitHub", false)
                    signInReason?.let { put("reason", it) }
                    facts.account?.let { put("account", it) }
                })
                if (waiting != null) put("waitingCode", buildJsonObject {
                    put("userCode", waiting.userCode)
                    put("verificationUri", waiting.verificationUri)
                    put("expiresAt", waiting.expiresAt.toString())
                })
                if (noApp) put("message", env.signInNotAvailable())
                facts.next?.let { putNext(it) }
            }
            val text = buildString {
                appendLine("Environment: ${env.displayName} (${env.name})")
                append("Sign-in: ")
                appendLine(
                    when {
                        signInState == "saved" -> "saved" + (facts.account?.let { " for $it" } ?: ", account not known yet") + ", not checked with GitHub"
                        signInState == "missing" -> "none saved"
                        signInState == "disabled" -> "saving is switched off"
                        else -> "cannot be used. $signInReason"
                    },
                )
                if (waiting != null) appendLine("Waiting code: ${waiting.userCode} at ${waiting.verificationUri}, runs out at ${waiting.expiresAt}")
                appendLine(registration?.let { "Registration: saved for ${it.account}, client ID ${it.clientId}" } ?: "Registration: none saved")
                when {
                    noApp -> append(env.signInNotAvailable())
                    facts.ready -> append("Ready.")
                    else -> facts.next?.let { append(nextLine(it)) }
                }
            }.trimEnd()
            answer(Reply(ExitCodes.DONE, json, text, exactValuesIn(json) + env.displayName))
        }
    }

    private class ImportLegacyCommand(io: Io) : Leaf("import-legacy", io) {
        override fun help(context: Context) =
            "Previews, or with --copy makes, a copy of the old Rumble client's state. The old files are only read, never changed or removed."

        private val config by argument("CONFIG", help = "The old client's rumble-client.json.")
        private val copy by option("--copy", help = "Make the copy. Without it only a preview is given and nothing is written.").flag()
        private val workFolder by option("--work-folder", help = "The old client's work folder, if it is not where the configuration says.")

        override fun run() {
            val base = base()
            val importer = LegacyClientImport(base, programVersion = programVersion())
            val configPath = Path.of(config)
            val work = workFolder?.let { Path.of(it) }
            fun relative(path: Path): String = try { base.relativize(path.toAbsolutePath().normalize()).toString().replace("\\", "/") } catch (e: IllegalArgumentException) { "" }
            val mode = if (copy) "copy" else "preview"

            val reply: Reply = if (!copy) {
                when (val preview = importer.preview(configPath, work)) {
                    is LegacyPreview.Plan -> {
                        val plan = preview.plan
                        val total = plan.files.sumOf { it.size }
                        Reply(
                            ExitCodes.DONE,
                            head("import-legacy") {
                                put("mode", mode); put("outcome", "plan"); put("environment", plan.environment.name)
                                put("target", relative(plan.target))
                                put("files", buildJsonArray { plan.files.forEach { f -> add(buildJsonObject { put("path", f.path); put("size", f.size) }) } })
                                put("notCopied", buildJsonArray { plan.notCopied.forEach { n -> add(buildJsonObject { put("path", n.path); put("reason", n.reason) }) } })
                            },
                            buildString {
                                appendLine("Would copy ${plan.files.size} files ($total bytes) into ${relative(plan.target)} for the ${plan.environment.displayName} Rumble. Nothing was written. Add --copy to copy.")
                                plan.notCopied.forEach { appendLine("Not copied: ${it.path}. ${it.reason}") }
                            }.trimEnd(),
                        )
                    }
                    is LegacyPreview.NothingToImport -> simple("nothing", ExitCodes.DONE, preview.message, mode)
                    is LegacyPreview.Refused -> simple("refused", ExitCodes.REFUSED, preview.message, mode)
                }
            } else {
                when (val result = importer.import(configPath, work)) {
                    is LegacyImportResult.Imported -> Reply(
                        ExitCodes.DONE,
                        head("import-legacy") {
                            put("mode", mode); put("outcome", "imported"); put("environment", result.environment)
                            put("copied", result.copied); put("alreadyCopied", result.alreadyCopied); put("target", relative(result.target))
                        },
                        "Copied ${result.copied} files and found ${result.alreadyCopied} already copied, into ${relative(result.target)}. The old files were not changed.",
                    )
                    is LegacyImportResult.NothingToImport -> simple("nothing", ExitCodes.DONE, result.message, mode)
                    is LegacyImportResult.Refused -> simple("refused", ExitCodes.REFUSED, result.message, mode)
                    is LegacyImportResult.Incomplete -> Reply(
                        ExitCodes.FAILED,
                        head("import-legacy") {
                            put("mode", mode); put("outcome", "incomplete"); put("copied", result.copied)
                            put("changedDuringImport", JsonArray(result.changedDuringImport.map { JsonPrimitive(it) }))
                        },
                        "Copied ${result.copied} files, but ${result.changedDuringImport.size} changed while they were read: ${result.changedDuringImport.joinToString()}. Run the same command again.",
                    )
                    is LegacyImportResult.Failed -> simple("failed", ExitCodes.FAILED, result.message, mode)
                }
            }
            answer(reply)
        }

        private fun simple(outcome: String, code: Int, message: String, mode: String) =
            Reply(code, head("import-legacy") { put("mode", mode); put("outcome", outcome); put("message", message) }, message)
    }
}
