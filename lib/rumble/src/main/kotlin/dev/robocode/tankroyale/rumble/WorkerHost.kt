package dev.robocode.tankroyale.rumble

import dev.robocode.tankroyale.rumble.protocol.Job
import dev.robocode.tankroyale.rumble.protocol.MessageCodec
import dev.robocode.tankroyale.rumble.protocol.MessageRejected
import dev.robocode.tankroyale.rumble.protocol.WorkerEvent
import java.io.IOException
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

/** What the host allows a worker to use. A worker that goes over a limit is stopped and never returns a result. */
data class WorkerLimits(
    /** The total time from start to the last event; the whole process tree is stopped after it. */
    val totalTime: Duration = Duration.ofMinutes(10),
    /** The most standard output the host reads. */
    val maxOutputBytes: Long = 1_000_000,
    /** The longest single line of standard output. */
    val maxLineBytes: Int = 64_000,
)

/** How running a job on the worker ended. Exactly one of these is returned for every job. */
sealed interface WorkerOutcome {
    /** The battle finished and the worker said so. */
    data class Completed(val result: WorkerEvent.Result) : WorkerOutcome

    /** The worker refused the job; [code] is an `ErrorCode`. */
    data class Refused(val code: String, val message: String) : WorkerOutcome

    /** The worker accepted the job but the battle did not complete; [code] is an `ErrorCode`. */
    data class Failed(val code: String, val message: String) : WorkerOutcome

    /** The worker did not finish within the total time; it and everything it started were stopped. */
    data class TimedOut(val limit: Duration) : WorkerOutcome

    /** The worker said more than a limit allows; it and everything it started were stopped. */
    data class LimitExceeded(val message: String) : WorkerOutcome

    /** The worker ended without a complete, consistent set of events. [exitCode] is its exit code, if it had one. */
    data class EndedWithoutFinish(val exitCode: Int?) : WorkerOutcome

    /** The worker wrote something that is not a message this program understands. */
    data class UnreadableOutput(val message: String) : WorkerOutcome

    /** The worker process could not be started. */
    data class CouldNotStart(val message: String) : WorkerOutcome
}

/**
 * Runs one job on a worker process: starts it in a clean environment (see [WorkerEnvironment]) in new empty folders,
 * writes the job to its standard input, reads events from its standard output, and stops it and everything it started
 * when a limit is passed. The outcome comes from the events and the exit code alone; the worker's diagnostic output is
 * read and thrown away so that it can never block the worker or fill memory.
 */
