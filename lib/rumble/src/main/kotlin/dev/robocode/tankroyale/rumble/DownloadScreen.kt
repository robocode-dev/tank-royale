package dev.robocode.tankroyale.rumble

import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.Executor

/** Why the screen could not go on. The texts are in the resources. */
enum class DownloadFailure { CATALOG_UNREADABLE, CATALOG_UNSUPPORTED, FOLDER_NOT_WRITABLE, NETWORK, UNSAFE_ARCHIVE, OTHER }

/** What reading the catalog and the folder gave. */
sealed interface DownloadRead {
    /**
     * [checks] says for each platform of the rows whether this computer can run it. [versionsKnown] is false when the
     * Rumble's list of supported versions could not be read, so a platform passed only because its tool is there.
     */
    data class Listed(
        val catalog: Catalog,
        val rows: List<DownloadRow>,
        val checks: Map<BotPlatform, PlatformCheck> = emptyMap(),
        val versionsKnown: Boolean = true,
    ) : DownloadRead
    data class Failed(val failure: DownloadFailure, val details: String = "") : DownloadRead
}

/** How a press of Download ended. */
sealed interface DownloadEnded {
    data class Done(val results: List<DownloadResult>) : DownloadEnded
    data class Failed(val failure: DownloadFailure, val details: String = "") : DownloadEnded
}

/** What the screen shows. */
sealed interface DownloadState {
    val folder: Path

    data class Reading(override val folder: Path) : DownloadState

    data class Choosing(
        override val folder: Path,
        val rows: List<DownloadRow>,
        /** The platforms that are shown; all of them to begin with. */
        val platforms: Set<String>,
        /** True for all active bots, false for only the ones in [chosen]. */
        val all: Boolean = true,
        val chosen: Set<String> = emptySet(),
        /** The changed earlier downloads the person asked to replace. */
        val replaceChanged: Set<String> = emptySet(),
        /** Whether this computer can run each platform. A platform with no check, unknown to the rules, is treated as runnable. */
        val checks: Map<BotPlatform, PlatformCheck> = emptyMap(),
        val versionsKnown: Boolean = true,
        /** How the last setup of Python ended, shown once on the list that follows it. */
        val setup: PythonSetupResult? = null,
    ) : DownloadState {
        /** The check of a platform as the catalog names it, or null when there is none. */
        fun checkOf(platform: String): PlatformCheck? = BotPlatform.of(platform)?.let { checks[it] }

        val allPlatforms: List<String> get() = rows.mapNotNull { it.platform }.distinct().sorted()

        /** The rows the platform choice shows. A bot with no known platform is always shown. */
        val shown: List<DownloadRow> get() = rows.filter { it.platform == null || it.platform in platforms }

        /** Exactly the bots a press of Download fetches. Never an inactive bot, a bot that is up to date or a folder the screen did not place. */
        val toDownload: List<CatalogBot>
            get() = shown.filter { row ->
                when (row.state) {
                    DownloadedState.NEW, DownloadedState.UPDATE -> all || row.name in chosen
                    DownloadedState.CHANGED_HERE -> row.name in replaceChanged
                    DownloadedState.UP_TO_DATE, DownloadedState.KEPT_APART, DownloadedState.NO_LONGER_ACTIVE -> false
                }
            }.mapNotNull { it.bot }
    }

    data class Downloading(override val folder: Path, val count: Int) : DownloadState

    data class SettingUpPython(override val folder: Path) : DownloadState

    data class Ended(override val folder: Path, val results: List<DownloadResult>) : DownloadState {
        val placed: Int get() = results.count { it is DownloadResult.Placed }
    }

    data class Failed(override val folder: Path, val failure: DownloadFailure, val details: String = "") : DownloadState
}

/** What the screen needs from the disk and from the Rumble. Every call may take a long time and is never made on the screen's thread. */
interface DownloadPort {
    fun read(folder: Path): DownloadRead
    fun download(catalog: Catalog, bots: List<CatalogBot>, replaceChanged: Set<String>, folder: Path): DownloadEnded

    /** The commands that setting up Python for [folder] runs, so the screen can show them first. */
    fun pythonSetupCommands(folder: Path): List<List<String>> = emptyList()

    fun setUpPython(folder: Path): PythonSetupResult = PythonSetupResult.Failed(PythonSetupStep.MAKE_ENVIRONMENT, "")
}

