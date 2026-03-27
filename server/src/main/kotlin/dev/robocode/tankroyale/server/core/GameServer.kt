package dev.robocode.tankroyale.server.core

import com.google.gson.Gson
import dev.robocode.tankroyale.schema.*
import dev.robocode.tankroyale.schema.GameSetup
import dev.robocode.tankroyale.server.connection.ConnectionHandler
import dev.robocode.tankroyale.server.connection.GameServerConnectionListener
import dev.robocode.tankroyale.server.mapper.*
import dev.robocode.tankroyale.server.model.*
import dev.robocode.tankroyale.server.model.InitialPosition
import org.java_websocket.WebSocket
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap


/**
 * Game server responsible for managing the full lifecycle of a Tank Royale game session.
 *
 * ## Threading contract
 * - The WebSocket I/O thread calls all `handle*` methods via [GameServerConnectionListener].
 * - [onNextTurn] runs on the turn-timeout timer thread and is the only place the game model is
 *   advanced; it acquires [tickLock] before touching [botIntents], [botsThatSentIntent], or
 *   [modelUpdater].
 * - [handleBotIntent] is called concurrently from WebSocket threads and also acquires [tickLock]
 *   to merge intent updates safely.
 * - All other `handle*` methods are called from the WebSocket thread and do not hold [tickLock].
 * - [modelUpdater] is marked `@Volatile` so its nullability is visible across threads without
 *   requiring [tickLock].
 */
class GameServer(private val config: ServerConfig) {
    /** JSON handler */
    private val gson = Gson()

    /** Connection handler for observers and bots */
    private val connectionHandler =
        ConnectionHandler(ServerSetup(config.gameTypes), GameServerConnectionListener(this), config.controllerSecrets, config.botSecrets)

    /** Registry for tracking game participants (bots). */
    private val participantRegistry = ParticipantRegistry(connectionHandler)

    /** Manager for controlling the game lifecycle. */
    private val lifecycleManager = GameLifecycleManager()

    /** Broadcaster for sending messages to bots and observers. */
    private val broadcaster = MessageBroadcaster(connectionHandler, gson)

    /** Builder for result objects sent at game/round end. */
    private val resultsBuilder = ResultsBuilder({ modelUpdater }, participantRegistry)

    /** Current game setup */
    private lateinit var gameSetup: dev.robocode.tankroyale.server.model.GameSetup

    /** Map over bot intents: bot connection -> bot intent */
    private val botIntents = ConcurrentHashMap<WebSocket, dev.robocode.tankroyale.server.model.BotIntent>()

    /** Model updater that keeps track of the game state/model */
    @Volatile
    private var modelUpdater: ModelUpdater? = null

    /** Current TPS setting (Turns Per Second) */
    @Volatile
    private var tps = config.tps

    /** Timestamp when the current turn started (for calculating bot processing duration) */
    @Volatile
    private var turnStartTimeNanos = 0L

    /** Logger */
    private val log = LoggerFactory.getLogger(this::class.java)

    /** Tick lock for onNextTurn() */
    private val tickLock = Any()

    /** Map over bots that sent their intent this turn */
    private val botsThatSentIntent = ConcurrentHashMap.newKeySet<WebSocket>()

    /** Starts this server */
    fun start() {
        log.info("Starting server on port ${config.port} with supporting game type(s): ${config.gameTypes.joinToString()}")
        connectionHandler.start()
    }

    /** Stops this server */
    fun stop() {
        log.info("Stopping server")
        lifecycleManager.stopTimers()
        connectionHandler.stop()
    }

    /** Prepares the game and wait for participants to become 'ready' */
    private fun prepareGame() {
        log.debug("Preparing game")

        lifecycleManager.serverState = ServerState.WAIT_FOR_READY_PARTICIPANTS

        participantRegistry.clear()
        botIntents.clear()
        botsThatSentIntent.clear()

        modelUpdater = null

        lifecycleManager.stopTimers()

        participantRegistry.prepareParticipantIds()
        prepareModelUpdater()
        sendGameStartedToParticipants()
        startReadyTimer()
    }

