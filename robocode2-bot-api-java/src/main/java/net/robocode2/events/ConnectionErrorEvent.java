package net.robocode2.events;

import lombok.Builder;
import lombok.Value;

/** Event occurring when a connection error occurs */
@Value
@Builder
public class ConnectionErrorEvent {

  /** The exception causing the error */
  Exception exception;
}
