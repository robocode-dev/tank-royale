package dev.robocode.tankroyale.gui.ui.replay

import dev.robocode.tankroyale.gui.client.Client
import dev.robocode.tankroyale.gui.player.ReplayBattlePlayer
import dev.robocode.tankroyale.gui.ui.MainFrame
import dev.robocode.tankroyale.gui.util.MessageDialog
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileFilter

/**
 * File chooser dialog specifically for selecting replay files.
 * Handles validation and integration with the BattleManager.
 */
object ReplayFileChooser {

    /**
     * Shows the file chooser dialog and handles replay file selection.
     * @return true if a file was selected and replay started successfully
     */
    fun chooseAndStartReplay(): Boolean {
        val fileChooser = createFileChooser()

        val result = fileChooser.showOpenDialog(MainFrame)
        if (result == JFileChooser.APPROVE_OPTION) {
            val selectedFile = fileChooser.selectedFile
            return startReplay(selectedFile)
        }

        return false
    }

    private fun createFileChooser(): JFileChooser {
        val fileChooser = JFileChooser().apply {
            dialogTitle = "Select Replay File"

            // Create custom file filters for precise matching
            val battleGzFilter = object : FileFilter() {
                override fun accept(f: File): Boolean {
                    // Show directories for navigation and .battle.gz files for selection
                    return f.isDirectory || f.name.endsWith(".battle.gz", ignoreCase = true)
                }
                override fun getDescription(): String = "Battle Replay Files (*.battle.gz)"
            }

            val allBattleFilter = object : FileFilter() {
                override fun accept(f: File): Boolean {
                    // Show directories for navigation and battle files for selection
                    return f.isDirectory || 
                           f.name.endsWith(".battle.gz", ignoreCase = true) ||
                           f.name.endsWith(".battle", ignoreCase = true)
                }
                override fun getDescription(): String = "All Battle Files (*.battle.gz, *.battle)"
            }

            // Add file filters
            addChoosableFileFilter(battleGzFilter)
            addChoosableFileFilter(allBattleFilter)

            // Set default filter to battle.gz files
            fileFilter = battleGzFilter

            // Allow only file selection
            fileSelectionMode = JFileChooser.FILES_ONLY
            isMultiSelectionEnabled = false
        }

        return fileChooser
    }

    private fun startReplay(file: File): Boolean {
        try {
            // Validate file exists and is readable
            if (!file.exists() || !file.canRead()) {
                MessageDialog.showError("Cannot read the selected replay file: ${file.name}")
                return false
            }

            // Create and start the replay player
            val replayPlayer = ReplayBattlePlayer(file)
            Client.setPlayer(replayPlayer)
            Client.start()

            return true

        } catch (e: Exception) {
            MessageDialog.showError("Failed to start replay: ${e.message}")
            return false
        }
    }
}
