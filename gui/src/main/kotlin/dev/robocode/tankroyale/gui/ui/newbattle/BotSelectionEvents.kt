package dev.robocode.tankroyale.gui.ui.newbattle

import dev.robocode.tankroyale.client.model.BotInfo
import dev.robocode.tankroyale.common.event.Event

object BotSelectionEvents {
    val onBotDirectorySelected = Event<BotInfo>()
    val onJoinedBotSelected = Event<BotInfo>()
    val onBotSelected = Event<BotInfo>()
    val onSelectedBotListUpdated = Event<List<BotInfo>>()

    /** Bots were placed in a bot directory, such as by a download, without the settings changing. */
    val onBotsPlaced = Event<Unit>()
}
