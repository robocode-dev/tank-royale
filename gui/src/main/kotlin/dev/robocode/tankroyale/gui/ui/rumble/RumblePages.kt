package dev.robocode.tankroyale.gui.ui.rumble

import dev.robocode.tankroyale.gui.ui.MainFrame
import dev.robocode.tankroyale.gui.ui.Messages
import dev.robocode.tankroyale.gui.util.Browser
import dev.robocode.tankroyale.rumble.EnvironmentChoice
import dev.robocode.tankroyale.rumble.RumbleLinks
import dev.robocode.tankroyale.rumble.RumbleMenuSwitch
import javax.swing.JOptionPane

/** The Rumble menu's entries that only open a page in the browser: Rankings and Help. Neither needs a sign-in. */
object RumblePages {

    /**
     * Opens the rankings of the Rumble the menu talks to: the public Beta's, unless a developer has set the program to
     * the development Rumble.
     */
    fun openRankings() {
        when (val choice = RumbleMenuSwitch.environment(System.getenv(RumbleMenuSwitch.ENVIRONMENT_VARIABLE))) {
            is EnvironmentChoice.Unknown -> JOptionPane.showMessageDialog(MainFrame, String.format(Messages.get("rumble.account.unknown_environment"), choice.name))
            is EnvironmentChoice.Chosen -> RumbleLinks.rankings(choice.environment)?.let { Browser.browse(it) }
        }
    }

    /** Opens the Rumble guide, which is the same for every environment, only as an https page like Rankings. */
    fun openHelp() {
        RumbleLinks.GUIDE.takeIf { RumbleLinks.isHttpsPage(it) }?.let { Browser.browse(it) }
    }
}
