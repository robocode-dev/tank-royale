package dev.robocode.tankroyale.rumble.worker

import dev.robocode.tankroyale.rumble.protocol.ExitCode
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.system.exitProcess

/**
 * The Rumble executable. Only worker mode exists so far: `rumble worker` reads one job line from standard input and
 * writes event lines to standard output. Anything meant for a person goes to standard error.
 */
fun main(args: Array<String>) {
    if (args.firstOrNull() != "worker") {
        System.err.println("Usage: rumble worker   (reads one job from standard input)")
        exitProcess(ExitCode.REFUSED)
    }
    val input = BufferedReader(InputStreamReader(System.`in`, Charsets.UTF_8))
    exitProcess(Worker.run(input, System.out, RunnerEngine))
}
