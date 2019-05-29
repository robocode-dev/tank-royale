package net.robocode2.events;

import lombok.Builder;
import lombok.ToString;
import lombok.Value;

/** Event occurring when bot gets connected to server */
@Value
@Builder
@ToString
public class ConnectedEvent {

  /** HTTP status code */
  short httpStatus;

  /** Status message */
  String httpStatusMessage;
}
