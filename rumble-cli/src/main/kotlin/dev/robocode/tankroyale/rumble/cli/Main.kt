package dev.robocode.tankroyale.rumble.cli

import kotlin.system.exitProcess

fun main(args: Array<String>) {
    exitProcess(RumbleCli.run(args, Io(System.out, System.err)))
}