/** The real port. The catalog and the sources are public, so no sign-in is asked for or sent. */
class LibraryDownloadPort(
    private val environment: RumbleEnvironment,
    private val api: GitHubApi,
    private val source: CatalogSource = GitHubCatalogSource(environment),
    private val runtimeList: RuntimeListSource = GitHubRuntimeListSource(api),
    private val runner: ToolRunner = ProcessToolRunner(),
    /** The file name pip needs for the program's own Python Bot API, and how that file is written to a given place. */
    private val pythonBotApiName: String? = null,
    private val writePythonBotApi: (Path) -> Unit = {},
) : DownloadPort {

    /**
     * The program's own Python Bot API is written to a folder of its own that only this person can use, made once for
     * this screen, so nobody else on the computer can put a file there for pip to install.
     */
    private val pythonBotApi: Path? by lazy {
        pythonBotApiName?.let { name ->
            Files.createTempDirectory("robocode-rumble-python-").also { it.toFile().deleteOnExit() }.resolve(name)
        }
    }

    /** The Rumble's list of supported versions, read once for this screen, so a fresh look costs no extra request to GitHub. */
    @Volatile
    private var versions: SupportedVersions? = null

    override fun read(folder: Path): DownloadRead {
        val catalog = try {
            Catalog.parse(source.catalogText(RumbleHead.commit(api, environment)))
        } catch (e: CatalogUnreadable) {
            return DownloadRead.Failed(if (e.unsupported) DownloadFailure.CATALOG_UNSUPPORTED else DownloadFailure.CATALOG_UNREADABLE, e.message.orEmpty())
        } catch (e: IOException) {
            return DownloadRead.Failed(DownloadFailure.CATALOG_UNREADABLE, e.message.orEmpty())
        }
        val rows = try {
            BotDownloads.rows(catalog, folder)
        } catch (e: IOException) {
            return DownloadRead.Failed(DownloadFailure.FOLDER_NOT_WRITABLE, e.message.orEmpty())
        } catch (e: java.io.UncheckedIOException) {
            return DownloadRead.Failed(DownloadFailure.FOLDER_NOT_WRITABLE, e.message.orEmpty())
        }
        val versions = versions ?: try {
            SupportedVersions.parse(runtimeList.text()).also { versions = it }
        } catch (e: IOException) {
            null
        }
        val platforms = rows.mapNotNull { row -> row.platform?.let { BotPlatform.of(it) } }
        return try {
            DownloadRead.Listed(catalog, rows, PlatformChecker(runner).check(platforms, versions, folder), versions != null)
        } catch (e: IOException) {
            DownloadRead.Failed(DownloadFailure.FOLDER_NOT_WRITABLE, e.message.orEmpty())
        } catch (e: java.io.UncheckedIOException) {
            DownloadRead.Failed(DownloadFailure.FOLDER_NOT_WRITABLE, e.message.orEmpty())
        }
    }

    override fun download(catalog: Catalog, bots: List<CatalogBot>, replaceChanged: Set<String>, folder: Path): DownloadEnded {
        if (!BotDownloads.writable(folder)) return DownloadEnded.Failed(DownloadFailure.FOLDER_NOT_WRITABLE)
        return try {
            DownloadEnded.Done(BotDownloads.download(source, catalog, bots, replaceChanged, folder))
        } catch (e: UnsafeFetch) {
            DownloadEnded.Failed(DownloadFailure.UNSAFE_ARCHIVE, e.message.orEmpty())
        } catch (e: CatalogUnreadable) {
            DownloadEnded.Failed(DownloadFailure.NETWORK, e.message.orEmpty())
        } catch (e: IOException) {
            DownloadEnded.Failed(DownloadFailure.NETWORK, e.message.orEmpty())
        }
    }

    override fun pythonSetupCommands(folder: Path): List<List<String>> =
        pythonBotApi?.let { PythonSetup(runner).commands(folder, it) }.orEmpty()

    override fun setUpPython(folder: Path): PythonSetupResult {
        val botApi = pythonBotApi ?: return PythonSetupResult.Failed(PythonSetupStep.INSTALL_BOT_API, "")
        try {
            writePythonBotApi(botApi)
            botApi.toFile().deleteOnExit()
        } catch (e: IOException) {
            return PythonSetupResult.Failed(PythonSetupStep.INSTALL_BOT_API, e.message.orEmpty())
        }
        return try {
            PythonSetup(runner).run(folder, botApi)
        } catch (e: IOException) {
            PythonSetupResult.Failed(PythonSetupStep.MAKE_ENVIRONMENT, e.message.orEmpty())
        }
    }
}

/**
 * The behavior of the Download bots screen, without any window. All work on the disk or on GitHub runs on [background].
 * All changes of state, and every call of [onState], run on [post], one at a time and in order, which in the desktop
 * program is the screen's event queue. A result that arrives after the person left is thrown away. Nothing is fetched
 * before [download] is pressed, and nothing that is placed is ever run, built or added to a list of bots to start.
 */
