package dev.robocode.tankroyale.server.core

import dev.robocode.tankroyale.common.rules.DEFAULT_GAME_TYPES

/** Server setup. */
data class ServerSetup(val gameTypes: Set<String> = DEFAULT_GAME_TYPES.split(",").map { it.trim() }.toSet())