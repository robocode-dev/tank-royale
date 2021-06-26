package dev.robocode.tankroyale.booter

import picocli.CommandLine
import java.io.BufferedReader
import java.io.InputStreamReader

internal class VersionFileProvider : CommandLine.IVersionProvider {

    override fun getVersion(): Array<String> {
        val inputStream = this.javaClass.classLoader.getResourceAsStream("version.txt")
        var version = "?"
        if (inputStream != null) {
            BufferedReader(InputStreamReader(inputStream))
                .use { br -> version = br.readLine() }
        }
        return arrayOf("Robocode Tank Royale Bot Booter $version")
    }
}