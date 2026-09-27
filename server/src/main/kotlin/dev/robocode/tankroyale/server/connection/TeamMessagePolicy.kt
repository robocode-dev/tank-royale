package dev.robocode.tankroyale.server.connection

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import dev.robocode.tankroyale.schema.TeamMessage
import dev.robocode.tankroyale.server.model.BotId
import dev.robocode.tankroyale.server.rules.MAX_LOGICAL_TEAM_MESSAGES_PER_TURN
import dev.robocode.tankroyale.server.rules.MAX_NUMBER_OF_TEAM_MESSAGES_PER_TURN
import dev.robocode.tankroyale.server.rules.MAX_TEAM_MESSAGE_SIZE
import dev.robocode.tankroyale.server.rules.MAX_TEAM_MESSAGES_BYTES_PER_TURN
import java.nio.charset.StandardCharsets

/** Validates the complete batch before any part of an intent enters the game loop. */
internal object TeamMessagePolicy {
    const val BATCH_MESSAGE_TYPE = "team-message-batch-v1"

    /**
     * Checks the receivers of an intent's team messages. Returns the policy-violation reason, or `null` when
     * the intent may be processed.
     *
     * A receiver must be one of the teammates the sender was given at game start. A teammate that has since
     * disconnected is still a valid receiver: its messages are not delivered, but the sender is not
     * penalized for a roster it cannot observe. Batch support is only required from connected recipients.
     */
    fun recipientViolation(
        messages: List<TeamMessage>?,
        gameStartTeammateIds: Set<BotId>,
        connectedTeammateIds: Set<BotId>,
        supportsBatch: (BotId) -> Boolean,
    ): String? {
        val teamMessages = messages.orEmpty()
        if (teamMessages.any { it.receiverId != null && BotId(it.receiverId) !in gameStartTeammateIds }) {
            return "Team message receiverId is not a teammate"
        }
        val batchRecipients = teamMessages
            .filter { it.messageType == BATCH_MESSAGE_TYPE }
            .flatMap { if (it.receiverId == null) connectedTeammateIds else listOf(BotId(it.receiverId)) }
            .filter { it in connectedTeammateIds }
        if (batchRecipients.any { !supportsBatch(it) }) {
            return "A batch recipient does not support team-message-batch-v1"
        }
        return null
    }

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
        var logicalMessageCount = 0
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
            require(messageType.asString.isNotBlank()) { "messageType must not be blank" }
            logicalMessageCount += if (messageType.asString == BATCH_MESSAGE_TYPE) {
                validateBatch(message.asString)
            } else 1
            val recipient = item.asJsonObject.get("receiverId")
            require(recipient == null || recipient.isJsonNull ||
                    (recipient.isJsonPrimitive && recipient.asJsonPrimitive.isNumber &&
                            recipient.asString.toIntOrNull() != null)) {
                "receiverId must be an integer teammate id"
            }
        }
        require(logicalMessageCount <= MAX_LOGICAL_TEAM_MESSAGES_PER_TURN) {
            "teamMessages exceeds $MAX_LOGICAL_TEAM_MESSAGES_PER_TURN logical messages"
        }
    }

    private fun validateBatch(payload: String): Int {
        val root = try { JsonParser.parseString(payload) } catch (exception: RuntimeException) {
            throw IllegalArgumentException("Invalid team message batch payload", exception)
        }
        require(root.isJsonObject) { "Invalid team message batch payload" }
        val messages = root.asJsonObject.get("messages")
        require(messages != null && messages.isJsonArray && messages.asJsonArray.size() > 0) {
            "A team message batch must contain at least one message"
        }
        messages.asJsonArray.forEach { item ->
            require(item.isJsonObject) { "Each team message batch item must be an object" }
            val objectItem = item.asJsonObject
            val itemType = objectItem.get("messageType")
            val itemPayload = objectItem.get("message")
            require(itemType != null && itemType.isJsonPrimitive && itemType.asJsonPrimitive.isString
                    && itemType.asString.isNotBlank()
                    && itemPayload != null && itemPayload.isJsonPrimitive && itemPayload.asJsonPrimitive.isString) {
                "Each team message batch item requires messageType and message strings"
            }
            try {
                val decoded = JsonParser.parseString(itemPayload.asString)
                require(!decoded.isJsonNull) { "A team message batch item cannot be null" }
            } catch (exception: RuntimeException) {
                throw IllegalArgumentException("Invalid JSON in team message batch item", exception)
            }
        }
        return messages.asJsonArray.size()
    }
}
