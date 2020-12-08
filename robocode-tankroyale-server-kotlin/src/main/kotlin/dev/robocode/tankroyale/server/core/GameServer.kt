package dev.robocode.tankroyale.server.dev.robocode.tankroyale.server.core

import com.google.gson.Gson
import dev.robocode.tankroyale.schema.*
import dev.robocode.tankroyale.server.Server.port
import dev.robocode.tankroyale.server.core.ModelUpdater
import dev.robocode.tankroyale.server.core.NanoTimer
import dev.robocode.tankroyale.server.core.RunningState
import dev.robocode.tankroyale.server.core.ServerSetup
import dev.robocode.tankroyale.server.dev.robocode.tankroyale.server.conn.ConnHandler
import dev.robocode.tankroyale.server.dev.robocode.tankroyale.server.conn.ConnListener
import dev.robocode.tankroyale.server.mapper.*
import dev.robocode.tankroyale.server.model.BotId
import dev.robocode.tankroyale.server.model.GameState
import dev.robocode.tankroyale.server.rules.DEFAULT_TURNS_PER_SECOND
import org.java_websocket.WebSocket
import org.java_websocket.exceptions.WebsocketNotConnectedException
import org.slf4j.LoggerFactory
import java.lang.Exception
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.HashMap
import kotlin.collections.HashSet
import kotlin.math.roundToInt


class GameServer(gameTypes: String, clientSecret: String) {
    private val gameTypes: String = gameTypes.replace("\\s".toRegex(), "")
    private val connHandler: ConnHandler
    private var runningState: RunningState
    private var gameSetup: dev.robocode.tankroyale.server.model.GameSetup? = null
    private var participants: Set<WebSocket>? = null
    private var readyParticipants: MutableSet<WebSocket>? = null
    private val participantIds: MutableMap<WebSocket, BotId> = HashMap()
    private val botIntents: MutableMap<WebSocket, dev.robocode.tankroyale.server.model.BotIntent> = ConcurrentHashMap()
    private var readyTimeoutTimer: NanoTimer? = null
    private var turnTimeoutTimer: NanoTimer? = null
    private var tps = DEFAULT_TURNS_PER_SECOND
    private var modelUpdater: ModelUpdater? = null

    private val log = LoggerFactory.getLogger(GameServer::class.java)
    private val gson = Gson()

    init {
        val serverSetup = ServerSetup(HashSet(listOf(*gameTypes.split(",").toTypedArray())))
        val connListener = GameServerConnListener()
        connHandler = ConnHandler(serverSetup, connListener, clientSecret)
        runningState = RunningState.WAIT_FOR_PARTICIPANTS_TO_JOIN
    }

    fun start() {
        log.info("Starting server on port $port with game types: $gameTypes")
        connHandler.start()
    }

    fun stop() {
        log.info("Stopping server")
        connHandler.stop()
    }

    private fun startGameIfParticipantsReady() {
        if (readyParticipants!!.size == participants!!.size) {
            readyTimeoutTimer!!.stop()
            readyParticipants!!.clear()
            botIntents.clear()
            startGame()
        }
    }

    private fun prepareGame() {
        log.debug("Preparing game")
        runningState = RunningState.WAIT_FOR_READY_PARTICIPANTS
        participantIds.clear()

        // Send NewBattle to all participant bots to get them started
        val gameStartedForBot = GameStartedEventForBot()
        gameStartedForBot.`$type` = Message.`$type`.GAME_STARTED_EVENT_FOR_BOT
        gameStartedForBot.gameSetup = GameSetupToGameSetupMapper.map(gameSetup!!)
        var id = 1
        for (conn in participants!!) {
            participantIds[conn] = BotId(id)
            gameStartedForBot.myId = id++
            send(conn, gameStartedForBot)
        }
        readyParticipants = HashSet()

        // Start 'bot-ready' timeout timer
        readyTimeoutTimer = NanoTimer(gameSetup!!.readyTimeout * 1000000L) { onReadyTimeout() }
        readyTimeoutTimer!!.start()
    }

