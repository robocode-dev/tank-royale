package dev.robocode.tankroyale.gui.ui.rumble

import com.formdev.flatlaf.FlatLaf
import dev.robocode.tankroyale.gui.settings.ConfigSettings
import dev.robocode.tankroyale.gui.ui.MainFrame
import dev.robocode.tankroyale.gui.ui.Messages
import dev.robocode.tankroyale.gui.ui.components.RcDialog
import dev.robocode.tankroyale.gui.util.Browser
import dev.robocode.tankroyale.rumble.BotDownloads
import dev.robocode.tankroyale.rumble.DownloadController
import dev.robocode.tankroyale.rumble.DownloadResult
import dev.robocode.tankroyale.rumble.DownloadRow
import dev.robocode.tankroyale.rumble.DownloadState
import dev.robocode.tankroyale.rumble.DownloadTexts
import dev.robocode.tankroyale.rumble.DownloadedState
import dev.robocode.tankroyale.rumble.EnvironmentChoice
import dev.robocode.tankroyale.rumble.HttpGitHubApi
import dev.robocode.tankroyale.rumble.LibraryDownloadPort
import dev.robocode.tankroyale.rumble.RumbleLicense
import dev.robocode.tankroyale.rumble.RumbleLicenses
import dev.robocode.tankroyale.rumble.RumbleMenuSwitch
import dev.robocode.tankroyale.rumble.Tone
import net.miginfocom.swing.MigLayout
import java.awt.Color
import java.awt.Component
import java.awt.Desktop
import java.awt.Dimension
import java.awt.Font
import java.awt.Point
import java.awt.Rectangle
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.io.File
import java.nio.file.Path
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import javax.swing.AbstractButton
import javax.swing.ButtonGroup
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JComboBox
import javax.swing.JFileChooser
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.JRadioButton
import javax.swing.JScrollPane
import javax.swing.JTextArea
import javax.swing.Scrollable
import javax.swing.SwingUtilities
import javax.swing.UIManager

/**
 * The Download bots screen. It only shows the state of a [DownloadController], which holds all the behavior and does
 * every read and write away from the event thread. This class runs on the event thread and never waits for anything.
 * The bots it downloads are only put in the folder: nothing here runs them or adds them to a list of bots to start.
 */
