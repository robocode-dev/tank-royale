package dev.robocode.tankroyale.botapi.events;

import java.net.URI;

/**
 * Base class of all connection events.
 */
abstract class ConnectionEvent implements IEvent {

    // URI of the server.
    private final URI serverUri;

    /**
     * Initializes a new instance of the ConnectionEvent class.
     *
     * <p>param serverUrl is the URI of the server.
     */
    protected ConnectionEvent(URI serverUri) {
        this.serverUri = serverUri;
    }

    /**
     * Return the URI of the server.
     *
     * @return the URI of the server.
     */
    public URI getServerUri() {
        return serverUri;
    }
}
