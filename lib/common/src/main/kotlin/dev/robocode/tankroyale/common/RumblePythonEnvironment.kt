package dev.robocode.tankroyale.common

import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.Path

/**
 * The layout of the private Python environment that the Download bots screen makes in a folder of bots downloaded from
 * the Rumble, shared by the screen that makes and checks it and the booter that starts the bots with it, so the two can
 * never disagree about where its Python is. The environment counts only when its setup finished, which the setup marks
 * with an empty file as its very last step, so a setup that was stopped half-way is never used.
 */
object RumblePythonEnvironment {
    const val FOLDER = RUMBLE_PYTHON_ENVIRONMENT
    const val READY = ".rumble-ready"

    fun home(downloadFolder: Path): Path = downloadFolder.resolve(FOLDER)

    fun scripts(downloadFolder: Path, windows: Boolean): Path = home(downloadFolder).resolve(if (windows) "Scripts" else "bin")

    fun python(downloadFolder: Path, windows: Boolean): Path = scripts(downloadFolder, windows).resolve(if (windows) "python.exe" else "python")

    /** True when the setup of the environment in [downloadFolder] finished and its Python is there. */
    fun isReady(downloadFolder: Path, windows: Boolean): Boolean =
        Files.isRegularFile(home(downloadFolder).resolve(READY), LinkOption.NOFOLLOW_LINKS) && Files.exists(python(downloadFolder, windows))
}
