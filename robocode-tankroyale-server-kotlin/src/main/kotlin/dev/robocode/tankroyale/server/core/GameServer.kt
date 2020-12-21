package dev.robocode.tankroyale.server.dev.robocode.tankroyale.server.core

import com.google.gson.Gson
import dev.robocode.tankroyale.schema.*
import dev.robocode.tankroyale.schema.Message.`$type`
import dev.robocode.tankroyale.server.Server
import dev.robocode.tankroyale.server.core.ModelUpdater
import dev.robocode.tankroyale.server.core.NanoTimer
import dev.robocode.tankroyale.server.core.ServerSetup
import dev.robocode.tankroyale.server.core.ServerState
import dev.robocode.tankroyale.server.dev.robocode.tankroyale.server.conn.ConnHandler
import dev.robocode.tankroyale.server.dev.robocode.tankroyale.server.conn.ConnListener
import dev.robocode.tankroyale.server.mapper.*
import dev.robocode.tankroyale.server.model.BotId
import dev.robocode.tankroyale.server.model.GameState
import dev.robocode.tankroyale.server.model.Round
import dev.robocode.tankroyale.server.model.Turn
import dev.robocode.tankroyale.server.rules.DEFAULT_TURNS_PER_SECOND
import org.java_websocket.WebSocket
import org.java_websocket.exceptions.WebsocketNotConnectedException
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.roundToInt


