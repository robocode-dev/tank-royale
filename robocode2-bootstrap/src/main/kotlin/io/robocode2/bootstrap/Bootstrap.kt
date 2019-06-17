package io.robocode2.bootstrap

import io.robocode2.bootstrap.util.BotFinder
import kotlinx.serialization.ImplicitReflectionSerializer
import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.Model.CommandSpec
import picocli.CommandLine.Option
import picocli.CommandLine.Spec
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

fun main(args: Array<String>) {
    CommandLine.run(Bootstrap(), System.out, CommandLine.Help.Ansi.OFF, *args)
}

@Command(
        name = "Robocode2 io.robocode2.bootstrap.Bootstrap",
        versionProvider = VersionFileProvider::class,
        header = [],
        descriptionHeading = "Description:%n",
        description = ["Used for starting up bot applications"]
)
class Bootstrap : Runnable {

    @Option(names = ["-V", "--version"], description = ["Display version info"])
    private var isVersionInfoRequested = false

    @Option(names = ["-h", "--help"], description = ["Display this help message"])
    private var isUsageHelpRequested = false

    @Option(names = ["-d", "--dir"], paramLabel = "DIR", description = ["Set the bootstrap directory"])
    private var dir = File("").absolutePath

    @Option(names = ["-l", "--list"], description = ["List filenames on available bots"])
    private var isListRequested = false

    @Spec
    private val spec: CommandSpec? = null

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
                // Print out header as banner
                val header = this.spec?.usageMessage()?.header()
                if (header != null) {
                    for (line in header) {
                        println(line)
                    }
                }
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