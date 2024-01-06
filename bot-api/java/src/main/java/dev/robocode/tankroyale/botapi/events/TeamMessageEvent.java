package dev.robocode.tankroyale.botapi.events;

/**
 * Event occurring when a bot has received a message from a teammate.
 */
public final class TeamMessageEvent extends BotEvent {

    // The message that was received
    private final Object message;

    // The ID of the teammate that sent the message
    private final int senderId;

    /**
     * Initializes a new instance of the TeamMessageEvent class.
     *
     * @param turnNumber is the turn number when the team message was received.
     * @param message    is the message that was received.
     * @param senderId   is the id of the teammate that sent the message.
     */
    public TeamMessageEvent(int turnNumber, Object message, int senderId) {
        super(turnNumber);
        if (message == null) {
            throw new IllegalArgumentException("'message' cannot be null");
        }
        this.message = message;
        this.senderId = senderId;
    }

    /**
     * Returns the message that was received.
     *
     * @return the message that was received.
     */
    public Object getMessage() {
        return message;
    }

    /**
     * Returns the ID of the teammate that sent the message.
     *
     * @return the ID of the teammate that sent the message.
     */
    public int getSenderId() {
        return senderId;
    }
}
