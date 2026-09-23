package dev.robocode.tankroyale.botapi;

import java.util.Collection;
import java.util.List;

/**
 * An immutable, ordered group of non-null payloads delivered through one team-message event on the next turn.
 * Use {@link IBaseBot#broadcastTeamMessageBatch(java.util.Collection)} or
 * {@link IBaseBot#sendTeamMessageBatch(int, java.util.Collection)} to enqueue a batch.
 */
public final class TeamMessageBatch {
    private final List<Object> messages;

    public TeamMessageBatch(Collection<?> messages) {
        if (messages == null || messages.isEmpty()) {
            throw new IllegalArgumentException("A team message batch must contain at least one message");
        }
        if (messages.stream().anyMatch(message -> message == null)) {
            throw new IllegalArgumentException("A team message batch cannot contain null messages");
        }
        this.messages = List.copyOf(messages);
    }

    public List<Object> getMessages() {
        return messages;
    }
}