    /** Starts the game if all participants are ready */
    private fun startGameIfParticipantsReady() {
        var timerToShutdown: ResettableTimer? = null
        synchronized(lifecycleManager.startGameLock) {
            val currentParticipantSize = participantRegistry.participants.size
            val currentReadyParticipantSize = participantRegistry.readyParticipants.size

            if (currentReadyParticipantSize == currentParticipantSize && currentParticipantSize > 0) {
                if (lifecycleManager.serverState != ServerState.WAIT_FOR_READY_PARTICIPANTS) return
                timerToShutdown = lifecycleManager.readyTimeoutTimer
                lifecycleManager.readyTimeoutTimer = null

                startGame()
            }
        }
        timerToShutdown?.shutdown()
    }

    /** Send game-started event to all participant bots to get them started */
    private fun sendGameStartedToParticipants() {
        val gameSetup = GameSetupMapper.map(gameSetup)
        val botHandshakes = connectionHandler.getBotHandshakes()

        participantRegistry.participantIds.forEach { (conn, botId) ->
            val teamId = botHandshakes[conn]?.teamId
            val gameStartedForBot = createGameStartedEventForBot(botId, teamId, gameSetup)
            broadcaster.send(conn, gameStartedForBot)
        }
    }

    private fun getTeammateIds(botId: BotId, teamId: Int?): Set<BotId> =
        teamId?.let { getParticipantTeamIds().filterValues { it == teamId }.keys.toSet().minus(botId) }
            ?: emptySet()

    private fun getParticipantTeamIds(): Map<BotId, Int?> = participantRegistry.participantIds
        .mapNotNull { (conn, botId) -> connectionHandler.getBotHandshakes()[conn]?.teamId?.let { botId to it } }
        .associateBy({ it.first }, { it.second })

    /** Creates a GameStartedEventForBot with current game setup */
    private fun createGameStartedEventForBot(botId: BotId, teamId: Int?, gameSetup: GameSetup) =
        GameStartedEventForBot().also { event ->
            event.type = Message.Type.GAME_STARTED_EVENT_FOR_BOT
            event.myId = botId.value
            event.teammateIds = getTeammateIds(botId, teamId).map { it.value }
            event.gameSetup = gameSetup

            val initialPositions = requireNotNull(modelUpdater) { "modelUpdater is null" }.getBotInitialPositions()
            initialPositions[botId]?.let { pos ->
                event.startX = pos.x
                event.startY = pos.y
            }
            requireNotNull(modelUpdater) { "modelUpdater is null" }.getBot(botId)?.let { bot ->
                event.startDirection = bot.direction
            }
        }

    /** Starts the 'ready' timer */
    private fun startReadyTimer() {
        lifecycleManager.startReadyTimer(gameSetup.readyTimeout.inWholeNanoseconds) { onReadyTimeout() }
    }

    /** Starts a new game */
    private fun startGame() {
        log.info("Starting game")
        participantRegistry.clearReadyParticipants()
        participantRegistry.populateParticipantMap()

        lifecycleManager.serverState = ServerState.GAME_RUNNING

        sendGameStartedToObservers()
        prepareModelUpdater()

        lifecycleManager.createTurnTimeoutTimer { onNextTurn() }
        resetTurnTimeout()
    }

    /** Send GameStarted to all participant observers to get them started */
    private fun sendGameStartedToObservers() {
        broadcaster.broadcastToObserverAndControllers(GameStartedEventForObserver().also {
            it.type = Message.Type.GAME_STARTED_EVENT_FOR_OBSERVER
            it.gameSetup = GameSetupMapper.map(gameSetup)
            it.participants = participantRegistry.participantMap.values.toList()
        })
    }

