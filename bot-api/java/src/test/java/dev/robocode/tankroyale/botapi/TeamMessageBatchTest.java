package dev.robocode.tankroyale.botapi;

import com.google.gson.JsonParser;
import dev.robocode.tankroyale.botapi.internal.IntentValidator;
import dev.robocode.tankroyale.botapi.internal.TeamMessageBatchCodec;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Tag("TR-API-TCK-022")
class TeamMessageBatchTest {
    @Test
    void batchPayloadKeepsOrderAndUsesReservedMarker() {
        var batch = new TeamMessageBatch(List.of("first", "second"));
        var payload = TeamMessageBatchCodec.encode(batch);
        var items = TeamMessageBatchCodec.decodeItems(payload);

        assertEquals(2, items.size());
        assertEquals("first", JsonParser.parseString(items.get(0).getAsJsonObject().get("message").getAsString()).getAsString());
        assertEquals("second", JsonParser.parseString(items.get(1).getAsJsonObject().get("message").getAsString()).getAsString());
        assertEquals("team-message-batch-v1", TeamMessageBatchCodec.MESSAGE_TYPE);
    }

    @Test
    void batchMustContainNonNullPayloads() {
        assertThrows(IllegalArgumentException.class, () -> new TeamMessageBatch(List.of()));
        assertThrows(IllegalArgumentException.class, () -> new TeamMessageBatch(java.util.Arrays.asList("ok", null)));
    }

    @Test
    void logicalItemLimitAccepts128AndRejects129() {
        assertDoesNotThrow(() -> IntentValidator.validateLogicalTeamMessageCount(128));
        assertThrows(BotException.class, () -> IntentValidator.validateLogicalTeamMessageCount(129));
    }
}
