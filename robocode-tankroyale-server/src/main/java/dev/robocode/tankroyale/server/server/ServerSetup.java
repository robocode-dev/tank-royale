package dev.robocode.tankroyale.server.server;

import dev.robocode.tankroyale.server.model.GameSetup;
import lombok.Data;

import java.util.Collections;
import java.util.Set;

@Data
final class ServerSetup {

  private Set<String> gameTypes = Collections.singleton(GameSetup.DEFAULT_GAME_TYPE);
}
