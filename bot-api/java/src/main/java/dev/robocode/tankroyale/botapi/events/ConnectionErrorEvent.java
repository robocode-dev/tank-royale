package dev.robocode.tankroyale.botapi.events;

import java.net.URI;

/**
 * Event occurring when a connection error occurs.
 */
@SuppressWarnings("unused")
public final class ConnectionErrorEvent extends ConnectionEvent {

    // The error.
    private final Throwable error;

    /**
     * Initializes a new instance of the ConnectionErrorEvent class.
     *
     * @param serverUri is the URI of the server.
     * @param error     is the error.
     */
    public ConnectionErrorEvent(URI serverUri, Throwable error) {
        super(serverUri);
        this.error = error;
    }

    /**
     * Returns the error.
     *
     * @return The error.
     */
    public Throwable getError() {
        return error;
    }
}
