package dev.robocode.tankroyale.gui.ui.newbattle

import dev.robocode.tankroyale.client.model.BotInfo

/**
 * How many participants the bots chosen in New Battle are. The server counts every bot that joins as a participant,
 * also a team's members, and starts a battle only with a number of bots within the game type's minimum and maximum; New
 * Battle counts the same way, so a TwinDuel of two teams of two bots fills its four places.
 */
object BattleParticipants {
    /** The number of participants [bots] are: one for every bot, whether alone or in a team. */
    fun count(bots: Collection<BotInfo>): Int = bots.size

    /** Whether a battle can be started with [bots] for a game type that needs [min] to [max] participants (no limit when null). */
    fun startable(bots: Collection<BotInfo>, min: Int, max: Int?): Boolean {
        val count = count(bots)
        return count >= min && (max == null || count <= max)
    }
}