    private fun prepareModelUpdater() {
        val botHandshakes = connectionHandler.getBotHandshakes()
        val participantIds = mutableSetOf<ParticipantId>()
        val initialPositions = mutableMapOf<BotId, InitialPosition>()
        val droidFlags = mutableMapOf<BotId, Boolean>()

        participantRegistry.participants.forEach { conn ->
            val botId = participantRegistry.participantIds[conn]!!
            val handshake = botHandshakes[conn]!!
            val teamId = handshake.teamId?.let { TeamId(it) }

            participantIds += ParticipantId(botId, teamId)
            InitialPositionMapper.map(handshake.initialPosition)?.let { initialPositions[botId] = it }
            droidFlags[botId] = handshake.isDroid
        }

        modelUpdater = ModelUpdater(
            gameSetup,
            participantIds,
            initialPositions,
            droidFlags,
            config.initialPositionEnabled
        )
    }

    private fun resetTurnTimeout() {
        lifecycleManager.turnTimeoutTimer?.schedule(
            minDelayNanos = 0L,
            maxDelayNanos = gameSetup.turnTimeout.inWholeNanoseconds
        )
        turnStartTimeNanos = System.nanoTime()
    }

    private fun applyVisualDelay(botProcessingDurationNanos: Long) {
        if (tps <= 0) return

        val turnDurationNanos = 1_000_000_000L / tps
        val sleepNanos = turnDurationNanos - botProcessingDurationNanos
        if (sleepNanos > 0) {
            Thread.sleep(sleepNanos / 1_000_000, (sleepNanos % 1_000_000).toInt())
        }
    }

    // Must be called while holding tickLock.
    private fun updateGameState(): GameState {
        val botIntentsSnapshot = botIntents.mapNotNull { (key, value) ->
            participantRegistry.participantIds[key]?.let { botId ->
                botId to dev.robocode.tankroyale.server.model.BotIntent().apply {
                    update(value)
                }
            }
        }.toMap()

        return modelUpdater?.update(botIntentsSnapshot)
            ?: throw IllegalStateException("Model updater is null when trying to update game state")
    }

    private fun onReadyTimeout() {
        log.debug("Ready timeout")
        var timerToShutdown: ResettableTimer? = null
        synchronized(lifecycleManager.startGameLock) {
            if (lifecycleManager.serverState !== ServerState.WAIT_FOR_READY_PARTICIPANTS) return

            timerToShutdown = lifecycleManager.readyTimeoutTimer
            lifecycleManager.readyTimeoutTimer = null

            if (participantRegistry.readyParticipants.size >= gameSetup.minNumberOfParticipants) {
                log.warn("Starting game with ${participantRegistry.readyParticipants.size}/${participantRegistry.participants.size} participants ready because of timeout")
                participantRegistry.removeNonReadyParticipants()
                startGame()
            } else {
                log.warn("Aborting the game as only ${participantRegistry.readyParticipants.size}/${participantRegistry.participants.size} participants are ready")
                lifecycleManager.serverState = ServerState.WAIT_FOR_PARTICIPANTS_TO_JOIN
                broadcastGameAborted()
            }
        }
        timerToShutdown?.shutdown()
    }

    private fun onNextTurn() {
        if (lifecycleManager.serverState !== ServerState.GAME_RUNNING) return

        val botProcessingDurationNanos = System.nanoTime() - turnStartTimeNanos

        synchronized(tickLock) {
            updateGameState().apply {
                onNextTick(lastRound)

                if (isGameEnded) {
                    onGameEnded()
                }
            }
            botsThatSentIntent.clear()
        }

        resetTurnTimeout()
        applyVisualDelay(botProcessingDurationNanos)
    }

    private fun onGameEnded() {
        log.info("Game ended")

        broadcastGameEndedToParticipants()
        broadcastGameEndedToObservers()

        synchronized(lifecycleManager.startGameLock) {
            lifecycleManager.serverState = ServerState.GAME_STOPPED
            cleanupAfterGameStopped()
        }
    }

