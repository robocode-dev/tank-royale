package dev.robocode.tankroyale.recorder.core

import dev.robocode.tankroyale.client.WebSocketClient
import dev.robocode.tankroyale.client.WebSocketClientEvents
import dev.robocode.tankroyale.client.model.MessageConstants
import dev.robocode.tankroyale.client.model.ObserverHandshake
import dev.robocode.tankroyale.client.model.ServerHandshake
import dev.robocode.tankroyale.common.util.Version
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.slf4j.LoggerFactory
import java.net.URI
import java.util.concurrent.CountDownLatch

class RecordingObserver(
    private val url: String,
    private val secret: String? = null,
    private val dir: String? = null
) {
    companion object {
        private val startRecordingEvents: Set<String> = setOf("GameStartedEventForObserver")
        private val endRecordingEvents: Set<String> = setOf("GameAbortedEvent", "GameEndedEventForObserver")
        private val eventsToRecord: Set<String> = setOf(
            "GameAbortedEvent",
            "GameEndedEventForObserver",
            "GameStartedEventForObserver",
            "RoundEndedEventForObserver",
            "RoundStartedEventForObserver",
            "TickEventForObserver",
        )
    }

    private val log = LoggerFactory.getLogger(this::class.java)
    private val client = WebSocketClient(URI(url))
    private val latch = CountDownLatch(1)
    private var recorder: GameRecorder? = null

    fun start() {
        WebSocketClientEvents.apply {
            onOpen.subscribe(client) { log.info("Connection to server established") }
            onMessage.subscribe(client) { onMessage(it) }
            onError.subscribe(client) {
                log.error("WebSocket error: ${it.message}", it)
                unsubscribeAll()
                latch.countDown()
            }
            onClose.subscribe(client) {
                log.info("Connection to server closed")
                unsubscribeAll()
                latch.countDown()
            }
            try {
                client.open() // must be called AFTER onOpen.subscribe()
            } catch (e: Exception) {
                log.error(
                    "Failed to connect to server at $url. Please ensure the server is running and the URL is correct.",
                    e
                )
                unsubscribeAll()
                latch.countDown()
            }
        }
    }

    private fun extractType(jsonElement: JsonElement): String? {
        if (jsonElement is JsonObject && "type" in jsonElement) {
            return jsonElement["type"]?.jsonPrimitive?.content
        }
        log.warn("Could not extract type from JSON element: {}", jsonElement)
        return null
    }

    private fun onMessage(msg: String) {
        log.debug("Received message: {}", msg)
        val jsonElement: JsonElement = Json.parseToJsonElement(msg)
        val type = extractType(jsonElement)
        if (type == "ServerHandshake") {
            handleServerHandshake(jsonElement)
        } else {
            if (startRecordingEvents.contains(type)) {
                startRecording()
                log.info("Starting recording to file: ${recorder?.file?.absolutePath}")
            }
            if (eventsToRecord.contains(type)) {
                recorder?.record(jsonElement)
            }
            if (endRecordingEvents.contains(type)) {
                stopRecording()
            }
        }
    }

    fun handleServerHandshake(jsonElement: JsonElement) {
        val serverHandshake = MessageConstants.json.decodeFromJsonElement(ServerHandshake.serializer(), jsonElement)
        log.info("Connected to server: ${serverHandshake.name} (version: ${serverHandshake.version})")

        val handshake = ObserverHandshake(
            sessionId = serverHandshake.sessionId,
            name = "Robocode Tank Royale Recorder",
            version = Version.version,
            author = "Jan Durovec",
            secret = secret
        )
        client.send(handshake)
    }

    fun awaitClose() {
        latch.await()
    }

    private fun startRecording() {
        stopRecording()
        recorder = GameRecorder(dir)
    }

    private fun stopRecording() {
        recorder?.let {
            it.close()
            log.info("Game recording stopped. Recorded to file: ${recorder?.file?.absolutePath}")
            recorder = null
        }
    }

    private fun unsubscribeAll() {
        WebSocketClientEvents.apply {
            onOpen.unsubscribe(client)
            onMessage.unsubscribe(client)
            onError.unsubscribe(client)
        }
    }

    fun stop() {
        unsubscribeAll()
        stopRecording()
        if (client.isOpen()) {
            client.close()
        }
        latch.countDown()
    }
}