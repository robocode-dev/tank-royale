package dev.robocode.tankroyale.gui.player

import dev.robocode.tankroyale.client.model.*
import dev.robocode.tankroyale.common.event.Event
import dev.robocode.tankroyale.gui.replay.ReplayFileReader
import dev.robocode.tankroyale.gui.settings.ConfigSettings
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean
import javax.swing.Timer

/**
 * Battle player implementation for replaying battles from recorded files.
 * Provides the same interface as LiveBattlePlayer but reads events from a file
 * and emits them at the configured TPS rate for TickEvents only.
 */
class ReplayBattlePlayer(private val replayFile: File) : BattlePlayer {

    private var currentGameSetup: GameSetup? = null
    private var currentTick: TickEvent? = null
    private var participants = listOf<Participant>()

    private val savedStdOutput =
        mutableMapOf<Int /* BotId */, MutableMap<Int /* round */, MutableMap<Int /* turn */, String>>>()
    private val savedStdError =
        mutableMapOf<Int /* BotId */, MutableMap<Int /* round */, MutableMap<Int /* turn */, String>>>()

    private val isRunning = AtomicBoolean(false)
    private val isPaused = AtomicBoolean(false)

    private var turns = mutableListOf<List<Message>>()
    private var currentMessageIndex = 0
    private var currentTps = ConfigSettings.tps

    private var playbackTimer: Timer? = null

    // Events
    override val onConnected = Event<Unit>()
    override val onGameStarted = Event<GameStartedEvent>()
    override val onGameEnded = Event<GameEndedEvent>()
    override val onGameAborted = Event<GameAbortedEvent>()
    override val onGamePaused = Event<GamePausedEvent>()
    override val onGameResumed = Event<GameResumedEvent>()
    override val onRoundStarted = Event<RoundStartedEvent>()
    override val onRoundEnded = Event<RoundEndedEvent>()
    override val onTickEvent = Event<TickEvent>()
    override val onBotListUpdate = Event<BotListUpdate>()
    override val onStdOutputUpdated = Event<TickEvent>()
    override val onSeekToTurn = Event<TickEvent>()

    val onReplayEvent = Event<Int>()

    init {
        val fileReader = ReplayFileReader(replayFile)
        if (fileReader.isValid()) {
            val events = fileReader.loadMessages()
            var currentEventIndex = 0
            var currentRound: MutableList<Message>? = null
            var containsTickEvent = false
            while (currentEventIndex < events.size) {
                if (currentRound == null || (events[currentEventIndex] is TickEvent && containsTickEvent)) {
                    currentRound = mutableListOf()
                    containsTickEvent = false
                    turns.add(currentRound)
                }
                var event = events[currentEventIndex]
                currentRound.add(event)
                if (event is TickEvent) {
                    containsTickEvent = true
                }
                // Extract GameStartedEvent on first encounter
                if (event is GameStartedEvent && currentGameSetup == null) {
                    currentGameSetup = event.gameSetup
                    participants = event.participants
                }
                // Extract participant info from the first TickEvent if no GameStartedEvent found
                if (event is TickEvent && participants.isEmpty()) {
                    participants = event.botStates.map { botState ->
                        Participant(
                            id = botState.id,
                            name = botState.name ?: "Bot ${botState.id}",
                            version = botState.version ?: "1.0",
                            authors = emptyList(),
                            description = "",
                            homepage = null,
                            countryCodes = emptyList(),
                            gameTypes = emptySet(),
                            platform = "UNKNOWN",
                            programmingLang = "UNKNOWN",
                            initialPosition = null,
                            teamId = null,
                            teamName = null,
                            teamVersion = null,
                            sessionId = botState.sessionId,
                            isDroid = botState.isDroid
                        )
                    }
                }
                currentEventIndex++
            }
        } else {
            throw IllegalArgumentException("Invalid replay file: ${replayFile.absolutePath}")
        }
    }

    fun getTotalRounds() = turns.size

    /**
     * @return list of pairs where first is the turn number and second is boolean indicating whether
     * this death also means end of round
     */
    fun getDeathMarkers() = turns
        .mapIndexed { index, turn -> index to turn }
        .filter { (_, turn) ->
            (turn.firstOrNull { it is TickEvent } as TickEvent?)
                ?.events?.any { it is BotDeathEvent } ?: false
        }
        .map { (i, turn) -> i to turn.any { it is RoundEndedEvent } }

