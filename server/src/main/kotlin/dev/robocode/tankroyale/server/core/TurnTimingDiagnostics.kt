package dev.robocode.tankroyale.server.core

import dev.robocode.tankroyale.server.model.BotId
import org.slf4j.LoggerFactory
import java.util.ArrayDeque

/** Opt-in server-side timing traces for diagnosing missed bot turns. */
internal object TurnTimingDiagnostics {
    private val enabled = java.lang.Boolean.getBoolean("robocode.turnTimingDiagnostics")
    private val logger = LoggerFactory.getLogger(TurnTimingDiagnostics::class.java)
    private const val MAX_RECORDS = 24_000
    private val records = ArrayDeque<Record>(MAX_RECORDS)

    private data class Record(
        val event: String,
        val botId: BotId?,
        val roundNumber: Int,
        val turnNumber: Int,
        val nanos: Long,
        val detail: String?,
    )

    fun timestampIfEnabled(): Long? = if (enabled) System.nanoTime() else null

    @Synchronized
    fun clear() {
        if (enabled) records.clear()
    }

    fun record(
        event: String,
        botId: BotId,
        roundNumber: Int,
        turnNumber: Int,
        atNanos: Long? = null,
        detail: String? = null,
    ) {
        if (!enabled) return
        append(Record(event, botId, roundNumber, turnNumber, atNanos ?: System.nanoTime(), detail))

        if (event == "bot-timing-batch" && detail != null) {
            logger.info(
                "TURN_TIMING source=bot event=timing-batch botId={} round={} turn={} trace={}",
                botId.value,
                roundNumber,
                turnNumber,
                detail,
            )
        }
    }

    fun recordServer(event: String, roundNumber: Int, turnNumber: Int, atNanos: Long, detail: String? = null) {
        if (!enabled) return
        append(Record(event, null, roundNumber, turnNumber, atNanos, detail))
    }

    private fun append(record: Record) {
        synchronized(this) {
            if (records.size == MAX_RECORDS) records.removeFirst()
            records.addLast(record)
        }
    }

    fun dumpSkippedTurnWindows() {
        if (!enabled) return
        val skips = synchronized(this) {
            records.filter { it.event == "skipped-turn-detected" }
                .distinctBy { Triple(it.botId, it.roundNumber, it.turnNumber) }
        }
        skips.forEach(::dumpNearbyRecords)
    }

    private fun dumpNearbyRecords(skip: Record) {
        val nearby = synchronized(this) {
            records.filter {
                (it.botId == skip.botId || it.botId == null) && it.roundNumber == skip.roundNumber &&
                    it.turnNumber in (skip.turnNumber - 1)..(skip.turnNumber + 1)
            }.sortedBy(Record::nanos)
        }
        nearby.forEach(::logRecord)
    }

    fun dumpSampledTiming() {
        if (!enabled) return
        val sampledTurns = setOf(10, 11, 12, 20, 30, 40, 50, 60, 70, 80, 1011)
        val sampled = synchronized(this) {
            records.filter { it.turnNumber in sampledTurns && it.event != "bot-timing-batch" }
                .sortedBy(Record::nanos)
        }
        sampled.forEach(::logRecord)
    }

    private fun logRecord(record: Record) {
        logger.info(
            "TURN_TIMING source={} event={} botId={} round={} turn={} nanos={} detail={}",
            if (record.botId == null) "server-phase" else "server",
            record.event,
            record.botId?.value ?: "all",
            record.roundNumber,
            record.turnNumber,
            record.nanos,
            record.detail ?: "",
        )
    }
}
