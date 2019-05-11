package net.robocode2.events;

import lombok.Builder;
import lombok.Value;

/** Event occurring when bot gets disconnected from server */
@Value
@Builder
public class DisconnectedEvent {

  /** WebSocket (RFC 6455) close code. Link: https://tools.ietf.org/html/rfc6455 */
  int code;

  /** Additional information string */
  String reason;

  /** Indication if closing of the connection was initiated by the remote host */
  boolean remote;
}
