package dev.robocode.tankroyale.botapi.internal;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.robocode.tankroyale.botapi.TeamMessageBatch;
import dev.robocode.tankroyale.botapi.internal.json.JsonConverter;

/** Compact, platform-independent payload representation for a team-message batch. */
public final class TeamMessageBatchCodec {
    public static final String MESSAGE_TYPE = "team-message-batch-v1";

    private TeamMessageBatchCodec() {
    }

    public static String encode(TeamMessageBatch batch) {
        var root = new JsonObject();
        var messages = new JsonArray();
        for (Object value : batch.getMessages()) {
            var item = new JsonObject();
            item.addProperty("messageType", value.getClass().getName());
            item.addProperty("message", JsonConverter.toJson(value));
            messages.add(item);
        }
        root.add("messages", messages);
        return JsonConverter.toJson(root);
    }

    public static JsonArray decodeItems(String payload) {
        var root = JsonParser.parseString(payload);
        if (!root.isJsonObject() || !root.getAsJsonObject().has("messages")
                || !root.getAsJsonObject().get("messages").isJsonArray()) {
            throw new IllegalArgumentException("Invalid team message batch payload");
        }
        var messages = root.getAsJsonObject().getAsJsonArray("messages");
        if (messages.isEmpty()) {
            throw new IllegalArgumentException("A team message batch must contain at least one message");
        }
        return messages;
    }
}