    private fun onNextTick(lastRound: MutableRound?) {
        lastRound?.apply {
            lastTurn?.apply {
                if (turnNumber == 1) {
                    log.debug("Round started: $roundNumber")
                    botIntents.clear()
                    transferDebugGraphicsFlagToModel()
                    broadcastRoundStartedToAll(roundNumber)
                } else {
                    checkForSkippedTurns(turnNumber)
                    botIntents.clear()
                }
                sendTickToParticipants(roundNumber, this)
                broadcastGameTickToObservers(roundNumber, this)

                if (roundEnded) {
                    log.debug("Round ended: $roundNumber")
                    broadcastRoundEndedToParticipants(roundNumber, turnNumber)
                    broadcastRoundEndedToObservers(roundNumber, turnNumber)
                }
            }
        }
    }

    private fun broadcastGameEndedToParticipants() {
        participantRegistry.participants.forEach { conn ->
            participantRegistry.participantIds[conn]?.let { botId ->
                GameEndedEventForBot().also { event ->
                    event.type = Message.Type.GAME_ENDED_EVENT_FOR_BOT
                    event.numberOfRounds = modelUpdater!!.numberOfRounds
                    event.results = getResultsForBot(botId)

                    broadcaster.send(conn, event)
                }
            }
        }
    }

    private fun broadcastGameEndedToObservers() {
        broadcaster.broadcastToObserverAndControllers(GameEndedEventForObserver().also {
            it.type = Message.Type.GAME_ENDED_EVENT_FOR_OBSERVER
            it.numberOfRounds = modelUpdater!!.numberOfRounds
            it.results = getResultsForObservers()
        })
    }

    private fun broadcastRoundStartedToAll(roundNumber: Int) {
        broadcaster.broadcastToAll(RoundStartedEvent().also {
            it.type = Message.Type.ROUND_STARTED_EVENT
            it.roundNumber = roundNumber
        }, participantRegistry.participants)
    }

    private fun broadcastRoundEndedToParticipants(roundNumber: Int, turnNumber: Int) {
        participantRegistry.participants.forEach { conn ->
            participantRegistry.participantIds[conn]?.let { botId ->
                RoundEndedEventForBot().also { event ->
                    event.type = Message.Type.ROUND_ENDED_EVENT_FOR_BOT
                    event.roundNumber = roundNumber
                    event.turnNumber = turnNumber
                    event.results = getResultsForBot(botId)

                    broadcaster.send(conn, event)
                }
            }
        }
    }

    private fun broadcastRoundEndedToObservers(roundNumber: Int, turnNumber: Int) {
        broadcaster.broadcastToObserverAndControllers(RoundEndedEventForObserver().also {
            it.type = Message.Type.ROUND_ENDED_EVENT_FOR_OBSERVER
            it.roundNumber = roundNumber
            it.turnNumber = turnNumber
            it.results = getResultsForObservers()
        })
    }

    private fun sendTickToParticipants(roundNumber: Int, turn: ITurn) {
        val updater = modelUpdater ?: return
        val aliveBotTeamIds = aliveBotToTeamIdMap(updater)

        for (conn in participantRegistry.participants) {
            val participantId = participantRegistry.participantIds[conn] ?: continue
            if (updater.isAlive(participantId) == false && turn.getEvents(participantId).isEmpty()) continue

            val teamId = aliveBotTeamIds[participantId]
            val enemyCount = aliveBotTeamIds.filterValues { it != teamId }.count()

            val event = TurnToTickEventForBotMapper.map(roundNumber, turn, participantId, enemyCount) ?: continue
            broadcaster.send(conn, event)
        }
    }

    private fun aliveBotToTeamIdMap(updater: ModelUpdater? = modelUpdater): Map<BotId, Int> {
        val currentUpdater = updater ?: return emptyMap()
        return participantRegistry.participantMap.filterKeys { botId -> currentUpdater.isAlive(botId) == true }.mapValues { (botId, participant) ->
            participant.teamId ?: -botId.value
        }
    }

