package dev.robocode.tankroyale.rumble

import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration
import java.time.Instant
import java.util.concurrent.Executor

/** Which bots a session chooses among, as `rumble contribute --mode` names them. */
enum class ContributeMode(val option: String) { ALL("all"), PREFER("prefer"), ONLY("only") }

/** What the desktop program does with a session it started when it closes. [ASK] asks the first time. */
enum class CloseChoice { ASK, KEEP, STOP }

/** What closing the desktop program does about the session. */
enum class CloseAction {
    /** No session of this program runs, or it was let run on already. */
    NOTHING,

    /** Ask the person whether to keep contributing, and keep the answer. */
    ASK,

    /** Release the session from its owner, so it runs on. */
    RELEASE,

    /** Close; the session stops after its battle because its owner is gone. */
    LET_STOP,
}

/** The person's choices for a session. [battles] null means keep running until stopped. */
data class ContributeChoices(
    val mode: ContributeMode = ContributeMode.ALL,
    val bots: Set<String> = emptySet(),
    val formats: Set<String> = RANKED_FORMATS.toSet(),
    val battles: Int? = DEFAULT_BATTLES,
) {
    /** Whether a session can be started with these choices. */
    val startable: Boolean get() = formats.isNotEmpty() && (mode == ContributeMode.ALL || bots.isNotEmpty()) && (battles == null || battles > 0)

    /** The command line's arguments for these choices in [environment], owned by [ownerPid] when it is not null. */
    fun arguments(environment: RumbleEnvironment, ownerPid: Long?): List<String> = buildList {
        add("contribute"); add("-e"); add(environment.name); add("--mode"); add(mode.option)
        if (mode != ContributeMode.ALL) bots.sorted().forEach { add("--bot"); add(it) }
        RANKED_FORMATS.filter { it in formats }.forEach { add("--format"); add(it) }
        battles?.let { add("--battles"); add(it.toString()) }
        ownerPid?.let { add("--owner-pid"); add(it.toString()) }
    }

    companion object {
        /** The formats the Rumble ranks, in the order the screen shows them. */
        val RANKED_FORMATS = listOf("1v1", "twinduel", "melee")
        const val DEFAULT_BATTLES = 10
    }
}

/** Why the screen could not do what was asked. The texts are in the resources. */
enum class ContributeProblem { CATALOG_UNREADABLE, PROGRAM_UNAVAILABLE, START_FAILED, SESSION_FOLDER_UNUSABLE }

/** What the screen shows. */
sealed interface ContributeState {
    /** The screen is looking at the session folder and the catalog. */
    data object Looking : ContributeState

    /**
     * No session runs: the person chooses. [bots] are the active bots of the catalog, null when it could not be read.
     * [last] is the last session's summary, if any, and [lastUnexpected] whether its process ended without ending it.
     * [problem] is why the last thing asked could not be done, with [details] from the program, and [program] the fetch
     * problem when that was it.
     */
    data class Choosing(
        val choices: ContributeChoices,
        val bots: List<String>?,
        val last: SessionStatus? = null,
        val lastUnexpected: Boolean = false,
        val problem: ContributeProblem? = null,
        val program: ProgramProblem? = null,
        val details: String = "",
    ) : ContributeState

    /** The command line program is needed and must be fetched; the screen asks the person, showing its size. */
    data class AskingForProgram(val choosing: Choosing, val plan: FetchPlan.Needed) : ContributeState

    /** Getting ready to start: looking for the program, or fetching it after the person agreed. */
    data class Preparing(val choosing: Choosing, val fetching: Boolean) : ContributeState

    /**
     * The session program was started as process [pid] and has not taken the session folder yet; [ownedHere] whether it
     * was started with this program as its owner.
     */
    data class Starting(val choosing: Choosing, val pid: Long, val since: Instant, val ownedHere: Boolean = true) : ContributeState

    /**
     * A session runs. [status] is what it said last, null while it starts; [ownedHere] whether this program is its
     * owner; [asked] the last request this screen put to it.
     */
    data class Running(val status: SessionStatus?, val ownedHere: Boolean, val asked: SessionControlRequest? = null) : ContributeState
}

/** What the screen needs from the disk, the Rumble and the computer. Every call may take a while and is never made on the screen's thread. */
interface ContributePort {
    /** This program's own process ID, which a session it starts is owned by. */
    val ownPid: Long

    fun look(): SessionLook

    /** The keys ("name version") of the catalog's active bots. Throws [IOException] or [CatalogUnreadable] when the catalog cannot be read. */
    fun activeBots(): List<String>

    fun planProgram(): FetchPlan

