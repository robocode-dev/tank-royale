package dev.robocode.tankroyale.server.core

import dev.robocode.tankroyale.server.rules.DEFAULT_GAME_TYPE

/** Server setup. */
data class ServerSetup(val gameTypes: Set<String> = setOf(DEFAULT_GAME_TYPE))