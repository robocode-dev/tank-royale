package dev.robocode.tankroyale.gui.ui.console

import dev.robocode.tankroyale.gui.client.Client
import dev.robocode.tankroyale.gui.client.ClientEvents
import dev.robocode.tankroyale.gui.model.BotDeathEvent
import dev.robocode.tankroyale.gui.model.Participant
import dev.robocode.tankroyale.gui.ui.Strings

class BotConsolePanel(bot: Participant) : BaseBotConsolePanel(bot) {

    init {
        subscribeToEvents()
        printInitialStdOutput()
    }

    private fun subscribeToEvents() {
        ClientEvents.apply {
            onStdOutputUpdated.subscribe(this@BotConsolePanel) { tickEvent ->
                updateStandardOutput(tickEvent.roundNumber, tickEvent.turnNumber)
            }
            onTickEvent.subscribe(this@BotConsolePanel) { tickEvent ->
                if (tickEvent.events.any { it is BotDeathEvent && it.victimId == bot.id }) {
                    appendInfo(Strings.get("bot_console.bot_died"), tickEvent.turnNumber)
                }
            }
            onGameEnded.subscribe(this@BotConsolePanel) {
                appendInfo(Strings.get("bot_console.game_has_ended"))
            }
            onGameAborted.subscribe(this@BotConsolePanel) {
                appendInfo(Strings.get("bot_console.game_was_aborted"))
            }
        }
    }

    private fun printInitialStdOutput() {
        // Create a thread-safe copy of entries before iteration
        Client.getStandardOutput(bot.id)?.entries?.toList()?.forEach { (_, map) ->
            // Create a snapshot of the inner map entries as well
            map.entries.toList().forEach { (turn, output) ->
                append(output, turn)
            }
        }

        Client.getStandardError(bot.id)?.values?.toList()?.forEach { turns ->
            turns.toList().forEach { (turn, error) ->
                appendError(error, turn)
            }
        }
    }

    private fun updateStandardOutput(roundNumber: Int, turnNumber: Int) {
        Client.getStandardOutput(bot.id)?.get(roundNumber)?.get(turnNumber)
            ?.let { output -> append(output, turnNumber) }

        Client.getStandardError(bot.id)?.get(roundNumber)?.get(turnNumber)
            ?.let { error -> appendError(error, turnNumber) }
    }
}