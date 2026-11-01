package dev.robocode.tankroyale.botproject

import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import kotlin.streams.toList

/**
 * The content hash of a bot's source tree, using the bot repository's own rule: a SHA-256 over the files sorted by
 * their slash-separated relative path, each contributing its path, a zero byte, its bytes and a zero byte. Cache
 * folders named `__pycache__` are skipped.
 */
object TreeHash {
    private const val CACHE_FOLDER = "__pycache__"

    /** The hash of files given as slash-separated relative path to bytes, written `sha256:<hex>`. */
    fun of(files: Map<String, ByteArray>): String {
        val digest = MessageDigest.getInstance("SHA-256")
        files.filterKeys { CACHE_FOLDER !in it.split('/') }
            .entries
            .sortedWith { a, b -> compareUtf8(a.key, b.key) }
            .forEach { (path, bytes) ->
                digest.update(path.toByteArray(Charsets.UTF_8))
                digest.update(0)
                digest.update(bytes)
                digest.update(0)
            }
        return "sha256:" + digest.digest().joinToString("") { "%02x".format(it) }
    }

    /** The hash of the files below [directory]. */
    fun ofDirectory(directory: Path): String {
        val files = Files.walk(directory).use { paths ->
            paths.filter { Files.isRegularFile(it) }
                .toList()
                .associate { directory.relativize(it).joinToString("/") to Files.readAllBytes(it) }
        }
        return of(files)
    }

    // Python sorts by code point; UTF-8 byte order is the same order, so the hash agrees on every platform.
    private fun compareUtf8(a: String, b: String): Int {
        val x = a.toByteArray(Charsets.UTF_8)
        val y = b.toByteArray(Charsets.UTF_8)
        for (i in 0 until minOf(x.size, y.size)) {
            val c = (x[i].toInt() and 0xff) - (y[i].toInt() and 0xff)
            if (c != 0) return c
        }
        return x.size - y.size
    }
}
