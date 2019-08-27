package dev.robocode.tankroyale.botapi.events;

import lombok.Builder;
import lombok.ToString;
import lombok.Value;

/** Event occurring when bot gets disconnected from server */
@Value
@Builder
@ToString
public class DisconnectedEvent {
  /** Indication if closing of the connection was initiated by the remote host */
  boolean remote;
}