    private fun broadcastGameTickToObservers(roundNumber: Int, turn: ITurn) {
        val enemyCountMap = HashMap<BotId, Int /* enemyCount */>()

        val aliveBotTeamIds = aliveBotToTeamIdMap()

        participantRegistry.participantMap.keys.forEach { botId ->
            val teamId = aliveBotTeamIds[botId]
            enemyCountMap[botId] = aliveBotTeamIds.filterValues { it != teamId }.count()
        }

        broadcaster.broadcastToObserverAndControllers(
            TurnToTickEventForObserverMapper
                .map(roundNumber, turn, participantRegistry.participantMap, enemyCountMap, participantRegistry.debugGraphicsEnableMap)
        )
    }

    private fun checkForSkippedTurns(currentTurnNumber: Int) {
        val botsSkippingTurn = getParticipantsThatSkippedTurn()

        if (botsSkippingTurn.isNotEmpty()) {
            val skippedTurn = SkippedTurnEvent().also {
                it.type = Message.Type.SKIPPED_TURN_EVENT
                it.turnNumber = currentTurnNumber - 1 // last turn number
            }
            val json = gson.toJson(skippedTurn)

            botsSkippingTurn.forEach { bot -> connectionHandler.send(bot, json) }
        }
    }

    private fun getParticipantsThatSkippedTurn(): Collection<WebSocket> =
        mutableListOf<WebSocket>().apply {
            participantRegistry.participants.forEach { participant ->
                participantRegistry.participantIds[participant]?.let {
                    if (modelUpdater?.isAlive(it) == true && botIntents[participant] == null) {
                        this += participant
                    }
                }
            }
        }

    internal fun sendBotListUpdate(conn: WebSocket) {
        broadcaster.sendBotListUpdate(conn)
    }

    /**
     * Called by [GameServerConnectionListener] on the WebSocket thread when a bot connects and completes its handshake.
     * Precondition: the bot has already been registered in [connectionHandler].
     */
    internal fun handleBotJoined() {
        broadcaster.updateBotListUpdateMessage()
        broadcaster.broadcastBotListUpdate()
    }

    /**
     * Called by [GameServerConnectionListener] on the WebSocket thread when a bot disconnects.
     * Aborts the game if the last participant leaves while a game is running or paused.
     * @param conn the WebSocket connection of the bot that left.
     */
    internal fun handleBotLeft(conn: WebSocket) {
        val shouldAbortGame = synchronized(participantRegistry.participantsLock) {
            val wasRemoved = participantRegistry.removeParticipant(conn)
            wasRemoved && participantRegistry.participants.isEmpty() && lifecycleManager.isGameRunningOrPaused()
        }

        if (shouldAbortGame) {
            handleAbortGame()
        } else {
            broadcaster.updateBotListUpdateMessage()
            broadcaster.broadcastBotListUpdate()

            if (lifecycleManager.serverState === ServerState.WAIT_FOR_READY_PARTICIPANTS) {
                startGameIfParticipantsReady()
            }
        }
    }

    /**
     * Called by [GameServerConnectionListener] on the WebSocket thread when a bot signals it is ready to start.
     * Ignored if the server is not in [ServerState.WAIT_FOR_READY_PARTICIPANTS].
     * @param conn the WebSocket connection of the bot that is ready.
     */
    internal fun handleBotReady(conn: WebSocket) {
        if (lifecycleManager.serverState !== ServerState.WAIT_FOR_READY_PARTICIPANTS) return

        participantRegistry.addReadyParticipant(conn)
        startGameIfParticipantsReady()
    }

