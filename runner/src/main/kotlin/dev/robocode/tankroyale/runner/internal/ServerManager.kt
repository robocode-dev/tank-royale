package dev.robocode.tankroyale.runner.internal

import dev.robocode.tankroyale.client.WebSocketClient
import dev.robocode.tankroyale.client.WebSocketClientEvents
import dev.robocode.tankroyale.client.model.MessageConstants
import dev.robocode.tankroyale.client.model.ServerHandshake
import dev.robocode.tankroyale.common.util.JavaExec
import dev.robocode.tankroyale.common.util.ResourceUtil
import dev.robocode.tankroyale.runner.BattleException
import dev.robocode.tankroyale.runner.BattleRunner.ServerMode
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import java.io.PrintStream
import java.net.ServerSocket
import java.net.URI
import java.util.Base64
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import java.util.logging.Level
import java.util.logging.Logger
import javax.crypto.KeyGenerator

/**
 * Manages the lifecycle of the Tank Royale server — either an embedded process
 * started from a bundled JAR or a connection to an external pre-started server.
 *
 * This class is internal to the runner module and is not part of the public API.
 */
internal class ServerManager(
    private val serverMode: ServerMode,
    private val captureOutput: Boolean = true,
) : AutoCloseable {

    private val logger = Logger.getLogger(ServerManager::class.java.name)

    private val processRef = AtomicReference<Process?>()
    private var serverJarFile: File? = null
    private var stdoutThread: Thread? = null

    /** The controller secret used for Observer and Controller handshakes. */
    val controllerSecret: String = generateSecret()

    /** The bot secret passed to the server for bot access control. */
    val botSecret: String = generateSecret()

    /** The resolved TCP port the server is listening on. */
    var port: Int = 0
        private set

    /** The WebSocket URL for connecting to the server (e.g. `ws://localhost:7654`). */
    val serverUrl: String
        get() = when (serverMode) {
            is ServerMode.Embedded -> "ws://localhost:$port"
            is ServerMode.External -> serverMode.url
        }

    /** True if the embedded server process is running or an external server is configured. */
    val isRunning: Boolean
        get() = when (serverMode) {
            is ServerMode.Embedded -> processRef.get()?.isAlive == true
            is ServerMode.External -> true
        }

    /**
     * Ensures the server is available — starts an embedded server process or validates
     * reachability of an external server.
     *
     * @throws BattleException if the server cannot be started or reached
     */
    fun ensureStarted() {
        when (serverMode) {
            is ServerMode.Embedded -> startEmbeddedServer(serverMode.port)
            is ServerMode.External -> validateExternalServer(serverMode.url)
        }
    }

    /**
     * Shuts down the embedded server (if running). For external servers, this is a no-op.
     */
    override fun close() {
        if (serverMode is ServerMode.External) return
        stopEmbeddedServer()
    }

    // -------------------------------------------------------------------------------------
    // Embedded server startup (4.1, 4.2, 4.8)
    // -------------------------------------------------------------------------------------

    private fun startEmbeddedServer(requestedPort: Int) {
        if (processRef.get()?.isAlive == true) return // already running (4.6 reuse)

        val resolvedPort = if (requestedPort == 0) allocateFreePort() else requestedPort
        port = resolvedPort

        // Join any leftover stdout thread from a previously crashed process
        stdoutThread?.let {
            try { it.join(500) } catch (_: InterruptedException) { Thread.currentThread().interrupt() }
            stdoutThread = null
        }

        val jarPath = extractServerJar()

        val command = mutableListOf(
            JavaExec.java(),
            "-jar",
            jarPath,
            "--port=$resolvedPort",
            "--controller-secrets=$controllerSecret",
            "--bot-secrets=$botSecret",
            "--tps=-1", // 4.8: max-speed by default
        )

        val process = ProcessBuilder(command)
            .redirectErrorStream(true)
            .start()

        processRef.set(process)

        // Start stdout reader thread to redirect server logs to java.util.logging
        startStdoutReader(process)

        // Wait for the server to be ready (4.3)
        waitForServerReady("ws://localhost:$resolvedPort")
    }

    // -------------------------------------------------------------------------------------
    // Stdout redirection
    // -------------------------------------------------------------------------------------

    private fun startStdoutReader(process: Process) {
        val thread = Thread({
            try {
                process.inputStream.bufferedReader().use { reader ->
                    reader.forEachLine { line ->
                        if (captureOutput) logger.info("[SERVER] $line")
                    }
                }
            } catch (_: Exception) {
                // Stream closed — process exiting
            }
        }, "ServerManager-StdOut-Thread")
        thread.isDaemon = true
        thread.start()
        stdoutThread = thread
    }

    // -------------------------------------------------------------------------------------
    // Dynamic port allocation (4.2)
    // -------------------------------------------------------------------------------------

    private fun allocateFreePort(): Int {
        return ServerSocket(0).use { it.localPort }
    }

    // -------------------------------------------------------------------------------------
    // Server readiness detection (4.3)
    // -------------------------------------------------------------------------------------

    private fun waitForServerReady(url: String) {
        val maxAttempts = 10
        val intervalMs = 500L

        (1..maxAttempts).forEach { _ ->
            if (tryHandshake(url)) return

            // Check if a server process died
            val process = processRef.get()
            if (process != null && !process.isAlive) {
                throw BattleException(
                    "Embedded server process exited unexpectedly with code ${process.exitValue()}"
                )
            }

            Thread.sleep(intervalMs)
        }

        throw BattleException(
            "Server at $url did not become ready after $maxAttempts attempts (${maxAttempts * intervalMs}ms)"
        )
    }

    /**
     * Attempts a WebSocket connection and waits for the `server-handshake` message.
     * Returns true if the handshake was successfully received.
     */
    private fun tryHandshake(url: String): Boolean {
        val handshakeReceived = CountDownLatch(1)
        val handshakeRef = AtomicReference<ServerHandshake?>()
        var wsClient: WebSocketClient? = null

        WebSocketClientEvents.onMessage.on(this) { message ->
            if (isServerHandshake(message)) {
                try {
                    val handshake = MessageConstants.json.decodeFromString<ServerHandshake>(message)
                    handshakeRef.set(handshake)
                } catch (_: Exception) {
                    // Parsing failed; still count as received if type matched
                }
                handshakeReceived.countDown()
            }
        }

        return try {
            wsClient = WebSocketClient(URI(url))
            wsClient.open()
            handshakeReceived.await(HANDSHAKE_TIMEOUT_MS, TimeUnit.MILLISECONDS)
        } catch (_: Exception) {
            false
        } finally {
            WebSocketClientEvents.onMessage.off(this)
            try { wsClient?.close() } catch (_: Exception) {}
        }
    }

    private fun isServerHandshake(message: String): Boolean {
        return try {
            val jsonElement = Json.parseToJsonElement(message)
            jsonElement.jsonObject["type"]?.jsonPrimitive?.content == "ServerHandshake"
        } catch (_: Exception) {
            false
        }
    }

    // -------------------------------------------------------------------------------------
    // External server validation (4.4)
    // -------------------------------------------------------------------------------------

    private fun validateExternalServer(url: String) {
        if (!tryHandshake(url)) {
            throw BattleException(
                "Could not connect to external server at $url — " +
                        "server-handshake was not received within ${HANDSHAKE_TIMEOUT_MS}ms"
            )
        }
    }

    // -------------------------------------------------------------------------------------
    // Server shutdown (4.5)
    // -------------------------------------------------------------------------------------

    private fun stopEmbeddedServer() {
        val process = processRef.getAndSet(null) ?: return

        // Send quit to stdin
        try {
            PrintStream(process.outputStream).use { ps ->
                ps.println("quit")
                ps.flush()
            }
        } catch (e: Exception) {
            logger.log(Level.FINE, "Failed to send quit command to server", e)
        }

        // Wait up to 2s for graceful exit
        try {
            if (!process.waitFor(GRACEFUL_EXIT_TIMEOUT_MS, TimeUnit.MILLISECONDS)) {
                process.destroyForcibly()
                process.waitFor(FORCE_TERMINATE_TIMEOUT_MS, TimeUnit.MILLISECONDS)
            }
        } catch (_: InterruptedException) {
            Thread.currentThread().interrupt()
            process.destroyForcibly()
        }

        // Wait for stdout thread to finish
        try {
            stdoutThread?.join(1000)
        } catch (_: InterruptedException) {
            Thread.currentThread().interrupt()
        }
        stdoutThread = null

        // Clean up temp JAR file
        cleanupServerJar()
    }

    // -------------------------------------------------------------------------------------
    // JAR extraction
    // -------------------------------------------------------------------------------------

    private fun extractServerJar(): String {
        // Reuse already-extracted JAR if still present
        serverJarFile?.let { if (it.exists()) return it.absolutePath }

        val file = ResourceUtil.getResourceFile(SERVER_JAR_RESOURCE)
            ?: throw BattleException(
                "Could not extract embedded server JAR from classpath resource: $SERVER_JAR_RESOURCE"
            )

        // Only track temp files for cleanup; filesystem resources must not be deleted
        if (file.absolutePath.startsWith(System.getProperty("java.io.tmpdir"))) {
            serverJarFile = file
        }
        return file.absolutePath
    }

    private fun cleanupServerJar() {
        serverJarFile?.let { file ->
            try {
                if (file.exists()) file.delete()
            } catch (e: Exception) {
                logger.log(Level.FINE, "Failed to delete temp server JAR: ${file.absolutePath}", e)
            }
        }
        serverJarFile = null
    }

    // -------------------------------------------------------------------------------------
    // Secret generation (4.7)
    // -------------------------------------------------------------------------------------

    companion object {
        private const val SERVER_JAR_RESOURCE = "robocode-tankroyale-server.jar"
        private const val HANDSHAKE_TIMEOUT_MS = 5000L
        private const val GRACEFUL_EXIT_TIMEOUT_MS = 2000L
        private const val FORCE_TERMINATE_TIMEOUT_MS = 3000L

        /** Generates a random Base64-encoded AES secret (same approach as GUI). */
        private fun generateSecret(): String {
            val secretKey = KeyGenerator.getInstance("AES").generateKey()
            val encodedKey = Base64.getEncoder().encodeToString(secretKey.encoded)
            // Remove trailing '==' padding
            return encodedKey.trimEnd('=')
        }
    }
}
