package dev.robocode.tankroyale.rumble

import dev.robocode.tankroyale.common.util.JavaExec
import java.nio.file.Path

/** The folders a worker run gets, all new and empty and its own: nothing of the participant's is in them. */
class WorkerFolders(
    val work: Path,
    val temp: Path,
    val home: Path,
    /**
     * A folder for what the host gives the battle, such as its copy of the Python Bot API. Unlike [temp] it is not named
     * to the bots as their temporary folder, so a bot that clears its temporary files does not clear it.
     */
    val battle: Path = work.resolveSibling("battle"),
)

/** How to start the worker: a command in the same form Tank Royale gives the process builder everywhere else. */
fun interface WorkerCommand {
    /** The program and its arguments; [folders] are the worker's own folders, for settings that must point at them. */
    fun build(folders: WorkerFolders): List<String>
}

/**
 * Starts the worker so that a Ctrl+C in the contributor's terminal does not reach it, nor the server and the bots it
 * starts. The terminal sends Ctrl+C to every process on its console or in its foreground group, so without this the first
 * Ctrl+C, which is meant to let the running battle finish, would end the battle. Java cannot start a process that
 * ignores Ctrl+C by itself, so the command is wrapped: on Windows in `start /b /wait`, which runs the program with Ctrl+C
 * ignored, a setting its children inherit; elsewhere in `sh` with `trap '' INT`, an ignored signal that `exec` and every
 * child keep. The worker is still ended through its process tree, as before.
 *
 * On Windows the worker's arguments hold paths of the participant's computer, which may contain characters `cmd` treats
 * as its own, such as `&`, `^`, `%` or `!`. They are therefore never written into `cmd`'s command line: each is passed in
 * a variable and read there with delayed expansion, which takes place after `cmd` has read its command line, so the
 * value is used as it is. `start` does not give the program's exit code back by itself, so it is read the same way and
 * handed on. A real console Ctrl+C on 2026-11-16 left the shielded program and its child running and `cmd` waiting for
 * it and handing on its exit code.
 */
object InterruptShield {
    /** The command to start, and the variables it needs on top of the worker's environment. */
    class Shielded(val command: List<String>, val environment: Map<String, String>)

    const val ARGUMENT_PREFIX = "RUMBLE_WORKER_ARG_"

    fun wrap(command: List<String>, windows: Boolean = isWindows(), systemRoot: String? = System.getenv("SystemRoot")): Shielded =
        if (windows) {
            val cmd = systemRoot?.let { "$it\\System32\\cmd.exe" } ?: "cmd.exe"
            val variables = command.withIndex().associate { (i, argument) -> "$ARGUMENT_PREFIX$i" to argument }
            // The empty title comes first so that a quoted program path is never taken for the window title.
            val line = "start \"\" /b /wait " + command.indices.joinToString(" ") { "\"!$ARGUMENT_PREFIX$it!\"" } + " & exit !errorlevel!"
            Shielded(listOf(cmd, "/d", "/v:on", "/c", line), variables)
        } else {
            Shielded(listOf("/bin/sh", "-c", "trap '' INT; exec \"\$@\"", "sh") + command, emptyMap())
        }

    private fun isWindows() = System.getProperty("os.name").orEmpty().startsWith("Windows", ignoreCase = true)
}

object WorkerCommands {
    /**
     * The usual command: the Java program from the shared Java helper, a heap limit, the worker's home and temporary
     * folder settings, `-jar`, [jar] and the argument that selects worker mode. [javaProgram] is the program to run.
     */
    fun javaJar(jar: Path, heapMegabytes: Int, javaProgram: String = JavaExec.java()): WorkerCommand = WorkerCommand { folders ->
        listOf(
            javaProgram,
            "-Xmx${heapMegabytes}m",
            "-Duser.home=${folders.home}",
            "-Djava.io.tmpdir=${folders.temp}",
            "-jar", jar.toAbsolutePath().toString(),
            "worker",
        )
    }
}

/**
 * Builds the environment of the worker, and so of everything the worker starts, from an allowlist. A variable is passed
 * only if it is named here and exists in the host's environment; nothing is passed because it looks harmless, and
 * nothing is removed because it looks harmful. The home and temporary folder variables, and the Java tool options that
 * set the home and temporary folders of every Java process, point at the worker's own empty folders.
 *
 * This keeps tokens, the participant's home folder and a container engine's address out of the worker's environment.
 * It does not stop a process running as the same operating system user from opening a file by its full path.
 */
object WorkerEnvironment {
    /** What the worker needs to find Java and the bot runtimes and to render text. */
    val ALLOWED: Set<String> = setOf(
        "PATH", "JAVA_HOME", "DOTNET_ROOT", "LANG", "LC_ALL", "LC_CTYPE", "TZ",
        // Windows needs these to start programs and to find its own libraries.
        "SystemRoot", "SystemDrive", "windir", "ComSpec", "PATHEXT", "OS", "NUMBER_OF_PROCESSORS", "PROCESSOR_ARCHITECTURE",
    )

    fun build(host: Map<String, String>, folders: WorkerFolders): Map<String, String> {
        val environment = linkedMapOf<String, String>()
        for (name in ALLOWED) {
            val value = host.entries.firstOrNull { it.key.equals(name, ignoreCase = true) }?.value ?: continue
            environment[name] = value
        }
        val home = folders.home.toString()
        val temp = folders.temp.toString()
        environment["HOME"] = home
        environment["USERPROFILE"] = home
        environment["TMPDIR"] = temp
        environment["TMP"] = temp
        environment["TEMP"] = temp
        // Java on Windows finds the user's real home folder from the operating system and ignores the variables above.
        // This variable is read by every Java process at start, so the worker, the server, the booter and every Java bot
        // use the worker's own empty folders, whatever command started the worker.
        environment["JAVA_TOOL_OPTIONS"] = "-Duser.home=\"$home\" -Djava.io.tmpdir=\"$temp\""
        return environment
    }
}
