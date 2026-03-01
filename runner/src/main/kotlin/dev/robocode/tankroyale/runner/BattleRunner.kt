package dev.robocode.tankroyale.runner

import dev.robocode.tankroyale.client.model.BotAddress
import dev.robocode.tankroyale.client.model.GameSetup
import dev.robocode.tankroyale.common.recording.GameRecorder
import dev.robocode.tankroyale.intent.IntentDiagnosticsProxy
import dev.robocode.tankroyale.intent.IntentStore
import dev.robocode.tankroyale.runner.internal.BooterManager
import dev.robocode.tankroyale.runner.internal.ServerConnection
import dev.robocode.tankroyale.runner.internal.ServerManager
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.function.Consumer
import java.util.logging.Logger

/**
 * Entry point for running Tank Royale battles programmatically.
 *
 * Create an instance using the [create] factory function, then call [runBattle] to execute a
 * battle. The runner is [AutoCloseable] and should be used with Kotlin `use {}` or Java
 * try-with-resources to release the embedded server and bot processes.
 *
 * **Kotlin:**
 * ```kotlin
 * BattleRunner.create { embeddedServer() }.use { runner ->
 *     val results = runner.runBattle(
 *         setup = BattleSetup.classic { numberOfRounds = 5 },
 *         bots  = listOf(BotEntry.of("/path/to/MyBot"), BotEntry.of("/path/to/EnemyBot"))
 *     )
 *     println("Winner: ${results.results.first().name}")
 * }
 * ```
 *
 * **Java:**
 * ```java
 * try (var runner = BattleRunner.create(b -> b.embeddedServer())) {
 *     var results = runner.runBattle(
 *         BattleSetup.classic(s -> s.setNumberOfRounds(5)),
 *         List.of(BotEntry.of("/path/to/MyBot"), BotEntry.of("/path/to/EnemyBot"))
 *     );
 *     System.out.println("Winner: " + results.getResults().get(0).getName());
 * }
 * ```
 */
class BattleRunner private constructor(val config: Config) : AutoCloseable {

    internal val serverManager = ServerManager(config.serverMode)
    internal var connection: ServerConnection? = null
    internal var booterManager: BooterManager? = null
    internal var intentProxy: IntentDiagnosticsProxy? = null
    private var gameRecorder: GameRecorder? = null

    private val battleInProgress = AtomicBoolean(false)
    private val closed = AtomicBoolean(false)
    private val shutdownHook = Thread(::close, "BattleRunner-ShutdownHook")

    init {
        Runtime.getRuntime().addShutdownHook(shutdownHook)
    }

    /**
     * Returns the intent diagnostics store for querying captured bot intents.
     * Only available when intent diagnostics are enabled via [Builder.enableIntentDiagnostics].
     *
     * @return the intent store, or `null` if diagnostics are disabled
     */
    val intentDiagnostics: IntentStore?
        get() = intentProxy?.store

    // -------------------------------------------------------------------------------------
    // 8.1 — Synchronous battle
    // -------------------------------------------------------------------------------------

    /**
     * Starts a battle, blocks until all rounds complete, and returns structured results.
     *
     * @param setup battle configuration (game type, arena size, number of rounds, etc.)
     * @param bots bots that participate in this battle
     * @return per-bot scores and rankings
     * @throws BattleException if the battle fails to start or cannot complete
     */
    fun runBattle(setup: BattleSetup, bots: List<BotEntry>): BattleResults {
        val handle = startBattleAsync(setup, bots)
        return handle.use { handle ->
            handle.awaitResults()
        }
    }

    // -------------------------------------------------------------------------------------
    // 8.2 — Async battle
    // -------------------------------------------------------------------------------------

