package dev.robocode.tankroyale.gui.ui.tps

import dev.robocode.tankroyale.gui.model.TpsChangedEvent
import dev.robocode.tankroyale.gui.util.Event

object TpsEvents {
    val onTpsChanged = Event<TpsChangedEvent>()
}