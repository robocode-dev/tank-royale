package dev.robocode.tankroyale.botapi.internal;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static dev.robocode.tankroyale.botapi.Constants.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TeamMessageLimitsTest {
    @Test @Tag("Unit")
    void accepts128MessagesAndRejects129th() {
        assertDoesNotThrow(() -> IntentValidator.validateTeamMessage("hello", 127));
        assertThrows(RuntimeException.class, () -> IntentValidator.validateTeamMessage("hello", 128));
    }

    @Test @Tag("Unit")
    void countsUnicodePayloadBytes() {
        assertDoesNotThrow(() -> IntentValidator.validateTeamMessageSize("é".repeat(TEAM_MESSAGE_MAX_SIZE / 2)));
        assertThrows(IllegalArgumentException.class,
                () -> IntentValidator.validateTeamMessageSize("é".repeat(TEAM_MESSAGE_MAX_SIZE / 2 + 1)));
    }

    @Test @Tag("Unit")
    void aggregateBoundaryIsInclusive() {
        assertDoesNotThrow(() -> IntentValidator.validateTeamMessagesSize("x".repeat(TEAM_MESSAGES_MAX_BYTES_PER_TURN)));
        assertThrows(IllegalArgumentException.class,
                () -> IntentValidator.validateTeamMessagesSize("x".repeat(TEAM_MESSAGES_MAX_BYTES_PER_TURN + 1)));
    }
}