    private fun startGame() {
        log.info("Starting game")
        runningState = RunningState.GAME_RUNNING
        val participantList: MutableList<Participant> = ArrayList()
        for (conn in participants!!) {
            val h = connHandler.getBotHandshakes()[conn]
            val p = Participant()
            p.id = participantIds[conn]!!.value
            p.name = h!!.name
            p.version = h.version
            p.description = h.description
            p.author = h.author
            p.url = h.url
            p.countryCode = h.countryCode
            p.gameTypes = h.gameTypes
            p.platform = h.platform
            p.programmingLang = h.programmingLang
            participantList.add(p)
        }

        // Send GameStarted to all participant observers to get them started
        if (connHandler.observerAndControllerConnections.isNotEmpty()) {
            val gameStartedForObserver = GameStartedEventForObserver()
            gameStartedForObserver.`$type` = Message.`$type`.GAME_STARTED_EVENT_FOR_OBSERVER
            gameStartedForObserver.gameSetup = GameSetupToGameSetupMapper.map(gameSetup!!)
            gameStartedForObserver.participants = participantList
            broadcastToObserverAndControllers(gameStartedForObserver)
        }

        // Prepare model update
        modelUpdater = ModelUpdater(gameSetup!!, HashSet(participantIds.values))

        // Restart turn timeout timer
        resetTimeoutTimer()
    }

    @Volatile
    var lastTurnTimeout: Long = 0
    private fun resetTimeoutTimer() {
        val timeout = calculateTurnTimeout()
        if (timeout != lastTurnTimeout) {
            lastTurnTimeout = timeout
            if (turnTimeoutTimer != null) {
                turnTimeoutTimer!!.stop()
            }
            turnTimeoutTimer = NanoTimer(timeout) { onNextTurn() }
            turnTimeoutTimer!!.start()
        }
    }

    private fun calculateTurnTimeout(): Long {
        var timeout: Long
        timeout = if (tps <= 0) {
            0
        } else {
            (1000000000 / tps).toLong()
        }
        val turnTimeout = gameSetup!!.turnTimeout * 1000
        if (turnTimeout > timeout) {
            timeout = turnTimeout.toLong()
        }
        return timeout
    }

    private fun startGame(
        gameSetup: GameSetup, botAddresses: Collection<BotAddress>
    ) {
        this.gameSetup = GameSetupToGameSetupMapper.map(gameSetup)
        participants = connHandler.getBotConnections(botAddresses)
        if (participants!!.isNotEmpty()) {
            prepareGame()
        }
    }

    private fun abortGame() {
        log.info("Aborting game")
        runningState = RunningState.GAME_STOPPED
        val abortedEvent = GameAbortedEventForObserver()
        abortedEvent.`$type` = Message.`$type`.GAME_ABORTED_EVENT_FOR_OBSERVER
        broadcastToObserverAndControllers(abortedEvent)

        // No score is generated for aborted games
    }

