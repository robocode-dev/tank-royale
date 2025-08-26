package dev.robocode.tankroyale.gui.player

import dev.robocode.tankroyale.client.model.*
import dev.robocode.tankroyale.common.Event
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
        // Load messages from the replay file
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
                currentEventIndex++
            }
        } else {
            throw IllegalArgumentException("Invalid replay file: ${replayFile.absolutePath}")
        }

        // TPS changes are now handled by BattleManager calling changeTps()
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

        // Clear stdout/stderr state
        savedStdOutput.clear()
        savedStdError.clear()

        // Emit "connected" event to simulate connection
        onConnected.fire(Unit)

        // Start processing messages
        startPlayback()
    }

    override fun stop() {
        stopPlaybackTimer()
        isRunning.set(false)
        isPaused.set(false)
        currentMessageIndex = 0

        // Fire game aborted if we were running
        if (currentGameSetup != null) {
            onGameAborted.fire(GameAbortedEvent)
        }
        onReplayEvent.fire(currentMessageIndex)
    }

    override fun pause() {
        if (isRunning.get() && !isPaused.get()) {
            isPaused.set(true)
            playbackTimer?.stop()
            onGamePaused.fire(GamePausedEvent)
        }
    }

    override fun resume() {
        if (isRunning.get() && isPaused.get()) {
            isPaused.set(false)
            continuePlayback()
            onGameResumed.fire(GameResumedEvent)
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
                onSeekToTurn.fire(it as TickEvent)
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

    override fun getParticipant(botId: Int): Participant = participants.first { participant -> participant.id == botId }

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
            onReplayEvent.fire(currentMessageIndex)
            currentMessageIndex++
        }
    }

    private fun handleMessage(message: Message) {
        when (message) {
            is GameStartedEvent -> {
                currentGameSetup = message.gameSetup
                participants = message.participants
                onGameStarted.fire(message)
            }

            is GameEndedEvent -> {
                onGameEnded.fire(message)
            }

            is GameAbortedEvent -> {
                onGameAborted.fire(message)
            }

            is RoundStartedEvent -> {
                onRoundStarted.fire(message)
            }

            is RoundEndedEvent -> {
                onRoundEnded.fire(message)
            }

            is TickEvent -> {
                currentTick = message
                onTickEvent.fire(message)
                updateSavedStdOutput(message)
            }

            is BotListUpdate -> {
                onBotListUpdate.fire(message)
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
                        // Update stdout/stderr state without firing events
                        updateSavedStdOutputSilently(message)
                    }

                    else -> {
                        // Only track state-changing messages, ignore events
                    }
                }
            }
        }
    }

    private fun updateSavedStdOutput(tickEvent: TickEvent) {
        tickEvent.apply {
            botStates.forEach { botState ->
                val id = botState.id
                botState.stdOut?.let { updateStandardOutput(savedStdOutput, id, roundNumber, turnNumber, it) }
                botState.stdErr?.let { updateStandardOutput(savedStdError, id, roundNumber, turnNumber, it) }
            }
            onStdOutputUpdated.fire(tickEvent)
        }
    }

    private fun updateSavedStdOutputSilently(tickEvent: TickEvent) {
        tickEvent.apply {
            botStates.forEach { botState ->
                val id = botState.id
                botState.stdOut?.let { updateStandardOutput(savedStdOutput, id, roundNumber, turnNumber, it) }
                botState.stdErr?.let { updateStandardOutput(savedStdError, id, roundNumber, turnNumber, it) }
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
