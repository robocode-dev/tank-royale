package dev.robocode.tankroyale.gui.recorder

import dev.robocode.tankroyale.gui.settings.ServerSettings
import dev.robocode.tankroyale.gui.util.FileUtil
import dev.robocode.tankroyale.gui.util.LineReaderThread
import dev.robocode.tankroyale.gui.util.ProcessUtil
import dev.robocode.tankroyale.gui.util.ResourceUtil
import java.io.FileNotFoundException
import java.io.PrintStream
import java.nio.file.Files
import java.nio.file.Paths
import java.util.concurrent.atomic.AtomicReference

/**
 * RecorderProcess is responsible for launching and controlling the Recorder
 * (robocode-tankroyale-recorder.jar).
 */
object RecorderProcess {

    private const val JAR_FILE_NAME = "robocode-tankroyale-recorder"

    private var processRef = AtomicReference<Process?>()
    private var logReaderRef = AtomicReference<LineReaderThread?>()

    fun isRunning(): Boolean {
        val process = processRef.get()
        return process != null && process.isAlive
    }

    /**
     * Starts the recorder process if not already running.
     *
     * @param url Optional server URL. Defaults to ServerSettings.serverUrl().
     * @param secret Optional secret. Defaults to ServerSettings.controllerSecret().
     * @param dir Optional directory where recordings are saved. Defaults to current dir when null.
     */
    @JvmOverloads
    fun start(url: String? = null, secret: String? = null, dir: String? = null) {
        if (isRunning()) return

        val effectiveUrl = url ?: ServerSettings.serverUrl()
        val effectiveSecret = secret ?: ServerSettings.controllerSecret()

        val command = mutableListOf(
            "java",
            "-Dapp.processName=RobocodeTankRoyale-Recorder",
            "-Dpicocli.ansi=true",
            "-jar",
            getRecorderJar(),
            "--url=$effectiveUrl"
        )

        if (effectiveSecret.isNotBlank()) {
            command += "--secret=$effectiveSecret"
        }
        if (!dir.isNullOrBlank()) {
            command += "--dir=$dir"
        }

        ProcessBuilder(command).apply {
            redirectErrorStream(true)
            val process = start()
            processRef.set(process)
        }

        startLogThread()
    }

    /**
     * Sends a start command to the recorder to begin recording.
     */
    fun startRecording() {
        sendCommand("start")
    }

    /**
     * Sends a stop command to the recorder to stop recording and keep the recording file.
     */
    fun stopRecording() {
        sendCommand("stop")
    }

    /**
     * Sends an abort command to the recorder to stop recording and delete the current recording file.
     */
    fun abortRecording() {
        sendCommand("abort")
    }

    /**
     * Stops the recorder process if running.
     */
    fun stop() {
        if (!isRunning()) return

        val process = processRef.get()
        ProcessUtil.stopProcess(process, "quit", true, true)
        processRef.set(null)

        stopLogThread()
    }

    private fun sendCommand(command: String) {
        val process = processRef.get() ?: return
        try {
            // Do NOT close the output stream here, as the recorder listens for further commands
            val ps = PrintStream(process.outputStream, true)
            ps.println(command)
            ps.flush()
        } catch (_: Exception) {
            // Swallow exceptions; commands are best-effort
        }
    }

    private fun getRecorderJar(): String {
        System.getProperty("recorderJar")?.let {
            Paths.get(it).apply {
                if (Files.exists(this)) {
                    throw FileNotFoundException(toString())
                }
                return toString()
            }
        }

        FileUtil.findFirstInCurrentDirectory(JAR_FILE_NAME, ".jar")?.let { return it }

        return try {
            ResourceUtil.getResourceFile("${JAR_FILE_NAME}.jar")?.absolutePath ?: ""
        } catch (ex: Exception) {
            System.err.println(ex.message)
            ""
        }
    }

    private fun startLogThread() {
        val process = processRef.get() ?: return
        val logReader = LineReaderThread(
            "RecorderProcess-Log-Thread",
            process.inputStream
        ) { line ->
            // For now, just print to stdout. A dedicated GUI frame can be added later if desired.
            println(line)
        }
        logReaderRef.set(logReader)
        logReader.start()
    }

    private fun stopLogThread() {
        logReaderRef.get()?.stop()
        logReaderRef.set(null)
    }
}
