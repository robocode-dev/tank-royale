package dev.robocode.tankroyale.gui.ui.console

import dev.robocode.tankroyale.gui.client.Client
import dev.robocode.tankroyale.gui.client.ClientEvents
import dev.robocode.tankroyale.gui.model.BotDeathEvent
import dev.robocode.tankroyale.gui.model.Participant
import dev.robocode.tankroyale.gui.ui.Strings

class BotConsolePanel(val bot: Participant) : ConsolePanel() {

    private var numberOfRounds: Int = 0

    init {
        subscribeToEvents()
        printInitialStdOutput()
    }

    private fun subscribeToEvents() {
        ClientEvents.apply {
            onGameStarted.subscribe(this@BotConsolePanel) { gameStartedEvent ->
                numberOfRounds = gameStartedEvent.gameSetup.numberOfRounds

                if (gameStartedEvent.participants.any { it.displayName == bot.displayName }) {
                    subscribeToEvents()
                }
            }
            onRoundStarted.subscribe(this@BotConsolePanel) {
                updateRoundInfo(it.roundNumber)
            }
            onTickEvent.subscribe(this@BotConsolePanel) { tickEvent ->
                if (tickEvent.events.any { it is BotDeathEvent && it.victimId == bot.id }) {
                    append("> ${Strings.get("bot_console.bot_died")}", "info", tickEvent.turnNumber)
                }
            }
            onGameEnded.subscribe(this@BotConsolePanel) {
                append("> ${Strings.get("bot_console.game_has_ended")}", "info")
                unsubscribeEvents()
            }
            onGameAborted.subscribe(this@BotConsolePanel) {
                append("> ${Strings.get("bot_console.game_was_aborted")}", "info")
                unsubscribeEvents()
            }
            onStdOutputUpdated.subscribe(this@BotConsolePanel) { tickEvent ->
                updateBotState(tickEvent.roundNumber, tickEvent.turnNumber)
            }
        }
    }

    private fun unsubscribeEvents() {
        ClientEvents.apply {
            onRoundStarted.unsubscribe(this@BotConsolePanel)
            onTickEvent.unsubscribe(this@BotConsolePanel)
            onGameAborted.unsubscribe(this@BotConsolePanel)
            onGameEnded.unsubscribe(this@BotConsolePanel)
        }
    }

    private fun printInitialStdOutput() {
        Client.getStandardOutput(bot.id)?.entries?.forEach { (round, map) ->
            updateRoundInfo(round)
            map.entries.toSet().forEach { (turn, output) ->
                append(output, null, turn)
            }
        }
        Client.getStandardError(bot.id)?.values?.forEach { turns ->
            turns.forEach { (turn, error) ->
                append(error, "error", turn)
            }
        }
    }

    private fun updateBotState(roundNumber: Int, turnNumber: Int) {
        Client.getStandardOutput(bot.id)?.get(roundNumber)?.get(turnNumber)?.let { output ->
            append(output, null, turnNumber)
        }
        Client.getStandardError(bot.id)?.get(roundNumber)?.get(turnNumber)?.let { error ->
            append(error, "error", turnNumber)
        }
    }

    private fun updateRoundInfo(roundNumber: Int) {
        var roundInfo = "${Strings.get("round")}: $roundNumber"
        if (numberOfRounds > 0) {
            roundInfo += "/$numberOfRounds"
        }

        append("""
            --------------------
            $roundInfo
            --------------------
        """.trimIndent(), "info"
        )
    }
}