package dev.robocode.tankroyale.gui.ui.tps

import dev.robocode.tankroyale.gui.util.Event

object TpsEventChannel {
    val onTpsChange = Event<TpsEvent>()
}