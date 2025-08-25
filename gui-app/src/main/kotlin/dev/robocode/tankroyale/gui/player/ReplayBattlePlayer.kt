package dev.robocode.tankroyale.gui.player

import dev.robocode.tankroyale.client.model.*
import dev.robocode.tankroyale.common.Event
import dev.robocode.tankroyale.gui.replay.ReplayFileReader
import dev.robocode.tankroyale.gui.ui.tps.TpsEvents
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

    private var messages = listOf<Message>()
    private var currentMessageIndex = 0
    private var currentTps = 30

    private var playbackTimer: Timer? = null

    // Events - same as LiveBattlePlayer
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

    init {
        // Load messages from the replay file
        val fileReader = ReplayFileReader(replayFile)
        if (fileReader.isValid()) {
            messages = fileReader.loadMessages()
        } else {
            throw IllegalArgumentException("Invalid replay file: ${replayFile.absolutePath}")
        }

        // Listen for TPS changes to adjust tick playback speed
        TpsEvents.onTpsChanged.subscribe(this) { event ->
            currentTps = event.tps
        }
    }

    override fun getSupportedFeatures(): Set<BattlePlayerFeature> {
        return setOf(BattlePlayerFeature.SEEK)
    }

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
            advanceToNextTick()
        }
    }

    override fun restart() {
        stop()
        start()
    }

    override fun seekToTurn(turnNumber: Int) {
        val wasRunning = isRunning.get()
        val wasPaused = isPaused.get()

        // Find the message index for the specified turn
        val targetIndex = findMessageIndexForTurn(turnNumber)
        if (targetIndex >= 0) {
            // Rebuild stdout state up to the target turn
            rebuildStateUpToIndex(targetIndex)
            currentMessageIndex = targetIndex

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
        }
    }

    override fun isRunning(): Boolean = isRunning.get()

    override fun isPaused(): Boolean = isPaused.get()

    override fun getCurrentGameSetup(): GameSetup? = currentGameSetup

    override fun getCurrentTick(): TickEvent? = currentTick

    fun getParticipant(botId: Int): Participant = participants.first { participant -> participant.id == botId }

    fun getStandardOutput(botId: Int): Map<Int /* round */, Map<Int /* turn */, String>>? = savedStdOutput[botId]

    fun getStandardError(botId: Int): Map<Int /* round */, Map<Int /* turn */, String>>? = savedStdError[botId]

    private fun startPlayback() {
        // Process all non-tick messages immediately until we hit a tick
        processMessagesUntilTick()
    }

    private fun continuePlayback() {
        // Continue from where we left off
        processMessagesUntilTick()
    }

    private fun processMessagesUntilTick() {
        // Process all non-tick events immediately
        while (currentMessageIndex < messages.size && isRunning.get() && !isPaused.get()) {
            val message = messages[currentMessageIndex]

            if (message is TickEvent) {
                // Found a tick - process it and set up timer for next tick
                handleMessage(message)
                currentMessageIndex++

                // Set up timer for next tick processing
                startTickTimer()
                break
            } else {
                // Non-tick event - process immediately
                handleMessage(message)
                currentMessageIndex++
            }
        }

        // If we've reached the end without finding a tick, we're done
        if (currentMessageIndex >= messages.size) {
            stop()
        }
    }

    private fun startTickTimer() {
        stopPlaybackTimer()

        val delay = if (currentTps > 0) 1000 / currentTps else -1
        playbackTimer = Timer(delay) {
            if (isRunning.get() && !isPaused.get()) {
                // Process next batch of messages
                processMessagesUntilTick()
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
        if (currentMessageIndex < messages.size) {
            val message = messages[currentMessageIndex]
            handleMessage(message)
        }
    }

    private fun advanceToNextTick() {
        // Find next tick event and process all messages up to and including it
        while (currentMessageIndex < messages.size) {
            val message = messages[currentMessageIndex]
            handleMessage(message)
            currentMessageIndex++

            if (message is TickEvent) {
                break
            }
        }
    }

    private fun findMessageIndexForTurn(turnNumber: Int): Int {
        var tickCount = 0
        for (i in messages.indices) {
            val message = messages[i]
            if (message is TickEvent) {
                if (tickCount == turnNumber) {
                    return i
                }
                tickCount++
            }
        }
        return -1 // Turn not found
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
            when (val message = messages[i]) {
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
