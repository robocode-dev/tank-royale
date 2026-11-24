package dev.robocode.tankroyale.rumble

import dev.robocode.tankroyale.rumble.protocol.BotOutcome
import dev.robocode.tankroyale.rumble.protocol.ErrorCode
import dev.robocode.tankroyale.rumble.protocol.MessageCodec
import dev.robocode.tankroyale.rumble.protocol.WorkerEvent
import dev.robocode.tankroyale.common.util.UserDataDirectory
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
    if (mode == "selfreport") {
        // Reports its own command line and everything on standard input, before it understands any of it.
        // The operating system may refuse to show a process its own command line, so the JVM's account of it is used:
        // its flags, its class path, and its main class with the arguments.
        val commandLine = (java.lang.management.ManagementFactory.getRuntimeMXBean().inputArguments +
            System.getProperty("java.class.path") + System.getProperty("sun.java.command")).joinToString(" ")
        val stdin = String(System.`in`.readBytes(), Charsets.UTF_8)
        val message = "commandLine=$commandLinestdin=$stdin"
        println(MessageCodec.write(WorkerEvent.Failed(jobId = "self", code = ErrorCode.BATTLE_FAILED, message = message)))
        System.out.flush()
        return
    }
    if (mode == "echoargs") {
        // Writes the arguments after the mode as they arrived, and ends with exit code 7.
        println(args.drop(1).joinToString("|"))
        System.out.flush()
        exitProcess(7)
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

    if (mode.startsWith("noconnect:")) {
        // The named bot never connects when it is in the battle; every other battle completes.
        val missing = job.bots.filter { it.name == mode.substringAfter(":") }
        if (missing.isEmpty()) return finishNormally()
        emit(WorkerEvent.Started(jobId = id))
        emit(WorkerEvent.Failed(jobId = id, code = ErrorCode.BOTS_DID_NOT_CONNECT, message = "Bot ended before it connected", bots = missing.map { "${it.name} ${it.version}" }))
        exitProcess(1)
    }

    when (mode) {
        "ok" -> finishNormally()
        "booting" -> {
            // Reports its bots connecting one after the other, as many times as it likes, before the result.
            emit(WorkerEvent.Started(jobId = id))
            for (connected in 0..job.bots.size) {
                emit(WorkerEvent.Booting(jobId = id, connected = connected, expected = job.bots.size, elapsedMillis = connected * 1000L, limitMillis = 90_000))
            }
            repeat(2_000) { emit(WorkerEvent.Booting(jobId = id, connected = job.bots.size, expected = job.bots.size, elapsedMillis = 5_000, limitMillis = 90_000)) }
            emit(WorkerEvent.Result(jobId = id, numberOfRounds = job.numberOfRounds, bots = job.bots.mapIndexed { i, b -> outcome(b.name, i + 1) }))
            emit(WorkerEvent.Finished(jobId = id))
        }
        "refuse" -> { emit(WorkerEvent.Refused(jobId = id, code = ErrorCode.PIN_MISMATCH, message = "wrong pin")); exitProcess(2) }
        "fail" -> { emit(WorkerEvent.Started(jobId = id)); emit(WorkerEvent.Failed(jobId = id, code = ErrorCode.BATTLE_FAILED, message = "no bots")); exitProcess(1) }
        "nofinal" -> emit(WorkerEvent.Started(jobId = id))
        "badexit" -> { finishNormally(); exitProcess(1) }
        "fewrounds" -> {
            emit(WorkerEvent.Started(jobId = id))
            emit(WorkerEvent.Result(jobId = id, numberOfRounds = job.numberOfRounds - 1, bots = job.bots.mapIndexed { i, b -> outcome(b.name, i + 1) }))
            emit(WorkerEvent.Finished(jobId = id))
        }
        "closedhang" -> {
            // Closes its output, as a worker that is shutting down does, and then hangs.
            emit(WorkerEvent.Started(jobId = id))
            System.out.close()
            Thread.sleep(120_000)
        }
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
        "discover" -> {
            // Looks for the saved sign-in the ways a program normally finds its own files.
            val loaded = SignInStore.inUserFolder(RumbleEnvironments.TEST).load()
            val report = listOf(
                "user.home=${System.getProperty("user.home")}",
                "tmpdir=${System.getProperty("java.io.tmpdir")}",
                "dataDir=${UserDataDirectory.get().absolutePath}",
                "env.HOME=${System.getenv("HOME")}",
                "env.USERPROFILE=${System.getenv("USERPROFILE")}",
                "env.LOCALAPPDATA=${System.getenv("LOCALAPPDATA")}",
                "env.APPDATA=${System.getenv("APPDATA")}",
                "env.XDG_CONFIG_HOME=${System.getenv("XDG_CONFIG_HOME")}",
                "load=${loaded::class.simpleName}",
            )
            emit(WorkerEvent.Failed(jobId = id, code = ErrorCode.BATTLE_FAILED, message = report.joinToString("")))
        }
        "envchild" -> {
            val java = File(System.getProperty("java.home"), "bin/java").absolutePath
            val child = ProcessBuilder(java, "-cp", System.getProperty("java.class.path"), "dev.robocode.tankroyale.rumble.FakeWorkerKt", "envreport")
                .redirectErrorStream(true).start()
            // Java prints one "Picked up JAVA_TOOL_OPTIONS" line on standard error, which is the expected side effect.
            val text = child.inputStream.bufferedReader().readLines().filter { !it.startsWith("Picked up") }.joinToString(" ")
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