    /**
     * Starts a battle asynchronously and returns a [BattleHandle] for event observation
     * and battle control.
     *
     * The caller is responsible for monitoring [BattleHandle.onGameEnded] or calling
     * [BattleHandle.awaitResults] to detect completion. The handle must be [closed][BattleHandle.close]
     * when done to release bot processes and allow subsequent battles.
     *
     * @param setup battle configuration (game type, arena size, number of rounds, etc.)
     * @param bots bots that participate in this battle
     * @return a handle for observing events and controlling the battle
     * @throws BattleException if the battle fails to start
     */
    fun startBattleAsync(setup: BattleSetup, bots: List<BotEntry>): BattleHandle {
        check(battleInProgress.compareAndSet(false, true)) {
            "A battle is already in progress"
        }
        check(!closed.get()) { "BattleRunner has been closed" }

        var handle: BattleHandle? = null

        try {
            // Validate inputs (8.5)
            require(bots.size >= setup.minNumberOfParticipants) {
                "Need at least ${setup.minNumberOfParticipants} bots, but only ${bots.size} provided"
            }
            setup.maxNumberOfParticipants?.let { max ->
                require(bots.size <= max) {
                    "At most $max bots allowed, but ${bots.size} provided"
                }
            }
            bots.forEach { BooterManager.validateBotDir(it.path) }

            // 1. Ensure server is running
            serverManager.ensureStarted()

            // Start intent diagnostics proxy if enabled
            if (config.intentDiagnosticsEnabled && intentProxy == null) {
                val proxy = IntentDiagnosticsProxy(serverManager.serverUrl)
                proxy.start()
                intentProxy = proxy
            }

            // Ensure Observer + Controller WebSocket connections (8.3: reuse across battles)
            ensureConnected()
            val conn = connection!!

            // Capture pre-existing bots (for external server mode)
            val preExistingBots = conn.latestBotList.get().map { it.botAddress }.toSet()

            // Create handle BEFORE starting game to capture GameEndedEvent
            handle = BattleHandle(conn) {
                booterManager?.close()
                booterManager = null
                battleInProgress.set(false)
            }

            // 2. Boot bots
            val botUrl = if (config.intentDiagnosticsEnabled) intentProxy!!.proxyUrl else serverManager.serverUrl
            booterManager = BooterManager(botUrl, serverManager.botSecret)
            booterManager!!.boot(bots.map { it.path })

            // Wait for bots to connect (detected via BotListUpdate)
            val ourBotAddresses = waitForBots(conn, preExistingBots, bots.size)

            // 3. Start game
            conn.startBattle(toClientGameSetup(setup), ourBotAddresses)

            // 4. Wait for game started or aborted (8.5)
            waitForGameStarted(conn)

            return handle

        } catch (e: Exception) {
            handle?.close() ?: run {
                booterManager?.close()
                booterManager = null
                battleInProgress.set(false)
            }
            if (e is BattleException) throw e
            throw BattleException("Failed to start battle: ${e.message}", e)
        }
    }

    // -------------------------------------------------------------------------------------
    // 8.4 — Graceful shutdown
    // -------------------------------------------------------------------------------------

    /** Terminates all managed resources (bot processes, intent proxy, WebSocket connections, embedded server). */
    override fun close() {
        if (!closed.compareAndSet(false, true)) return

        // Stop battle if in progress
        connection?.let { if (it.isConnected) it.stopBattle() }

        booterManager?.close()
        booterManager = null
        gameRecorder?.close()
        gameRecorder = null
        intentProxy?.close()
        intentProxy = null
        connection?.close()
        connection = null
        serverManager.close()

        try {
            Runtime.getRuntime().removeShutdownHook(shutdownHook)
        } catch (_: IllegalStateException) {
            // JVM already shutting down — shutdown hook is executing
        }
    }

    // -------------------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------------------

    /** Creates or reuses a ServerConnection. (8.3: reuse across battles) */
    private fun ensureConnected() {
        connection?.let { if (it.isConnected) return }
        connection?.close()
        val conn = ServerConnection(serverManager.serverUrl, serverManager.controllerSecret)
        conn.connect()

        // Subscribe to raw observer messages for recording (9.3)
        if (config.recordingPath != null) {
            conn.onRawObserverMessage.on(this) { message -> handleRecordingMessage(message) }
        }

        connection = conn
    }

    // -------------------------------------------------------------------------------------
    // Recording (9.3)
    // -------------------------------------------------------------------------------------

