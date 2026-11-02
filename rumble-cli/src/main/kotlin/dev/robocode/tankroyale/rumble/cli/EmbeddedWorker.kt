package dev.robocode.tankroyale.rumble.cli

import java.io.IOException
import java.io.InputStream
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.security.MessageDigest

/**
 * The battle worker, carried inside the command line as a jar resource and not as classes on its own classpath, the
 * way the battle runner carries the server and the booter. It is extracted into the state folder when it is needed.
 */
object EmbeddedWorker {
    const val RESOURCE = "/robocode-tankroyale-rumble-worker.jar"
    private const val FILE_NAME = "robocode-tankroyale-rumble-worker.jar"

    class Info(val size: Long, val sha256: String)

    /** The size and SHA-256 hash of the carried worker, or null when this program does not carry one. */
    fun info(): Info? {
        val input = stream() ?: return null
        input.use {
            val digest = MessageDigest.getInstance("SHA-256")
            var size = 0L
            val buffer = ByteArray(64 * 1024)
            while (true) {
                val count = it.read(buffer)
                if (count < 0) break
                digest.update(buffer, 0, count)
                size += count
            }
            return Info(size, hex(digest.digest()))
        }
    }

    /**
     * Writes the carried worker to `worker/` below [stateFolder] unless a copy with the same hash is already there, and
     * returns its path. Throws [IOException] when this program carries no worker or it cannot be written.
     */
    fun extract(stateFolder: Path): Path {
        val info = info() ?: throw IOException("This program does not carry the battle worker.")
        val folder = stateFolder.resolve("worker")
        val target = folder.resolve(FILE_NAME)
        if (Files.isRegularFile(target) && hashOf(target) == info.sha256) return target
        Files.createDirectories(folder)
        val temp = folder.resolve("$FILE_NAME.part")
        (stream() ?: throw IOException("This program does not carry the battle worker.")).use { Files.copy(it, temp, StandardCopyOption.REPLACE_EXISTING) }
        try {
            Files.move(temp, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
        } catch (e: AtomicMoveNotSupportedException) {
            Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING)
        }
        return target
    }

    private fun stream(): InputStream? = EmbeddedWorker::class.java.getResourceAsStream(RESOURCE)

    private fun hashOf(path: Path): String {
        val digest = MessageDigest.getInstance("SHA-256")
        Files.newInputStream(path).use {
            val buffer = ByteArray(64 * 1024)
            while (true) {
                val count = it.read(buffer)
                if (count < 0) break
                digest.update(buffer, 0, count)
            }
        }
        return hex(digest.digest())
    }

    private fun hex(bytes: ByteArray) = bytes.joinToString("") { "%02x".format(it) }
}
