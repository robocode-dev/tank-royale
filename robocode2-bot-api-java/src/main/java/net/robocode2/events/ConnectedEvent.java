package net.robocode2.events;

import lombok.Builder;
import lombok.Value;

/** Event occurring when bot gets connected to server */
@Value
@Builder
public class ConnectedEvent {

  /** HTTP status code */
  short httpStatus;

  /** Status message */
  String httpStatusMessage;
}
