package dev.robocode.tankroyale.server.connection

import com.google.gson.JsonObject
import dev.robocode.tankroyale.server.rules.MAX_NUMBER_OF_TEAM_MESSAGES_PER_TURN
import dev.robocode.tankroyale.server.rules.MAX_TEAM_MESSAGE_SIZE
import dev.robocode.tankroyale.server.rules.MAX_TEAM_MESSAGES_BYTES_PER_TURN
import java.nio.charset.StandardCharsets

/** Validates the complete batch before any part of an intent enters the game loop. */
internal object TeamMessagePolicy {
    fun validate(intent: JsonObject) {
        val value = intent.get("teamMessages") ?: return
        if (value.isJsonNull) return
        require(value.isJsonArray) { "teamMessages must be an array" }
        val messages = value.asJsonArray
        require(messages.size() <= MAX_NUMBER_OF_TEAM_MESSAGES_PER_TURN) {
            "teamMessages exceeds $MAX_NUMBER_OF_TEAM_MESSAGES_PER_TURN messages"
        }
        require(messages.toString().toByteArray(StandardCharsets.UTF_8).size <= MAX_TEAM_MESSAGES_BYTES_PER_TURN) {
            "teamMessages exceeds $MAX_TEAM_MESSAGES_BYTES_PER_TURN UTF-8 bytes"
        }
        for (item in messages) {
            require(item.isJsonObject) { "Each team message must be an object" }
            val message = item.asJsonObject.get("message")
            require(message != null && message.isJsonPrimitive && message.asJsonPrimitive.isString) {
                "Each team message requires an encoded string payload"
            }
            require(message.asString.toByteArray(StandardCharsets.UTF_8).size <= MAX_TEAM_MESSAGE_SIZE) {
                "Team message exceeds $MAX_TEAM_MESSAGE_SIZE UTF-8 bytes"
            }
            val messageType = item.asJsonObject.get("messageType")
            require(messageType != null && messageType.isJsonPrimitive && messageType.asJsonPrimitive.isString) {
                "Each team message requires a messageType string"
            }
            val recipient = item.asJsonObject.get("receiverId")
            require(recipient == null || recipient.isJsonNull ||
                    (recipient.isJsonPrimitive && recipient.asJsonPrimitive.isNumber &&
                            recipient.asString.toIntOrNull() != null)) {
                "receiverId must be an integer teammate id"
            }
        }
    }
}
