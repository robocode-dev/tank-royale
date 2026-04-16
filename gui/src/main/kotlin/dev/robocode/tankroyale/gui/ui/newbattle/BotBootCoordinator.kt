package dev.robocode.tankroyale.gui.ui.newbattle

import dev.robocode.tankroyale.client.model.BotInfo
import dev.robocode.tankroyale.gui.booter.BotIdentity
import dev.robocode.tankroyale.gui.booter.BotIdentityReader
import dev.robocode.tankroyale.gui.booter.BootProcess
import dev.robocode.tankroyale.gui.client.Client
import dev.robocode.tankroyale.gui.settings.ConfigSettings
import java.awt.Window
import java.nio.file.Paths

/**
 * Coordinates the bot boot lifecycle: reads identities, starts the boot process,
 * and manages the [BootProgressDialog]. Extracted from [BotSelectionPanel] to keep
 * that panel focused on UI concerns only (SOLID-SRP).
 */
class BotBootCoordinator(private val windowOwner: () -> Window?) {

    private var activeProgressDialog: BootProgressDialog? = null

    fun bootBots(botInfoList: List<BotInfo>) {
        var unknownCount = 0
        val expectedIdentities = botInfoList.flatMap { botInfo ->
            try {
                BotIdentityReader.readIdentities(Paths.get(botInfo.host))
            } catch (e: Exception) {
                System.err.println("Failed to read bot identity from ${botInfo.host}: ${e.message}")
                unknownCount++
                emptyList()
            }
        }

        BootProcess.boot(botInfoList.map { it.host })

        val existing = activeProgressDialog
        if (existing != null && existing.isVisible) {
            existing.addExpectedBots(expectedIdentities, unknownCount)
        } else {
            val baseline = Client.joinedBots.map { BotIdentity(it.name, it.version) }.groupingBy { it }.eachCount()
            val dialog = BootProgressDialog(
                owner = windowOwner(),
                expectedIdentities = expectedIdentities,
                unknownCount = unknownCount,
                baseline = baseline,
                timeoutSeconds = ConfigSettings.bootTimeout,
                onSuccess = { activeProgressDialog = null },
                onCancel = {
                    activeProgressDialog = null
                    BootProcess.stop()
                },
            )
            activeProgressDialog = dialog
            dialog.isVisible = true
        }
    }

    fun reset() {
        activeProgressDialog = null
    }
}
