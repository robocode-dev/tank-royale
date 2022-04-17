package dev.robocode.tankroyale.gui.ui.control

import dev.robocode.tankroyale.gui.client.Client

object ControlEventHandlers {
    init {
        ControlEvents.apply {

            onStop.enqueue(this) {
                Client.stopGame()
            }

            onRestart.enqueue(this) {
                Client.restartGame()
            }

            onPauseResume.enqueue(this) {
                Client.apply {
                    if (isGamePaused()) {
                        resumeGame()
                    } else {
                        pauseGame()
                    }
                }
            }

            onNextTurn.enqueue(this) {
                Client.doNextTurn()
            }
        }
    }
}