    fun fetchProgram(plan: FetchPlan.Needed): ProgramFetch

    /** Starts the command line program [jar] with [arguments] as a process of its own, without a window, and gives its process ID. */
    fun start(jar: Path, arguments: List<String>): Long

    fun isAlive(pid: Long): Boolean

    /** The last lines the session program wrote as errors, to show when it ended before it took the session folder. */
    fun errors(): String = ""

    fun ask(request: SessionControlRequest): Boolean
}

/** The real port: the environment's session folder below [userData], the catalog read from GitHub, and the program fetched by [program]. */
class LibraryContributePort(
    private val environment: RumbleEnvironment,
    userData: Path,
    private val api: GitHubApi,
    private val program: RumbleProgram,
    private val source: CatalogSource = GitHubCatalogSource(environment),
    /** The Java that runs the session program: the one running this program. */
    private val java: Path = Path.of(System.getProperty("java.home"), "bin", "java"),
) : ContributePort {
    private val folder = SessionFolder.of(environment, userData)
    private val errorsFile = folder.folder.resolve("errors.log")

    override val ownPid: Long = ProcessHandle.current().pid()

    override fun look(): SessionLook = folder.look()

    override fun activeBots(): List<String> =
        Catalog.parse(source.catalogText(RumbleHead.commit(api, environment))).activeBots.map { SessionPlanner.key(it) }.sorted()

    override fun planProgram(): FetchPlan = program.plan()

    override fun fetchProgram(plan: FetchPlan.Needed): ProgramFetch = program.fetch(plan)

    override fun start(jar: Path, arguments: List<String>): Long {
        Files.createDirectories(folder.folder)
        val command = buildList {
            // On Linux a session started from a terminal would end with it; a session of its own runs on.
            SETSID.takeIf { Files.isExecutable(it) && !isWindows() }?.let { add(it.toString()) }
            add(java.toString()); add("-jar"); add(jar.toString())
            addAll(arguments)
        }
        // Java starts the process without a console window; its answer is in the session folder, its errors in a file of their own.
        val process = ProcessBuilder(command)
            .redirectInput(ProcessBuilder.Redirect.from(nullFile()))
            .redirectOutput(ProcessBuilder.Redirect.DISCARD)
            .redirectError(ProcessBuilder.Redirect.to(errorsFile.toFile()))
            .start()
        return process.pid()
    }

    override fun isAlive(pid: Long): Boolean = ProcessHandle.of(pid).map { it.isAlive }.orElse(false)

    override fun errors(): String = try {
        if (Files.exists(errorsFile)) Files.readAllLines(errorsFile).takeLast(20).joinToString("\n") else ""
    } catch (e: IOException) {
        ""
    }

    override fun ask(request: SessionControlRequest): Boolean = folder.ask(request)

    private fun isWindows() = System.getProperty("os.name").orEmpty().lowercase().startsWith("windows")

    private fun nullFile() = File(if (isWindows()) "NUL" else "/dev/null")

    private companion object {
        val SETSID: Path = Path.of("/usr/bin/setsid")
    }
}

/**
 * The behavior of the Contribute battles screen, without any window. It never runs a battle: it starts the Rumble
 * command line's `contribute` as a program of its own and follows and controls it through the environment's session
 * folder, as the decision that a contribution session runs as a program of its own sets out. All work on the disk, on
 * GitHub or with processes runs on [background]; all changes of state, and every call of [onState] and [onDone], run on
 * [post], one at a time and in order, which in the desktop program is its event queue. [refresh] is called every second
 * or so by whatever shows the state; it looks at the session folder unless a look is still on its way.
 */