    private val resultsForBots: List<BotResultsForBot>
        get() {
            val results: MutableList<BotResultsForBot> = ArrayList()
            modelUpdater?.results?.forEach { score ->
                val result = BotResultsForBot()
                results + result
                result.id = score.botId.value
                result.survival = score.survival.roundToInt()
                result.lastSurvivorBonus = score.lastSurvivorBonus.roundToInt()
                result.bulletDamage = score.bulletDamage.roundToInt()
                result.bulletKillBonus = score.bulletKillBonus.toInt()
                result.ramDamage = score.ramDamage.roundToInt()
                result.ramKillBonus = score.ramKillBonus.roundToInt()
                result.totalScore = score.totalScore.roundToInt()
                result.firstPlaces = score.firstPlaces
                result.secondPlaces = score.secondPlaces
                result.thirdPlaces = score.thirdPlaces
            }
            var rank = 1
            results.forEach { it.rank = rank++ }
            return results
        }
    private val resultsForObservers: List<BotResultsForObserver>
        get() {
            val results: MutableList<BotResultsForObserver> = ArrayList()
            modelUpdater?.results?.forEach { score ->
                val result = BotResultsForObserver()
                results + result
                result.id = score.botId.value
                result.survival = score.survival.roundToInt()
                result.lastSurvivorBonus = score.lastSurvivorBonus.roundToInt()
                result.bulletDamage = score.bulletDamage.roundToInt()
                result.bulletKillBonus = score.bulletKillBonus.toInt()
                result.ramDamage = score.ramDamage.roundToInt()
                result.ramKillBonus = score.ramKillBonus.roundToInt()
                result.totalScore = score.totalScore.roundToInt()
                result.firstPlaces = score.firstPlaces
                result.secondPlaces = score.secondPlaces
                result.thirdPlaces = score.thirdPlaces

                var conn: WebSocket? = null
                for ((key, value) in participantIds) {
                    if (value == score.botId) {
                        conn = key
                        break
                    }
                }
                val botHandshake = connHandler.getBotHandshakes()[conn]
                result.name = botHandshake!!.name
                result.version = botHandshake.version
            }
            var rank = 1
            results.forEach { it.rank = rank++ }
            return results
        }

    private fun pauseGame() {
        log.info("Pausing game")
        if (runningState === RunningState.GAME_RUNNING) {
            runningState = RunningState.GAME_PAUSED
            turnTimeoutTimer?.pause()
            val pausedEvent = GamePausedEventForObserver()
            pausedEvent.`$type` = Message.`$type`.GAME_PAUSED_EVENT_FOR_OBSERVER
            broadcastToObserverAndControllers(pausedEvent)
        }
    }

    private fun resumeGame() {
        log.info("Resuming game")
        if (runningState === RunningState.GAME_PAUSED) {
            runningState = RunningState.GAME_RUNNING
            val resumedEvent = GameResumedEventForObserver()
            resumedEvent.`$type` = Message.`$type`.GAME_RESUMED_EVENT_FOR_OBSERVER
            broadcastToObserverAndControllers(resumedEvent)
            if (turnTimeoutTimer != null) {
                turnTimeoutTimer!!.resume()
            }
        }
    }

    private fun changeTps(tps: Int) {
        log.info("Changing TPS: $tps")
        if (this.tps == tps) {
            return
        }
        this.tps = tps
        val tpsChangedEvent = TpsChangedEvent()
        tpsChangedEvent.`$type` = Message.`$type`.TPS_CHANGED_EVENT
        tpsChangedEvent.tps = tps
        broadcastToObserverAndControllers(tpsChangedEvent)
        if (tps == 0) {
            pauseGame()
        } else {
            resetTimeoutTimer()
            if (runningState === RunningState.GAME_PAUSED) {
                resumeGame()
            }
        }
    }

    private fun updateGameState(): GameState {
        val mappedBotIntents: MutableMap<BotId, dev.robocode.tankroyale.server.model.BotIntent> = HashMap()
        for ((key, value) in botIntents) {
            val botId = participantIds[key]!!
            mappedBotIntents[botId] = value
        }
        return modelUpdater!!.update(Collections.unmodifiableMap(mappedBotIntents))
    }

    private fun onReadyTimeout() {
        log.debug("Ready timeout")
        if (readyParticipants!!.size >= gameSetup!!.minNumberOfParticipants) {
            // Start the game with the participants that are ready
            participants = readyParticipants
            startGame()
        } else {
            // Not enough participants -> prepare another game
            runningState = RunningState.WAIT_FOR_PARTICIPANTS_TO_JOIN
        }
    }

