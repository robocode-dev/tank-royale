package dev.robocode.tankroyale.rumble.cli

import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.restrictTo
import dev.robocode.tankroyale.rumble.AdviceKeeper
import dev.robocode.tankroyale.rumble.AutomaticUpload
import dev.robocode.tankroyale.rumble.JournalClient
import dev.robocode.tankroyale.rumble.JournalUnusable
import dev.robocode.tankroyale.rumble.PinRefusal
import dev.robocode.tankroyale.rumble.RankedJournal
import dev.robocode.tankroyale.rumble.RecordState
import dev.robocode.tankroyale.rumble.Registration
import dev.robocode.tankroyale.rumble.RegisteredClient
import dev.robocode.tankroyale.rumble.RegistrationRecord
import dev.robocode.tankroyale.rumble.ResultUploader
import dev.robocode.tankroyale.rumble.RumbleEnvironment
import dev.robocode.tankroyale.rumble.RumbleSnapshot
import dev.robocode.tankroyale.rumble.SavedRenewal
import dev.robocode.tankroyale.rumble.SavedSignInUpload
import dev.robocode.tankroyale.rumble.SelectionMode
import dev.robocode.tankroyale.rumble.SentBatches
import dev.robocode.tankroyale.rumble.SessionControl
import dev.robocode.tankroyale.rumble.SessionEnd
import dev.robocode.tankroyale.rumble.SessionEvent
import dev.robocode.tankroyale.rumble.SessionPin
import dev.robocode.tankroyale.rumble.SessionPins
import dev.robocode.tankroyale.rumble.SessionPlanner
import dev.robocode.tankroyale.rumble.SessionRequest
import dev.robocode.tankroyale.rumble.SessionRunner
import dev.robocode.tankroyale.rumble.SessionSetup
import dev.robocode.tankroyale.rumble.UploadEvent
import dev.robocode.tankroyale.rumble.WorkerHost
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import java.io.IOException
import java.util.concurrent.atomic.AtomicInteger

/** The ranked formats a session runs when none are chosen. */
private val RANKED_FORMATS = listOf("1v1", "twinduel", "melee")

/**
 * Runs a contribution session: checks that the sign-in, the registration, the catalog, the engine pin and the battle worker
 * are ready, then runs battles with the shared session loop and sends the results between them, until the battle count
 * is reached or the person stops it. Every event is a line, or with `--json` a JSON line, and a final answer follows.
 */
internal class ContributeCommand(io: Io, services: Services) : SignInLeaf("contribute", io, services) {
    override fun help(context: Context) =
        "Runs ranked battles and sends their results, until the battle count is reached or Ctrl+C. " +
            "The first Ctrl+C finishes the battle that is running and stops; a second ends it at once and keeps nothing of it."

    private val bots by option("--bot", help = "A bot to choose, as \"Name version\". Give it again for more bots.").multiple()
    private val mode by option("--mode", help = "all: any bots; prefer: the chosen bots first; only: battles always hold a chosen bot.")
        .choice("all", "prefer", "only").default("all")
    private val formats by option("--format", help = "A game format: 1v1, twinduel or melee. Give it again for more. Defaults to all three.")
        .choice(*RANKED_FORMATS.toTypedArray()).multiple()
    private val battles by option("--battles", help = "How many battles to keep, then stop. Without it the session runs until stopped.")
        .int().restrictTo(min = 1)

    /** Events come from the session and from Ctrl+C on another thread; one line is written whole before the next. */
    private val output = Any()

