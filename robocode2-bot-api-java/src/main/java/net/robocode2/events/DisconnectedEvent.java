package net.robocode2.events;

import lombok.Builder;
import lombok.Value;

/** Event occurring when bot gets disconnected from server */
@Value
@Builder
public class DisconnectedEvent {
  /** Indication if closing of the connection was initiated by the remote host */
  boolean remote;
}
