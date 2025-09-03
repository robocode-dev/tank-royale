package dev.robocode.tankroyale.gui.ui.tps

import dev.robocode.tankroyale.client.model.TpsChangedEvent
import dev.robocode.tankroyale.common.Event

object TpsEvents {
    val onTpsChanged = Event<TpsChangedEvent>()
}