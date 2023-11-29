package dev.robocode.tankroyale.gui.ui.console

import dev.robocode.tankroyale.gui.client.ClientEvents
import dev.robocode.tankroyale.gui.model.Participant
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
        }
    }

    private fun updateRoundInfo(roundNumber: Int) {
        var roundInfo = "${Strings.get("round")}: $roundNumber"
        if (numberOfRounds > 0) {
            roundInfo += "/$numberOfRounds"
        }

        appendBanner("""
            --------------------
            $roundInfo
            --------------------
        """.trimIndent()
        )
    }
}