package dev.robocode.tankroyale.recorder.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.versionOption
import dev.robocode.tankroyale.common.util.Version

class RecorderCli : CliktCommand(
    name = "recorder",
    help = "Tool for recording Robocode Tank Royale battles.",
) {
    private val urlOpt by option("-u", "--url", help = "Server URL (default: ws://localhost:7654)")
    private val secretOpt by option("-s", "--secret", help = "Secret used for server authentication")
    private val dirOpt by option("-d", "--dir", help = "Directory to save recordings (default: current directory)")

    init {
        versionOption("Robocode Tank Royale Recorder ${Version.version}", names = setOf("-v", "--version"))
    }

    override fun run() {
        val runtime = RecorderRuntime(
            url = urlOpt ?: RecorderRuntime.DEFAULT_URL,
            secret = secretOpt,
            dir = dirOpt,
        )
        runtime.run()
    }
}

