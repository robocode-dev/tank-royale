package dev.robocode.tankroyale.gui.ui.console

import dev.robocode.tankroyale.client.model.Participant
import dev.robocode.tankroyale.client.model.TickEvent
import dev.robocode.tankroyale.gui.client.ClientEvents
import dev.robocode.tankroyale.gui.ui.Strings

abstract class BaseBotConsolePanel(val bot: Participant) : ConsolePanel() {

    private var numberOfRounds: Int = 0

    init {
        updateRoundInfo(1)
        subscribeToEvents()
    }

    private fun subscribeToEvents() {
        ClientEvents.apply {
            onGameStarted.subscribe(this@BaseBotConsolePanel) { gameStartedEvent ->
                numberOfRounds = gameStartedEvent.gameSetup.numberOfRounds
            }
            onRoundStarted.subscribe(this@BaseBotConsolePanel) {
                updateRoundInfo(it.roundNumber)
            }
            onSeekToTurn.subscribe(this@BaseBotConsolePanel) {
                informAboutSeek(it)
            }
        }
    }

    private fun informAboutSeek(tickEvent: TickEvent) {
        val text = Strings.get("bot_console.seek_to_turn").format(tickEvent.roundNumber, tickEvent.turnNumber)
        banner(text)
    }

    private fun updateRoundInfo(roundNumber: Int) {
        var roundInfo = "${Strings.get("round")}: $roundNumber"
        if (numberOfRounds > 0) {
            roundInfo += "/$numberOfRounds"
        }
        banner(roundInfo)
    }

    private fun banner(text: String) {
        appendBanner(
            """
            --------------------
            $text
            --------------------
        """.trimIndent()
        )
    }
}