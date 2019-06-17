package io.robocode2.bootstrap

import io.robocode2.bootstrap.util.BotFinder
import kotlinx.serialization.ImplicitReflectionSerializer
import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.file.Paths

fun main(args: Array<String>) {
    CommandLine.run(Bootstrap(), System.out, CommandLine.Help.Ansi.OFF, *args)
}

@Command(
        name = "bootstrap",
        versionProvider = VersionFileProvider::class,
        description = ["Tool for booting up Robocode 2 bots"]
)
class Bootstrap : Runnable {

    @Option(names = ["-V", "--version"], description = ["Display version info"])
    private var isVersionInfoRequested = false

    @Option(names = ["-h", "--help"], description = ["Display this help message"])
    private var isUsageHelpRequested = false

    @Option(names = ["-d", "--dir"], paramLabel = "DIR", description = ["Set the bootstrap directory"])
    private var dir = Paths.get("").toAbsolutePath()

    @Option(names = ["-l", "--list"], description = ["List filenames on available bots"])
    private var isListRequested = false

    @ImplicitReflectionSerializer
    override fun run() {
        val cmdLine = CommandLine(Bootstrap())

        when {
            isUsageHelpRequested -> {
                cmdLine.usage(System.out)
                System.exit(0)
            }
            isVersionInfoRequested -> {
                cmdLine.printVersionHelp(System.out)
                System.exit(0)
            }
            isListRequested -> {
                BotFinder(dir).findBotInfos().forEach { println(it.key) }
                System.exit(0)
            }
            else -> {
                // TODO: Print header/banner?
                cmdLine.printVersionHelp(System.out)
            }
        }

        println(BotFinder(dir).findOsScript("TestBot"))
    }
}

internal class VersionFileProvider : CommandLine.IVersionProvider {

    override fun getVersion(): Array<String> {
        val inputStream = this.javaClass.classLoader.getResourceAsStream("version.txt")
        var version = "?"
        if (inputStream != null) {
            BufferedReader(InputStreamReader(inputStream)).use { br -> version = br.readLine() }
        }
        return arrayOf("Robocode2 io.robocode2.bootstrap.Bootstrap $version")
    }
}