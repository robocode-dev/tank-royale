package net.robocode2.gui.bootstrap

import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.parseList

@UnstableDefault
@ImplicitReflectionSerializer
object Bootstrap {

    private val json = Json(JsonConfiguration.Default)

    fun list(): List<BotEntry> {
        val entries = BootstrapProcess.list()
        return json.parseList(entries)
    }
}

@UnstableDefault
@ImplicitReflectionSerializer
fun main() {
    Bootstrap.list().forEach { println(it) }
}