package dev.robocode.tankroyale.gui.util

import dev.robocode.tankroyale.common.RECORDINGS_DIR
import dev.robocode.tankroyale.common.util.UserDataDirectory
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile
import kotlin.io.path.listDirectoryEntries

/**
 * Handles migration of user data from the old location (CWD) to the new user data directory.
 *
 * Prior to version 0.35.0, properties files were stored in the current working directory (CWD)
 * where the GUI jar was started. Starting with 0.35.0, these files are stored in a platform-specific
 * user data directory.
 *
 * This migration utility copies files from the old location to the new location when:
 * - The file exists in the old location (CWD)
 * - The file does NOT already exist in the new location (user data directory)
 *
 * Files that are migrated:
 * - gui.properties
 * - server.properties
 * - game-setups.properties
 * - recordings/ directory (contents)
 */
object UserDataMigration {

    private val PROPERTY_FILES = listOf(
        "gui.properties",
        "server.properties",
        "game-setups.properties"
    )

    /**
     * Migrates user data from the old CWD location to the user data directory if needed.
     *
     * This method should be called early during application startup, before any settings are loaded.
     */
    fun migrateIfNeeded() {
        val userDataDir = UserDataDirectory.get().toPath()

        // Collect potential old locations (CWD and JAR directory)
        val oldLocations = getOldLocations()

        // Migrate property files from any old location
        PROPERTY_FILES.forEach { fileName ->
            oldLocations.forEach { oldDir ->
                migrateFileIfNeeded(oldDir, userDataDir, fileName)
            }
        }

        // Migrate recordings directory from any old location
        oldLocations.forEach { oldDir ->
            migrateRecordingsIfNeeded(oldDir, userDataDir)
        }
    }

    /**
     * Returns potential old locations where user data might exist.
     * Includes:
     * - Current working directory
     * - JAR file's parent directory (for Windows double-click launch)
     * - User's home directory (common location for running portable apps)
     */
    private fun getOldLocations(): List<Path> {
        val locations = mutableSetOf<Path>()

        // Current working directory
        locations.add(Path.of(System.getProperty("user.dir")))

        // User's home directory (common location for portable app usage)
        locations.add(Path.of(System.getProperty("user.home")))

        // JAR file's parent directory (important for Windows double-click launch)
        runCatching {
            val jarPath = UserDataMigration::class.java.protectionDomain.codeSource?.location?.toURI()
            jarPath?.let {
                val path = Path.of(it)
                // If it's a JAR file, get its parent directory
                if (path.isRegularFile()) {
                    locations.add(path.parent)
                } else if (path.isDirectory()) {
                    locations.add(path)
                }
            }
        }

        return locations.toList()
    }

    /**
     * Migrates a single file from the old location to the new location if:
     * - The file exists in the old location
     * - The file does NOT exist in the new location
     */
    private fun migrateFileIfNeeded(oldDir: Path, newDir: Path, fileName: String) {
        val oldFile = oldDir.resolve(fileName)
        val newFile = newDir.resolve(fileName)

        if (oldFile.isRegularFile() && !newFile.exists()) {
            runCatching {
                Files.createDirectories(newFile.parent)
                Files.move(oldFile, newFile, StandardCopyOption.ATOMIC_MOVE)
                println("Migrated $fileName from $oldFile to $newFile")
            }.onFailure {
                // ATOMIC_MOVE may fail across filesystems, fall back to copy + delete
                runCatching {
                    Files.copy(oldFile, newFile, StandardCopyOption.COPY_ATTRIBUTES)
                    Files.delete(oldFile)
                    println("Migrated $fileName from $oldFile to $newFile")
                }.onFailure { e ->
                    System.err.println("Failed to migrate $fileName: ${e.message}")
                }
            }
        }
    }

    /**
     * Migrates the recordings directory from the old location to the new location if:
     * - The recordings directory exists in the old location
     * - The recordings directory does NOT exist in the new location (or is empty)
     */
    private fun migrateRecordingsIfNeeded(oldDir: Path, newDir: Path) {
        val oldRecordingsDir = oldDir.resolve(RECORDINGS_DIR)
        val newRecordingsDir = newDir.resolve(RECORDINGS_DIR)

        if (!oldRecordingsDir.isDirectory()) return

        val oldFiles = oldRecordingsDir.listDirectoryEntries().filter { it.isRegularFile() }
        if (oldFiles.isEmpty()) return

        // Only migrate if the new directory doesn't exist or is empty
        val newDirEmpty = !newRecordingsDir.exists() ||
            newRecordingsDir.listDirectoryEntries().none { it.isRegularFile() }

        if (!newDirEmpty) return

        runCatching {
            Files.createDirectories(newRecordingsDir)

            oldFiles.forEach { file ->
                val targetFile = newRecordingsDir.resolve(file.fileName)
                if (!targetFile.exists()) {
                    runCatching {
                        Files.move(file, targetFile, StandardCopyOption.ATOMIC_MOVE)
                    }.onFailure {
                        // Fall back to copy + delete if atomic move fails
                        Files.copy(file, targetFile, StandardCopyOption.COPY_ATTRIBUTES)
                        Files.delete(file)
                    }
                }
            }

            // Delete old recordings directory if empty
            if (oldRecordingsDir.listDirectoryEntries().isEmpty()) {
                Files.delete(oldRecordingsDir)
            }

            println("Migrated recordings directory from $oldRecordingsDir to $newRecordingsDir")
        }.onFailure { e ->
            System.err.println("Failed to migrate recordings directory: ${e.message}")
        }
    }
}
