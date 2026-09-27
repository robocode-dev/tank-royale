package dev.robocode.tankroyale.server.connection

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import dev.robocode.tankroyale.schema.TeamMessage
import dev.robocode.tankroyale.server.model.BotId
import dev.robocode.tankroyale.server.rules.MAX_TEAM_MESSAGE_SIZE
import dev.robocode.tankroyale.server.rules.MAX_TEAM_MESSAGES_BYTES_PER_TURN
import dev.robocode.tankroyale.server.rules.MAX_LOGICAL_TEAM_MESSAGES_PER_TURN
import io.kotest.core.Tag
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.assertions.throwables.shouldThrow
import java.nio.charset.StandardCharsets

class TeamMessagePolicyTest : FunSpec({
    tags(Tag("Unit"))

    fun item(payload: String, recipient: Int? = null) = JsonObject().apply {
        addProperty("message", payload)
        addProperty("messageType", "String")
        recipient?.let { addProperty("receiverId", it) }
    }

    fun intent(messages: JsonArray) = JsonObject().apply { add("teamMessages", messages) }

    test("Unit: 11 and 64 messages are accepted in order; 65 are rejected") {
        val messages = JsonArray()
        repeat(11) { messages.add(item("message-$it")) }
        TeamMessagePolicy.validate(intent(messages))
        messages.size() shouldBe 11
        messages[10].asJsonObject.get("message").asString shouldBe "message-10"
        repeat(53) { messages.add(item("message-${it + 11}")) }
        TeamMessagePolicy.validate(intent(messages))
        messages.add(item("message-64"))
        shouldThrow<IllegalArgumentException> { TeamMessagePolicy.validate(intent(messages)) }
    }

    test("Unit: payload limit counts UTF-8 bytes including Unicode") {
        TeamMessagePolicy.validate(intent(JsonArray().apply { add(item("é".repeat(MAX_TEAM_MESSAGE_SIZE / 2))) }))
        shouldThrow<IllegalArgumentException> {
            TeamMessagePolicy.validate(intent(JsonArray().apply { add(item("é".repeat(MAX_TEAM_MESSAGE_SIZE / 2 + 1))) }))
        }
    }

    test("Unit: compact array boundary is inclusive") {
        val messages = JsonArray()
        repeat(5) { messages.add(item("x".repeat(45_000))) }
        val baseSize = messages.toString().toByteArray(StandardCharsets.UTF_8).size
        messages.add(item(""))
        val overhead = messages.toString().toByteArray(StandardCharsets.UTF_8).size - baseSize
        val remaining = MAX_TEAM_MESSAGES_BYTES_PER_TURN - baseSize - overhead
        messages.remove(messages.size() - 1)
        messages.add(item("x".repeat(remaining)))
        messages.toString().toByteArray(StandardCharsets.UTF_8).size shouldBe MAX_TEAM_MESSAGES_BYTES_PER_TURN
        TeamMessagePolicy.validate(intent(messages))
        messages.remove(messages.size() - 1)
        messages.add(item("x".repeat(remaining + 1)))
        shouldThrow<IllegalArgumentException> { TeamMessagePolicy.validate(intent(messages)) }
    }

    test("Unit: malformed message rejects the batch") {
        val messages = JsonArray().apply { add(item("valid", 7)); add(JsonObject().apply { addProperty("message", 3) }) }
        shouldThrow<IllegalArgumentException> { TeamMessagePolicy.validate(intent(messages)) }
        shouldThrow<IllegalArgumentException> {
            TeamMessagePolicy.validate(intent(JsonArray().apply {
                add(item("valid").apply { addProperty("messageType", " ") })
            }))
        }
    }

    test("Unit: batch counts logical payloads and rejects malformed or null entries") {
        fun batch(count: Int): String = JsonObject().apply {
            add("messages", JsonArray().apply {
                repeat(count) { index -> add(JsonObject().apply {
                    addProperty("messageType", "String")
                    addProperty("message", "\"item-$index\"")
                }) }
            })
        }.toString()
        TeamMessagePolicy.validate(intent(JsonArray().apply { add(item(batch(MAX_LOGICAL_TEAM_MESSAGES_PER_TURN)).apply {
            addProperty("messageType", TeamMessagePolicy.BATCH_MESSAGE_TYPE)
        }) }))
        shouldThrow<IllegalArgumentException> {
            TeamMessagePolicy.validate(intent(JsonArray().apply { add(item(batch(MAX_LOGICAL_TEAM_MESSAGES_PER_TURN + 1)).apply {
                addProperty("messageType", TeamMessagePolicy.BATCH_MESSAGE_TYPE)
            }) }))
        }
        val malformed = JsonObject().apply {
            addProperty("messageType", "String")
            addProperty("message", "null")
        }.toString()
        shouldThrow<IllegalArgumentException> {
            TeamMessagePolicy.validate(intent(JsonArray().apply { add(item(malformed).apply {
                addProperty("messageType", TeamMessagePolicy.BATCH_MESSAGE_TYPE)
            }) }))
        }
    }

    fun teamMessage(receiverId: Int?, messageType: String = "String") = TeamMessage().apply {
        this.message = "\"payload\""
        this.messageType = messageType
        this.receiverId = receiverId
    }

    test("Unit: a message to a teammate that disconnected after game start is not a violation").config(tags = setOf(Tag("PRO-009"))) {
        val violation = TeamMessagePolicy.recipientViolation(
            listOf(teamMessage(3), teamMessage(3, TeamMessagePolicy.BATCH_MESSAGE_TYPE)),
            gameStartTeammateIds = setOf(BotId(2), BotId(3)),
            connectedTeammateIds = setOf(BotId(2)),
            supportsBatch = { it == BotId(2) },
        )
        violation shouldBe null
    }

    test("Unit: a receiver outside the game-start team is a violation").config(tags = setOf(Tag("PRO-009"))) {
        TeamMessagePolicy.recipientViolation(
            listOf(teamMessage(9)),
            gameStartTeammateIds = setOf(BotId(2)),
            connectedTeammateIds = setOf(BotId(2)),
            supportsBatch = { true },
        ) shouldBe "Team message receiverId is not a teammate"
    }

    test("Unit: a connected batch recipient without batch support is a violation") {
        TeamMessagePolicy.recipientViolation(
            listOf(teamMessage(null, TeamMessagePolicy.BATCH_MESSAGE_TYPE)),
            gameStartTeammateIds = setOf(BotId(2), BotId(3)),
            connectedTeammateIds = setOf(BotId(2), BotId(3)),
            supportsBatch = { it == BotId(2) },
        ) shouldBe "A batch recipient does not support team-message-batch-v1"
    }
})
