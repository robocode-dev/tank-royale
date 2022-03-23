package dev.robocode.tankroyale.server.core

import com.google.gson.Gson
import dev.robocode.tankroyale.schema.*
import dev.robocode.tankroyale.schema.Message.`$type`
import dev.robocode.tankroyale.server.Server
import dev.robocode.tankroyale.server.dev.robocode.tankroyale.server.core.ConnHandler
import dev.robocode.tankroyale.server.dev.robocode.tankroyale.server.model.InitialPosition
import dev.robocode.tankroyale.server.mapper.*
import dev.robocode.tankroyale.server.model.BotId
import dev.robocode.tankroyale.server.model.GameState
import dev.robocode.tankroyale.server.model.IRound
import dev.robocode.tankroyale.server.model.ITurn
import dev.robocode.tankroyale.server.rules.DEFAULT_TURNS_PER_SECOND
import org.java_websocket.WebSocket
import org.java_websocket.exceptions.WebsocketNotConnectedException
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.roundToInt


/** Game server. */
class GameServer(
    /** Supported game types */
    private val gameTypes: Set<String>,
    /** Optional controller secrets */
    controllerSecrets: Set<String>,
    /** Optional bot secrets */
    botSecrets: Set<String>
) {
    /** Connection handler for observers and bots */
    private val connHandler: ConnHandler

    /** Current server state */
    private var serverState = ServerState.WAIT_FOR_PARTICIPANTS_TO_JOIN

    /** Current game setup */
    private var gameSetup: dev.robocode.tankroyale.server.model.GameSetup? = null

    /** Game participants (bots connections) */
    private val participants = ConcurrentHashMap.newKeySet<WebSocket>()

    /** Game participants that signalled 'ready' for battle */
    private val readyParticipants = ConcurrentHashMap.newKeySet<WebSocket>()

    /** Map over participant ids: bot connection -> bot id */
    private val participantIds = ConcurrentHashMap<WebSocket, BotId>()

    /** Map over bot intents: bot connection -> bot intent */
    private val botIntents = ConcurrentHashMap<WebSocket, dev.robocode.tankroyale.server.model.BotIntent>()

    /** Map over participants sent to clients */
    private val participantMap = mutableMapOf<BotId, Participant>()

    /** Model updater that keeps track of the game state/model */
    private lateinit var modelUpdater: ModelUpdater

    /** Timer for 'ready' timeout */
    private var readyTimeoutTimer = NanoTimer(0) {} // dummy

    /** Timer for 'turn' timeout */
    private var turnTimeoutTimer: NanoTimer? = null

    /** Current TPS setting (Turns Per Second) */
    private var tps = DEFAULT_TURNS_PER_SECOND

    /** Logger */
    private val log = LoggerFactory.getLogger(GameServer::class.java)

    /** JSON handler */
    private val gson = Gson()

    /** Tick lock for onNextTurn() */
    private val tickLock = Any()

    @Suppress("RemoveRedundantQualifierName")
    private var botListUpdateMessage = BotListUpdate().apply {
        this.`$type` = Message.`$type`.BOT_LIST_UPDATE
        this.bots = listOf<BotInfo>()
    }

    init {
        /** Initializes connection handler */
        val serverSetup = ServerSetup(gameTypes)
        connHandler = ConnHandler(serverSetup, GameServerConnListener(this), controllerSecrets, botSecrets)
    }

    /** Starts this server */
    fun start() {
        log.info("Starting server on port ${Server.port} with game type(s): $gameTypes")
        connHandler.start()
    }

    /** Stops this server */
    fun stop() {
        log.info("Stopping server")
        connHandler.stop()
    }

    /** Prepares the game and wait for participants to become 'ready' */
    private fun prepareGame() {
        log.debug("Preparing game")

        serverState = ServerState.WAIT_FOR_READY_PARTICIPANTS
        participantIds.clear()
        readyParticipants.clear()

        prepareModelUpdater()
        sendGameStartedToParticipants()
        startReadyTimer()
    }

    /** Starts the game if all participants are ready */
    private fun startGameIfParticipantsReady() {
        if (readyParticipants.size == participants.size) {
            participantMap.apply {
                clear()
                putAll(createParticipantMap())
            }

            readyTimeoutTimer.stop()
            readyParticipants.clear()
            botIntents.clear()

            startGame()
        }
    }

    /** Send game-started event to all participant bots to get them started */
    private fun sendGameStartedToParticipants() {
        val gameStartedForBot = createGameStartedEventForBot()
        var id = 1
        for (conn in participants) {
            participantIds[conn] = BotId(id)
            gameStartedForBot.myId = id++
            send(conn, gameStartedForBot)
        }
    }

    /** Creates a GameStartedEventForBot with current game setup */
    private fun createGameStartedEventForBot(): GameStartedEventForBot {
        val gameStartedForBot = GameStartedEventForBot()
        gameStartedForBot.`$type` = `$type`.GAME_STARTED_EVENT_FOR_BOT
        gameStartedForBot.gameSetup = GameSetupToGameSetupMapper.map(gameSetup!!)
        return gameStartedForBot
    }

    /** Starts the 'ready' timer */
    private fun startReadyTimer() {
        readyTimeoutTimer = NanoTimer(gameSetup!!.readyTimeout * 1000000L) { onReadyTimeout() }
        readyTimeoutTimer.start()
    }

    /** Starts a new game */
    private fun startGame() {
        log.info("Starting game")

        serverState = ServerState.GAME_RUNNING

        sendGameStartedToObservers()
        prepareModelUpdater()
        resetTurnTimeout()
    }

    /** Send GameStarted to all participant observers to get them started */
    private fun sendGameStartedToObservers() {
        if (connHandler.observerAndControllerConnections.isNotEmpty()) {
            val gameStartedForObserver = GameStartedEventForObserver()
            gameStartedForObserver.`$type` = `$type`.GAME_STARTED_EVENT_FOR_OBSERVER
            gameStartedForObserver.gameSetup = GameSetupToGameSetupMapper.map(gameSetup!!)
            gameStartedForObserver.participants = participantMap.values.toList()
            broadcastToObserverAndControllers(gameStartedForObserver)
        }
    }

    /** Creates a map over participants from the bot connection handshakes */
    private fun createParticipantMap(): Map<BotId, Participant> {
        val participantMap = mutableMapOf<BotId, Participant>()
        for (conn in participants) {
            val handshake = connHandler.getBotHandshakes()[conn]
            val botId = participantIds[conn] ?: continue
            val participant = Participant().apply {
                id = botId.value
                name = handshake!!.name
                version = handshake.version
                description = handshake.description
                authors = handshake.authors
                homepage = handshake.homepage
                countryCodes = handshake.countryCodes
                gameTypes = handshake.gameTypes
                platform = handshake.platform
                programmingLang = handshake.programmingLang
                initialPosition = handshake.initialPosition
            }
            participantMap[botId] = participant
        }
        return participantMap
    }

    /** Prepares model-updater */
    private fun prepareModelUpdater() {
        val initialPositions = participantMap.filter { it.value.initialPosition != null }.mapValues {
            val p = it.value.initialPosition
            InitialPosition(p.x, p.y, p.angle)
        }
        modelUpdater = ModelUpdater(gameSetup!!, HashSet(participantIds.values), initialPositions)
    }

    /** Last reset turn timeout period */
    @Volatile
    var lastResetTurnTimeoutPeriod: Long = 0

    /** Resets turn timeout timer */
    private fun resetTurnTimeout() {
        val period = calculateTurnTimeoutPeriod()

        // New timer is only set, if the period is different from the last reset time
        if (period != lastResetTurnTimeoutPeriod) {
            lastResetTurnTimeoutPeriod = period

            // Start new turn timeout timer to invoke onNextTurn()
            turnTimeoutTimer?.stop()
            turnTimeoutTimer = NanoTimer(period) { onNextTurn() }
            turnTimeoutTimer?.start()
        }
    }

    /** Calculates and returns a timeout turn period measured in nano-seconds based on current TPS */
    private fun calculateTurnTimeoutPeriod(): Long {
        var period = if (tps <= 0) 0 else 1_000_000_000L / tps

        val turnTimeout = gameSetup!!.turnTimeout * 1000L
        if (turnTimeout > period) {
            period = turnTimeout
        }
        return period
    }

    /** Broadcast game-aborted event to all observers and controllers */
    private fun broadcastGameAborted() {
        val gameAborted = GameAbortedEvent()
        gameAborted.`$type` = `$type`.GAME_ABORTED_EVENT
        broadcastToAll(gameAborted)
    }

    /** Returns a list of bot results (for bots) ordered on the score ranks */
    private fun getResultsForBots(): List<BotResultsForBot> {
        val results = mutableListOf<BotResultsForBot>()

        for (score in modelUpdater.results) {
            BotResultsForBot().apply {
                id = score.botId.value
                survival = score.survival.roundToInt()
                lastSurvivorBonus = score.lastSurvivorBonus.roundToInt()
                bulletDamage = score.bulletDamage.roundToInt()
                bulletKillBonus = score.bulletKillBonus.toInt()
                ramDamage = score.ramDamage.roundToInt()
                ramKillBonus = score.ramKillBonus.roundToInt()
                totalScore = score.totalScore.roundToInt()
                firstPlaces = score.firstPlaces
                secondPlaces = score.secondPlaces
                thirdPlaces = score.thirdPlaces

                results += this
            }
        }
        var rank = 1
        results.forEach { score -> score.rank = rank++ }
        return results
    }

    /** Returns a list of bot results (for observers and controllers) ordered on the score ranks */
    private fun getResultsForObservers(): List<BotResultsForObserver> {
        val results = mutableListOf<BotResultsForObserver>()

        for (score in modelUpdater.results) {
            val conn = getConnection(score.botId)
            val botHandshake = connHandler.getBotHandshakes()[conn]

            BotResultsForObserver().apply {
                id = score.botId.value
                name = botHandshake!!.name
                version = botHandshake.version
                survival = score.survival.roundToInt()
                lastSurvivorBonus = score.lastSurvivorBonus.roundToInt()
                bulletDamage = score.bulletDamage.roundToInt()
                bulletKillBonus = score.bulletKillBonus.toInt()
                ramDamage = score.ramDamage.roundToInt()
                ramKillBonus = score.ramKillBonus.roundToInt()
                totalScore = score.totalScore.roundToInt()
                firstPlaces = score.firstPlaces
                secondPlaces = score.secondPlaces
                thirdPlaces = score.thirdPlaces

                results += this
            }
        }
        var rank = 1
        results.forEach { score -> score.rank = rank++ }
        return results
    }

    /**
     * Returns the connection for a bot.
     * @param botId is the id of the bot.
     * @return The connection for the bot or `null` if no connection was found for the bot id.
     */
    private fun getConnection(botId: BotId): WebSocket {
        return participantIds.entries.first { (_, id) -> botId == id }.key
    }

    /** Broadcast pause event to all observers */
    private fun broadcastGamedPausedToObservers() {
        val gamePaused = GamePausedEventForObserver()
        gamePaused.`$type` = `$type`.GAME_PAUSED_EVENT_FOR_OBSERVER
        broadcastToObserverAndControllers(gamePaused)
    }

    /** Broadcast resume event to all observers */
    private fun broadcastGameResumedToObservers() {
        val gameResumed = GameResumedEventForObserver()
        gameResumed.`$type` = `$type`.GAME_RESUMED_EVENT_FOR_OBSERVER
        broadcastToObserverAndControllers(gameResumed)
    }

    /** Broadcast TPS-changed event to all observers */
    private fun broadcastTpsChangedToObservers(tps: Int) {
        val tpsChanged = TpsChangedEvent()
        tpsChanged.`$type` = `$type`.TPS_CHANGED_EVENT
        tpsChanged.tps = tps
        broadcastToObserverAndControllers(tpsChanged)
    }

    private fun updateGameState(): GameState {
        val mappedBotIntents = mutableMapOf<BotId, dev.robocode.tankroyale.server.model.BotIntent>()
        for ((key, value) in botIntents) {
            val botId = participantIds[key]
            if (botId != null) {
                mappedBotIntents[botId] = value
            }
        }
        return modelUpdater.update(mappedBotIntents.toMap())
    }

    private fun onReadyTimeout() {
        log.debug("Ready timeout")
        if (readyParticipants.size >= gameSetup!!.minNumberOfParticipants) {
            // Start the game with the participants that are ready
            participants.clear()
            participants += readyParticipants

            startGame()
        } else {
            // Not enough participants -> prepare another game
            serverState = ServerState.WAIT_FOR_PARTICIPANTS_TO_JOIN
        }
    }

    private fun onNextTurn() {
        if (serverState !== ServerState.GAME_RUNNING) {
            return
        }
        // Required as this method can be called again while already running.
        // This would give a raise condition without the synchronized lock.
        synchronized(tickLock) {
            // Update game state
            val gameState = updateGameState()
            if (gameState.isGameEnded) {
                onGameEnded()
            } else {
                onNextTick(gameState.lastRound)
            }
        }
    }

    private fun onGameEnded() {
        log.info("Game ended")
        serverState = ServerState.GAME_STOPPED
        modelUpdater.calculatePlacements()

        broadcastGameEndedToParticipants()
        broadcastGameEndedToObservers()
    }

    private fun onNextTick(lastRound: IRound?) {
        // Send tick
        if (lastRound != null) {
            val roundNumber = lastRound.roundNumber
            val turn = lastRound.lastTurn
            if (turn != null) {
                if (turn.turnNumber == 1) {
                    log.debug("Round started: $roundNumber")
                    broadcastRoundStartedToAll(roundNumber)
                } else if (lastRound.roundEnded) {
                    log.debug("Round ended: $roundNumber")
                    broadcastRoundEndedToAll(roundNumber, turn.turnNumber)
                }
                broadcastGameTickToParticipants(roundNumber, turn)
                broadcastGameTickToObservers(roundNumber, turn)
            }
            broadcastSkippedTurnToParticipants()
        }
        // Clear bot intents
        botIntents.clear()
    }

    private fun broadcastGameEndedToParticipants() {
        val gameEnded = GameEndedEventForBot()
        gameEnded.`$type` = `$type`.GAME_ENDED_EVENT_FOR_BOT
        gameEnded.numberOfRounds = modelUpdater.numberOfRounds
        gameEnded.results = getResultsForBots() // Use the stored score!
        broadcastToParticipants(gameEnded)
    }

    private fun broadcastGameEndedToObservers() {
        val gameEnded = GameEndedEventForObserver()
        gameEnded.`$type` = `$type`.GAME_ENDED_EVENT_FOR_OBSERVER
        gameEnded.numberOfRounds = modelUpdater.numberOfRounds
        gameEnded.results = getResultsForObservers() // Use the stored score!
        broadcastToObserverAndControllers(gameEnded)
    }

    private fun broadcastRoundStartedToAll(roundNumber: Int) {
        val roundStarted = RoundStartedEvent()
        roundStarted.`$type` = `$type`.ROUND_STARTED_EVENT
        roundStarted.roundNumber = roundNumber
        broadcastToAll(roundStarted)
    }

    private fun broadcastRoundEndedToAll(roundNumber: Int, turnNumber: Int) {
        val roundEnded = RoundEndedEvent()
        roundEnded.`$type` = `$type`.ROUND_ENDED_EVENT
        roundEnded.roundNumber = roundNumber
        roundEnded.turnNumber = turnNumber
        broadcastToAll(roundEnded)
    }

    private fun broadcastGameTickToParticipants(roundNumber: Int, turn: ITurn) {
        for (conn in participants) {
            val botId = participantIds[conn]
            if (botId != null) {
                val gameTickForBot = TurnToTickEventForBotMapper.map(roundNumber, turn, botId)
                if (gameTickForBot != null) { // Bot alive?
                    send(conn, gameTickForBot)
                }
            }
        }
    }

    private fun broadcastGameTickToObservers(roundNumber: Int, turn: ITurn) {
        broadcastToObserverAndControllers(TurnToTickEventForObserverMapper.map(roundNumber, turn))
    }

    private fun broadcastSkippedTurnToParticipants() {
        val skippedTurn = SkippedTurnEvent()
        skippedTurn.`$type` = `$type`.SKIPPED_TURN_EVENT
        skippedTurn.turnNumber = modelUpdater.turn.turnNumber
        connHandler.broadcast(getParticipantsThatSkippedTurn(), gson.toJson(skippedTurn))
    }

    private fun getParticipantsThatSkippedTurn(): Collection<WebSocket> {
        val participantsThatSkippedTurn = mutableListOf<WebSocket>()
        for (conn in participants) {
            if (botIntents[conn] == null) {
                // No intent was received from the participant during the turn
                participantsThatSkippedTurn += conn
            }
        }
        return participantsThatSkippedTurn
    }

    private val botsThatSentIntent = mutableSetOf<WebSocket>()

    private fun updateBotListUpdateMessage() {
        val botsList = mutableListOf<BotInfo>()

        botListUpdateMessage.bots = botsList

        for (conn in connHandler.getBotConnections()) {
            val address = conn.remoteSocketAddress
            val botInfo = BotHandshakeToBotInfoMapper.map(
                connHandler.getBotHandshakes()[conn]!!,
                address.hostString, address.port
            )
            botsList += botInfo
        }
    }

    private fun send(conn: WebSocket, message: Message) {
        val msg = gson.toJson(message)
        try {
            conn.send(msg)
        } catch (ignore: WebsocketNotConnectedException) {
            // Bot cannot receive events and send new intents.
        }
    }

    private fun broadcastToParticipants(msg: Message) {
        requireNotNull(msg.`$type`) { "\$type is required on the message" }
        connHandler.broadcast(participants, gson.toJson(msg))
    }

    private fun broadcastToObserverAndControllers(msg: Message) {
        requireNotNull(msg.`$type`) { "\$type is required on the message" }
        connHandler.broadcastToObserverAndControllers(gson.toJson(msg))
    }

    private fun broadcastToAll(msg: Message) {
        requireNotNull(msg.`$type`) { "\$type is required on the message" }
        connHandler.broadcastToObserverAndControllers(gson.toJson(msg))
        connHandler.broadcast(participants, gson.toJson(msg))
    }

    private fun sendBotListUpdateToObservers() {
        broadcastToObserverAndControllers(botListUpdateMessage)
    }

    internal fun sendBotListUpdate(conn: WebSocket) {
        send(conn, botListUpdateMessage)
    }

    internal fun onBotJoined() {
        updateBotListUpdateMessage()
        sendBotListUpdateToObservers()
    }

    internal fun onBotLeft(conn: WebSocket) {
        // If a bot leaves while in a game, make sure to reset all intent values to zeroes
        botIntents[conn]?.disableMovement()
        updateBotListUpdateMessage()
        sendBotListUpdateToObservers()
    }

    internal fun onBotReady(conn: WebSocket) {
        if (serverState === ServerState.WAIT_FOR_READY_PARTICIPANTS) {
            readyParticipants += conn
            startGameIfParticipantsReady()
        }
    }

    internal fun onBotIntent(conn: WebSocket, intent: BotIntent) {
        if (!participants.contains(conn)) return

        // Update bot intent
        val botIntent = botIntents[conn] ?: dev.robocode.tankroyale.server.model.BotIntent()
        botIntent.update(BotIntentToBotIntentMapper.map(intent))
        botIntents[conn] = botIntent

        // If all bot intents have been received, we can start next turn
        botsThatSentIntent += conn
        if (botIntents.size == botsThatSentIntent.size) {
            botsThatSentIntent.clear()
            turnTimeoutTimer?.reset()
            resetTurnTimeout()
        }
    }

    /**
     * Starts a new game with a new game setup and new participants.
     * @param gameSetup is the new game setup.
     * @param botAddresses is the addresses of the new participants.
     */
    internal fun onStartGame(gameSetup: GameSetup, botAddresses: Collection<BotAddress>) {
        this.gameSetup = GameSetupToGameSetupMapper.map(gameSetup)
        participants.clear()
        participants += connHandler.getBotConnections(botAddresses)

        if (participants.isNotEmpty()) {
            prepareGame()
        }
    }

    /** Aborts current game */
    internal fun onAbortGame() {
        log.info("Aborting game")
        serverState = ServerState.GAME_STOPPED
        broadcastGameAborted()

        // No score is generated for aborted games
    }

    /** Pauses current game */
    internal fun onPauseGame() {
        if (serverState === ServerState.GAME_RUNNING) {
            log.info("Pausing game")
            serverState = ServerState.GAME_PAUSED
            turnTimeoutTimer?.pause()
            broadcastGamedPausedToObservers()
        }
    }

    /** Resumes current game */
    internal fun onResumeGame() {
        if (serverState === ServerState.GAME_PAUSED) {
            log.info("Resuming game")
            serverState = ServerState.GAME_RUNNING
            turnTimeoutTimer?.resume()
            broadcastGameResumedToObservers()
        }
    }

    /**
     * Changes the TPS.
     * @param tps is the new TPS to change to.
     */
    internal fun onChangeTps(tps: Int) {
        if (this.tps == tps) return
        this.tps = tps

        broadcastTpsChangedToObservers(tps)

        if (tps == 0) {
            onPauseGame()
        } else {
            if (serverState === ServerState.GAME_PAUSED) {
                onResumeGame()
            }
            resetTurnTimeout()
        }
    }
}