    override fun start() {
        if (isRunning.get()) {
            stop()
        }

        currentMessageIndex = 0
        isRunning.set(true)
        isPaused.set(false)

        savedStdOutput.clear()
        savedStdError.clear()

        onConnected(Unit)

        // Participants are extracted in init block from GameStartedEvent or first TickEvent
        if (participants.isNotEmpty()) {
            // If GameStartedEvent was found, use its setup; otherwise create a synthetic one
            val setup = currentGameSetup ?: GameSetup(
                gameType = "CLASSIC",
                arenaWidth = 800,
                isArenaWidthLocked = false,
                arenaHeight = 600,
                isArenaHeightLocked = false,
                minNumberOfParticipants = 2,
                isMinNumberOfParticipantsLocked = false,
                maxNumberOfParticipants = null,
                isMaxNumberOfParticipantsLocked = false,
                numberOfRounds = 1,
                isNumberOfRoundsLocked = false,
                gunCoolingRate = 0.1,
                isGunCoolingRateLocked = false,
                maxInactivityTurns = 10000,
                isMaxInactivityTurnsLocked = false,
                turnTimeout = 30000,
                isTurnTimeoutLocked = false,
                readyTimeout = 30000,
                isReadyTimeoutLocked = false,
                defaultTurnsPerSecond = 30
            )
            currentGameSetup = setup
            onGameStarted(GameStartedEvent(setup, participants))
        }

        startPlayback()
    }

    override fun stop() {
        stopPlaybackTimer()
        isRunning.set(false)
        isPaused.set(false)
        currentMessageIndex = 0

        // Fire game aborted if we were running
        if (currentGameSetup != null) {
            onGameAborted(GameAbortedEvent)
        }
        onReplayEvent(currentMessageIndex)
    }

    override fun pause() {
        if (isRunning.get() && !isPaused.get()) {
            isPaused.set(true)
            playbackTimer?.stop()
            onGamePaused(GamePausedEvent)
        }
    }

    override fun resume() {
        if (isRunning.get() && isPaused.get()) {
            isPaused.set(false)
            continuePlayback()
            onGameResumed(GameResumedEvent)
        }
    }

    override fun nextTurn() {
        if (isRunning.get() && isPaused.get()) {
            // When paused, advance to next tick event
            processCurrentMessage()
        }
    }

    override fun restart() {
        stop()
        start()
    }

    fun seekToTurn(turnNumber: Int) {
        val wasRunning = isRunning.get()
        val wasPaused = isPaused.get()

        if (turnNumber >= 0) {
            // Rebuild stdout state up to the target turn
            rebuildStateUpToIndex(turnNumber)
            currentMessageIndex = turnNumber

            // If we were running, continue; if paused, stay paused
            if (wasRunning) {
                if (wasPaused) {
                    // Process current message to update state, but stay paused
                    processCurrentMessage()
                    pause()
                } else {
                    // Continue playing from new position
                    continuePlayback()
                }
            }

            turns[turnNumber].firstOrNull { it is TickEvent }?.let {
                onSeekToTurn(it as TickEvent)
            }
        }
    }

    override fun isRunning(): Boolean = isRunning.get()

    override fun isPaused(): Boolean = isPaused.get()

    override fun getCurrentGameSetup(): GameSetup? = currentGameSetup

    override fun getCurrentTick(): TickEvent? = currentTick

    override fun getJoinedBots(): Set<BotInfo> {
        // Convert participants to BotInfo objects for compatibility
        return participants.map { participant ->
            BotInfo(
                name = participant.name,
                version = participant.version,
                authors = participant.authors,
                description = participant.description,
                homepage = participant.homepage,
                countryCodes = participant.countryCodes,
                gameTypes = participant.gameTypes,
                platform = participant.platform,
                programmingLang = participant.programmingLang,
                initialPosition = participant.initialPosition,
                teamId = participant.teamId,
                teamName = participant.teamName,
                teamVersion = participant.teamVersion,
                host = "replay", // Placeholder host for replay
                port = participant.id, // Use participant ID as port for uniqueness
                sessionId = participant.sessionId
            )
        }.toSet()
    }

    override fun getParticipant(botId: Int): Participant {
        return participants.firstOrNull { participant -> participant.id == botId }
            ?: throw IllegalStateException("Participant with id $botId not found in replay. Available participants: ${participants.map { it.id }}")
    }

    override fun getStandardOutput(botId: Int): Map<Int /* round */, Map<Int /* turn */, String>>? =
        savedStdOutput[botId]

    override fun getStandardError(botId: Int): Map<Int /* round */, Map<Int /* turn */, String>>? = savedStdError[botId]

    override fun changeBotPolicy(botPolicyUpdate: BotPolicyUpdate) {
        // the events are already recorded, change policy is not supported when running replay
    }

    override fun changeTps(tps: Int) {
        currentTps = tps
        // No need to update timer immediately - it will use the new TPS on next tick
    }

