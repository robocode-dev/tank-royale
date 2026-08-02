package dev.robocode.tankroyale.client.model

import io.kotest.core.Tag
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull

class MessagesTest : FunSpec({

    tags(Tag("PRO-005"))

    test("PRO-005: Positive: compatibility client reads a handshake without behavior version") {
        val handshake = MessageConstants.json.decodeFromString<ServerHandshake>(
            """
            {
              "sessionId": "session",
              "name": "server",
              "version": "1.0.0",
              "variant": "Tank Royale",
              "gameTypes": ["classic"]
            }
            """.trimIndent(),
        )

        handshake.behaviorVersion.shouldBeNull()
    }
})
