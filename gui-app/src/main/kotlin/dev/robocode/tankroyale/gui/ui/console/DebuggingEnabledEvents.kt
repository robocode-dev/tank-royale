package dev.robocode.tankroyale.gui.ui.console

import dev.robocode.tankroyale.gui.model.SetDebuggingEnabledForBot
import dev.robocode.tankroyale.gui.util.Event

object DebuggingEnabledEvents {
    val onDebuggingEnabledChanged = Event<SetDebuggingEnabledForBot>()
}