    @Synchronized
    private fun onNextTurn() {
        log.debug("Next turn => updating game state")
        if (runningState !== RunningState.GAME_STOPPED) {
            // Update game state
            val gameState = updateGameState()
            if (gameState.isGameEnded) {
                runningState = RunningState.GAME_STOPPED
                log.info("Game ended")
                modelUpdater!!.calculatePlacements()

                // End game for bots
                val endEventForBot = GameEndedEventForBot()
                endEventForBot.`$type` = Message.`$type`.GAME_ENDED_EVENT_FOR_BOT
                endEventForBot.numberOfRounds = modelUpdater!!.numberOfRounds
                endEventForBot.results = resultsForBots
                broadcastToBots(endEventForBot)

                // End game for observers
                val endEventForObserver = GameEndedEventForObserver()
                endEventForObserver.`$type` = Message.`$type`.GAME_ENDED_EVENT_FOR_OBSERVER
                endEventForObserver.numberOfRounds = modelUpdater!!.numberOfRounds
                endEventForObserver.results = resultsForObservers // Use the stored score!
                broadcastToObserverAndControllers(endEventForObserver)
            } else {
                // Send tick
                val round = gameState.lastRound
                if (round != null) {
                    val turn = round.lastTurn
                    if (turn != null) {
                        // Send game state as 'game tick' to participants
                        for (conn in participants!!) {
                            val gameTickForBot = TurnToTickEventForBotMapper.map(
                                round, turn, participantIds[conn]!!
                            )
                            if (gameTickForBot != null) { // Bot alive?
                                send(conn, gameTickForBot)
                            }
                        }
                        val gameTickForObserver = TurnToTickEventForObserverMapper.map(round, turn)
                        broadcastToObserverAndControllers(gameTickForObserver)
                    }

                    // Send SkippedTurnEvents to all bots that skipped a turn, i.e. where the server did not
                    // receive a bot intent before the turn ended.
                    participantIds.keys.forEach { conn: WebSocket ->
                        if (botIntents[conn] == null) {
                            val skippedTurnEvent = SkippedTurnEvent()
                            skippedTurnEvent.`$type` = Message.`$type`.SKIPPED_TURN_EVENT
                            skippedTurnEvent.turnNumber = modelUpdater!!.turnNumber
                            send(conn, skippedTurnEvent)
                        }
                    }
                }
                // Clear bot intents
                botIntents.clear()
            }
        }
    }

    private fun updateBotIntent(conn: WebSocket, intent: BotIntent) {
        if (!participants!!.contains(conn)) {
            return
        }
        val botIntent = botIntents[conn] ?: dev.robocode.tankroyale.server.model.BotIntent()
        botIntent.update(BotIntentToBotIntentMapper.map(intent))

        // If all bot intents have been received, we can start next turn
        /*
        botsThatSentIntent.add(conn);
        if (botIntents.size() == botsThatSentIntent.size()) {
            botsThatSentIntent.clear();

            onNextTurn();
            turnTimeoutTimer.reset();
        }
        */
    }

    private fun createBotListUpdateMessage(): Message {
        val botListUpdate = BotListUpdate()
        botListUpdate.`$type` = Message.`$type`.BOT_LIST_UPDATE
        val bots: MutableList<BotInfo> = ArrayList()
        botListUpdate.bots = bots
        val botConnections = connHandler.getBotConnections()
        for (conn in botConnections) {
            val address = conn.remoteSocketAddress
            val botInfo = BotHandshakeToBotInfoMapper.map(connHandler.getBotHandshakes()[conn]!!,
                address.hostString, address.port
            )
            bots.add(botInfo)
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

    private fun broadcastToBots(msg: Message) {
        requireNotNull(msg.`$type`) { "\$type is required on the message" }
        connHandler.broadcastToBots(gson.toJson(msg))
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
            if (runningState === RunningState.WAIT_FOR_READY_PARTICIPANTS) {
                readyParticipants!!.add(conn)
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
            val trimmedName = name.trim()
            if (trimmedName.isNotEmpty()) {
                displayName = trimmedName
            }
            val trimmedVersion = version.trim()
            if (trimmedVersion.isNotEmpty()) {
                displayName += " $trimmedVersion"
            }
            return displayName
        }
    }
}
