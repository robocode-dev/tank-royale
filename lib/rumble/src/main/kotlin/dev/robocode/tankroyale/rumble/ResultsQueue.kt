package dev.robocode.tankroyale.rumble

import java.nio.file.Path
import java.util.concurrent.Executor

/** How many ranked results this computer keeps, by where each stands. */
data class ResultCounts(
    val kept: Int = 0,
    val waiting: Int = 0,
    val sent: Int = 0,
    val accepted: Int = 0,
    val rejected: Int = 0,
    val setAside: Int = 0,
)

/** Where a sent batch stands. */
sealed interface BatchStanding {
    /** Some of its results still wait for the Rumble's receipt. */
    data object WaitingForReceipt : BatchStanding

    /** The receipt was taken: [accepted] results were accepted and [rejected] rejected; [other] went back to wait or were set aside. */
    data class Answered(val accepted: Int, val rejected: Int, val other: Int) : BatchStanding
}

/**
 * One sent batch: its id, how many battles it holds, when it was sent (an ISO-8601 instant, or null when not known),
 * where it stands, the Rumble's reasons for the results it rejected, and the page of its issue on GitHub, when it
 * became one.
 */
data class BatchRow(
    val batchId: String,
    val battles: Int,
    val sentAt: String?,
    val standing: BatchStanding,
    val reasons: List<String>,
    val issuePage: String?,
)

/** Everything Results and queue shows. */
data class ResultsOverview(
    val counts: ResultCounts,
    val batches: List<BatchRow>,
    /** The Rumble's reason for refusing a batch, while sending is stopped for it. */
    val refusal: String?,
    /** Whether this computer holds a saved sign-in for the Rumble; without one, a session cannot send. */
    val signedIn: Boolean,
)

/** Builds what Results and queue shows from the two readings, without a window and without the disk. */
object ResultsQueue {
    /**
     * Counts [results] by state and makes one row per batch in [sent], newest first by when it was sent, with the
     * Rumble's reasons for its rejected results and its issue's page in [dataRepositoryPage].
     */
    fun overview(results: List<KeptResult>, sent: SentBatchesRead, dataRepositoryPage: String, signedIn: Boolean): ResultsOverview {
        val byState = results.groupingBy { it.state }.eachCount()
        val counts = ResultCounts(
            kept = results.size,
            waiting = byState[RecordState.WAITING] ?: 0,
            sent = byState[RecordState.HANDED_OVER] ?: 0,
            accepted = byState[RecordState.ACCEPTED] ?: 0,
            rejected = byState[RecordState.REJECTED] ?: 0,
            setAside = byState[RecordState.SET_ASIDE] ?: 0,
        )
        val byId = results.associateBy { it.battleId }
        val rows = sent.batches.withIndex().map { (index, batch) ->
            val mine = batch.battleIds.mapNotNull { byId[it] }
            val states = mine.groupingBy { it.state }.eachCount()
            val standing = if ((states[RecordState.HANDED_OVER] ?: 0) > 0 || (batch.batchId !in sent.done && mine.isEmpty())) {
                BatchStanding.WaitingForReceipt
            } else {
                val accepted = states[RecordState.ACCEPTED] ?: 0
                val rejected = states[RecordState.REJECTED] ?: 0
                BatchStanding.Answered(accepted, rejected, batch.battleIds.size - accepted - rejected)
            }
            val reasons = mine.filter { it.state == RecordState.REJECTED }.map { it.detail.firstOrNull() ?: it.reason.orEmpty() }
                .filter { it.isNotBlank() }.distinct()
            val issue = batch.issue?.let { "$dataRepositoryPage/issues/$it" }
            index to BatchRow(batch.batchId, batch.battleIds.size, mine.mapNotNull { it.sentAt }.minOrNull(), standing, reasons, issue)
        }
        // Newest first: by when it was sent, and by the order of the list when that is not known.
        val newestFirst = rows.sortedWith(compareByDescending<Pair<Int, BatchRow>> { it.second.sentAt ?: "" }.thenByDescending { it.first }).map { it.second }
        return ResultsOverview(counts, newestFirst, sent.refusal, signedIn)
    }

    /** True when results wait to be sent, which only a contribution session does. */
    fun waitsToBeSent(overview: ResultsOverview): Boolean = overview.counts.waiting > 0

    /** True when results wait and no saved sign-in lets a session send them. */
    fun needsSignIn(overview: ResultsOverview): Boolean = overview.counts.waiting > 0 && !overview.signedIn
}

/** What reading Results and queue gave. */
sealed interface ResultsReading {
    data class Read(val overview: ResultsOverview) : ResultsReading

    /** The files could not be read, or belong to another Rumble; [message] says why in plain words. */
    data class Failed(val message: String) : ResultsReading
}

/** What Results and queue needs of this computer, so a test can stand in for it. */
fun interface ResultsPort {
    fun read(): ResultsReading
}

/**
 * Reads the results a contribution session keeps for [environment] below [stateRoot], the environment's own state
 * folder, without writing to anything, and whether this computer holds a saved sign-in for it.
 */
class LibraryResultsPort(
    private val environment: RumbleEnvironment,
    private val stateRoot: Path,
    private val signedIn: () -> Boolean,
) : ResultsPort {
    override fun read(): ResultsReading = try {
        val ranked = stateRoot.resolve("ranked")
        val results = RankedJournal.read(environment, ranked.resolve("journal.jsonl"))
        val sent = SentBatches.read(ranked.resolve("sent-batches.jsonl"))
        val page = "https://github.com/${RumbleEnvironments.owner}/${environment.dataRepository}"
        ResultsReading.Read(ResultsQueue.overview(results, sent, page, signedIn()))
    } catch (e: JournalUnusable) {
        ResultsReading.Failed(e.message.orEmpty())
    } catch (e: EnvironmentRefused) {
        ResultsReading.Failed(e.message.orEmpty())
    }
}

/** The state of the Results and queue screen. */
sealed interface ResultsState {
    data object Reading : ResultsState
    data class Shown(val overview: ResultsOverview) : ResultsState
    data class Failed(val details: String) : ResultsState
}

/**
 * The behaviour of the Results and queue screen: it reads once when started, away from the screen's thread, and hands
 * the state to [onState] through [post]. A reading that arrives after [close] is thrown away. There is no refresh:
 * opening the screen again reads it again.
 */
class ResultsController(
    private val port: ResultsPort,
    private val background: Executor,
    private val post: Executor,
    private val onState: (ResultsState) -> Unit,
) {
    @Volatile
    var state: ResultsState = ResultsState.Reading
        private set

    @Volatile
    private var closed = false

    fun start() {
        background.execute {
            val next = try {
                when (val reading = port.read()) {
                    is ResultsReading.Read -> ResultsState.Shown(reading.overview)
                    is ResultsReading.Failed -> ResultsState.Failed(reading.message)
                }
            } catch (e: Exception) {
                ResultsState.Failed(e.message.orEmpty())
            }
            post.execute {
                if (!closed) {
                    state = next
                    onState(next)
                }
            }
        }
    }

    fun close() {
        closed = true
    }
}
