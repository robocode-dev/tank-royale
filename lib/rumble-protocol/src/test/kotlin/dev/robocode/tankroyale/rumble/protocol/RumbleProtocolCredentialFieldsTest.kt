package dev.robocode.tankroyale.rumble.protocol

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class RumbleProtocolCredentialFieldsTest {

    private val credentialLike = Regex("(?i)(token|secret|password|passwd|credential|authorization|apikey|api_key|privatekey|private_key|(^|_)key($|_)|^key$)")

    private val messageTypes: List<Class<*>> = listOf(
        Job::class.java, JobBot::class.java, BotOutcome::class.java,
        WorkerEvent.Started::class.java, WorkerEvent.Result::class.java, WorkerEvent.Refused::class.java,
        WorkerEvent.Failed::class.java, WorkerEvent.Finished::class.java,
    )

    private fun suspectFields(type: Class<*>): List<String> =
        type.declaredFields.filter { !it.isSynthetic }.map { it.name }.filter { credentialLike.containsMatchIn(it) }

    @Suppress("unused")
    private class Leaky(val accessToken: String, val sourceHash: String, val apiKey: String)

    // RBW-021

    @Test
    @Tag("RBW-021")
    @Tag("Unit")
    @Tag("Positive")
    fun testRBW021_UnitPositive_noFieldOfAnyMessageIsNamedLikeACredential() {
        assertTrue(messageTypes.sumOf { it.declaredFields.size } > 20, "The guard must see the real fields")

        messageTypes.forEach { type -> assertEquals(emptyList<String>(), suspectFields(type), type.simpleName) }
    }

    @Test
    @Tag("RBW-021")
    @Tag("Unit")
    @Tag("Negative")
    fun testRBW021_UnitNegative_aFieldNamedLikeACredentialIsFoundAndAnOrdinaryHashIsNot() {
        assertEquals(listOf("accessToken", "apiKey"), suspectFields(Leaky::class.java))
    }
}