    override fun run() {
        val env = environment()
        val stateRoot = env.stateRoot(base())
        val chosenFormats = formats.distinct().ifEmpty { RANKED_FORMATS }
        val selectionMode = when (mode) {
            "prefer" -> SelectionMode.PREFER
            "only" -> SelectionMode.ONLY
            else -> SelectionMode.ALL
        }
        if (selectionMode != SelectionMode.ALL && bots.isEmpty()) {
            throw BadInput("--mode $mode needs at least one --bot.")
        }

        // The sign-in and the registration.
        val flow = flow(env)
        val renewal = when (val r = flow.renewSaved()) {
            is SavedRenewal.Renewed -> r
            is SavedRenewal.SignInNeeded -> return answer(reply(env, ExitCodes.SIGN_IN_NEEDED, outcomeOf(r), r.message, "login"))
            is SavedRenewal.Failed -> return answer(reply(env, ExitCodes.FAILED, "sign-in-failed", "The sign-in could not be renewed: ${r.message} Try again later."))
        }
        val token = renewal.tokens.accessToken
        val account = renewal.account ?: flow.identify(renewal)
            ?: return answer(reply(env, ExitCodes.FAILED, "account-unknown", "GitHub did not say who is signed in. Try again later."))
        val memory = flow.registrationMemory(renewal)
        val clientId = memory.load(account)?.clientId ?: when (val read = Registration(env, services.api, memory).registeredClient(account, token)) {
            is RegisteredClient.Found -> read.clientId.also {
                // Remembered, so the next session needs no request for it.
                memory.save(RegistrationRecord(account, it, env.name, services.clock.instant()))
            }
            RegisteredClient.None -> return answer(reply(env, ExitCodes.SIGN_IN_NEEDED, "not-registered", "The account $account is not registered with this Rumble yet.", "register"))
            is RegisteredClient.SignInRefused -> {
                flow.forgetRefused(renewal)
                return answer(reply(env, ExitCodes.SIGN_IN_NEEDED, "sign-in-refused", read.message, "login"))
            }
            is RegisteredClient.Unreadable -> return answer(reply(env, ExitCodes.FAILED, "registration-unreadable", "The account's registration could not be read: ${read.message} Try again later."))
        }

        // The catalog, the engine pin and the battle worker.
        val source = services.catalog(env)
        val snapshot = RumbleSnapshot.read(services.api, env, source, token)
        val catalog = snapshot.catalog
            ?: return answer(reply(env, ExitCodes.FAILED, "catalog-unreadable", "The Rumble's bot catalog could not be read: ${snapshot.problem.orEmpty()} Try again later."))
        val active = catalog.activeBots.map { SessionPlanner.key(it) }.toSet()
        val known = catalog.bots.map { SessionPlanner.key(it) }.toSet()
        bots.firstOrNull { it !in active }?.let {
            throw BadInput(
                if (it in known) "The bot \"$it\" is not active in the Rumble, so no battle can hold it."
                else "The catalog has no bot \"$it\". Give a bot as its name and version, as in \"Walls 1.0.0\".",
            )
        }
        val data = services.resultData(env)
        val pin = when (val prepared = SessionPins.prepare(data, chosenFormats)) {
            is SessionPin.Ready -> prepared
            is SessionPin.Refused -> return answer(pinRefused(env, prepared))
        }
        val worker = try {
            services.worker(stateRoot)
        } catch (e: IOException) {
            return answer(reply(env, ExitCodes.FAILED, "worker-unavailable", "The battle worker could not be put in place: ${e.message}"))
        }

        // The session.
        val ranked = stateRoot.resolve("ranked")
        val (journal, sent) = try {
            RankedJournal.open(env, ranked.resolve("journal.jsonl")) to SentBatches.open(ranked.resolve("sent-batches.jsonl"))
        } catch (e: JournalUnusable) {
            return answer(reply(env, ExitCodes.FAILED, "journal-unusable", e.message.orEmpty()))
        }
        val version = programVersion()
        val uploader = ResultUploader(
            env, journal, sent, services.api,
            SavedSignInUpload(flow.savedSignIn()) { services.clock.instant() },
            data, { report(uploadEvent(it)) },
        )
        val automatic = AutomaticUpload(uploader, clock = { services.clock.instant() })
        val advice = AdviceKeeper(data, chosenFormats, clock = { services.clock.instant() })
        val control = SessionControl()
        val interrupts = AtomicInteger()
        val kept = AtomicInteger()
        val setup = SessionSetup(
            commit = catalog.commit, source = source, botCache = ranked.resolve("bot-cache"), host = WorkerHost(services.workerLimits),
            command = worker, journal = journal, engine = pin.engine(version, version), client = JournalClient(clientId, version),
            roundsPerFormat = pin.roundsPerFormat, sleep = services.sleep, clock = { services.clock.instant() },
            betweenBattles = { automatic.betweenBattles() },
            // The catalog is read once, so a session that cannot choose a battle would wait for ever: it ends instead.
            endWhenWaiting = true,
        )
        val seed = services.seed()
        report(started(env, account, catalog.commit, pin, chosenFormats))
        // The Ctrl+C handle stays open until the answer is written, so where a shutdown hook stands in for it the program is
        // held open while the results are sent and the answer is written.
        services.interrupts {
            if (interrupts.incrementAndGet() == 1) {
                control.finishAndStop()
                report(note("stopping", "Stopping after the battle that is running. Press Ctrl+C again to end it now."))
            } else {
                control.cancelNow()
                report(note("cancelling", "Ending the battle that is running now; nothing of it is kept, and nothing is sent now."))
            }
        }.use {
            var end = SessionRunner(setup, control) { event ->
                if (event is SessionEvent.BattleKept) kept.incrementAndGet()
                report(sessionEvent(event))
            }.run {
                SessionRequest(catalog.bots, bots.toSet(), selectionMode, chosenFormats, pin.participantCounts, battles, advice.current(), seed)
            }
            // What waits is sent now, unless the person asked to end at once or the journal cannot be written.
            if (interrupts.get() < 2 && end != SessionEnd.Cancelled && end !is SessionEnd.JournalFailed) {
                try {
                    automatic.finish()
                } catch (e: JournalUnusable) {
                    end = SessionEnd.JournalFailed(e.message.orEmpty())
                }
            }
            answer(ended(env, end, kept.get(), journal))
        }
    }