/** Game server. */
class GameServer(
    /** Supported game types (comma-separated list) */
    gameTypes: String,
    /** Optional client secret */
    clientSecret: String?
) {
    /** Game types in a comma-separated list containing no white-spaces */
    private val gameTypes: String = gameTypes.replace("\\s".toRegex(), "")

    /** Connection handler for observers and bots */
    private val connHandler: ConnHandler

    /** Current server state */
    private var serverState: ServerState = ServerState.WAIT_FOR_PARTICIPANTS_TO_JOIN

    /** Current game setup */
    private var gameSetup: dev.robocode.tankroyale.server.model.GameSetup? = null

    /** Game participants (bots connections) */
    private val participants = mutableSetOf<WebSocket>()

    /** Game participants that signalled 'ready' for battle */
    private val readyParticipants = mutableSetOf<WebSocket>()

    /** Map over participant ids: bot connection -> bot id */
    private val participantIds = mutableMapOf<WebSocket, BotId>()

    /** Map over bot intents: bot connection -> bot intent */
    private val botIntents: MutableMap<WebSocket, dev.robocode.tankroyale.server.model.BotIntent> = ConcurrentHashMap()

    /** Model updater that keeps track of the game state/model */
    private lateinit var modelUpdater: ModelUpdater

    /** Timer for 'ready' timeout */
    private lateinit var readyTimeoutTimer: NanoTimer

    /** Timer for 'turn' timeout */
    private var turnTimeoutTimer: NanoTimer? = null

    /** Current TPS setting (Turns Per Second) */
    private var tps = DEFAULT_TURNS_PER_SECOND

    /** Logger */
    private val log = LoggerFactory.getLogger(GameServer::class.java)

    /** JSON handler */
    private val gson = Gson()

    init {
        /** Initializes connection handler */
        val serverSetup = ServerSetup(HashSet(listOf(*gameTypes.split(",").toTypedArray())))
        connHandler = ConnHandler(serverSetup, GameServerConnListener(), clientSecret)
    }

    /** Starts this server */
    fun start() {
        log.info("Starting server on port ${Server.port} with game types: $gameTypes")
        connHandler.start()
    }

    /** Stops this server */
    fun stop() {
        log.info("Stopping server")
        connHandler.stop()
    }

    /** Starts the game if all participants are ready */
    private fun startGameIfParticipantsReady() {
        if (readyParticipants.size == participants.size) {
            readyTimeoutTimer.stop()
            readyParticipants.clear()
            botIntents.clear()

            startGame()
        }
    }

    /** Prepares the game and wait for participants to become 'ready' */
    private fun prepareGame() {
        log.debug("Preparing game")

        serverState = ServerState.WAIT_FOR_READY_PARTICIPANTS
        participantIds.clear()
        readyParticipants.clear()

        sendGameStartedToParticipants()

        startReadyTimer()
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
            gameStartedForObserver.participants = createParticipantList()
            broadcastToObserverAndControllers(gameStartedForObserver)
        }
    }

    /** Creates a list of participants from the bot connection handshakes */
    private fun createParticipantList(): List<Participant> {
        val participantList = mutableListOf<Participant>()
        for (conn in participants) {
            val handshake = connHandler.getBotHandshakes()[conn]
            val participant = Participant().apply {
                id = participantIds[conn]!!.value
                name = handshake!!.name
                version = handshake.version
                description = handshake.description
                author = handshake.author
                url = handshake.url
                countryCode = handshake.countryCode
                gameTypes = handshake.gameTypes
                platform = handshake.platform
                programmingLang = handshake.programmingLang
            }
            participantList += participant
        }
        return participantList
    }

    /** Prepares model-updater */
    private fun prepareModelUpdater() {
        modelUpdater = ModelUpdater(gameSetup!!, HashSet(participantIds.values))
    }

    /** Last reset turn timeout period */
    @Volatile // FIXME: Remove?
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

    /**
     * Starts a new game with a new game setup and new participants.
     * @param gameSetup is the new game setup.
     * @param botAddresses is the addresses of the new participants.
     */
    private fun startGame(gameSetup: GameSetup, botAddresses: Collection<BotAddress>) {
        this.gameSetup = GameSetupToGameSetupMapper.map(gameSetup)
        participants.clear()
        participants += connHandler.getBotConnections(botAddresses)

        if (participants.isNotEmpty()) {
            prepareGame()
        }
    }

    /** Aborts current game */
    private fun abortGame() {
        log.info("Aborting game")

        serverState = ServerState.GAME_STOPPED

        broadcastGameAborted()

        // No score is generated for aborted games
    }

    /** Broadcast game-aborted event to all observers and controllers */
    private fun broadcastGameAborted() {
        broadcastToObserverAndControllers(
            GameAbortedEventForObserver().apply {
                `$type` = Message.`$type`.GAME_ABORTED_EVENT_FOR_OBSERVER
            })
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
    private fun getConnection(botId: BotId): WebSocket? {
        for ((conn, id) in participantIds) {
            if (id == botId) {
                return conn
            }
        }
        return null
    }

    /** Pauses current game */
    private fun pauseGame() {
        log.info("Pausing game")

        if (serverState === ServerState.GAME_RUNNING) {
            serverState = ServerState.GAME_PAUSED

            turnTimeoutTimer?.pause()

            broadcastGamedPausedToObservers()
        }
    }

    /** Resumes current game */
    private fun resumeGame() {
        log.info("Resuming game")

        if (serverState === ServerState.GAME_PAUSED) {
            serverState = ServerState.GAME_RUNNING

            turnTimeoutTimer?.resume()

            broadcastGameResumedToObservers()
        }
    }

    /** Broadcast pause event to all observers */
    private fun broadcastGamedPausedToObservers() {
        broadcastToObserverAndControllers(
            GamePausedEventForObserver().apply {
                `$type` = Message.`$type`.GAME_PAUSED_EVENT_FOR_OBSERVER
            })
    }

    /** Broadcast resume event to all observers */
    private fun broadcastGameResumedToObservers() {
        broadcastToObserverAndControllers(
            GameResumedEventForObserver().apply {
                `$type` = Message.`$type`.GAME_RESUMED_EVENT_FOR_OBSERVER
            })
    }

    /**
     * Changes the TPS.
     * @param tps is the new TPS to change to.
     */
    private fun changeTps(tps: Int) {
        log.info("Changing TPS to $tps")

        if (this.tps == tps) return
        this.tps = tps

        broadcastTpsChangedToObservers()

        if (tps == 0) {
            pauseGame()
        } else {
            if (serverState === ServerState.GAME_PAUSED) {
                resumeGame()
            }
            resetTurnTimeout()
        }
    }

    /** Broadcast TPS-changed event to all observers */
    private fun broadcastTpsChangedToObservers() {
        broadcastToObserverAndControllers(
            TpsChangedEvent().apply {
                `$type` = Message.`$type`.TPS_CHANGED_EVENT
                this.tps = tps
            })
    }

    private fun updateGameState(): GameState {
        val mappedBotIntents = mutableMapOf<BotId, dev.robocode.tankroyale.server.model.BotIntent>()
        for ((key, value) in botIntents) {
            val botId = participantIds[key]!!
            mappedBotIntents[botId] = value
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
        log.debug("Next turn => updating game state")
        if (serverState === ServerState.GAME_STOPPED) {
            return
        }
        // Update game state
        val gameState = updateGameState()
        if (gameState.isGameEnded) {
            onGameEnded()
        } else {
            onNextTick(gameState.lastRound)
        }
    }

    private fun onGameEnded() {
        log.info("Game ended")
        serverState = ServerState.GAME_STOPPED
        modelUpdater.calculatePlacements()

        broadcastGameEndedToParticipants()
        broadcastGameEndedToObservers()
    }

    private fun onNextTick(lastRound: Round?) {
        // Send tick
        if (lastRound != null) {
            val turn = lastRound.lastTurn
            if (turn != null) {
                broadcastGameTickToParticipants(lastRound, turn)
                broadcastGameTickToObservers(lastRound, turn)
            }
            broadcastSkippedTurnToParticipant()
        }
        // Clear bot intents
        botIntents.clear()
    }

    private fun broadcastGameEndedToParticipants() {
        broadcastToParticipants(
            GameEndedEventForBot().apply {
                `$type` = Message.`$type`.GAME_ENDED_EVENT_FOR_BOT
                numberOfRounds = modelUpdater.numberOfRounds
                results = getResultsForBots()
            })
    }

    private fun broadcastGameEndedToObservers() {
        broadcastToObserverAndControllers(
            GameEndedEventForObserver().apply {
                `$type` = Message.`$type`.GAME_ENDED_EVENT_FOR_OBSERVER
                numberOfRounds = modelUpdater.numberOfRounds
                results = getResultsForObservers() // Use the stored score!
            })
    }

    private fun broadcastGameTickToParticipants(round: Round, turn: Turn) {
        for (conn in participants) {
            val botId = participantIds[conn]
            if (botId != null) {
                val gameTickForBot = TurnToTickEventForBotMapper.map(round, turn, botId)
                if (gameTickForBot != null) { // Bot alive?
                    send(conn, gameTickForBot)
                }
            }
        }
    }

    private fun broadcastGameTickToObservers(round: Round, turn: Turn) {
        broadcastToObserverAndControllers(TurnToTickEventForObserverMapper.map(round, turn))
    }

    private fun broadcastSkippedTurnToParticipant() {
        // Broadcast skipped turn event to the participant that skipped the turn
        val skippedTurnEvent = SkippedTurnEvent().apply {
            `$type` = Message.`$type`.SKIPPED_TURN_EVENT
            turnNumber = modelUpdater.turnNumber
        }
        connHandler.broadcast(getParticipantsThatSkippedTurn(), gson.toJson(skippedTurnEvent))
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

    private val botsThatSentIntent = mutableListOf<WebSocket>()

    private fun updateBotIntent(conn: WebSocket, intent: BotIntent) {
        if (!participants.contains(conn)) {
            return
        }
        val botIntent = botIntents[conn] ?: dev.robocode.tankroyale.server.model.BotIntent()
        botIntent.update(BotIntentToBotIntentMapper.map(intent))
        botIntents[conn] = botIntent

        // If all bot intents have been received, we can start next turn
        botsThatSentIntent += conn
        if (botIntents.size == botsThatSentIntent.size) {
            botsThatSentIntent.clear()

            onNextTurn()
            turnTimeoutTimer?.reset()
        }
    }

    private fun createBotListUpdateMessage(): Message {
        val botsList = mutableListOf<BotInfo>()
        val botListUpdate = BotListUpdate().apply {
            `$type` = Message.`$type`.BOT_LIST_UPDATE
            bots = botsList
        }
        val botConnections = connHandler.getBotConnections()
        for (conn in botConnections) {
            val address = conn.remoteSocketAddress
            val botInfo = BotHandshakeToBotInfoMapper.map(
                connHandler.getBotHandshakes()[conn]!!,
                address.hostString, address.port
            )
            botsList += botInfo
        }
        return botListUpdate
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

    private fun sendBotListUpdateToObservers() {
        broadcastToObserverAndControllers(createBotListUpdateMessage())
    }

    private inner class GameServerConnListener : ConnListener {
        override fun onException(exception: Exception) {
            exception.printStackTrace()
        }

        override fun onBotJoined(conn: WebSocket, handshake: BotHandshake) {
            log.info("Bot joined: ${getDisplayName(handshake)}")
            sendBotListUpdateToObservers()
        }

        override fun onBotLeft(conn: WebSocket, handshake: BotHandshake) {
            log.info("Bot left: ${getDisplayName(handshake)}")

            // If a bot leaves while in a game, make sure to reset all intent values to zeroes
            botIntents[conn]!!.resetMovement()
            sendBotListUpdateToObservers()
        }

        override fun onObserverJoined(conn: WebSocket, handshake: ObserverHandshake) {
            log.info("Observer joined: ${getDisplayName(handshake)}")
            val msg = createBotListUpdateMessage()
            send(conn, msg)
        }

        override fun onObserverLeft(conn: WebSocket, handshake: ObserverHandshake) {
            log.info("Observer left: ${getDisplayName(handshake)}")
        }

        override fun onControllerJoined(conn: WebSocket, handshake: ControllerHandshake) {
            log.info("Controller joined: ${getDisplayName(handshake)}")
            val msg = createBotListUpdateMessage()
            send(conn, msg)
        }

        override fun onControllerLeft(conn: WebSocket, handshake: ControllerHandshake) {
            log.info("Controller left: ${getDisplayName(handshake)}")
        }

        override fun onBotReady(conn: WebSocket) {
            if (serverState === ServerState.WAIT_FOR_READY_PARTICIPANTS) {
                readyParticipants += conn
                startGameIfParticipantsReady()
            }
        }

        override fun onBotIntent(conn: WebSocket, intent: BotIntent) {
            updateBotIntent(conn, intent)
        }

        override fun onStartGame(gameSetup: GameSetup, botAddresses: Collection<BotAddress>) {
            startGame(gameSetup, botAddresses)
        }

        override fun onAbortGame() {
            abortGame()
        }

        override fun onPauseGame() {
            pauseGame()
        }

        override fun onResumeGame() {
            resumeGame()
        }

        override fun onChangeTps(tps: Int) {
            changeTps(tps)
        }

        private fun getDisplayName(handshake: BotHandshake): String {
            return getDisplayName(handshake.name, handshake.version)
        }

        private fun getDisplayName(handshake: ObserverHandshake): String {
            return getDisplayName(handshake.name, handshake.version)
        }

        private fun getDisplayName(handshake: ControllerHandshake): String {
            return getDisplayName(handshake.name, handshake.version)
        }

        private fun getDisplayName(name: String, version: String): String {
            var displayName = ""
            name.trim().apply {
                if (isNotEmpty()) {
                    displayName = this
                }
            }
            version.trim().apply {
                if (isNotEmpty()) {
                    displayName += " $this"
                }
            }
            return displayName
        }
    }
}
