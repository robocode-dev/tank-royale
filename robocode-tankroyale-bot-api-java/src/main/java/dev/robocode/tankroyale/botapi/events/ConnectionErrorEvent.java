package dev.robocode.tankroyale.botapi.events;

import lombok.Builder;
import lombok.ToString;
import lombok.Value;

/** Event occurring when a connection error occurs */
@Value
@Builder
@ToString
public final class ConnectionErrorEvent {

  /** The exception causing the error */
  Exception exception;
}
