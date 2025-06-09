package dev.robocode.tankroyale.server.core

import com.google.gson.Gson
import dev.robocode.tankroyale.schema.*
import dev.robocode.tankroyale.schema.BotIntent
import dev.robocode.tankroyale.schema.GameSetup
import dev.robocode.tankroyale.server.Server
import dev.robocode.tankroyale.server.connection.ConnectionHandler
import dev.robocode.tankroyale.server.connection.GameServerConnectionListener
import dev.robocode.tankroyale.server.mapper.*
import dev.robocode.tankroyale.server.model.*
import dev.robocode.tankroyale.server.model.InitialPosition
import dev.robocode.tankroyale.server.score.ResultsView
import org.java_websocket.WebSocket
import org.java_websocket.exceptions.WebsocketNotConnectedException
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.roundToInt
import kotlin.time.Duration
import kotlin.time.Duration.Companion.nanoseconds


/** Game server. */
class GameServer(
    /** Supported game types */
    private val gameTypes: Set<String>,
    /** Optional controller secrets */
    controllerSecrets: Set<String>,
    /** Optional bot secrets */
    botSecrets: Set<String>,
) {
    companion object {
        const val TYPE_IS_REQUIRED_ON_MESSAGE = "'type' is required on the message"
    }

    /** Connection handler for observers and bots */
    /** Initializes connection handler */
    private val connectionHandler: ConnectionHandler =
        ConnectionHandler(ServerSetup(gameTypes), GameServerConnectionListener(this), controllerSecrets, botSecrets)

    /** Current server state */
    private var serverState = ServerState.WAIT_FOR_PARTICIPANTS_TO_JOIN

    /** Current game setup */
    private lateinit var gameSetup: dev.robocode.tankroyale.server.model.GameSetup

    /** Game participants (bots connections) */
    private val participants = ConcurrentHashMap.newKeySet<WebSocket>()

    /** Game participants that signalled 'ready' for battle */
    private val readyParticipants = ConcurrentHashMap.newKeySet<WebSocket>()

    /** Map over participant ids: bot connection -> bot id */
    private val participantIds = ConcurrentHashMap<WebSocket, BotId>()

    /** Map over bot intents: bot connection -> bot intent */
    private val botIntents = ConcurrentHashMap<WebSocket, dev.robocode.tankroyale.server.model.BotIntent>()

    /** Map over participants sent to clients */
    private val participantMap = ConcurrentHashMap<BotId, Participant>()

    /** Model updater that keeps track of the game state/model */
    private var modelUpdater: ModelUpdater? = null

    /** Timer for 'ready' timeout */
    private lateinit var readyTimeoutTimer: NanoTimer

    /** Timer for 'turn' timeout */
    private var turnTimeoutTimer: NanoTimer? = null

    /** Current TPS setting (Turns Per Second) */
    private var tps = Server.tps

    /** Logger */
    private val log = LoggerFactory.getLogger(this::class.java)

    /** JSON handler */
    private val gson = Gson()

    /** Tick lock for onNextTurn() */
    private val tickLock = Any()

    /** Lock for participant-related operations */
    private val participantsLock = Any()

    /** Map over debug graphics enable flags */
    private val debugGraphicsEnableMap = ConcurrentHashMap<BotId, Boolean /* isDebugEnabled */>()


    private var botListUpdateMessage = BotListUpdate().apply {
        this.type = Message.Type.BOT_LIST_UPDATE
        this.bots = listOf<BotInfo>()
    }

    /** Starts this server */
    fun start() {
        log.info("Starting server on port ${Server.portNumber} with supporting game type(s): ${gameTypes.joinToString()}")
        connectionHandler.start()
    }

    /** Stops this server */
    fun stop() {
        log.info("Stopping server")
        connectionHandler.stop()
    }

    /** Prepares the game and wait for participants to become 'ready' */
    private fun prepareGame() {
        log.debug("Preparing game")

        serverState = ServerState.WAIT_FOR_READY_PARTICIPANTS

        participantIds.clear()
        readyParticipants.clear()
        botIntents.clear()
        participantMap.clear()
        botsThatSentIntent.clear()

        modelUpdater = null

        debugGraphicsEnableMap.clear()

        turnTimeoutTimer?.stop()
        turnTimeoutTimer = null

        prepareParticipantIds()
        prepareModelUpdater()
        sendGameStartedToParticipants()
        startReadyTimer()
    }

    private val startGameLock = Any()

    /** Starts the game if all participants are ready */
    private fun startGameIfParticipantsReady() {
        synchronized(startGameLock) {
            // Make a local copy of participant size to prevent race condition
            val currentParticipantSize = participants.size
            val currentReadyParticipantSize = readyParticipants.size

            if (currentReadyParticipantSize == currentParticipantSize && currentParticipantSize > 0) {
                // Try to stop the timer, but if we can't (already stopped), make sure we're in the right state
                if (!readyTimeoutTimer.stop() && serverState != ServerState.WAIT_FOR_READY_PARTICIPANTS) return

                startGame()
            }
        }
    }

    private fun prepareParticipantIds() {
        participants.forEachIndexed { index, conn ->
            participantIds[conn] = BotId(index + 1)
        }
    }

    /** Send game-started event to all participant bots to get them started */
    private fun sendGameStartedToParticipants() {
        val gameSetup = GameSetupMapper.map(gameSetup)
        val botHandshakes = connectionHandler.getBotHandshakes()

        participantIds.forEach { (conn, botId) ->
            val teamId = botHandshakes[conn]?.teamId
            val gameStartedForBot = createGameStartedEventForBot(botId, teamId, gameSetup)
            send(conn, gameStartedForBot)
        }
    }

    private fun getTeammateIds(botId: BotId, teamId: Int?): Set<BotId> =
        teamId?.let { getParticipantTeamIds().filterValues { it == teamId }.keys.toSet().minus(botId) }
            ?: emptySet()

    private fun getParticipantTeamIds(): Map<BotId, Int?> = participantIds
        .mapNotNull { (conn, botId) -> connectionHandler.getBotHandshakes()[conn]?.teamId?.let { botId to it } }
        .associateBy({ it.first }, { it.second })

    /** Creates a GameStartedEventForBot with current game setup */
    private fun createGameStartedEventForBot(botId: BotId, teamId: Int?, gameSetup: GameSetup) =
        GameStartedEventForBot().apply {
            type = Message.Type.GAME_STARTED_EVENT_FOR_BOT
            myId = botId.value
            teammateIds = getTeammateIds(botId, teamId).map { it.value }
            this.gameSetup = gameSetup

            val botsMap: MutableMap<BotId, MutableBot> = modelUpdater?.botsMap!!
            botsMap[botId]?.let {
                startX = it.x
                startY = it.y
                startDirection = it.direction
            }
        }

    /** Starts the 'ready' timer */
    private fun startReadyTimer() {
        readyTimeoutTimer = NanoTimer(
            minPeriodInNanos = 0,
            maxPeriodInNanos = gameSetup.readyTimeout.inWholeNanoseconds,
            job = { onReadyTimeout() }
        ).apply { start() }
    }

    /** Starts a new game */
    private fun startGame() {
        log.info("Starting game")
        readyParticipants.clear()
        participantMap.putAll(createParticipantMap())

        serverState = ServerState.GAME_RUNNING

        sendGameStartedToObservers()
        prepareModelUpdater()
        resetTurnTimeout()
    }

    /** Send GameStarted to all participant observers to get them started */
    private fun sendGameStartedToObservers() {
        broadcastToObserverAndControllers(GameStartedEventForObserver().apply {
            type = Message.Type.GAME_STARTED_EVENT_FOR_OBSERVER
            gameSetup = GameSetupMapper.map(this@GameServer.gameSetup)
            participants = participantMap.values.toList()
        })
    }

    /** Creates a map over participants from the bot connection handshakes */
    private fun createParticipantMap(): Map<BotId, Participant> {
        val participantMap = mutableMapOf<BotId, Participant>()
        for (conn in participants) {
            val handshake = connectionHandler.getBotHandshakes()[conn]
            val botId = participantIds[conn] ?: continue
            val participant = Participant().apply {
                id = botId.value
                sessionId = handshake!!.sessionId
                name = handshake.name
                version = handshake.version
                description = handshake.description
                authors = handshake.authors
                homepage = handshake.homepage
                countryCodes = handshake.countryCodes
                gameTypes = handshake.gameTypes
                platform = handshake.platform
                programmingLang = handshake.programmingLang
                initialPosition = handshake.initialPosition
                teamId = handshake.teamId
                teamName = handshake.teamName
                teamVersion = handshake.teamVersion
                isDroid = handshake.isDroid
            }
            participantMap[botId] = participant
        }
        return participantMap
    }

    /** Prepares model-updater */
    private fun prepareModelUpdater() {
        val participantIds = createParticipantIds()

        val initialPositions = participantMap.filter { it.value.initialPosition != null }.mapValues {
            val p = it.value.initialPosition
            InitialPosition(p.x, p.y, p.direction)
        }
        val droidFlags = participantMap.mapValues { it.value.isDroid == true }

        modelUpdater = ModelUpdater(gameSetup, participantIds, initialPositions, droidFlags)
    }

    private fun createParticipantIds(): Set<ParticipantId> {

        val participantIds = mutableSetOf<ParticipantId>()

        connectionHandler.getBotHandshakes().forEach { (conn, botHandshake) ->
            this.participantIds[conn]?.let { botId ->
                val teamId = botHandshake.teamId?.let { TeamId(it) }
                participantIds += ParticipantId(botId, teamId)
            }
        }
        return participantIds
    }

    /** Resets turn timeout timer with min and max bounds */
    private fun resetTurnTimeout() {
        turnTimeoutTimer?.stop()
        turnTimeoutTimer = NanoTimer(
            minPeriodInNanos = calculateTurnTimeoutMinPeriod().inWholeNanoseconds,
            maxPeriodInNanos = calculateTurnTimeoutMaxPeriod().inWholeNanoseconds,
            job = { onNextTurn() }
        ).apply { start() }
    }

    private fun calculateTurnTimeoutMinPeriod(): Duration {
        return if (tps <= 0) Duration.ZERO else 1_000_000_000.nanoseconds / tps
    }

    private fun calculateTurnTimeoutMaxPeriod(): Duration {
        return gameSetup.turnTimeout
    }

    /** Broadcast game-aborted event to all observers and controllers */
    private fun broadcastGameAborted() {
        broadcastToAll(GameAbortedEvent().apply {
            type = Message.Type.GAME_ABORTED_EVENT
        })
    }

    /** Returns a list of bot results (for bots) ordered on the score ranks */
    private fun getResultsForBot(botId: BotId): ResultsForBot {
        val results = modelUpdater!!.getResults()

        val index = results.indexOfFirst { it.participantId.botId == botId }
        check(index >= 0) { "botId was not found in results: $botId" }

        val score = results[index]
        return ResultsForBot().apply {
            this.rank = index + 1
            survival = score.survivalScore.roundToInt()
            lastSurvivorBonus = score.lastSurvivorBonus.roundToInt()
            bulletDamage = score.bulletDamageScore.roundToInt()
            bulletKillBonus = score.bulletKillBonus.toInt()
            ramDamage = score.ramDamageScore.roundToInt()
            ramKillBonus = score.ramKillBonus.roundToInt()
            totalScore = score.totalScore.roundToInt()
            firstPlaces = score.firstPlaces
            secondPlaces = score.secondPlaces
            thirdPlaces = score.thirdPlaces
        }
    }

    /** Returns a list of bot results (for observers and controllers) ordered on the score ranks */
    private fun getResultsForObservers(): List<ResultsForObserver> {

        val results = mutableListOf<ResultsForObserver>()

        val scores = ResultsView.getResults(modelUpdater!!.getResults(), participantMap.values)
        scores.forEach { score ->
            participantMap[score.participantId.botId]?.let { participant ->

                val (id, name, version) =
                    if (participant.teamId == null)
                        Triple(participant.id, participant.name, participant.version)
                    else
                        Triple(participant.teamId, participant.teamName, participant.teamVersion)

                ResultsForObserver().apply {
                    this.id = id
                    this.name = name
                    this.version = version
                    this.rank = score.rank
                    survival = score.survivalScore.roundToInt()
                    lastSurvivorBonus = score.lastSurvivorBonus.roundToInt()
                    bulletDamage = score.bulletDamageScore.roundToInt()
                    bulletKillBonus = score.bulletKillBonus.toInt()
                    ramDamage = score.ramDamageScore.roundToInt()
                    ramKillBonus = score.ramKillBonus.roundToInt()
                    totalScore = score.totalScore.roundToInt()
                    firstPlaces = score.firstPlaces
                    secondPlaces = score.secondPlaces
                    thirdPlaces = score.thirdPlaces

                    results += this
                }
            }
        }
        return results
    }


    /** Broadcast pause event to all observers */
    private fun broadcastGamedPausedToObservers() {
        broadcastToObserverAndControllers(GamePausedEventForObserver().apply {
            type = Message.Type.GAME_PAUSED_EVENT_FOR_OBSERVER
        })
    }

    /** Broadcast resume event to all observers */
    private fun broadcastGameResumedToObservers() {
        broadcastToObserverAndControllers(GameResumedEventForObserver().apply {
            type = Message.Type.GAME_RESUMED_EVENT_FOR_OBSERVER
        })
    }

    /** Broadcast TPS-changed event to all observers */
    private fun broadcastTpsChangedToObservers(tps: Int) {
        broadcastToObserverAndControllers(TpsChangedEvent().apply {
            type = Message.Type.TPS_CHANGED_EVENT
            this.tps = tps
        })
    }

    private fun updateGameState(): GameState {
        val botIntentsSnapshot = synchronized(tickLock) {
            botIntents.mapNotNull { (key, value) ->
                participantIds[key]?.let { botId ->
                    botId to dev.robocode.tankroyale.server.model.BotIntent().apply {
                        update(value)
                    }
                }
            }.toMap()
        }

        return modelUpdater?.update(botIntentsSnapshot)
            ?: throw IllegalStateException("Model updater is null when trying to update game state")
    }

    private fun onReadyTimeout() {
        log.debug("Ready timeout")
        synchronized(startGameLock) {
            // Check again in case state changed during timer
            if (serverState !== ServerState.WAIT_FOR_READY_PARTICIPANTS) return

            if (readyParticipants.size >= gameSetup.minNumberOfParticipants) {
                // Start the game with the participants that are ready
                log.warn("Starting game with ${readyParticipants.size}/${participants.size} participants ready because of timeout")
                val participantIterator = participants.iterator()
                while (participantIterator.hasNext()) {
                    val participantConn = participantIterator.next()
                    if (!readyParticipants.contains(participantConn)) {
                        participantIterator.remove()
                        participantIds.remove(participantConn)
                    }
                }
                startGame()
            } else {
                // Not enough participants -> prepare another game
                log.warn("Aborting the game as only ${readyParticipants.size}/${participants.size} participants are ready")
                serverState = ServerState.WAIT_FOR_PARTICIPANTS_TO_JOIN
                broadcastGameAborted()
            }
        }
    }

    private fun onNextTurn() {
        if (serverState !== ServerState.GAME_RUNNING) return

        // Required as this method can be called again while already running.
        // This would give a race condition without the synchronized lock.
        synchronized(tickLock) {
            // Update game state
            updateGameState().apply {
                onNextTick(lastRound)

                if (isGameEnded) {
                    onGameEnded()
                }
            }

            // Clear inside synchronized block to prevent race condition
            botsThatSentIntent.clear()
        }

        resetTurnTimeout()
    }

    private fun onGameEnded() {
        log.info("Game ended")

        broadcastGameEndedToParticipants()
        broadcastGameEndedToObservers()

        // Must be done after the broadcasting
        serverState = ServerState.GAME_STOPPED

        cleanupAfterGameStopped()
    }

    private fun onNextTick(lastRound: IRound?) {
        lastRound?.apply {
            lastTurn?.apply {
                if (turnNumber == 1) {
                    log.debug("Round started: $roundNumber")

                    // Clear in first turn (left over from other round?), but BEFORE broadcasting round started event
                    botIntents.clear()

                    transferDebugGraphicsFlagToModel()

                    broadcastRoundStartedToAll(roundNumber)

                } else { // not turn 1
                    // Send SkippedTurn, except in turn 1
                    checkForSkippedTurns(turnNumber)

                    // Clear bot intents after skipped turns have been handled, but BEFORE broadcasting tick event
                    botIntents.clear()
                }
                sendTickToParticipants(roundNumber, this)
                broadcastGameTickToObservers(roundNumber, this)

                // Send round ended _after_ tick has been sent
                if (roundEnded) {
                    log.debug("Round ended: $roundNumber")
                    broadcastRoundEndedToParticipants(roundNumber, turnNumber)
                    broadcastRoundEndedToObservers(roundNumber, turnNumber)
                }
            }
        }
    }

    private fun broadcastGameEndedToParticipants() {
        participants.forEach { conn ->
            participantIds[conn]?.let { botId ->
                GameEndedEventForBot().apply {
                    type = Message.Type.GAME_ENDED_EVENT_FOR_BOT
                    numberOfRounds = modelUpdater!!.numberOfRounds
                    results = getResultsForBot(botId)

                    send(conn, this)
                }
            }
        }
    }

    private fun broadcastGameEndedToObservers() {
        broadcastToObserverAndControllers(GameEndedEventForObserver().apply {
            type = Message.Type.GAME_ENDED_EVENT_FOR_OBSERVER
            numberOfRounds = modelUpdater!!.numberOfRounds
            results = getResultsForObservers() // Use the stored score!
        })
    }

    private fun broadcastRoundStartedToAll(roundNumber: Int) {
        broadcastToAll(RoundStartedEvent().also {
            it.type = Message.Type.ROUND_STARTED_EVENT
            it.roundNumber = roundNumber
        })
    }

    private fun broadcastRoundEndedToParticipants(roundNumber: Int, turnNumber: Int) {
        participants.forEach { conn ->
            participantIds[conn]?.let { botId ->
                RoundEndedEventForBot().apply {
                    type = Message.Type.ROUND_ENDED_EVENT_FOR_BOT
                    this.roundNumber = roundNumber
                    this.turnNumber = turnNumber
                    results = getResultsForBot(botId)

                    send(conn, this)
                }
            }
        }
    }

    private fun broadcastRoundEndedToObservers(roundNumber: Int, turnNumber: Int) {
        broadcastToObserverAndControllers(RoundEndedEventForObserver().also {
            it.type = Message.Type.ROUND_ENDED_EVENT_FOR_OBSERVER
            it.roundNumber = roundNumber
            it.turnNumber = turnNumber
            it.results = getResultsForObservers()
        })
    }

    private fun sendTickToParticipants(roundNumber: Int, turn: ITurn) {
        val aliveBotTeamIds = aliveBotToTeamIdMap()

        for (conn in participants) {
            val participantId = participantIds[conn] ?: continue
            if (modelUpdater?.isAlive(participantId) == false) continue

            val teamId = aliveBotTeamIds[participantId]
            val enemyCount = aliveBotTeamIds.filterValues { it != teamId }.count()

            val event = TurnToTickEventForBotMapper.map(roundNumber, turn, participantId, enemyCount) ?: continue
            send(conn, event)
        }
    }

    private fun aliveBotToTeamIdMap(): Map<BotId, Int> =
        participantMap.filterKeys { botId -> modelUpdater?.isAlive(botId) == true }.mapValues { (botId, participant) ->
            participant.teamId ?: -botId.value
        }

    private fun broadcastGameTickToObservers(roundNumber: Int, turn: ITurn) {
        val enemyCountMap = HashMap<BotId, Int /* enemyCount */>()

        val aliveBotTeamIds = aliveBotToTeamIdMap()

        participantMap.keys.forEach { botId ->
            val teamId = aliveBotTeamIds[botId]
            enemyCountMap[botId] = aliveBotTeamIds.filterValues { it != teamId }.count()
        }

        broadcastToObserverAndControllers(
            TurnToTickEventForObserverMapper
                .map(roundNumber, turn, participantMap, enemyCountMap, debugGraphicsEnableMap)
        )
    }

    private fun checkForSkippedTurns(currentTurnNumber: Int) {
        val botsSkippingTurn = getParticipantsThatSkippedTurn()

        if (botsSkippingTurn.isNotEmpty()) {
            val skippedTurn = SkippedTurnEvent().apply {
                type = Message.Type.SKIPPED_TURN_EVENT
                turnNumber = currentTurnNumber - 1 // last turn number
            }
            val json = gson.toJson(skippedTurn)

            botsSkippingTurn.forEach { bot -> connectionHandler.send(bot, json) }
        }
    }

    private fun getParticipantsThatSkippedTurn(): Collection<WebSocket> =
        mutableListOf<WebSocket>().apply {
            participants.forEach { participant ->
                participantIds[participant]?.let {
                    // Check if no intent was received from the (alive) participant during the turn
                    if (modelUpdater?.isAlive(it) == true && botIntents[participant] == null) {
                        this += participant
                    }
                }
            }
        }

    private val botsThatSentIntent = mutableSetOf<WebSocket>()

    private fun updateBotListUpdateMessage() {
        val newBotsList = mutableListOf<BotInfo>()

        connectionHandler.apply {
            mapToBotSockets().forEach { conn ->
                getBotHandshakes()[conn]?.let { botHandshake ->
                    conn.remoteSocketAddress.apply {
                        newBotsList.add(BotHandshakeToBotInfoMapper.map(botHandshake, hostString, port))
                    }
                }
            }
        }

        // Set the new list after it's fully populated to avoid race condition
        botListUpdateMessage.bots = newBotsList
    }

    private fun send(conn: WebSocket, msg: Message) {
        requireNotNull(msg.type) { TYPE_IS_REQUIRED_ON_MESSAGE }
        gson.toJson(msg).also {
            try {
                conn.send(it)
            } catch (_: WebsocketNotConnectedException) {
                // Bot cannot receive events and send new intents.
            }
        }
    }

    private fun broadcastToObserverAndControllers(msg: Message) {
        requireNotNull(msg.type) { TYPE_IS_REQUIRED_ON_MESSAGE }
        connectionHandler.broadcastToObserverAndControllers(gson.toJson(msg))
    }

    private fun broadcastToAll(msg: Message) {
        requireNotNull(msg.type) { TYPE_IS_REQUIRED_ON_MESSAGE }
        val json = gson.toJson(msg)
        connectionHandler.broadcastToObserverAndControllers(json)
        connectionHandler.broadcast(participants, json) // note: it is only participants, not all bots
    }

    private fun sendBotListUpdateToObservers() {
        // Send a clone of the message to prevent race conditions if the message is updated during broadcast
        broadcastToObserverAndControllers(cloneBotListUpdate(botListUpdateMessage))
    }

    internal fun sendBotListUpdate(conn: WebSocket) {
        // Send a clone of the message to prevent race conditions
        send(conn, cloneBotListUpdate(botListUpdateMessage))
    }

    private fun cloneBotListUpdate(original: BotListUpdate): BotListUpdate {
        return BotListUpdate().apply {
            type = Message.Type.BOT_LIST_UPDATE
            bots = ArrayList(original.bots) // Create a new list with the same elements
        }
    }

    internal fun handleBotJoined() {
        updateBotListUpdateMessage()
        sendBotListUpdateToObservers()
    }

    internal fun handleBotLeft(conn: WebSocket) {
        val shouldAbortGame = synchronized(participantsLock) {
            val wasRemoved = participants.remove(conn)
            wasRemoved && participants.isEmpty() &&
                    (serverState === ServerState.GAME_RUNNING || serverState === ServerState.GAME_PAUSED)
        }

        if (shouldAbortGame) {
            handleAbortGame() // Abort the battle when all bots left it!
        }

        synchronized(tickLock) {
            // If a bot leaves while in a game, make sure to reset all intent values to zeroes
            botIntents[conn]?.disableMovement()
        }

        updateBotListUpdateMessage()
        sendBotListUpdateToObservers()
    }

    internal fun handleBotReady(conn: WebSocket) {
        synchronized(participantsLock) {
            if (serverState === ServerState.WAIT_FOR_READY_PARTICIPANTS) {
                readyParticipants += conn
                // Start the game check from within the synchronized block
                startGameIfParticipantsReady()
            }
        }
    }

    internal fun handleBotIntent(conn: WebSocket, intent: BotIntent) {
        if (!participants.contains(conn)) return

        // Update bot intent using a synchronized block to ensure atomic operation
        synchronized(tickLock) {
            // Get existing intent or null if it doesn't exist yet
            val existingIntent = botIntents[conn]

            if (existingIntent == null) {
                // If there's no existing intent, create a new one with default values for null fields
                botIntents[conn] = BotIntentMapper.map(intent)
            } else {
                // If intent exists, only update non-null values from new intent
                intent.apply {
                    // Only update fields that aren't null
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

            // If all bot intents have been received, we can start next turn
            botsThatSentIntent += conn
            if (botIntents.size == botsThatSentIntent.size) {
                turnTimeoutTimer?.notifyReady()
            }
        }
    }

    internal fun handleStartGame(gameSetup: GameSetup, botAddresses: Collection<BotAddress>) {
        this.gameSetup = GameSetupMapper.map(gameSetup)

        participants.apply {
            clear()
            this += connectionHandler.mapToBotSockets(botAddresses)

            if (isNotEmpty()) {
                prepareGame()
            }
        }
    }

    internal fun handleAbortGame() {
        log.info("Aborting game")
        serverState = ServerState.GAME_STOPPED
        broadcastGameAborted()
        cleanupAfterGameStopped()

        // No score is generated for aborted games
    }

    internal fun handlePauseGame() {
        if (serverState === ServerState.GAME_RUNNING) {
            log.info("Pausing game")
            serverState = ServerState.GAME_PAUSED
            turnTimeoutTimer?.pause()
            broadcastGamedPausedToObservers()
        }
    }

    internal fun handleResumeGame() {
        if (serverState === ServerState.GAME_PAUSED) {
            log.info("Resuming game")
            serverState = ServerState.GAME_RUNNING
            turnTimeoutTimer?.resume()
            broadcastGameResumedToObservers()
        }
    }

    internal fun handleNextTurn() {
        if (serverState === ServerState.GAME_PAUSED) {
            handleResumeGame()
            onNextTurn()
            handlePauseGame()
        }
    }

    internal fun handleChangeTps(newTps: Int) {
        if (tps == newTps) return
        tps = newTps

        broadcastTpsChangedToObservers(newTps)

        if (tps == 0) {
            handlePauseGame()
        } else {
            if (serverState === ServerState.GAME_PAUSED) {
                handleResumeGame()
            }
            resetTurnTimeout()
        }
    }

    internal fun handleBotPolicyUpdate(botPolicyUpdate: BotPolicyUpdate) {
        val botId = BotId(botPolicyUpdate.botId)
        debugGraphicsEnableMap[botId] = botPolicyUpdate.debuggingEnabled

        // Update the current flag as well
        modelUpdater?.botsMap?.get(botId)?.isDebuggingEnabled = botPolicyUpdate.debuggingEnabled
    }

    private fun cleanupAfterGameStopped() {
        turnTimeoutTimer?.stop()

        modelUpdater = null
        System.gc()
    }

    private fun transferDebugGraphicsFlagToModel() {
        modelUpdater?.botsMap?.forEach { (botId, bot) ->
            bot.isDebuggingEnabled = debugGraphicsEnableMap[botId] ?: false
        }
    }
}