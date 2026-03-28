package dev.robocode.tankroyale.server.core

import org.slf4j.LoggerFactory

/** Manager for controlling the game lifecycle. */
class GameLifecycleManager {

    private val log = LoggerFactory.getLogger(this::class.java)

    /** Current server state */
    @Volatile
    var serverState = ServerState.WAIT_FOR_PARTICIPANTS_TO_JOIN

    /** Lock for starting the game */
    val startGameLock = Any()

    /** Timer for 'ready' timeout */
    @Volatile
    var readyTimeoutTimer: ResettableTimer? = null

    /** Timer for 'turn' timeout */
    @Volatile
    var turnTimeoutTimer: ResettableTimer? = null

    fun stopTimers() {
        readyTimeoutTimer?.shutdown()
        readyTimeoutTimer = null
        turnTimeoutTimer?.shutdown()
        turnTimeoutTimer = null
    }

    fun startReadyTimer(readyTimeoutNanos: Long, onTimeout: () -> Unit) {
        synchronized(startGameLock) {
            if (readyTimeoutTimer == null) {
                readyTimeoutTimer = ResettableTimer(onTimeout)
            }
            readyTimeoutTimer?.schedule(
                minDelayNanos = 0L,
                maxDelayNanos = readyTimeoutNanos
            )
        }
    }

    fun createTurnTimeoutTimer(onNextTurn: () -> Unit) {
        // Create timer ONCE per game start, not per turn (fixes memory leak)
        turnTimeoutTimer = ResettableTimer(onNextTurn)
    }

    fun pauseGame() {
        if (serverState === ServerState.GAME_RUNNING) {
            log.info("Pausing game")
            serverState = ServerState.GAME_PAUSED
            turnTimeoutTimer?.pause()
        }
    }

    fun resumeGame() {
        if (serverState === ServerState.GAME_PAUSED) {
            log.info("Resuming game")
            serverState = ServerState.GAME_RUNNING
            turnTimeoutTimer?.resume()
        }
    }

    fun isGameRunningOrPaused() = serverState === ServerState.GAME_RUNNING || serverState === ServerState.GAME_PAUSED
}