    /** Handles raw observer messages for battle recording. */
    private fun handleRecordingMessage(message: String) {
        val type = extractMessageType(message) ?: return

        if (type in GameRecorder.START_RECORDING_TYPES) {
            gameRecorder?.close()
            gameRecorder = GameRecorder(config.recordingPath?.toString())
        }

        if (type in GameRecorder.RECORDABLE_EVENT_TYPES) {
            gameRecorder?.record(message)
        }

        if (type in GameRecorder.END_RECORDING_TYPES) {
            gameRecorder?.close()
            gameRecorder = null
        }
    }

    private fun extractMessageType(message: String): String? {
        return try {
            Json.parseToJsonElement(message).jsonObject["type"]?.jsonPrimitive?.content
        } catch (_: Exception) {
            null
        }
    }

    /** Waits for the expected number of new bots to appear in BotListUpdate. */
    private fun waitForBots(conn: ServerConnection, preExistingBots: Set<BotAddress>, expectedCount: Int): Set<BotAddress> {
        val botsReadyLatch = CountDownLatch(1)
        val latestBots = ConcurrentHashMap.newKeySet<BotAddress>()
        val botOwner = Any()

        conn.onBotListUpdate.on(botOwner) { update ->
            latestBots.clear()
            latestBots.addAll(update.bots.map { it.botAddress })
            if (update.bots.size - preExistingBots.size >= expectedCount) {
                botsReadyLatch.countDown()
            }
        }

        try {
            // Check if bots already appeared
            val currentBots = conn.latestBotList.get()
            if (currentBots.size - preExistingBots.size >= expectedCount) {
                latestBots.addAll(currentBots.map { it.botAddress })
            } else if (!botsReadyLatch.await(BOT_CONNECT_TIMEOUT_MS, TimeUnit.MILLISECONDS)) {
                val connected = conn.latestBotList.get().size - preExistingBots.size
                throw BattleException(
                    "Only $connected of $expectedCount bots connected within ${BOT_CONNECT_TIMEOUT_MS}ms"
                )
            }

            return latestBots.toSet() - preExistingBots
        } finally {
            conn.onBotListUpdate.off(botOwner)
        }
    }

    /** Waits for GameStartedEvent or detects GameAbortedEvent. */
    private fun waitForGameStarted(conn: ServerConnection) {
        val gameStartedLatch = CountDownLatch(1)
        val gameAbortedDuringStart = AtomicBoolean(false)
        val startOwner = Any()

        conn.onGameStarted.on(startOwner) { _ -> gameStartedLatch.countDown() }
        conn.onGameAborted.on(startOwner) { _ ->
            gameAbortedDuringStart.set(true)
            gameStartedLatch.countDown()
        }

        try {
            if (!gameStartedLatch.await(GAME_START_TIMEOUT_MS, TimeUnit.MILLISECONDS)) {
                throw BattleException("Game did not start within ${GAME_START_TIMEOUT_MS}ms")
            }
            if (gameAbortedDuringStart.get()) {
                throw BattleException("Battle was aborted — not enough bots ready to start")
            }
        } finally {
            conn.onGameStarted.off(startOwner)
            conn.onGameAborted.off(startOwner)
        }
    }

    // -----------------------------------------------------------------------------------------
    // Configuration
    // -----------------------------------------------------------------------------------------

    /** Immutable configuration produced by [Builder]. */
    data class Config(
        val serverMode: ServerMode,
        val intentDiagnosticsEnabled: Boolean,
        val recordingPath: Path?,
    )

    /** Describes how the server is acquired for this runner instance. */
    sealed class ServerMode {
        /**
         * The runner starts and manages its own embedded server.
         * @property port TCP port to bind (0 = let the OS assign a free port)
         */
        data class Embedded(val port: Int = 0) : ServerMode()

        /**
         * The runner connects to a pre-started external server.
         * @property url WebSocket URL of the server (e.g. `ws://localhost:7654`)
         */
        data class External(val url: String) : ServerMode()
    }

    // -----------------------------------------------------------------------------------------
    // Builder / DSL
    // -----------------------------------------------------------------------------------------

