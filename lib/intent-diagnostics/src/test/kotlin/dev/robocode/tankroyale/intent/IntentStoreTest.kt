package dev.robocode.tankroyale.intent

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class IntentStoreTest {

    private lateinit var store: IntentStore

    @BeforeEach
    fun setUp() {
        store = IntentStore()
    }

    @Tag("Unit")
    @Test
    fun `new store is empty`() {
        assertThat(store.size).isEqualTo(0)
        assertThat(store.botNames()).isEmpty()
        assertThat(store.getAllIntents()).isEmpty()
    }

    @Tag("Unit")
    @Test
    fun `add and retrieve intent`() {
        val intent = capturedIntent("BotA", round = 1, turn = 5)
        store.add(intent)

        assertThat(store.size).isEqualTo(1)
        assertThat(store.botNames()).containsExactly("BotA")
        assertThat(store.getIntentsForBot("BotA")).containsExactly(intent)
    }

    @Tag("Unit")
    @Test
    fun `retrieve intents for specific bot`() {
        store.add(capturedIntent("BotA", round = 1, turn = 1))
        store.add(capturedIntent("BotB", round = 1, turn = 1))
        store.add(capturedIntent("BotA", round = 1, turn = 2))

        assertThat(store.getIntentsForBot("BotA")).hasSize(2)
        assertThat(store.getIntentsForBot("BotB")).hasSize(1)
        assertThat(store.getIntentsForBot("BotC")).isEmpty()
    }

    @Tag("Unit")
    @Test
    fun `get intent at specific round and turn`() {
        val intent1 = capturedIntent("BotA", round = 1, turn = 1, firepower = 2.0)
        val intent2 = capturedIntent("BotA", round = 1, turn = 2, firepower = 3.0)
        store.add(intent1)
        store.add(intent2)

        assertThat(store.getIntentForBotAtTurn("BotA", 1, 1)).isEqualTo(intent1)
        assertThat(store.getIntentForBotAtTurn("BotA", 1, 2)).isEqualTo(intent2)
        assertThat(store.getIntentForBotAtTurn("BotA", 1, 3)).isNull()
        assertThat(store.getIntentForBotAtTurn("BotX", 1, 1)).isNull()
    }

    @Tag("Unit")
    @Test
    fun `getAllIntents returns grouped snapshot`() {
        store.add(capturedIntent("BotA", round = 1, turn = 1))
        store.add(capturedIntent("BotB", round = 1, turn = 1))
        store.add(capturedIntent("BotA", round = 1, turn = 2))

        val all = store.getAllIntents()
        assertThat(all).hasSize(2)
        assertThat(all["BotA"]).hasSize(2)
        assertThat(all["BotB"]).hasSize(1)
    }

    @Tag("Unit")
    @Test
    fun `clear removes all intents`() {
        store.add(capturedIntent("BotA", round = 1, turn = 1))
        store.add(capturedIntent("BotB", round = 1, turn = 1))
        assertThat(store.size).isEqualTo(2)

        store.clear()

        assertThat(store.size).isEqualTo(0)
        assertThat(store.botNames()).isEmpty()
        assertThat(store.getAllIntents()).isEmpty()
    }

    @Tag("Unit")
    @Test
    fun `size counts intents across all bots`() {
        store.add(capturedIntent("BotA", round = 1, turn = 1))
        store.add(capturedIntent("BotA", round = 1, turn = 2))
        store.add(capturedIntent("BotB", round = 1, turn = 1))

        assertThat(store.size).isEqualTo(3)
    }

    private fun capturedIntent(
        botName: String,
        round: Int = 1,
        turn: Int = 1,
        firepower: Double? = null,
    ) = CapturedIntent(
        botName = botName,
        botVersion = "1.0",
        roundNumber = round,
        turnNumber = turn,
        intent = BotIntent(firepower = firepower),
    )
}
