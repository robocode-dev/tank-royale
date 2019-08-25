package dev.robocode.tankroyale.bootstrap

import dev.robocode.tankroyale.bootstrap.util.BootUtil
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.stringify
import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import picocli.CommandLine.Parameters
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    CommandLine.run(RC2Boot(), System.out, CommandLine.Help.Ansi.ON, *args)
}

@Command(
        name = "bootstrap",
        versionProvider = VersionFileProvider::class,
        description = ["Tool for booting up Robocode bots"],
        mixinStandardHelpOptions = true
)
class RC2Boot : Runnable {

    @Option(names = ["--boot-dir"], paramLabel = "BOOTDIR", description = ["Sets the path to the boot directory"])
    private var bootDir = getBootDir()

    override fun run() {
        val cmdLine = CommandLine(RC2Boot())
        when {
            cmdLine.isUsageHelpRequested -> cmdLine.usage(System.out)
            cmdLine.isVersionHelpRequested -> cmdLine.printVersionHelp(System.out)
            else -> cmdLine.usage(System.out)
        }
        exitProcess(0)
    }

    @Command(name = "filenames", description = ["List filenames of available bots"])
    @ImplicitReflectionSerializer
    private fun filenames(@Option(names = ["--boot-dir"], paramLabel = "BOOTDIR",
            description = ["Sets the path to the boot directory"]) bootDir: Path?) {
        BootUtil(getBootDir(bootDir)).findBotEntries().forEach { entry -> println(entry.filename) }
    }

    @Command(name = "list", description = ["List available bot entries"])
    @UnstableDefault
    @ImplicitReflectionSerializer
    private fun list(@Option(names = ["--boot-dir"], paramLabel = "BOOTDIR",
            description = ["Sets the path to the boot directory"]) bootDir: Path?) {
        val entries = BootUtil(getBootDir(bootDir)).findBotEntries()
        println(Json(JsonConfiguration.Default).stringify(entries))
    }

    @ImplicitReflectionSerializer
    @Command(name = "run", description = [
        "Start running the specified bots in individual processes.",
        "Press enter key to stop all started bots and quit this tool."
    ])
    private fun boot(
            @Option(names = ["--boot-dir"], paramLabel = "BOOTDIR",
                    description = ["Sets the path to the boot directory"]) bootDir: Path?,
            @Parameters(arity = "1..*", paramLabel = "FILE",
                    description = ["Filenames of the bots to start without file extensions"]) filenames: Array<String>
    ) {
        val processes = BootUtil(getBootDir(bootDir)).startBots(filenames)

        readLine()

        processes.parallelStream().forEach {
            p -> p.descendants().forEach {
                d -> d.destroy()
                if (d.isAlive)
                    d.destroyForcibly()
            }
            p.destroy()
            p.waitFor(10, TimeUnit.SECONDS)
            if (p.isAlive) {
                p.destroyForcibly()
                p.waitFor(10, TimeUnit.SECONDS)
            }
        }
    }

    /** Returns the set boot-dir which will be set to the current working directory if it is not provided as option */
    private fun getBootDir(dir: Path? = null): Path {
        return dir ?: Paths.get("").toAbsolutePath()
    }
}

internal class VersionFileProvider : CommandLine.IVersionProvider {

    override fun getVersion(): Array<String> {
        val inputStream = this.javaClass.classLoader.getResourceAsStream("version.txt")
        var version = "?"
        if (inputStream != null) {
            BufferedReader(InputStreamReader(inputStream)).use { br -> version = br.readLine() }
        }
        return arrayOf("Robocode Tank Royale Bootstrap $version")
    }
}