class DownloadController(
    private val port: DownloadPort,
    firstFolder: Path,
    private val background: Executor,
    private val post: Executor,
    private val onState: (DownloadState) -> Unit,
) {
    // Everything below is touched only on [post].
    private var epoch = 0
    private var catalog: Catalog? = null

    @Volatile
    var state: DownloadState = DownloadState.Reading(firstFolder)
        private set

    /** Reads the catalog, the folder and what this computer can run. Used for the first look and for trying again. */
    fun start() = post.execute {
        if (!busy()) read(state.folder)
    }

    /** The person chose another folder. Nothing is created or written there yet. */
    fun chooseFolder(folder: Path) = post.execute {
        if (!busy()) read(folder)
    }

    /**
     * The person asked to set up Python for the bots in the folder. Only from the list, and only once at a time. When it
     * ends, the list is read again, so the checks show the new environment, and the result is shown on it.
     */
    fun setUpPython() = post.execute {
        val choosing = state as? DownloadState.Choosing ?: return@execute
        val mine = ++epoch
        show(DownloadState.SettingUpPython(choosing.folder))
        work(mine, choosing.folder, { port.setUpPython(choosing.folder) }) { ended ->
            // The list is read again with the person's choices kept, and the result shown on that list only.
            read(choosing.folder, ended, choosing)
        }
    }

    /** The commands that setting up Python runs for the folder shown, so the screen can show them first. Reads nothing. */
    fun pythonSetupCommands(): List<List<String>> = port.pythonSetupCommands(state.folder)

    private fun busy() = state is DownloadState.Downloading || state is DownloadState.SettingUpPython

    fun setPlatform(platform: String, shown: Boolean) = changeChoosing {
        it.copy(platforms = if (shown) it.platforms + platform else it.platforms - platform)
    }

    fun setAll(all: Boolean) = changeChoosing { it.copy(all = all) }

    fun setChosen(name: String, chosen: Boolean) = changeChoosing {
        it.copy(chosen = if (chosen) it.chosen + name else it.chosen - name)
    }

    /** The person asks to replace a downloaded bot they have changed. Nothing in the program ticks this. */
    fun setReplaceChanged(name: String, replace: Boolean) = changeChoosing {
        it.copy(replaceChanged = if (replace) it.replaceChanged + name else it.replaceChanged - name)
    }

    /** Fetches the bots the person chose. Does nothing unless the screen offers it, so a second press starts nothing new. */
    fun download() = post.execute {
        val choosing = state as? DownloadState.Choosing ?: return@execute
        val known = catalog ?: return@execute
        val bots = choosing.toDownload
        if (bots.isEmpty()) return@execute
        val mine = ++epoch
        show(DownloadState.Downloading(choosing.folder, bots.size))
        work(mine, choosing.folder, { port.download(known, bots, choosing.replaceChanged, choosing.folder) }) { ended ->
            show(
                when (ended) {
                    is DownloadEnded.Done -> DownloadState.Ended(choosing.folder, ended.results)
                    is DownloadEnded.Failed -> DownloadState.Failed(choosing.folder, ended.failure, ended.details)
                },
            )
        }
    }

    /** The person closes the screen: results still on their way are thrown away. */
    fun close() = post.execute { epoch++ }

    /** [setup] is the result to show on the list read now, and [kept] the choices to keep from the list before. */
    private fun read(folder: Path, setup: PythonSetupResult? = null, kept: DownloadState.Choosing? = null) {
        val mine = ++epoch
        show(DownloadState.Reading(folder))
        work(mine, folder, { port.read(folder) }) { read ->
            show(
                when (read) {
                    is DownloadRead.Listed -> {
                        catalog = read.catalog
                        // Only the platforms this computer can run are ticked; the person may tick the others.
                        val platforms = read.rows.mapNotNull { it.platform }.toSet()
                        val runnable = platforms.filter { platform -> BotPlatform.of(platform)?.let { read.checks[it]?.ready } ?: true }.toSet()
                        val fresh = DownloadState.Choosing(folder, read.rows, runnable, checks = read.checks, versionsKnown = read.versionsKnown, setup = setup)
                        val names = read.rows.map { it.name }.toSet()
                        // The person's own ticks stay; only a platform that could not run before and can now is ticked anew.
                        val before = platforms.filter { platform -> kept?.checkOf(platform)?.ready ?: true }.toSet()
                        if (kept == null || kept.folder != folder) fresh else fresh.copy(
                            platforms = (kept.platforms intersect platforms) + (runnable - before),
                            all = kept.all,
                            chosen = kept.chosen intersect names,
                            replaceChanged = kept.replaceChanged intersect names,
                        )
                    }
                    is DownloadRead.Failed -> DownloadState.Failed(folder, read.failure, read.details)
                },
            )
        }
    }

    private fun changeChoosing(change: (DownloadState.Choosing) -> DownloadState.Choosing) = post.execute {
        (state as? DownloadState.Choosing)?.let { show(change(it)) }
    }

    /**
     * Runs [call] on [background] and its result on [post], unless the person has left since. Whatever a call throws,
     * even an error such as running out of memory, ends as a plain failure with its message as details, so the screen
     * never stays waiting.
     */
    private fun <T : Any> work(mine: Int, folder: Path, call: () -> T, then: (T) -> Unit) {
        background.execute {
            var failure: Throwable? = null
            val result = try {
                call()
            } catch (e: Throwable) {
                failure = e
                null
            }
            post.execute {
                if (mine != epoch) return@execute
                if (result != null) then(result) else show(DownloadState.Failed(folder, DownloadFailure.OTHER, failure?.message.orEmpty()))
            }
        }
    }

    private fun show(next: DownloadState) {
        state = next
        onState(next)
    }
}