    /** Writes one event as a line of text, or as one JSON line. */
    private fun report(event: Pair<JsonObject, String>) {
        synchronized(output) { if (json) io.event(event.first) else io.say(event.second) }
    }

    private fun note(kind: String, text: String) = event(kind) { put("message", text) } to text

    private fun event(kind: String, build: kotlinx.serialization.json.JsonObjectBuilder.() -> Unit): JsonObject = buildJsonObject {
        put("event", kind)
        build()
    }

    private fun started(env: RumbleEnvironment, account: String, commit: String, pin: SessionPin.Ready, formats: List<String>): Pair<JsonObject, String> =
        event("started") {
            put("environment", env.name)
            put("account", account)
            put("catalogCommit", commit)
            put("behaviorVersion", pin.pin.behaviorVersion)
            putJsonArray("formats") { formats.forEach { add(kotlinx.serialization.json.JsonPrimitive(it)) } }
            battles?.let { put("battles", it) }
        } to "Contributing to ${env.displayName} as $account: ${formats.joinToString()}" +
            (battles?.let { ", $it battles" } ?: ", until stopped") + ". Press Ctrl+C to stop."

    private fun sessionEvent(event: SessionEvent): Pair<JsonObject, String> = when (event) {
        is SessionEvent.BattleStarted -> event("battle-started") {
            put("battleId", event.battleId); put("gameType", event.gameType)
            putJsonArray("participants") { event.participants.forEach { add(kotlinx.serialization.json.JsonPrimitive(it)) } }
        } to "Battle ${event.gameType}: ${event.participants.joinToString(" vs ")}"
        is SessionEvent.BattleKept -> event("battle-kept") { put("battleId", event.battleId) } to "  kept"
        is SessionEvent.BattleNotKept -> event("battle-not-kept") {
            put("battleId", event.battleId); put("reason", event.reason.name); put("detail", event.detail)
        } to "  not kept (${event.reason.name.lowercase()})" + if (event.detail.isNotBlank()) ": ${io.outside(event.detail)}" else ""
        is SessionEvent.BotNotPlaced -> event("bot-not-placed") {
            put("bot", event.bot); put("reason", event.reason.name); put("detail", event.detail)
        } to "Bot ${event.bot} could not be put in place (${event.reason.name.lowercase()})" + if (event.detail.isNotBlank()) ": ${io.outside(event.detail)}" else ""
        is SessionEvent.Waiting -> event("waiting") {
            put("reason", event.reason.name)
            putJsonArray("values") { event.values.forEach { add(kotlinx.serialization.json.JsonPrimitive(it)) } }
        } to "Waiting (${event.reason.name.lowercase()}): ${event.values.joinToString()}"
        SessionEvent.Paused -> event("paused") {} to "Paused"
        SessionEvent.Resumed -> event("resumed") {} to "Resumed"
    }

    private fun uploadEvent(event: UploadEvent): Pair<JsonObject, String> = when (event) {
        is UploadEvent.BatchSent -> event("batch-sent") {
            put("batchId", event.batchId); put("issue", event.issue); put("results", event.results)
        } to "Sent ${event.results} result(s) as issue ${event.issue}"
        is UploadEvent.NotSent -> event("not-sent") {
            put("reason", event.reason.name)
            putJsonArray("values") { event.values.forEach { add(kotlinx.serialization.json.JsonPrimitive(it)) } }
        } to "Results not sent now (${event.reason.name.lowercase()}); they are kept and sent later"
        is UploadEvent.RecordAccepted -> event("result-accepted") { put("battleId", event.battleId) } to "Result ${event.battleId} accepted"
        is UploadEvent.RecordRejected -> event("result-rejected") {
            put("battleId", event.battleId); put("reason", event.reason)
        } to "Result ${event.battleId} rejected: ${io.outside(event.reason)}"
        is UploadEvent.BatchRefused -> event("batch-refused") {
            put("batchId", event.batchId); put("issue", event.issue); put("reason", event.reason)
        } to "The Rumble refused issue ${event.issue}: ${io.outside(event.reason)}. Sending stops until this is fixed."
        is UploadEvent.SetAside -> event("set-aside") {
            putJsonArray("battleIds") { event.battleIds.forEach { add(kotlinx.serialization.json.JsonPrimitive(it)) } }
        } to "${event.battleIds.size} result(s) of an older engine are kept apart and not sent"
    }

