package dev.robocode.tankroyale.booter

import com.github.ajalt.clikt.core.subcommands
import dev.robocode.tankroyale.booter.cli.BootCli
import dev.robocode.tankroyale.booter.cli.BooterCli
import dev.robocode.tankroyale.booter.cli.DirCli
import dev.robocode.tankroyale.booter.cli.InfoCli

fun main(args: Array<String>) = BooterCli().subcommands(DirCli(), InfoCli(), BootCli()).main(args)