    /**
     * Called by [GameServerConnectionListener] on the WebSocket thread when a bot sends a turn intent.
     * Acquires [tickLock] to safely merge the intent and track which bots have responded this turn.
     * Ignored if the game is not running or paused.
     * @param conn the WebSocket connection of the bot.
     * @param intent the bot intent received from the bot.
     */
    internal fun handleBotIntent(conn: WebSocket, intent: dev.robocode.tankroyale.schema.BotIntent) {
        if (lifecycleManager.serverState !== ServerState.GAME_RUNNING && lifecycleManager.serverState !== ServerState.GAME_PAUSED) return

        synchronized(tickLock) {
            val existingIntent = botIntents[conn]
            if (existingIntent == null) {
                botIntents[conn] = BotIntentMapper.map(intent)
            } else {
                intent.apply {
                    targetSpeed?.let { existingIntent.targetSpeed = it }
                    turnRate?.let { existingIntent.turnRate = it }
                    gunTurnRate?.let { existingIntent.gunTurnRate = it }
                    radarTurnRate?.let { existingIntent.radarTurnRate = it }
                    firepower?.let { existingIntent.firepower = it }
                    adjustGunForBodyTurn?.let { existingIntent.adjustGunForBodyTurn = it }
                    adjustRadarForBodyTurn?.let { existingIntent.adjustRadarForBodyTurn = it }
                    adjustRadarForGunTurn?.let { existingIntent.adjustRadarForGunTurn = it }
                    rescan?.let { existingIntent.rescan = it }
                    fireAssist?.let { existingIntent.fireAssist = it }
                    bodyColor?.let { existingIntent.bodyColor = it.ifBlank { null } }
                    turretColor?.let { existingIntent.turretColor = it.ifBlank { null } }
                    radarColor?.let { existingIntent.radarColor = it.ifBlank { null } }
                    bulletColor?.let { existingIntent.bulletColor = it.ifBlank { null } }
                    scanColor?.let { existingIntent.scanColor = it.ifBlank { null } }
                    tracksColor?.let { existingIntent.tracksColor = it.ifBlank { null } }
                    gunColor?.let { existingIntent.gunColor = it.ifBlank { null } }
                    stdOut?.let { existingIntent.stdOut = it.ifBlank { null } }
                    stdErr?.let { existingIntent.stdErr = it.ifBlank { null } }
                    teamMessages?.let { existingIntent.teamMessages = TeamMessageMapper.map(it) }
                    debugGraphics?.let { existingIntent.debugGraphics = it.ifBlank { null } }
                }
            }
            botsThatSentIntent += conn
        }
        checkAllBotsResponded()
    }

    private fun checkAllBotsResponded() {
        val aliveParticipants = participantRegistry.participants.filter { conn ->
            participantRegistry.participantIds[conn]?.let { botId -> modelUpdater?.isAlive(botId) == true } ?: false
        }
        if (botsThatSentIntent.containsAll(aliveParticipants)) {
            lifecycleManager.turnTimeoutTimer?.notifyReady()
        }
    }

    /**
     * Called by [GameServerConnectionListener] on the WebSocket thread when a controller requests a game start.
     * Maps the requested bot addresses to active WebSocket connections and initiates game preparation.
     * @param gameSetup the game setup configuration sent by the controller.
     * @param botAddresses the set of bot addresses that should participate.
     */
    internal fun handleStartGame(gameSetup: GameSetup, botAddresses: Collection<BotAddress>) {
        this.gameSetup = GameSetupMapper.map(gameSetup)

        val sockets = connectionHandler.mapToBotSockets(botAddresses)
        participantRegistry.setParticipants(sockets)
        if (participantRegistry.participants.isNotEmpty()) {
            prepareGame()
        }
    }

    /**
     * Called by [GameServerConnectionListener] on the WebSocket thread when a controller requests a game abort.
     * Acquires [GameLifecycleManager.startGameLock], transitions state to [ServerState.GAME_STOPPED],
     * broadcasts the abort event, and cleans up all game state.
     */
    internal fun handleAbortGame() {
        log.info("Aborting game")
        synchronized(lifecycleManager.startGameLock) {
            lifecycleManager.serverState = ServerState.GAME_STOPPED
            broadcastGameAborted()
            cleanupAfterGameStopped()
        }
    }

