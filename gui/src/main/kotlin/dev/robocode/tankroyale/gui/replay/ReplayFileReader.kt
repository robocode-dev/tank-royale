package dev.robocode.tankroyale.gui.replay

import dev.robocode.tankroyale.client.model.Message
import dev.robocode.tankroyale.client.model.MessageConstants
import kotlinx.serialization.json.Json
import java.io.File
import java.util.zip.GZIPInputStream

/**
 * Reads replay files in NDJSON format created by the recorder.
 * Each line in the file contains a JSON-serialized Message object.
 * Supports both gzipped and plain text files.
 */
class ReplayFileReader(private val replayFile: File) {

    private val json = MessageConstants.json
    private var messages: List<Message>? = null

    /**
     * Loads and parses all messages from the replay file.
     * @return List of messages in chronological order
     * @throws Exception if file cannot be read or parsed
     */
    fun loadMessages(): List<Message> {
        if (messages == null) {
            messages = readMessagesFromFile()
        }
        return messages!!
    }

    /**
     * Returns the total number of messages in the replay file.
     */
    fun getMessageCount(): Int = loadMessages().size

    /**
     * Returns true if the replay file exists and is readable.
     */
    fun isValid(): Boolean {
        return replayFile.exists() && replayFile.canRead()
    }

    private fun readMessagesFromFile(): List<Message> {
        val messageList = mutableListOf<Message>()

        // Try to detect if file is gzipped by attempting to read as GZIP first
        val inputStream = try {
            GZIPInputStream(replayFile.inputStream())
        } catch (e: Exception) {
            // Not a GZIP file, read as plain text
            replayFile.inputStream()
        }

        inputStream.use { stream ->
            stream.bufferedReader().useLines { lines ->
                for (line in lines) {
                    if (line.trim().isNotEmpty()) {
                        try {
                            val message = json.decodeFromString<Message>(line)
                            messageList.add(message)
                        } catch (e: Exception) {
                            // Log warning but continue processing other lines
                            System.err.println("Warning: Failed to parse replay line: ${e.message}")
                        }
                    }
                }
            }
        }

        return messageList
    }
}
