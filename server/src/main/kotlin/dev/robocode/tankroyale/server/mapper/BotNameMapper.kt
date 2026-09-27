package dev.robocode.tankroyale.server.mapper

import dev.robocode.tankroyale.schema.BotName

/** Assigns unique battle names in the order selected by the controller. */
internal object BotNameMapper {

    data class Identity(val botId: Int, val teamMemberName: String, val version: String?) {
        val baseName: String
            get() = teamMemberName + version?.takeIf { it.isNotBlank() }?.let { " $it" }.orEmpty()
    }

    fun assignNames(identitiesInBattleOrder: List<Identity>): Map<Int, String> {
        val duplicateCounts = identitiesInBattleOrder.groupingBy { it.baseName }.eachCount()
        val occurrences = mutableMapOf<String, Int>()
        val names = linkedMapOf<Int, String>()

        identitiesInBattleOrder.forEach { identity ->
            val baseName = identity.baseName
            val uniqueName = if (duplicateCounts.getValue(baseName) > 1) {
                val occurrence = (occurrences[baseName] ?: 0) + 1
                occurrences[baseName] = occurrence
                "$baseName ($occurrence)"
            } else {
                baseName
            }
            names[identity.botId] = uniqueName
        }

        return names
    }

    fun namesForBotAndTeammates(
        botId: Int,
        teammateIds: Collection<Int>,
        namesById: Map<Int, String>,
    ): List<BotName> {
        val includedIds = teammateIds.toSet() + botId
        return namesById.mapNotNull { (id, name) ->
            if (id !in includedIds) return@mapNotNull null
            BotName().apply {
                this.botId = id
                this.name = name
            }
        }
    }
}