class ContributeController(
    private val port: ContributePort,
    private val environment: RumbleEnvironment,
    private val background: Executor,
    private val post: Executor,
    private val onState: (ContributeState) -> Unit,
    /** Called once when a session this controller saw running has ended, with its summary. */
    private val onDone: (SessionStatus, Boolean) -> Unit = { _, _ -> },
    private val clock: () -> Instant = Instant::now,
    private val startTimeout: Duration = Duration.ofSeconds(60),
) {
    // Everything below is touched only on [post].
    private var looking = false
    private var choices = ContributeChoices()
    private var bots: List<String>? = null
    private var keepOnClose = false

    @Volatile
    var state: ContributeState = ContributeState.Looking
        private set

    /** When the session folder was last looked at successfully, so a screen can show that it keeps looking. */
    @Volatile
    var lastLook: Instant? = null
        private set

    /** The first look: a running session is shown again, or else the catalog is read and the last summary shown. */
    fun start() = post.execute {
        if (state != ContributeState.Looking || looking) return@execute
        looking = true
        work({ port.look() to (if (bots == null) readBots() else bots) }) { (look, read) ->
            looking = false
            bots = read
            showLook(look, seenRunning = false)
        }
    }

    /** Looks at the session folder again. Called often; does nothing while a look or a start is on its way. */
    fun refresh() = post.execute {
        if (looking) return@execute
        when (val now = state) {
            is ContributeState.Running, is ContributeState.Starting, is ContributeState.Choosing -> Unit
            else -> return@execute
        }
        looking = true
        work({ port.look() }) { look ->
            looking = false
            lastLook = clock()
            when (val now = state) {
                is ContributeState.Running -> showLook(look, seenRunning = true)
                is ContributeState.Starting -> starting(now, look)
                // A session started elsewhere, such as from the command line, is shown too.
                is ContributeState.Choosing -> if (look is SessionLook.Running) showLook(look, seenRunning = false)
                else -> Unit
            }
        }
    }

    fun setMode(mode: ContributeMode) = change { it.copy(mode = mode) }

    fun setBot(bot: String, chosen: Boolean) = change { it.copy(bots = if (chosen) it.bots + bot else it.bots - bot) }

    fun setFormat(format: String, chosen: Boolean) = change { it.copy(formats = if (chosen) it.formats + format else it.formats - format) }

    /** [battles] null keeps the session running until it is stopped. */
    fun setBattles(battles: Int?) = change { it.copy(battles = battles) }

    /** Reads the catalog again after it could not be read. */
    fun readAgain() = post.execute {
        val choosing = state as? ContributeState.Choosing ?: return@execute
        looking = true
        show(ContributeState.Looking)
        work({ readBots() }) { read ->
            looking = false
            bots = read
            show(choosing.copy(bots = read, problem = if (read == null) ContributeProblem.CATALOG_UNREADABLE else null, program = null, details = ""))
        }
    }

    /**
     * Starts a session with the choices, owned by this program unless [keepOnClose] says the person lets sessions run on.
     * The program is looked for first; when it must be fetched, the screen asks the person before anything is fetched.
     */
    fun startSession(keepOnClose: Boolean) = post.execute {
        val choosing = (state as? ContributeState.Choosing)?.takeIf { it.choices.startable && it.bots != null } ?: return@execute
        val clean = choosing.copy(problem = null, program = null, details = "")
        show(ContributeState.Preparing(clean, fetching = false))
        work({ port.planProgram() }) { plan ->
            when (plan) {
                is FetchPlan.NotNeeded -> launch(clean, plan.status.jar, keepOnClose)
                is FetchPlan.Needed -> {
                    this.keepOnClose = keepOnClose
                    show(ContributeState.AskingForProgram(clean, plan))
                }
                is FetchPlan.Unavailable -> show(clean.copy(problem = ContributeProblem.PROGRAM_UNAVAILABLE, program = plan.reason.problem, details = plan.reason.message))
            }
        }
    }

    /** The person agreed to fetch the program; it is fetched, checked, and the session started. */
    fun fetchProgram() = post.execute {
        val asking = state as? ContributeState.AskingForProgram ?: return@execute
        show(ContributeState.Preparing(asking.choosing, fetching = true))
        work({ port.fetchProgram(asking.plan) }) { fetched ->
            when (fetched) {
                is ProgramFetch.Fetched -> launch(asking.choosing, fetched.jar, keepOnClose)
                is ProgramFetch.Failed -> show(asking.choosing.copy(problem = ContributeProblem.PROGRAM_UNAVAILABLE, program = fetched.reason.problem, details = fetched.reason.message))
            }
        }
    }

    /** The person did not want the program fetched; nothing is fetched or started. */
    fun declineProgram() = post.execute {
        (state as? ContributeState.AskingForProgram)?.let { show(it.choosing) }
    }

    fun pause() = control(SessionControlRequest.PAUSE)

    fun resume() = control(SessionControlRequest.RESUME)

    fun stop() = control(SessionControlRequest.STOP)

    fun stopNow() = control(SessionControlRequest.STOP_NOW)

    /** Lets the running session go on when this program ends. */
    fun release() = control(SessionControlRequest.RELEASE)

    /** What closing this program does about the session, given the person's [setting]. Reads only the state. */
    fun closeAction(setting: CloseChoice): CloseAction {
        // A session that is still starting is owned the same way, so closing then is asked about too.
        val owned = when (val now = state) {
            // The session clears its owner once it took the release, so a release that never arrived is asked again.
            is ContributeState.Running -> now.ownedHere
            is ContributeState.Starting -> now.ownedHere
            else -> false
        }
        if (!owned) return CloseAction.NOTHING
        return when (setting) {
            CloseChoice.ASK -> CloseAction.ASK
            CloseChoice.KEEP -> CloseAction.RELEASE
            CloseChoice.STOP -> CloseAction.LET_STOP
        }
    }

    private fun control(request: SessionControlRequest) = post.execute {
        val running = state as? ContributeState.Running ?: return@execute
        show(running.copy(asked = request))
        work({ port.ask(request) }) { asked -> if (!asked) refresh() }
    }

    private fun launch(choosing: ContributeState.Choosing, jar: Path, keepOnClose: Boolean) {
        val arguments = choosing.choices.arguments(environment, if (keepOnClose) null else port.ownPid)
        show(ContributeState.Preparing(choosing, fetching = false))
        work({ port.start(jar, arguments) }) { pid -> show(ContributeState.Starting(choosing, pid, clock(), ownedHere = !keepOnClose)) }
    }

    private fun starting(now: ContributeState.Starting, look: SessionLook) {
        when {
            look is SessionLook.Running && look.status?.pid == now.pid -> showLook(look, seenRunning = false)
            // It ended before its first battle, such as without a sign-in: its summary says why.
            look is SessionLook.Ended && look.status.pid == now.pid -> showLook(look, seenRunning = true)
            port.isAlive(now.pid) && Duration.between(now.since, clock()) < startTimeout -> Unit
            else -> {
                val errors = port.errors()
                show(now.choosing.copy(problem = ContributeProblem.START_FAILED, details = errors))
            }
        }
    }

    private fun showLook(look: SessionLook, seenRunning: Boolean) {
        when (look) {
            is SessionLook.Running -> {
                val before = state as? ContributeState.Running
                val asked = before?.asked?.takeIf { look.status?.pid == before.status?.pid || before.status == null }
                show(ContributeState.Running(look.status, look.status?.owner?.pid == port.ownPid, asked))
            }
            is SessionLook.Ended -> {
                show(choosingWith(look.status, unexpected = false))
                if (seenRunning) onDone(look.status, false)
            }
            is SessionLook.EndedUnexpectedly -> {
                show(choosingWith(look.status, unexpected = true))
                if (seenRunning) onDone(look.status, true)
            }
            SessionLook.None -> show(choosingWith(null, unexpected = false))
        }
    }

    private fun choosingWith(last: SessionStatus?, unexpected: Boolean): ContributeState.Choosing {
        // The person's choices stay; the bots that are no longer active are dropped from them.
        val known = bots
        if (known != null) choices = choices.copy(bots = choices.bots intersect known.toSet())
        return ContributeState.Choosing(
            choices, known, last, unexpected,
            problem = if (known == null) ContributeProblem.CATALOG_UNREADABLE else null,
        )
    }

    private fun change(change: (ContributeChoices) -> ContributeChoices) = post.execute {
        val choosing = state as? ContributeState.Choosing ?: return@execute
        choices = change(choosing.choices)
        show(choosing.copy(choices = choices))
    }

    /** The catalog's active bots, or null when it cannot be read for any reason, which the screen shows. */
    private fun readBots(): List<String>? = try {
        port.activeBots()
    } catch (e: InterruptedException) {
        Thread.currentThread().interrupt()
        null
    } catch (e: Exception) {
        null
    }

    /**
     * Runs [call] on [background] and its result on [post]. Whatever a call throws ends as a problem shown on the
     * screen, so the screen never waits for an answer that will not come.
     */
    private fun <T> work(call: () -> T, then: (T) -> Unit) {
        background.execute {
            val result = attempt(call)
            post.execute {
                result.fold(then) { failure ->
                    val wasLooking = looking
                    looking = false
                    val details = failure.message.orEmpty()
                    when (val now = state) {
                        // A look that failed is tried again at the next refresh, unless it was the first.
                        ContributeState.Looking -> if (wasLooking) show(ContributeState.Choosing(choices, bots, problem = ContributeProblem.SESSION_FOLDER_UNUSABLE, details = details))
                        is ContributeState.Preparing -> show(now.choosing.copy(problem = if (now.fetching) ContributeProblem.PROGRAM_UNAVAILABLE else ContributeProblem.START_FAILED, details = details))
                        is ContributeState.Starting -> show(now.choosing.copy(problem = ContributeProblem.START_FAILED, details = details))
                        // A request that could not be asked is no longer shown as asked.
                        is ContributeState.Running -> show(now.copy(asked = null))
                        else -> Unit
                    }
                }
            }
        }
    }

    private fun show(next: ContributeState) {
        // A look that found nothing new changes nothing on the screen.
        if (next == state) return
        state = next
        onState(next)
    }
}
