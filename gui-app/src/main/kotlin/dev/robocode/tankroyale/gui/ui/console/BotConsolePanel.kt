package dev.robocode.tankroyale.gui.ui.console

import dev.robocode.tankroyale.gui.client.Client
import dev.robocode.tankroyale.gui.client.ClientEvents
import dev.robocode.tankroyale.gui.model.BotDeathEvent
import dev.robocode.tankroyale.gui.model.Participant
import dev.robocode.tankroyale.gui.ui.Strings
import javax.swing.JPanel

class BotConsolePanel(val bot: Participant) : ConsolePanel() {

    override val buttonPanel
        get() = JPanel().apply {
            add(okButton)
            add(clearButton)
            add(copyToClipboardButton)
        }

    private var numberOfRounds: Int = 0

    init {
        subscribeToEvents()
        printInitialStdOutput()
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
            if (tickEvent.events.any { it is BotDeathEvent && it.victimId == bot.id }) {
                appendText("> ${Strings.get("bot_console.bot_died")}", "info", tickEvent.turnNumber)
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
        ClientEvents.onStdOutputUpdated.subscribe(this) { tickEvent ->
            updateBotState(tickEvent.roundNumber, tickEvent.turnNumber)
        }
    }

    private fun unsubscribeEvents() {
        ClientEvents.onRoundStarted.unsubscribe(this)
        ClientEvents.onTickEvent.unsubscribe(this)
        ClientEvents.onGameAborted.unsubscribe(this)
        ClientEvents.onGameEnded.unsubscribe(this)
    }

    private fun printInitialStdOutput() {
        Client.getStandardOutput(bot.id)?.entries?.forEach { (round, map) ->
            updateRoundInfo(round)
            map.entries.forEach { (turn, output) ->
                appendText(output, null, turn)
            }
        }
        Client.getStandardError(bot.id)?.values?.forEach { turns ->
            turns.forEach { (turn, error) ->
                appendText(error, "error", turn)
            }
        }
    }

    private fun updateBotState(roundNumber: Int, turnNumber: Int) {
        Client.getStandardOutput(bot.id)?.get(roundNumber)?.get(turnNumber)?.let { output ->
            appendText(output, null, turnNumber)
        }
        Client.getStandardError(bot.id)?.get(roundNumber)?.get(turnNumber)?.let { error ->
            appendText(error, "error", turnNumber)
        }
    }

    private fun updateRoundInfo(roundNumber: Int) {
        var roundInfo = "${Strings.get("round")}: $roundNumber"
        if (numberOfRounds > 0) {
            roundInfo += "/$numberOfRounds"
        }

        appendText("""
            --------------------
            $roundInfo
            --------------------
        """.trimIndent(), "info"
        )
    }

    private fun appendText(text: String?, cssClass: String? = null, turnNumber: Int? = null) {
        var html = text
        if (html != null) {
            html = html
                .replace("\\n", "<br>")
                .replace("\\t", "&#9;")
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