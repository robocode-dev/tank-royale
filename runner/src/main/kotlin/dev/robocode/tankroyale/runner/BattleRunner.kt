package dev.robocode.tankroyale.runner

import dev.robocode.tankroyale.intent.IntentDiagnosticsProxy
import dev.robocode.tankroyale.intent.IntentStore
import dev.robocode.tankroyale.runner.internal.BooterManager
import dev.robocode.tankroyale.runner.internal.ServerManager
import java.nio.file.Path
import java.util.function.Consumer

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
    internal var booterManager: BooterManager? = null
    internal var intentProxy: IntentDiagnosticsProxy? = null

    /**
     * Returns the intent diagnostics store for querying captured bot intents.
     * Only available when intent diagnostics are enabled via [Builder.enableIntentDiagnostics].
     *
     * @return the intent store, or `null` if diagnostics are disabled
     */
    val intentDiagnostics: IntentStore?
        get() = intentProxy?.store

    /**
     * Starts a battle, blocks until all rounds complete, and returns structured results.
     *
     * @param setup battle configuration (game type, arena size, number of rounds, etc.)
     * @param bots bots that participate in this battle
     * @return per-bot scores and rankings
     * @throws BattleException if the battle fails to start or cannot complete
     */
    fun runBattle(setup: BattleSetup, bots: List<BotEntry>): BattleResults {
        TODO("Implemented in task 8: Battle Orchestration")
    }

    /** Terminates all managed resources (bot processes, intent proxy, embedded server, WebSocket connections). */
    override fun close() {
        booterManager?.close()
        booterManager = null
        intentProxy?.close()
        intentProxy = null
        serverManager.close()
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