class RumbleDownloadDialog private constructor(environmentName: String, controllerFor: (RumbleDownloadDialog) -> DownloadController) :
    RcDialog(MainFrame, "rumble_download_dialog") {

    /** A panel that is as wide as the window, so wrapped texts wrap at the window's edge and never run out over it. */
    private class TrackingPanel : JPanel(MigLayout("insets 12, wrap 1, fillx", "[grow, fill]")), Scrollable {
        override fun getPreferredScrollableViewportSize(): Dimension = preferredSize
        override fun getScrollableUnitIncrement(visibleRect: Rectangle, orientation: Int, direction: Int) = 16
        override fun getScrollableBlockIncrement(visibleRect: Rectangle, orientation: Int, direction: Int) = 64
        override fun getScrollableTracksViewportWidth() = true
        override fun getScrollableTracksViewportHeight() = false
    }

    private val content = TrackingPanel()
    private val scroll = JScrollPane(content, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER).apply { border = null }
    private val controller: DownloadController = controllerFor(this)
    private var shownLicense: RumbleLicense = RumbleLicenses.ALL.first()

    init {
        title = text("rumble.download.title", title, environmentName)
        contentPane.add(scroll)
        setSize(620, 640)
        setLocationRelativeTo(owner)
        addWindowListener(object : WindowAdapter() {
            override fun windowClosed(e: WindowEvent) = controller.close()
        })
        render(controller.state)
        controller.start()
    }

    private fun text(key: String, vararg arguments: Any): String = String.format(Messages.get(key), *arguments)

    /** Shows [state] where the person was looking, so ticking a box does not move the list. Called on the event thread. */
    private fun render(state: DownloadState) {
        val position: Point = scroll.viewport.viewPosition
        content.removeAll()
        when (state) {
            is DownloadState.Reading -> put(wrapped(text("rumble.download.reading")))
            is DownloadState.Choosing -> renderChoosing(state)
            is DownloadState.Downloading -> put(wrapped(text("rumble.download.downloading", state.count)))
            is DownloadState.Ended -> renderEnded(state)
            is DownloadState.Failed -> renderFailed(state)
        }
        content.revalidate()
        content.repaint()
        SwingUtilities.invokeLater { scroll.viewport.viewPosition = if (state is DownloadState.Choosing) position else Point(0, 0) }
    }

    private fun renderChoosing(state: DownloadState.Choosing) {
        put(wrapped(text("rumble.download.intro")))
        put(wrapped(text("rumble.download.warning"), bold = true))
        folderLine(state.folder)
        licenses()
        put(wrapped(text("rumble.download.which"), bold = true))
        val group = ButtonGroup()
        put(choice(JRadioButton().also { group.add(it) }, text("rumble.download.all"), state.all, true) { if (it) controller.setAll(true) })
        put(choice(JRadioButton().also { group.add(it) }, text("rumble.download.selected"), !state.all, true) { if (it) controller.setAll(false) })
        if (state.allPlatforms.isNotEmpty()) put(wrapped(text("rumble.download.platforms"), bold = true))
        state.allPlatforms.forEach { platform ->
            put(choice(JCheckBox(), platform, platform in state.platforms, true) { controller.setPlatform(platform, it) })
        }
        if (state.shown.isEmpty()) put(wrapped(text("rumble.download.none")))
        state.shown.forEach { put(row(it, state)) }
        put(button("rumble.download.button.download") { controller.download() }.apply { isEnabled = state.toDownload.isNotEmpty() })
        put(button("rumble.download.button.close") { dispose() })
    }

    /** One bot: a box when there is a choice to make for it, and only its line when there is not. */
    private fun row(row: DownloadRow, state: DownloadState.Choosing): Component {
        val line = rowText(row)
        return when (row.state) {
            DownloadedState.NEW, DownloadedState.UPDATE ->
                choice(JCheckBox(), line, state.all || row.name in state.chosen, !state.all) { controller.setChosen(row.name, it) }
            DownloadedState.CHANGED_HERE ->
                choice(JCheckBox(), line + " " + text("rumble.download.replace_changed"), row.name in state.replaceChanged, true) { controller.setReplaceChanged(row.name, it) }
            DownloadedState.UP_TO_DATE, DownloadedState.KEPT_APART, DownloadedState.NO_LONGER_ACTIVE -> wrapped(line)
        }
    }

    private fun rowText(row: DownloadRow): String {
        val unknown = text("rumble.download.unknown")
        val line = text("rumble.download.row", row.name, row.version ?: unknown, row.platform ?: unknown, text(DownloadTexts.forState(row.state)))
        return row.license?.let { line + " " + text("rumble.download.row_license", it) } ?: line
    }

    private fun renderEnded(state: DownloadState.Ended) {
        val summary = text("rumble.download.ended", state.placed, state.results.size)
        put(status(state.results.none { DownloadTexts.toneOf(it) == Tone.BAD }, summary))
        state.results.forEach { result -> put(toned(resultText(result), DownloadTexts.toneOf(result))) }
        put(wrapped(state.folder.toString()))
        put(button("rumble.download.button.show_folder") { showFolder(state.folder) })
        put(wrapped(text("rumble.download.add_folder_hint")))
        licenses()
        put(button("rumble.download.button.back") { controller.start() })
        put(button("rumble.download.button.close") { dispose() })
    }

    private fun resultText(result: DownloadResult): String {
        val line = text("rumble.download.result_line", result.bot.name, result.bot.version, text(DownloadTexts.forResult(result)))
        return when (result) {
            is DownloadResult.Placed -> line + " " + (result.license?.let { text("rumble.download.license_of", it) } ?: text("rumble.download.no_license"))
            is DownloadResult.NotPlaced -> if (result.message.isBlank()) line else line + " " + text("rumble.download.details", result.message)
            else -> line
        }
    }

    private fun renderFailed(state: DownloadState.Failed) {
        put(status(false, text(DownloadTexts.forFailure(state.failure))))
        if (state.details.isNotBlank()) put(wrapped(text("rumble.download.details", state.details)))
        folderLine(state.folder)
        put(button("rumble.download.button.try_again") { controller.start() })
        put(button("rumble.download.button.close") { dispose() })
    }

    private fun folderLine(folder: Path) {
        put(wrapped(text("rumble.download.folder", folder.toString())))
        put(button("rumble.download.button.change_folder") { chooseFolder(folder) })
    }

    /** The list box of the licenses the Rumble allows, and the short explanation of the one that is chosen. */
    private fun licenses() {
        put(wrapped(text("rumble.download.licenses"), bold = true))
        put(wrapped(text("rumble.download.licenses_intro")))
        val explanation = JPanel(MigLayout("insets 0, wrap 1, fillx", "[grow, fill]"))
        val box = JComboBox(RumbleLicenses.ALL.map { it.id }.toTypedArray()).apply { selectedItem = shownLicense.id }
        box.addActionListener {
            shownLicense = RumbleLicenses.ALL.first { it.id == box.selectedItem }
            fillExplanation(explanation)
            explanation.revalidate()
            explanation.repaint()
        }
        put(JPanel(MigLayout("insets 0", "[][grow, fill]")).apply {
            add(wrapped(text("rumble.download.licenses_label")), "wmin 10")
            add(box)
        })
        fillExplanation(explanation)
        put(explanation)
    }

    private fun fillExplanation(panel: JPanel) {
        val license = shownLicense
        fun line(component: Component) = panel.add(component, "growx, wmin 10")
        fun part(name: String) = text(DownloadTexts.forLicense(license, name))
        line(wrapped(text("rumble.download.for_author"), bold = true))
        line(wrapped(part("author")))
        line(wrapped(text("rumble.download.author_later")))
        line(wrapped(text("rumble.download.for_user"), bold = true))
        line(wrapped(text("rumble.download.may", part("may"))))
        line(wrapped(text("rumble.download.must", part("must"))))
        line(wrapped(text("rumble.download.note", part("note"))))
        line(button("rumble.download.button.official") { open(license.officialPage) })
        line(button("rumble.download.button.explanation") { open(license.explanationPage(ConfigSettings.language.lowercase())) })
        line(wrapped(text("rumble.download.credit")))
    }

    /** Opens one of the fixed pages about the licenses in the browser, and never any other address. */
    private fun open(address: String) {
        if (RumbleLicenses.isFixedPage(address)) Browser.browse(address)
    }

    private fun showFolder(folder: Path) {
        Thread({
            try {
                Desktop.getDesktop().open(folder.toFile())
            } catch (e: Exception) {
                // The desktop cannot show it, so the person gets the path to go to.
                SwingUtilities.invokeLater { JOptionPane.showMessageDialog(this, folder.toString()) }
            }
        }, "rumble-download-show-folder").apply { isDaemon = true }.start()
    }

    private fun chooseFolder(current: Path) {
        val chooser = JFileChooser().apply {
            fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
            val shown = generateSequence(current.toFile()) { it.parentFile }.firstOrNull(File::isDirectory)
            if (shown != null) currentDirectory = shown
        }
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) controller.chooseFolder(chooser.selectedFile.toPath())
    }

    private fun toned(message: String, tone: Tone): Component = when (tone) {
        Tone.GOOD -> status(true, message)
        Tone.BAD -> status(false, message)
        Tone.NEUTRAL -> wrapped(message)
    }

    /** A line that says at a glance whether it is good news or bad: green for success, red for failure, with the word in the text. */
    private fun status(success: Boolean, message: String) =
        wrapped(text(if (success) "rumble.download.status_success" else "rumble.download.status_failed", message), bold = true)
            .apply { foreground = statusColor(success) }

    /** Colors that can be read on both the light and the dark theme, the same as on the other Rumble screens. */
    private fun statusColor(success: Boolean): Color {
        val dark = FlatLaf.isLafDark()
        return if (success) (if (dark) Color(0x6FCF73) else Color(0x1B7F2A)) else (if (dark) Color(0xFF7B72) else Color(0xB3261E))
    }

    /** A check box or a radio button with a text that wraps; the text can be clicked too. */
    private fun choice(box: AbstractButton, label: String, ticked: Boolean, enabled: Boolean, changed: (Boolean) -> Unit): Component {
        box.isSelected = ticked
        box.isEnabled = enabled
        box.addActionListener { changed(box.isSelected) }
        val words = wrapped(label).apply {
            if (enabled) addMouseListener(object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent) = box.doClick()
            })
        }
        return JPanel(MigLayout("insets 0", "[][grow, fill]", "[top]")).apply {
            add(box)
            add(words, "growx, wmin 10")
        }
    }

    /** Adds a component that is as wide as the window and never wider, so nothing runs out over the edge. */
    private fun put(component: Component) {
        content.add(component, "growx, wmin 10")
    }

    /** A text that wraps at the window's edge and can be selected and copied. */
    private fun wrapped(text: String, bold: Boolean = false) = JTextArea(text).apply {
        lineWrap = true
        wrapStyleWord = true
        isEditable = false
        isOpaque = false
        border = null
        font = UIManager.getFont("Label.font").let { if (bold) it.deriveFont(Font.BOLD) else it }
    }

    private fun button(key: String, action: () -> Unit) = JButton(text(key)).apply { addActionListener { action() } }

    companion object {
        /** Opens the screen for the environment the variable names. It needs no sign-in, and it never waits for the network. */
        fun open() {
            when (val choice = RumbleMenuSwitch.environment(System.getenv(RumbleMenuSwitch.ENVIRONMENT_VARIABLE))) {
                is EnvironmentChoice.Unknown -> JOptionPane.showMessageDialog(MainFrame, String.format(Messages.get("rumble.account.unknown_environment"), choice.name))
                is EnvironmentChoice.Chosen -> show(choice)
            }
        }

        private fun show(choice: EnvironmentChoice.Chosen) {
            val environment = choice.environment
            val background = Executors.newCachedThreadPool { runnable -> Thread(runnable, "rumble-download").apply { isDaemon = true } }
            val post = Executor { SwingUtilities.invokeLater(it) }
            val botFolders = ConfigSettings.botDirectories.filter { it.enabled }.map { Path.of(it.path) }
            val folder = BotDownloads.defaultFolder(botFolders, Path.of(System.getProperty("user.home")))
            RumbleDownloadDialog(environment.displayName) { self ->
                DownloadController(LibraryDownloadPort(environment, HttpGitHubApi()), folder, background, post) { state -> self.render(state) }
            }.isVisible = true
        }
    }
}
