package dev.robocode.tankroyale.rumble

import dev.robocode.tankroyale.rumble.protocol.BotOutcome
import dev.robocode.tankroyale.rumble.protocol.ErrorCode
import dev.robocode.tankroyale.rumble.protocol.MessageCodec
import dev.robocode.tankroyale.rumble.protocol.WorkerEvent
import java.io.File
import kotlin.system.exitProcess

/**
 * A tiny worker for the host tests, started as a real process. The first argument selects what it does; the job's
 * id is read from standard input and is used by some modes as a file to write to.
 */
fun main(args: Array<String>) {
    val mode = args.firstOrNull() ?: "ok"
    if (mode == "sleep") {
        Thread.sleep(120_000)
        return
    }
    fun environmentReportText(): String {
        val own = System.getenv().entries.sortedBy { it.key }.joinToString("") { "${it.key}=${it.value}" }
        return "$ownuser.home=${System.getProperty("user.home")}cwd=${File(".").canonicalPath}"
    }
    if (mode == "envreport") {
        print(environmentReportText())
        return
    }
    val job = MessageCodec.readJob(readLine().orEmpty())
    val id = job.jobId

    fun emit(event: WorkerEvent) {
        println(MessageCodec.write(event))
        System.out.flush()
    }

    fun outcome(name: String, rank: Int) = BotOutcome(name, "1.0", false, rank, 10, 1, 1, 1, 1, 1, 1, 1, 0, 0)

    fun finishNormally() {
        emit(WorkerEvent.Started(jobId = id))
        emit(WorkerEvent.Result(jobId = id, numberOfRounds = job.numberOfRounds, bots = job.bots.mapIndexed { i, b -> outcome(b.name, i + 1) }))
        emit(WorkerEvent.Finished(jobId = id))
    }

    fun environmentReport(): String {
        val own = System.getenv().entries.sortedBy { it.key }.joinToString("\u0001") { "${it.key}=${it.value}" }
        return "$own\u0002user.home=${System.getProperty("user.home")}\u0002cwd=${File(".").canonicalPath}"
    }

    when (mode) {
        "ok" -> finishNormally()
        "refuse" -> { emit(WorkerEvent.Refused(jobId = id, code = ErrorCode.PIN_MISMATCH, message = "wrong pin")); exitProcess(2) }
        "fail" -> { emit(WorkerEvent.Started(jobId = id)); emit(WorkerEvent.Failed(jobId = id, code = ErrorCode.BATTLE_FAILED, message = "no bots")); exitProcess(1) }
        "nofinal" -> emit(WorkerEvent.Started(jobId = id))
        "badexit" -> { finishNormally(); exitProcess(1) }
        "garbage" -> println("this is not an event")
        "bigoutput" -> {
            val line = MessageCodec.write(WorkerEvent.Started(jobId = id))
            while (true) println(line)
        }
        "longline" -> { println("y".repeat(200_000)); Thread.sleep(120_000) }
        "noisystderr" -> {
            val noise = "e".repeat(1000)
            repeat(5_000) { System.err.println(noise) }
            finishNormally()
        }
        "env" -> emit(WorkerEvent.Failed(jobId = id, code = ErrorCode.BATTLE_FAILED, message = environmentReportText()))
        "envchild" -> {
            val java = File(System.getProperty("java.home"), "bin/java").absolutePath
            val child = ProcessBuilder(java, "-cp", System.getProperty("java.class.path"), "dev.robocode.tankroyale.rumble.FakeWorkerKt", "envreport")
                .redirectErrorStream(true).start()
            val text = child.inputStream.bufferedReader().readText()
            child.waitFor()
            emit(WorkerEvent.Failed(jobId = id, code = ErrorCode.BATTLE_FAILED, message = text.trim()))
        }
        "child" -> {
            // Starts a long-running child, records its process id in the file named by the job id, then hangs.
            val java = File(System.getProperty("java.home"), "bin/java").absolutePath
            val child = ProcessBuilder(java, "-cp", System.getProperty("java.class.path"), "dev.robocode.tankroyale.rumble.FakeWorkerKt", "sleep").start()
            File(id).writeText(child.pid().toString())
            emit(WorkerEvent.Started(jobId = id))
            Thread.sleep(120_000)
        }
        "childthenfinish" -> {
            val java = File(System.getProperty("java.home"), "bin/java").absolutePath
            val child = ProcessBuilder(java, "-cp", System.getProperty("java.class.path"), "dev.robocode.tankroyale.rumble.FakeWorkerKt", "sleep").start()
            File(id).writeText(child.pid().toString())
            finishNormally()
        }
        else -> exitProcess(3)
    }
}
