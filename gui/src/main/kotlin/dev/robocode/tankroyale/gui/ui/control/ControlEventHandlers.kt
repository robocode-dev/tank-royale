package dev.robocode.tankroyale.gui.ui.control

import dev.robocode.tankroyale.client.model.TpsChangedEvent
import dev.robocode.tankroyale.gui.client.Client
import dev.robocode.tankroyale.gui.settings.ConfigSettings.DEFAULT_TPS
import dev.robocode.tankroyale.gui.ui.Messages
import dev.robocode.tankroyale.gui.ui.tps.TpsEvents
import dev.robocode.tankroyale.gui.ui.tps.TpsField
import dev.robocode.tankroyale.gui.util.MessageDialog
import dev.robocode.tankroyale.gui.util.enqueue

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
                        if (TpsField.tps == 0) {
                            if (MessageDialog.showConfirm(
                                    String.format(Messages.get("resume_at_tps_zero"), DEFAULT_TPS)
                                )) {
                                TpsEvents.onTpsChanged(TpsChangedEvent(DEFAULT_TPS))
                            }
                        } else {
                            resumeGame()
                        }
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