    private fun pinRefused(env: RumbleEnvironment, refused: SessionPin.Refused): Reply {
        val message = when (refused.reason) {
            PinRefusal.BEHAVIOR_VERSION_DIFFERS ->
                "This Rumble runs engine behavior version ${refused.values[0]} and this program runs ${refused.values[1]}, so its results would be refused. " +
                    if (refused.values[0].toInt() > refused.values[1].toInt()) "Update Tank Royale to take part." else "The Rumble has not moved to this version yet; try again later."
            PinRefusal.UNREADABLE -> "The Rumble's engine pin could not be read: ${refused.values.firstOrNull().orEmpty()} Try again later."
            PinRefusal.MISSING -> "The Rumble has no engine pin, so no ranked battle can run."
            PinRefusal.UNSUPPORTED_SCHEMA -> "The Rumble's engine pin is newer than this program reads. Update Tank Royale to take part."
            PinRefusal.FORMAT_NOT_PINNED -> "The Rumble does not run the format ${refused.values.firstOrNull().orEmpty()}."
            PinRefusal.FORMAT_NOT_RUNNABLE -> "This program cannot run the format ${refused.values.firstOrNull().orEmpty()} the way the Rumble pins it."
            PinRefusal.NOT_UNDERSTOOD -> "The Rumble's engine pin could not be understood: ${refused.values.firstOrNull().orEmpty()}"
        }
        val code = if (refused.reason == PinRefusal.UNREADABLE) ExitCodes.FAILED else ExitCodes.REFUSED
        return reply(env, code, "pin-refused", message) {
            put("reason", refused.reason.name)
            putJsonArray("values") { refused.values.forEach { add(kotlinx.serialization.json.JsonPrimitive(it)) } }
        }
    }

    /** The final answer: how the session ended, what it kept, and where the results stand. */
    private fun ended(env: RumbleEnvironment, end: SessionEnd, kept: Int, journal: RankedJournal): Reply {
        val (code, outcome, message) = endedBy(end)
        return endedReply(env, code, outcome, message, kept, journal)
    }

    private fun endedBy(end: SessionEnd): Triple<Int, String, String> =
        when (end) {
            SessionEnd.Finished -> Triple(ExitCodes.DONE, "finished", "The session ran its battles.")
            SessionEnd.Stopped -> Triple(ExitCodes.DONE, "stopped", "The session was stopped after its battle finished.")
            SessionEnd.Cancelled -> Triple(
                ExitCodes.NOT_FINISHED, "cancelled",
                "The session was stopped at once; the battle that was running is not kept, and the waiting results are sent by the next session.",
            )
            is SessionEnd.NothingToRun -> Triple(
                ExitCodes.FAILED, "nothing-to-run",
                "No battle can be run with these choices (${end.reason.name.lowercase()}: ${end.values.joinToString()}), so the session ended. Choose other bots, a mode or formats, or try again when the catalog has changed.",
            )
            is SessionEnd.GaveUp -> Triple(ExitCodes.FAILED, "gave-up", "${end.failures} battles in a row did not complete, so the session gave up. The reasons are in the lines above.")
            is SessionEnd.JournalFailed -> Triple(ExitCodes.FAILED, "journal-failed", "The results could not be kept on the disk, so the session stopped: ${end.message}")
        }

    private fun endedReply(env: RumbleEnvironment, code: Int, outcome: String, message: String, kept: Int, journal: RankedJournal): Reply {
        val states = journal.entries().groupingBy { it.state }.eachCount()
        fun count(state: RecordState) = states[state] ?: 0
        val json = head(commandName) {
            put("environment", env.name)
            put("outcome", outcome)
            put("message", message)
            put("keptThisSession", kept)
            put("waiting", count(RecordState.WAITING))
            put("sent", count(RecordState.HANDED_OVER))
            put("accepted", count(RecordState.ACCEPTED))
            put("rejected", count(RecordState.REJECTED))
            put("setAside", count(RecordState.SET_ASIDE))
        }
        val text = "$message Kept this session: $kept. Results: ${count(RecordState.WAITING)} waiting, ${count(RecordState.HANDED_OVER)} sent, " +
            "${count(RecordState.ACCEPTED)} accepted, ${count(RecordState.REJECTED)} rejected, ${count(RecordState.SET_ASIDE)} kept apart."
        return Reply(code, json, text)
    }
}
