package dev.robocode.tankroyale.botapi.internal;

import dev.robocode.tankroyale.botapi.BotException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static dev.robocode.tankroyale.botapi.Constants.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TeamMessageLimitsTest {
    @Test @Tag("Unit")
    void accepts64MessagesAndRejects65th() {
        assertDoesNotThrow(() -> IntentValidator.validateTeamMessage("hello", 10));
        assertDoesNotThrow(() -> IntentValidator.validateTeamMessage("hello", MAX_NUMBER_OF_TEAM_MESSAGES_PER_TURN - 1));
        assertThrows(BotException.class, () -> IntentValidator.validateTeamMessage("hello", MAX_NUMBER_OF_TEAM_MESSAGES_PER_TURN));
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