    private fun startPlayback() {
        // Process all non-tick messages immediately until we hit a tick
        processUntilNextChunk()
    }

    private fun continuePlayback() {
        // Continue from where we left off
        processUntilNextChunk()
    }

    private fun processUntilNextChunk() {
        if (isRunning.get() && !isPaused.get()) {
            if (currentMessageIndex < turns.size) {
                processCurrentMessage()
                startTickTimer()
            }
        } else {
            stop()
        }
    }

    private fun startTickTimer() {
        stopPlaybackTimer()

        val delay = if (currentTps > 0) 1000 / currentTps else -1
        playbackTimer = Timer(delay) {
            if (isRunning.get() && !isPaused.get()) {
                // Process next batch of messages
                processUntilNextChunk()
            }
        }
        playbackTimer?.isRepeats = false // Only fire once
        playbackTimer?.start()
    }

    private fun stopPlaybackTimer() {
        playbackTimer?.stop()
        playbackTimer = null
    }

    private fun processCurrentMessage() {
        if (currentMessageIndex < turns.size) {
            val messageList = turns[currentMessageIndex]
            messageList.forEach {
                handleMessage(it)
            }
            onReplayEvent(currentMessageIndex)
            currentMessageIndex++
        }
    }

    private fun handleMessage(message: Message) {
        when (message) {
            is GameStartedEvent -> {
                currentGameSetup = message.gameSetup
                participants = message.participants
                onGameStarted(message)
            }

            is GameEndedEvent -> {
                onGameEnded(message)
            }

            is GameAbortedEvent -> {
                onGameAborted(message)
            }

            is RoundStartedEvent -> {
                onRoundStarted(message)
            }

            is RoundEndedEvent -> {
                onRoundEnded(message)
            }

            is TickEvent -> {
                currentTick = message
                onTickEvent(message)
                updateSavedStdOutput(message)
            }

            is BotListUpdate -> {
                onBotListUpdate(message)
            }

            else -> {
                // Ignore other message types (server handshakes, etc.)
            }
        }
    }

    private fun rebuildStateUpToIndex(targetIndex: Int) {
        // Clear current state
        savedStdOutput.clear()
        savedStdError.clear()
        currentGameSetup = null
        currentTick = null
        val originalParticipants = participants // Preserve participants extracted in init
        participants = listOf()

        // Replay all messages up to target index to rebuild state
        for (i in 0 until targetIndex) {
            turns[i].forEach { message ->
                when (message) {
                    is GameStartedEvent -> {
                        currentGameSetup = message.gameSetup
                        participants = message.participants
                    }

                    is TickEvent -> {
                        currentTick = message
                        // Extract participants from first TickEvent if not found from GameStartedEvent
                        if (participants.isEmpty()) {
                            participants = message.botStates.map { botState ->
                                Participant(
                                    id = botState.id,
                                    name = botState.name ?: "Bot ${botState.id}",
                                    version = botState.version ?: "1.0",
                                    authors = emptyList(),
                                    description = "",
                                    homepage = null,
                                    countryCodes = emptyList(),
                                    gameTypes = emptySet(),
                                    platform = "UNKNOWN",
                                    programmingLang = "UNKNOWN",
                                    initialPosition = null,
                                    teamId = null,
                                    teamName = null,
                                    teamVersion = null,
                                    sessionId = botState.sessionId,
                                    isDroid = botState.isDroid
                                )
                            }
                        }
                        // Update stdout/stderr state without firing events
                        updateSavedStdOutput(message, fireEvent = false)
                    }

                    else -> {
                        // Only track state-changing messages, ignore events
                    }
                }
            }
        }

        // If we still don't have participants (empty replay or targetIndex is 0), restore the original
        if (participants.isEmpty()) {
            participants = originalParticipants
        }
    }

    private fun updateSavedStdOutput(tickEvent: TickEvent, fireEvent: Boolean = true) {
        tickEvent.apply {
            botStates.forEach { botState ->
                val id = botState.id
                botState.stdOut?.let { updateStandardOutput(savedStdOutput, id, roundNumber, turnNumber, it) }
                botState.stdErr?.let { updateStandardOutput(savedStdError, id, roundNumber, turnNumber, it) }
            }
            if (fireEvent) {
                onStdOutputUpdated(tickEvent)
            }
        }
    }

    private fun updateStandardOutput(
        stdOutputMaps: MutableMap<Int /* BotId */, MutableMap<Int /* round */, MutableMap<Int /* turn */, String>>>,
        id: Int, round: Int, turn: Int, output: String
    ) {
        stdOutputMaps
            .getOrPut(id) { LinkedHashMap() }
            .getOrPut(round) { LinkedHashMap() }[turn] = output
    }
}
