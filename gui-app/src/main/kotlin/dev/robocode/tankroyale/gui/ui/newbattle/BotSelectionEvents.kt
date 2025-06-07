package dev.robocode.tankroyale.gui.ui.newbattle

import dev.robocode.tankroyale.client.Event
import dev.robocode.tankroyale.client.model.BotInfo

object BotSelectionEvents {
    val onBotDirectorySelected = Event<BotInfo>()
    val onJoinedBotSelected = Event<BotInfo>()
    val onBotSelected = Event<BotInfo>()
    val onSelectedBotListUpdated = Event<List<BotInfo>>()
}
