package dev.robocode.tankroyale.gui.ui.arena

import dev.robocode.tankroyale.gui.client.ClientEvents
import dev.robocode.tankroyale.gui.model.BotState
import dev.robocode.tankroyale.gui.model.Participant
import dev.robocode.tankroyale.gui.ui.Strings
import dev.robocode.tankroyale.gui.ui.components.ConsoleFrame

class BotConsoleFrame(var bot: Participant, frameCounter: Int = 0) :
    ConsoleFrame(bot.displayName, isTitlePropertyName = false) {

    private var numberOfRounds: Int = 0

    init {
        setLocation(10, 10 + frameCounter * 50) // increment y for each bot console frame
        setSize(600, 400)

        subscribeToEvents()
    }

    private fun subscribeToEvents() {
        ClientEvents.onGameStarted.subscribe(this) { gameStartedEvent ->
            numberOfRounds = gameStartedEvent.gameSetup.numberOfRounds

            if (gameStartedEvent.participants.any { it.displayName == bot.displayName }) {
                subscribeToEvents()
            }
        }
        ClientEvents.onRoundStarted.subscribe(this) {
            updateRoundInfo(it.roundNumber)
        }
        ClientEvents.onTickEvent.subscribe(this) { tickEvent ->
            run {
                val botStates = tickEvent.botStates.filter { it.id == bot.id }
                if (botStates.isNotEmpty()) {
                    updateBotState(botStates[0], tickEvent.turnNumber)
                }
            }
        }
        ClientEvents.onGameEnded.subscribe(this) {
            appendText("> ${Strings.get("bot_console.game_has_ended")}", "info")
            unsubscribeEvents()
        }
        ClientEvents.onGameAborted.subscribe(this) {
            appendText("> ${Strings.get("bot_console.game_was_aborted")}", "info")
            unsubscribeEvents()
        }
    }

    private fun unsubscribeEvents() {
        ClientEvents.onRoundStarted.unsubscribe(this)
        ClientEvents.onTickEvent.unsubscribe(this)
        ClientEvents.onGameAborted.unsubscribe(this)
        ClientEvents.onGameEnded.unsubscribe(this)
    }

    private fun updateRoundInfo(roundNumber: Int) {
        var roundInfo = "${Strings.get("round")}: $roundNumber"
        if (numberOfRounds > 0) {
            roundInfo += "/$numberOfRounds"
        }

        appendText("""
            --------------------<br>
            $roundInfo<br>
            --------------------<br>
        """.trimIndent(), "info"
        )
    }

    private fun updateBotState(botState: BotState, turnNumber: Int) {
        appendText(botState.stdOut, turnNumber = turnNumber)
        appendText(botState.stdErr, "error", turnNumber)
    }

    private fun appendText(text: String?, cssClass: String? = null, turnNumber: Int? = null) {
        var html = text
        if (html != null) {
            html = html.replace("\\n", "<br>")
            if (cssClass != null) {
                html = "<span class=\"$cssClass\">$html</span>"
            }
            if (turnNumber != null) {
                html = "<span class=\"linenumber\">$turnNumber:</span> $html"
            }
            super.append(html)
        }
    }
}
