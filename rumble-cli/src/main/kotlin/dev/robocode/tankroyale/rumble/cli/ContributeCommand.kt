package dev.robocode.tankroyale.rumble.cli

import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.long
import com.github.ajalt.clikt.parameters.types.restrictTo
import dev.robocode.tankroyale.rumble.AdviceKeeper
import dev.robocode.tankroyale.rumble.AutomaticUpload
import dev.robocode.tankroyale.rumble.BotPlatform
import dev.robocode.tankroyale.rumble.CatalogBot
import dev.robocode.tankroyale.rumble.JournalClient
import dev.robocode.tankroyale.rumble.JournalUnusable
import dev.robocode.tankroyale.rumble.PinRefusal
import dev.robocode.tankroyale.rumble.RankedJournal
import dev.robocode.tankroyale.rumble.RecordState
import dev.robocode.tankroyale.rumble.RegisteredClient
import dev.robocode.tankroyale.rumble.Registration
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
import dev.robocode.tankroyale.rumble.SessionControlRequest
import dev.robocode.tankroyale.rumble.SessionFolder
import dev.robocode.tankroyale.rumble.SessionLook
import dev.robocode.tankroyale.rumble.SessionOwner
import dev.robocode.tankroyale.rumble.SessionPython
import dev.robocode.tankroyale.rumble.SessionResults
import dev.robocode.tankroyale.rumble.SessionTracker
import dev.robocode.tankroyale.rumble.SessionPythonProblem
import dev.robocode.tankroyale.rumble.SessionPythonSetup
import dev.robocode.tankroyale.rumble.SessionRequest
import dev.robocode.tankroyale.rumble.SessionRunner
import dev.robocode.tankroyale.rumble.SessionSetup
import dev.robocode.tankroyale.rumble.SupportedVersions
import dev.robocode.tankroyale.rumble.UploadEvent
import dev.robocode.tankroyale.rumble.WorkerFolders
import dev.robocode.tankroyale.rumble.WorkerHost
import java.io.IOException
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.serialization.json.JsonObject
import dev.robocode.tankroyale.rumble.DesktopSettings
import dev.robocode.tankroyale.rumble.OpenTankRoyale
import dev.robocode.tankroyale.rumble.SessionShow
import dev.robocode.tankroyale.rumble.SessionStatus
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray

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
    private val ownerPid by option(
        "--owner-pid",
        help = "The process that owns the session. When it is gone, the session finishes the running battle and ends. The desktop program gives its own.",
    ).long()
    private val show by option(
        "--show",
        help = "How the session shows itself while it runs: tray (an icon, or its window where there is no tray), window, or none. Defaults to none.",
    ).choice("tray", "window", "none").default("none")
    private val openCommand by option(
        "--open-command",
        help = "One part of the command that starts Tank Royale, for the session's Open Tank Royale. Give it again for each part. The desktop program gives its own.",
    ).multiple()

    /** The session's tray icon or window, as chosen; null when it shows nothing. */
    @Volatile
    private var display: SessionDisplay? = null

    /** The session's last status once it ended, which the tray icon or window shows when [run] is done with all else. */
    @Volatile
    private var endedStatus: SessionStatus? = null

    /** The session's folder in the Rumble state, kept up to date from the events reported; null before the lock is taken. */
    @Volatile
    private var tracker: SessionTracker? = null

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
        val owner = ownerPid?.let { pid ->
            SessionOwner.of(pid) ?: throw BadInput("--owner-pid $pid names no process that runs.")
        }

        // One session of an environment at a time: both would write the same journal and send the same results.
        val folder = SessionFolder.of(env, base())
        val running = try {
            folder.claim()
        } catch (e: IOException) {
            return answer(reply(env, ExitCodes.FAILED, "session-folder-unusable", "The session folder could not be used: ${e.message}"))
        } ?: return answer(sessionRunning(env, folder))
        tracker = SessionTracker(running, env, owner, chosenFormats, battles) { services.clock.instant() }
        display = SessionDisplay(
            SessionShow.of(show) ?: SessionShow.NONE, { tracker?.status }, folder, OpenTankRoyale(folder, openCommand), DesktopSettings(base()),
        ).also { it.start() }
        try {
            contribute(env, stateRoot, chosenFormats, selectionMode, owner)
        } catch (e: Throwable) {
            // Bad input found only now, or a fault: the session still ends in its folder with the reason.
            val t = tracker
            tracker = null
            if (t != null) {
                runCatching { t.end(if (e is BadInput) "bad-input" else "failed", e.message.orEmpty()) }
                runCatching { t.close() }
            }
            // The answer to a fault is written by the caller, so the end is not waited for here: the person sees the
            // failure on the Contribute battles screen or in the terminal, and the icon and window go with the program.
            endedStatus = null
            throw e
        } finally {
            // Only now, with the answer written and the Ctrl+C handler, the watcher and Python let go of, the tray
            // icon or window shows the end and waits until the person closes it.
            endedStatus?.let { final -> display?.ended(final) }
        }
    }

    private fun contribute(
        env: RumbleEnvironment,
        stateRoot: java.nio.file.Path,
        chosenFormats: List<String>,
        selectionMode: SelectionMode,
        owner: SessionOwner?,
    ) {
        // The sign-in and the registration.
        val flow = flow(env)
        val renewal = when (val r = flow.renewSaved()) {
            is SavedRenewal.Renewed -> r
            is SavedRenewal.SignInNeeded -> return endWith(reply(env, ExitCodes.SIGN_IN_NEEDED, outcomeOf(r), r.message, "login"))
            is SavedRenewal.Failed -> return endWith(reply(env, ExitCodes.FAILED, "sign-in-failed", "The sign-in could not be renewed: ${r.message} Try again later."))
        }
        val token = renewal.tokens.accessToken
        val account = renewal.account ?: flow.identify(renewal)
            ?: return endWith(reply(env, ExitCodes.FAILED, "account-unknown", "GitHub did not say who is signed in. Try again later."))
        val memory = flow.registrationMemory(renewal)
        val clientId = memory.load(account)?.clientId ?: when (val read = Registration(env, services.api, memory).registeredClient(account, token)) {
            is RegisteredClient.Found -> read.clientId.also {
                // Remembered, so the next session needs no request for it.
                memory.save(RegistrationRecord(account, it, env.name, services.clock.instant()))
            }
            RegisteredClient.None -> return endWith(reply(env, ExitCodes.SIGN_IN_NEEDED, "not-registered", "The account $account is not registered with this Rumble yet.", "register"))
            is RegisteredClient.SignInRefused -> {
                flow.forgetRefused(renewal)
                return endWith(reply(env, ExitCodes.SIGN_IN_NEEDED, "sign-in-refused", read.message, "login"))
            }
            is RegisteredClient.Unreadable -> return endWith(reply(env, ExitCodes.FAILED, "registration-unreadable", "The account's registration could not be read: ${read.message} Try again later."))
        }

        // The catalog, the engine pin and the battle worker.
        val source = services.catalog(env)
        val snapshot = RumbleSnapshot.read(services.api, env, source, token)
        val catalog = snapshot.catalog
            ?: return endWith(reply(env, ExitCodes.FAILED, "catalog-unreadable", "The Rumble's bot catalog could not be read: ${snapshot.problem.orEmpty()} Try again later."))
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
            is SessionPin.Refused -> return endWith(pinRefused(env, prepared))
        }
        val worker = try {
            services.worker(stateRoot)
        } catch (e: IOException) {
            return endWith(reply(env, ExitCodes.FAILED, "worker-unavailable", "The battle worker could not be put in place: ${e.message}"))
        }

        // The session.
        val ranked = stateRoot.resolve("ranked")
        val (journal, sent) = try {
            RankedJournal.open(env, ranked.resolve("journal.jsonl")) to SentBatches.open(ranked.resolve("sent-batches.jsonl"))
        } catch (e: JournalUnusable) {
            return endWith(reply(env, ExitCodes.FAILED, "journal-unusable", e.message.orEmpty()))
        }
        val version = programVersion()
        tracker?.results = { journalResults(journal) }
        val uploader = ResultUploader(
            env, journal, sent, services.api,
            SavedSignInUpload(flow.savedSignIn()) { services.clock.instant() },
            data, { report(uploadEvent(it)) },
        )
        val interrupts = AtomicInteger()
        // After the second Ctrl+C nothing more is started over the network.
        val automatic = AutomaticUpload(uploader, clock = { services.clock.instant() }, stop = { interrupts.get() >= 2 })
        val advice = AdviceKeeper(data, chosenFormats, clock = { services.clock.instant() })
        val control = SessionControl()
        val kept = AtomicInteger()
        report(started(env, account, catalog.commit, pin, chosenFormats))
        val seed = services.seed()
        // Python bots get the program's own Python Bot API, set up once the session can be stopped with Ctrl+C.
        val python = SessionPythonHolder { reason, detail ->
            report(event("python-unavailable") { put("reason", reason.name) } to "${io.outside(detail)} The Python bots are left out of the rest of this session.")
        }
        val setup = SessionSetup(
            commit = catalog.commit, source = source, botCache = ranked.resolve("bot-cache"), host = WorkerHost(services.workerLimits),
            command = worker, journal = journal, engine = pin.engine(version, version), client = JournalClient(clientId, version),
            roundsPerFormat = pin.roundsPerFormat, sleep = services.sleep, clock = { services.clock.instant() },
            betweenBattles = { automatic.betweenBattles() },
            battleEnvironment = { battleBots, folders -> python.environment(battleBots, folders) },
            // The catalog is read once, so a session that cannot choose a battle would wait for ever: it ends instead.
            endWhenWaiting = true,
        )
        // The Ctrl+C handle stays open until the answer is written, so where a shutdown hook stands in for it the program is
        // held open while the results are sent and the answer is written.
        // Only a stop from the terminal can be followed by a second Ctrl+C; one asked through the session folder or by the
        // owner being gone says how to end it now from anywhere.
        fun stopAfterBattle(byCtrlC: Boolean = false) {
            if (interrupts.compareAndSet(0, 1)) {
                control.finishAndStop()
                report(
                    note(
                        "stopping",
                        if (byCtrlC) "Stopping after the battle that is running. Press Ctrl+C again to end it now."
                        else "Stopping after the battle that is running. Run rumble session stop-now to end it now.",
                    ),
                )
            }
        }
        fun stopNow() {
            if (interrupts.getAndSet(2) < 2) {
                control.cancelNow()
                report(note("cancelling", "Ending the battle that is running now; nothing of it is kept, and nothing is sent now."))
            }
        }
        val watcher = SessionWatcher(
            requests = { tracker?.take().orEmpty() },
            owner = owner,
            onRequest = { request ->
                when (request) {
                    SessionControlRequest.PAUSE -> if (interrupts.get() == 0 && control.pause()) {
                        report(note("pausing", "Pausing after the battle that is running."))
                    }
                    SessionControlRequest.RESUME -> if (control.resume() && tracker?.status?.state != "paused") {
                        // A pause that had not taken hold yet is simply lifted; a session that paused says "resumed" itself.
                        report(note("resuming", "Going on."))
                    }
                    SessionControlRequest.STOP -> stopAfterBattle()
                    SessionControlRequest.STOP_NOW -> stopNow()
                    SessionControlRequest.RELEASE -> report(note("released", "The session no longer stops when the program that started it ends."))
                }
            },
            onOwnerGone = {
                if (interrupts.get() == 0) {
                    report(note("owner-gone", "The program that started the session has ended, so the session stops after the battle that is running."))
                    stopAfterBattle()
                } else {
                    report(note("owner-gone", "The program that started the session has ended."))
                }
            },
        )
        services.interrupts {
            if (interrupts.get() == 0) stopAfterBattle(byCtrlC = true) else stopNow()
        }.use {
          watcher.use {
            watcher.start()
            // Any active bot may be chosen as an opponent, also in `only` mode, so Python is set up whenever the catalog holds an
            // active Python bot.
            python.python = pythonFor(catalog.activeBots, ranked) { interrupts.get() > 0 }
            // Without a usable Python the Python bots are left out, and with them every team that holds one.
            fun entries() = if (python.python != null) catalog.bots else catalog.bots.filter { !isPython(it) }
            try {
                var end = SessionRunner(setup, control) { event ->
                    if (event is SessionEvent.BattleKept) kept.incrementAndGet()
                    report(sessionEvent(event))
                }.run {
                    SessionRequest(entries(), bots.toSet(), selectionMode, chosenFormats, pin.participantCounts, battles, advice.current(), seed)
                }
                // What waits is sent now, unless the person asked to end at once or the journal cannot be written.
                if (interrupts.get() < 2 && end != SessionEnd.Cancelled && end !is SessionEnd.JournalFailed) {
                    report(note("finishing", "Sending the results that wait and reading their receipts before the session ends."))
                    try {
                        automatic.finish()
                    } catch (e: JournalUnusable) {
                        end = SessionEnd.JournalFailed(e.message.orEmpty())
                    }
                }
                // Written under the same lock as the events, so no late event line follows the answer.
                synchronized(output) { endWith(ended(env, end, kept.get(), journal)) }
            } finally {
                python.close()
            }
          }
        }
    }

    /** Answers [reply] and records it as the session's end in its folder, which lets go of the lock. */
    private fun endWith(reply: Reply) {
        val t = tracker
        tracker = null
        if (t != null) {
            val message = (reply.json["message"] as? kotlinx.serialization.json.JsonPrimitive)?.content.orEmpty()
            val outcome = (reply.json["outcome"] as? kotlinx.serialization.json.JsonPrimitive)?.content.orEmpty()
            // The answer is written whatever happens to the session folder.
            runCatching { t.end(outcome, message) }
            runCatching { t.close() }
            answer(reply)
            // The end is shown when run() is done with everything else, outside every lock; see run().
            endedStatus = t.status
            return
        }
        answer(reply)
    }

    private fun sessionRunning(env: RumbleEnvironment, folder: SessionFolder): Reply {
        val status = (folder.look() as? SessionLook.Running)?.status
        val by = status?.let { " (process ${it.pid}, started ${it.startedAt})" }.orEmpty()
        return reply(env, ExitCodes.REFUSED, "session-running", "A contribution session of this environment already runs$by. Follow it with rumble session status.") {
            status?.let { put("pid", it.pid) }
        }
    }


    /**
     * Sets up the program's own Python Bot API for the session when the catalog holds an active Python bot,
     * and says how that went. Null when there is none to run or Python cannot be used here; the Python bots are then left
     * out. A Ctrl+C during the setup, which [stopped] tells, also ends pip, so it is reported as a stop and not as a failure.
     */
    private fun pythonFor(active: List<CatalogBot>, ranked: java.nio.file.Path, stopped: () -> Boolean): SessionPython? {
        if (active.none { isPython(it) }) return null
        val needed = try {
            SupportedVersions.parse(services.runtimeList(services.api).text()).neededFor(BotPlatform.PYTHON)
        } catch (e: IOException) {
            null
        } catch (e: IllegalArgumentException) {
            null
        }
        report(note("python-setting-up", "Setting up Python for the Python bots; the first time this takes a little while."))
        val python = SessionPython(services.tools, ranked, services.pythonBotApi, needed)
        return when (val setUp = python.setUp()) {
            is SessionPythonSetup.Ready -> python.also {
                report(event("python-ready") { put("version", setUp.version) } to "Python ${setUp.version} is ready for the Python bots.")
            }
            is SessionPythonSetup.Unavailable -> {
                python.close()
                if (stopped()) {
                    report(note("python-stopped", "Setting up Python was stopped."))
                    return null
                }
                val why = when (setUp.problem) {
                    SessionPythonProblem.MISSING -> "No Python was found on the path." + (setUp.needed?.let { " Install Python $it or newer." } ?: "")
                    SessionPythonProblem.TOO_OLD -> "Python ${setUp.found} is older than the Rumble supports. Install Python ${setUp.needed} or newer."
                    SessionPythonProblem.INSTALL_FAILED -> "The Python Bot API could not be installed: ${io.outside(setUp.details)}"
                    SessionPythonProblem.NO_BOT_API -> "This program does not carry the Python Bot API."
                    SessionPythonProblem.BATTLE_COPY_FAILED -> "A battle's copy of the Python Bot API could not be made."
                }
                report(event("python-unavailable") {
                    put("reason", setUp.problem.name)
                    setUp.needed?.let { put("needed", it) }
                    setUp.found?.let { put("found", it) }
                } to "$why The Python bots are left out of this session.")
                null
            }
        }
    }

    /** Writes one event as a line of text, or as one JSON line. */
    private fun report(event: Pair<JsonObject, String>) {
        synchronized(output) {
            tracker?.record(event.first)
            if (json) io.event(event.first) else io.say(event.second)
        }
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

private fun isPython(bot: CatalogBot) = BotPlatform.of(bot.platform) == BotPlatform.PYTHON

/**
 * What the worker of one battle gets for its Python bots: a battle that holds one gets its own checked copy of the
 * session's Python Bot API in the battle's own folder, any other battle nothing.
 */
internal fun pythonBattleEnvironment(python: SessionPython?, battleBots: List<CatalogBot>, folders: WorkerFolders): Map<String, String> =
    if (python != null && battleBots.any { isPython(it) }) python.forBattle(folders.battle) else emptyMap()

/**
 * The session's Python Bot API, when it has one. When a battle's copy can no longer be made, for example because installing
 * it again needs the network or the disk is full, it is closed and dropped, [onUnavailable] is told why, and the battle does
 * not start; the session then leaves its Python bots out instead of failing battle after battle.
 */
internal class SessionPythonHolder(private val onUnavailable: (SessionPythonProblem, String) -> Unit) : AutoCloseable {
    @Volatile
    var python: SessionPython? = null

    fun environment(battleBots: List<CatalogBot>, folders: WorkerFolders): Map<String, String> =
        try {
            pythonBattleEnvironment(python, battleBots, folders)
        } catch (e: Exception) {
            val reason = if (e.message.orEmpty().startsWith("The Python Bot API changed and could not be installed again")) SessionPythonProblem.INSTALL_FAILED
            else SessionPythonProblem.BATTLE_COPY_FAILED
            close()
            onUnavailable(reason, e.message ?: e.javaClass.simpleName)
            throw if (e is IOException) e else IOException(e.message, e)
        }

    override fun close() {
        python?.close()
        python = null
    }
}

/** The journal's result counts, as a session shows them. */
internal fun journalResults(journal: RankedJournal): SessionResults {
    val states = journal.entries().groupingBy { it.state }.eachCount()
    return SessionResults(
        waiting = states[RecordState.WAITING] ?: 0, sent = states[RecordState.HANDED_OVER] ?: 0, accepted = states[RecordState.ACCEPTED] ?: 0,
        rejected = states[RecordState.REJECTED] ?: 0, setAside = states[RecordState.SET_ASIDE] ?: 0,
    )
}

/**
 * Looks, while a session runs, for the requests asked of it in its session folder, about twice a second, and every few
 * seconds whether its owner still runs. It stops looking when closed.
 */
internal class SessionWatcher(
    private val requests: () -> List<SessionControlRequest>,
    private val owner: SessionOwner?,
    private val onRequest: (SessionControlRequest) -> Unit,
    private val onOwnerGone: () -> Unit,
    private val pollMillis: Long = 500,
    private val ownerEvery: Int = 4,
) : AutoCloseable {
    @Volatile
    private var running = true

    @Volatile
    private var watchedOwner: SessionOwner? = owner

    private val thread = Thread({ loop() }, "rumble-session-watcher").apply { isDaemon = true }

    fun start() = thread.start()

    private fun loop() {
        var round = 0
        while (running) {
            try {
                for (request in requests()) {
                    if (request == SessionControlRequest.RELEASE) watchedOwner = null
                    try {
                        onRequest(request)
                    } catch (e: Exception) {
                        // The request was taken, so it is not read again; the next one is still acted on.
                    }
                }
                val current = watchedOwner
                if (current != null && round % ownerEvery == 0 && !current.isAlive()) {
                    watchedOwner = null
                    onOwnerGone()
                }
            } catch (e: Exception) {
                // A request that cannot be read now is read at the next look.
            }
            round++
            try {
                Thread.sleep(pollMillis)
            } catch (e: InterruptedException) {
                return
            }
        }
    }

    override fun close() {
        running = false
        thread.interrupt()
    }
}