    /**
     * Called by [GameServerConnectionListener] on the WebSocket thread when a controller requests a game pause.
     * Broadcasts the paused event to observers if the state transitions to [ServerState.GAME_PAUSED].
     */
    internal fun handlePauseGame() {
        lifecycleManager.pauseGame()
        if (lifecycleManager.serverState === ServerState.GAME_PAUSED) {
            broadcastGamedPausedToObservers()
        }
    }

    /**
     * Called by [GameServerConnectionListener] on the WebSocket thread when a controller requests a game resume.
     * Broadcasts the resumed event to observers if the state transitions to [ServerState.GAME_RUNNING].
     */
    internal fun handleResumeGame() {
        lifecycleManager.resumeGame()
        if (lifecycleManager.serverState === ServerState.GAME_RUNNING) {
            broadcastGameResumedToObservers()
        }
    }

    /**
     * Called by [GameServerConnectionListener] on the WebSocket thread when a controller requests a single turn advance.
     * Only has effect when the game is paused: briefly resumes, executes one turn, then re-pauses.
     */
    internal fun handleNextTurn() {
        if (lifecycleManager.serverState === ServerState.GAME_PAUSED) {
            handleResumeGame()
            onNextTurn()
            handlePauseGame()
        }
    }

    /**
     * Called by [GameServerConnectionListener] on the WebSocket thread when a controller changes the TPS setting.
     * Broadcasts the new TPS to observers and controllers. A value of 0 pauses the game;
     * any positive value resumes a paused game and resets the turn timeout.
     * @param newTps the requested turns-per-second value.
     */
    internal fun handleChangeTps(newTps: Int) {
        if (tps == newTps) return
        tps = newTps

        broadcaster.broadcastToObserverAndControllers(TpsChangedEvent().also {
            it.type = Message.Type.TPS_CHANGED_EVENT
            it.tps = newTps
        })

        if (tps == 0) {
            handlePauseGame()
        } else {
            if (lifecycleManager.serverState === ServerState.GAME_PAUSED) {
                handleResumeGame()
            }
            resetTurnTimeout()
        }
    }

    /**
     * Called by [GameServerConnectionListener] on the WebSocket thread when a controller updates a bot's policy.
     * Updates the debug-graphics enable flag in both the participant registry and the live model.
     * @param botPolicyUpdate the policy update containing the bot id and the new debugging flag.
     */
    internal fun handleBotPolicyUpdate(botPolicyUpdate: BotPolicyUpdate) {
        val botId = BotId(botPolicyUpdate.botId)
        participantRegistry.setDebugGraphicsEnabled(botId, botPolicyUpdate.debuggingEnabled)
        modelUpdater?.setDebugEnabled(botId, botPolicyUpdate.debuggingEnabled)
    }

    private fun cleanupAfterGameStopped() {
        lifecycleManager.stopTimers()
        participantRegistry.clear()
        botIntents.clear()
        botsThatSentIntent.clear()
        modelUpdater = null
    }

    private fun transferDebugGraphicsFlagToModel() {
        participantRegistry.debugGraphicsEnableMap.forEach { (botId, isEnabled) ->
            modelUpdater?.setDebugEnabled(botId, isEnabled)
        }
    }

    private fun broadcastGameAborted() {
        broadcaster.broadcastToObserverAndControllers(GameAbortedEvent().also {
            it.type = Message.Type.GAME_ABORTED_EVENT
        })
    }

    private fun broadcastGamedPausedToObservers() {
        broadcaster.broadcastToObserverAndControllers(GamePausedEventForObserver().also {
            it.type = Message.Type.GAME_PAUSED_EVENT_FOR_OBSERVER
        })
    }

    private fun broadcastGameResumedToObservers() {
        broadcaster.broadcastToObserverAndControllers(GameResumedEventForObserver().also {
            it.type = Message.Type.GAME_RESUMED_EVENT_FOR_OBSERVER
        })
    }

    private fun getResultsForBot(botId: BotId): ResultsForBot = resultsBuilder.buildResultsForBot(botId)

    private fun getResultsForObservers(): List<ResultsForObserver> = resultsBuilder.buildResultsForObservers()
}
