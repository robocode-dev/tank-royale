package dev.robocode.tankroyale.botapi.events;

import java.net.URI;

/**
 * Event occurring when bot gets connected to server
 */
public final class ConnectedEvent extends ConnectionEvent {

    /**
     * Initializes a new instance of the ConnectedEvent class.
     *
     * @param serverUri is the URI of the server.
     */
    public ConnectedEvent(URI serverUri) {
        super(serverUri);
    }
}
