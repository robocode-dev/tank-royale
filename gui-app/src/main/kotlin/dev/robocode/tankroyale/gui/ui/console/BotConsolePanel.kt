package dev.robocode.tankroyale.gui.ui.console

import dev.robocode.tankroyale.gui.client.Client
import dev.robocode.tankroyale.gui.client.ClientEvents
import dev.robocode.tankroyale.gui.model.BotDeathEvent
import dev.robocode.tankroyale.gui.model.Participant
import dev.robocode.tankroyale.gui.ui.Strings
import javax.swing.JPanel

class BotConsolePanel(val bot: Participant) : ConsolePanel() {

    override val buttonPanel get() = JPanel().apply {
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
            run {
                val botStates = tickEvent.botStates.filter { it.id == bot.id }
                if (botStates.isNotEmpty()) {
                    updateBotState(bot.id, tickEvent.turnNumber)
                }
                if (tickEvent.events.any { it is BotDeathEvent && it.victimId == bot.id }) {
                    appendText("> ${Strings.get("bot_console.bot_died")}", "info", tickEvent.turnNumber)
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

    private fun printInitialStdOutput() {
        Client.getStandardOutput(bot.id)?.entries?.forEach { (turn, text) ->
            appendText(text, null,  turn)
        }
        Client.getStandardError(bot.id)?.entries?.forEach { (turn, text) ->
            appendText(text, "error",  turn)
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

    private fun updateBotState(botId: Int, turnNumber: Int) {
        val output = Client.getStandardOutput(botId)?.get(botId)
        val error = Client.getStandardError(botId)?.get(botId)

        appendText(output, null, turnNumber)
        appendText(error, "error", turnNumber)
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