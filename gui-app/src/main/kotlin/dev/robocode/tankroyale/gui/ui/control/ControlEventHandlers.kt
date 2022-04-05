package dev.robocode.tankroyale.gui.ui.control

import dev.robocode.tankroyale.gui.client.Client

object ControlEventHandlers {
    init {
        ControlEvents.apply {

            onStop.enqueue(ControlPanel) {
                Client.stopGame()
            }

            onRestart.enqueue(ControlPanel) {
                Client.restartGame()
            }

            onPauseResume.enqueue(ControlPanel) {
                Client.apply {
                    if (isGamePaused) {
                        resumeGame()
                    } else {
                        pauseGame()
                    }
                }
            }
        }
    }
}