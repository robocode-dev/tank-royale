package dev.robocode.tankroyale.gui.ui.newbattle

import dev.robocode.tankroyale.gui.model.BotInfo
import dev.robocode.tankroyale.gui.util.Event

object BotSelectionChannel {

    val onBotsSelected = Event<List<BotInfo>>()
}