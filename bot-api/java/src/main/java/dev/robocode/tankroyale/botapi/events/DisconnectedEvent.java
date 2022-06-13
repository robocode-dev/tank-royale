package dev.robocode.tankroyale.botapi.events;

import java.net.URI;
import java.util.Optional;

/**
 * Event occurring when bot gets disconnected from server.
 */
@SuppressWarnings("unused")
public final class DisconnectedEvent extends ConnectionEvent {

    // Indication if closing of the connection was initiated by the remote host.
    private final boolean remote;

    // Status code that indicates the reason for closing the connection.
    private final Integer statusCode;

    // Message with the reason for closing the connection.
    private final String reason;

    /**
     * Initializes a new instance of the DisconnectedEvent class.
     *
     * @param serverUri  is the URI of the server.
     * @param remote     is a flag indicating if closing of the connection was initiated by the remote
     *                   host.
     * @param statusCode is a status code that indicates the reason for closing the connection.
     * @param reason     is a message with the reason for closing the connection.
     */
    public DisconnectedEvent(URI serverUri, boolean remote, Integer statusCode, String reason) {
        super(serverUri);
        this.remote = remote;
        this.statusCode = statusCode;
        this.reason = reason;
    }

    /**
     * Checks if closing the connection was initiated by the remote host.
     *
     * @return {@code true} if closing the connection was initiated by the remote host; {@code false} otherwise.
     */
    public boolean isRemote() {
        return remote;
    }

    /**
     * Returns a status code that indicates the reason for closing the connection, if such status code exists.
     *
     * @return a status code that indicates the reason for closing the connection, if such status code exists.
     */
    public Optional<Integer> getStatusCode() {
        return Optional.ofNullable(statusCode);
    }

    /**
     * Returns a message with the reason for closing the connection, if such reason exists.
     *
     * @return a message with the reason for closing the connection, if such reason exists.
     */
    public Optional<String> getReason() {
        return Optional.ofNullable(reason);
    }
}
