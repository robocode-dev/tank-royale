package dev.robocode.tankroyale.gui.ui.tps

import dev.robocode.tankroyale.client.Event
import dev.robocode.tankroyale.client.model.TpsChangedEvent

object TpsEvents {
    val onTpsChanged = Event<TpsChangedEvent>()
}