    /**
     * DSL builder for configuring a [BattleRunner].
     *
     * Defaults to embedded-server mode on a dynamically assigned port.
     * Builder methods return `this` for fluent chaining from Java.
     */
    class Builder {
        private var serverMode: ServerMode = ServerMode.Embedded()
        private var intentDiagnosticsEnabled: Boolean = false
        private var recordingPath: Path? = null

        /**
         * Use an embedded server, binding it to [port] (default 0 = dynamic port assignment).
         * This is the default mode when no server configuration is supplied.
         */
        @JvmOverloads
        fun embeddedServer(port: Int = 0): Builder = apply {
            serverMode = ServerMode.Embedded(port)
        }

        /**
         * Connect to a pre-started server at the given WebSocket [url]
         * (e.g. `ws://localhost:7654`).
         */
        fun externalServer(url: String): Builder = apply {
            serverMode = ServerMode.External(url)
        }

        /**
         * Enable intent diagnostics via a transparent WebSocket proxy (Decision 8).
         * When enabled, bot intents are captured per-bot per-turn in memory.
         * Disabled by default to avoid the extra network hop.
         */
        fun enableIntentDiagnostics(): Builder = apply {
            intentDiagnosticsEnabled = true
        }

        /**
         * Enable battle recording, writing a `.battle.gz` file to [outputPath].
         * Disabled by default.
         */
        fun enableRecording(outputPath: Path): Builder = apply {
            recordingPath = outputPath
        }

        internal fun build(): BattleRunner = BattleRunner(
            Config(
                serverMode = serverMode,
                intentDiagnosticsEnabled = intentDiagnosticsEnabled,
                recordingPath = recordingPath,
            )
        )
    }

    companion object {
        private const val BOT_CONNECT_TIMEOUT_MS = 30_000L
        private const val GAME_START_TIMEOUT_MS = 10_000L

        /** Converts a public [BattleSetup] to the client model [GameSetup] for the server protocol. */
        internal fun toClientGameSetup(setup: BattleSetup): GameSetup = GameSetup(
            gameType = setup.gameType.displayName,
            arenaWidth = setup.arenaWidth,
            isArenaWidthLocked = false,
            arenaHeight = setup.arenaHeight,
            isArenaHeightLocked = false,
            minNumberOfParticipants = setup.minNumberOfParticipants,
            isMinNumberOfParticipantsLocked = false,
            maxNumberOfParticipants = setup.maxNumberOfParticipants,
            isMaxNumberOfParticipantsLocked = false,
            numberOfRounds = setup.numberOfRounds,
            isNumberOfRoundsLocked = false,
            gunCoolingRate = setup.gunCoolingRate,
            isGunCoolingRateLocked = false,
            maxInactivityTurns = setup.maxInactivityTurns,
            isMaxInactivityTurnsLocked = false,
            turnTimeout = setup.turnTimeoutMicros,
            isTurnTimeoutLocked = false,
            readyTimeout = setup.readyTimeoutMicros,
            isReadyTimeoutLocked = false,
            defaultTurnsPerSecond = -1, // max-speed (Decision 7)
        )

        /**
         * Creates a new [BattleRunner] with the supplied Kotlin DSL configuration.
         *
         * @param block DSL block for configuring the runner
         */
        @JvmSynthetic
        fun create(block: Builder.() -> Unit): BattleRunner =
            Builder().apply(block).build()

        /**
         * Creates a new [BattleRunner] with default configuration
         * (embedded server on a dynamically assigned port).
         */
        @JvmStatic
        fun create(): BattleRunner = Builder().build()

        /**
         * Creates a new [BattleRunner] with the supplied Java consumer configuration.
         * Defaults to embedded-server mode on a dynamically assigned port.
         *
         * **Java:**
         * ```java
         * var runner = BattleRunner.create(b -> b.embeddedServer().enableIntentDiagnostics());
         * ```
         *
         * @param configurer consumer that configures the builder
         */
        @JvmStatic
        fun create(configurer: Consumer<Builder>): BattleRunner =
            Builder().also { configurer.accept(it) }.build()
    }
}