class WorkerHost(
    private val limits: WorkerLimits = WorkerLimits(),
    private val hostEnvironment: () -> Map<String, String> = { System.getenv() },
    private val runFolders: Path? = null,
) {
    private class Reading(val events: MutableList<WorkerEvent> = mutableListOf())

    fun run(command: WorkerCommand, job: Job): WorkerOutcome {
        val base = try {
            runFolders?.also { Files.createDirectories(it) }?.let { Files.createTempDirectory(it, "rumble-worker") }
                ?: Files.createTempDirectory("rumble-worker")
        } catch (e: IOException) {
            return WorkerOutcome.CouldNotStart("A folder for the worker could not be made.")
        }
        try {
            val folders = WorkerFolders(
                work = Files.createDirectory(base.resolve("work")),
                temp = Files.createDirectory(base.resolve("temp")),
                home = Files.createDirectory(base.resolve("home")),
            )
            return runIn(command, job, folders)
        } catch (e: IOException) {
            return WorkerOutcome.CouldNotStart("A folder for the worker could not be made.")
        } finally {
            base.toFile().deleteRecursively()
        }
    }

    private fun runIn(command: WorkerCommand, job: Job, folders: WorkerFolders): WorkerOutcome {
        val builder = ProcessBuilder(command.build(folders)).directory(folders.work.toFile())
        builder.environment().clear()
        builder.environment().putAll(WorkerEnvironment.build(hostEnvironment(), folders))
        val process = try {
            builder.start()
        } catch (e: IOException) {
            return WorkerOutcome.CouldNotStart("The worker could not be started.")
        }

        val reading = Reading()
        val stopRequested = AtomicReference<WorkerOutcome?>(null)
        val reader = Thread({ read(process.inputStream, reading, stopRequested, process) }, "rumble-worker-stdout").apply { isDaemon = true }
        val drain = Thread({ discard(process.errorStream) }, "rumble-worker-stderr").apply { isDaemon = true }
        reader.start()
        drain.start()

        try {
            process.outputStream.bufferedWriter(Charsets.UTF_8).use { it.write(MessageCodec.write(job) + "\n") }
        } catch (e: IOException) {
            // The worker may have ended before reading its job; its events and exit code tell what happened.
        }

        val deadline = System.nanoTime() + limits.totalTime.toNanos()
        val descendants = mutableListOf<ProcessHandle>()
        while (reader.isAlive) {
            descendants.addAll(descendantsOf(process))
            if (System.nanoTime() >= deadline) {
                stopTree(process, descendants)
                reader.join(2_000)
                return WorkerOutcome.TimedOut(limits.totalTime)
            }
            reader.join(50)
        }
        stopRequested.get()?.let { limit ->
            stopTree(process, descendants)
            return limit
        }
        val remaining = TimeUnit.NANOSECONDS.toMillis(maxOf(0L, deadline - System.nanoTime()))
        if (!process.waitFor(remaining, TimeUnit.MILLISECONDS)) {
            stopTree(process, descendants)
            return WorkerOutcome.TimedOut(limits.totalTime)
        }
        // Anything the worker started and left behind is stopped too.
        descendants.filter { it.isAlive }.forEach { it.destroyForcibly() }
        return decide(reading, process.exitValue())
    }

    private fun decide(reading: Reading, exitCode: Int): WorkerOutcome {
        val events = reading.events
        events.filterIsInstance<WorkerEvent.Refused>().firstOrNull()?.let { return WorkerOutcome.Refused(it.code, it.message) }
        events.filterIsInstance<WorkerEvent.Failed>().firstOrNull()?.let { return WorkerOutcome.Failed(it.code, it.message) }
        val result = events.filterIsInstance<WorkerEvent.Result>().singleOrNull()
        val finished = events.any { it is WorkerEvent.Finished }
        if (result != null && finished && exitCode == 0 && events.last() is WorkerEvent.Finished) return WorkerOutcome.Completed(result)
        return WorkerOutcome.EndedWithoutFinish(exitCode)
    }

    /** Reads event lines up to the ceilings. A ceiling or an unreadable line is recorded and ends the reading. */
    private fun read(input: InputStream, reading: Reading, stop: AtomicReference<WorkerOutcome?>, process: Process) {
        try {
            var total = 0L
            val line = java.io.ByteArrayOutputStream()
            val buffer = ByteArray(8192)
            while (true) {
                val count = input.read(buffer)
                if (count < 0) break
                for (i in 0 until count) {
                    total++
                    if (total > limits.maxOutputBytes) {
                        stop.set(WorkerOutcome.LimitExceeded("The worker wrote more than ${limits.maxOutputBytes} bytes."))
                        return
                    }
                    val b = buffer[i]
                    if (b == '\n'.code.toByte()) {
                        if (!accept(line.toString(Charsets.UTF_8), reading, stop)) return
                        line.reset()
                    } else {
                        if (line.size() >= limits.maxLineBytes) {
                            stop.set(WorkerOutcome.LimitExceeded("The worker wrote a line longer than ${limits.maxLineBytes} bytes."))
                            return
                        }
                        line.write(b.toInt())
                    }
                }
            }
            if (line.size() > 0) accept(line.toString(Charsets.UTF_8), reading, stop)
        } catch (e: IOException) {
            // The process ended or was stopped; what was read so far decides the outcome.
        }
    }

    /** Records one line. A line that is not a message ends the reading and the worker, which cannot be trusted any more. */
    private fun accept(raw: String, reading: Reading, stop: AtomicReference<WorkerOutcome?>): Boolean {
        val text = raw.trimEnd('\r')
        if (text.isBlank()) return true
        return try {
            reading.events += MessageCodec.readEvent(text)
            true
        } catch (rejected: MessageRejected) {
            stop.set(WorkerOutcome.UnreadableOutput(rejected.message.orEmpty()))
            false
        }
    }

    private fun discard(input: InputStream) {
        try {
            val buffer = ByteArray(8192)
            while (input.read(buffer) >= 0) { /* thrown away */ }
        } catch (e: IOException) {
            // The process ended.
        }
    }

    /** Stops the worker and every process it started, the ones seen while it ran and the ones alive now. */
    private fun stopTree(process: Process, seen: List<ProcessHandle>) {
        val all = (seen + descendantsOf(process)).distinctBy { it.pid() }
        all.forEach { it.destroyForcibly() }
        process.destroyForcibly()
        process.waitFor(5, TimeUnit.SECONDS)
        all.forEach { handle -> runCatching { handle.onExit().get(5, TimeUnit.SECONDS) } }
    }

    private fun descendantsOf(process: Process): List<ProcessHandle> =
        process.descendants().collect(java.util.stream.Collectors.toList())
}
