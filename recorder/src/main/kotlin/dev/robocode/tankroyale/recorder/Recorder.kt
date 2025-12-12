package dev.robocode.tankroyale.recorder

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.versionOption
import dev.robocode.tankroyale.common.util.Version
import dev.robocode.tankroyale.recorder.core.RecordingObserver
import dev.robocode.tankroyale.recorder.util.VersionFileProvider
import org.slf4j.LoggerFactory
import java.io.File
import java.util.*
import kotlin.system.exitProcess

fun main(args: Array<String>) = RecorderCli().main(args)

class Recorder : Runnable {

    companion object {
        private const val EXIT_COMMAND = "quit"
        private const val STOP_COMMAND = "stop"
        private const val ABORT_COMMAND = "abort"
        private const val START_COMMAND = "start"

        private const val NOT_INITIALIZED_WARNING = "Recorder is not initialized yet. Please wait and try again."

        const val DEFAULT_URL: String = "ws://localhost:7654"

        // Set by CLI
        @JvmStatic
        var url: String = DEFAULT_URL

        @JvmStatic
        var secret: String? = null

        @JvmStatic
        var dir: String? = null

    }

    private val log = LoggerFactory.getLogger(this::class.java)
    private lateinit var recordingObserver: RecordingObserver

    override fun run() {
        println(VersionFileProvider.getVersion())
        startExitInputMonitorThread()
        val canonicalDir = File(dir, ".").canonicalPath
        log.info("Recordings will be stored in $canonicalDir")
        startRecorder()
    }

    private fun startExitInputMonitorThread() {
        Thread {
            monitorStandardInputForExit()
        }.apply {
            isDaemon = true
            start()
        }
    }

    private fun monitorStandardInputForExit() {
        Scanner(System.`in`).use { scanner ->
            while (scanner.hasNextLine()) {
                val input = scanner.nextLine().trim()
                handleInput(input)
            }
        }
    }

    private fun handleInput(input: String) {
        when {
            input.equals(EXIT_COMMAND, ignoreCase = true) -> handleExit()
            input.equals(START_COMMAND, ignoreCase = true) -> handleStart()
            input.equals(STOP_COMMAND, ignoreCase = true) -> handleStop()
            input.equals(ABORT_COMMAND, ignoreCase = true) -> handleAbort()
        }
    }

    private fun handleExit() {
        if (this::recordingObserver.isInitialized) {
            recordingObserver.stop()
        }
        exitProcess(1)
    }

    private fun startRecorder() {
        recordingObserver = RecordingObserver(url, secret, dir)
        recordingObserver.start()
        recordingObserver.awaitClose()
    }

    private fun handleStart() {
        if (!this::recordingObserver.isInitialized) {
            log.warn(NOT_INITIALIZED_WARNING)
            return
        }
        if (!recordingObserver.isRecording()) {
            recordingObserver.startRecording()
            log.info("Recording started.")
        } else {
            log.info("Recording is already started.")
        }
    }

    private fun handleStop() {
        if (!this::recordingObserver.isInitialized) {
            log.warn(NOT_INITIALIZED_WARNING)
            return
        }
        if (recordingObserver.isRecording()) {
            recordingObserver.stopRecordingKeepFile()
        } else {
            log.info("No active recording to stop.")
        }
    }

    private fun handleAbort() {
        if (!this::recordingObserver.isInitialized) {
            log.warn(NOT_INITIALIZED_WARNING)
            return
        }
        if (recordingObserver.isRecording()) {
            recordingObserver.stopAndDeleteRecording()
        } else {
            log.info("No active recording to abort.")
        }
    }
}

private class RecorderCli : CliktCommand(name = "recorder", help = "Tool for recording Robocode Tank Royale battles.") {
    private val urlOpt by option("-u", "--url", help = "Server URL (default: ws://localhost:7654)")
    private val secretOpt by option("-s", "--secret", help = "Secret used for server authentication")
    private val dirOpt by option("-d", "--dir", help = "Directory to save recordings (default: current directory)")

    init {
        versionOption("Robocode Tank Royale Recorder ${Version.version}", names = setOf("-v", "--version"))
    }

    override fun run() {
        Recorder.url = urlOpt ?: Recorder.DEFAULT_URL
        Recorder.secret = secretOpt
        Recorder.dir = dirOpt
        Recorder().run()
    }
}
