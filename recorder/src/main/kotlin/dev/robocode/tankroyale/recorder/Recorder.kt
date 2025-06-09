package dev.robocode.tankroyale.recorder

import dev.robocode.tankroyale.recorder.core.RecordingObserver
import dev.robocode.tankroyale.recorder.util.VersionFileProvider
import org.fusesource.jansi.AnsiConsole
import org.slf4j.LoggerFactory
import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import java.io.File
import java.util.*
import kotlin.system.exitProcess

val cmdLine = CommandLine(Recorder())

fun main(args: Array<String>) {
    System.setProperty("jansi.force", "true")
    AnsiConsole.systemInstall()

    cmdLine.apply {
        isSubcommandsCaseInsensitive = true
        isOptionsCaseInsensitive = true

        exitProcess(execute(*args))
    }
}

@Command(
    name = "recorder",
    versionProvider = VersionFileProvider::class,
    description = ["Tool for recording Robocode Tank Royale battles."],
    mixinStandardHelpOptions = true
)
class Recorder : Runnable {

    companion object {
        private const val EXIT_COMMAND = "q"
        private const val DEFAULT_URL: String = "ws://localhost:7654"

        @Option(names = ["-v", "--version"], description = ["Display version info"])
        private var isVersionInfoRequested = false

        @Option(names = ["-h", "--help"], description = ["Display this help message"])
        private var isUsageHelpRequested = false

        @Option(
            names = ["-u", "--url"],
            type = [String::class],
            description = ["Server URL (default: $DEFAULT_URL)"]
        )
        private var url: String = DEFAULT_URL

        @Option(
            names = ["-s", "--secret"],
            type = [String::class],
            description = ["Secret used for server authentication"]
        )
        private var secret: String? = null

        @Option(
            names = ["-d", "--dir"],
            type = [String::class],
            description = ["Directory to save recordings (default: current directory)"]
        )
        private var dir: String? = null

    }

    private val log = LoggerFactory.getLogger(this::class.java)
    private lateinit var recordingObserver: RecordingObserver

    override fun run() {
        val cmdLine = CommandLine(this)

        when {
            isUsageHelpRequested -> cmdLine.usage(System.out)
            isVersionInfoRequested -> cmdLine.printVersionHelp(System.out)
            else -> {
                cmdLine.printVersionHelp(System.out)
                startExitInputMonitorThread()
                val canonicalDir = File(dir, ".").canonicalPath
                log.info("Recordings will be stored in $canonicalDir")
                startRecorder()
            }
        }
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
                if (input.equals(EXIT_COMMAND, ignoreCase = true)) {
                    recordingObserver.stop()
                    exitProcess(1)
                }
            }
        }
    }

    private fun startRecorder() {
        recordingObserver = RecordingObserver(url, secret, dir)
        recordingObserver.start()
        recordingObserver.awaitClose()
    }

}
