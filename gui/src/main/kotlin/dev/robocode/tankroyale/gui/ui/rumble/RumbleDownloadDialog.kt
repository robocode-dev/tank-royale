package dev.robocode.tankroyale.gui.ui.rumble

import com.formdev.flatlaf.FlatLaf
import dev.robocode.tankroyale.gui.botapi.BotApiPlatform
import dev.robocode.tankroyale.gui.settings.ConfigSettings
import dev.robocode.tankroyale.gui.ui.MainFrame
import dev.robocode.tankroyale.gui.ui.Messages
import dev.robocode.tankroyale.gui.ui.Strings
import dev.robocode.tankroyale.gui.ui.components.RcDialog
import dev.robocode.tankroyale.gui.util.Browser
import dev.robocode.tankroyale.gui.ui.newbattle.BotSelectionEvents
import dev.robocode.tankroyale.gui.ui.newbattle.CopyLevelTexts
import dev.robocode.tankroyale.rumble.BotDownloads
import dev.robocode.tankroyale.rumble.DownloadController
import dev.robocode.tankroyale.rumble.DownloadFolders
import dev.robocode.tankroyale.rumble.FolderOutcome
import dev.robocode.tankroyale.rumble.DownloadResult
import dev.robocode.tankroyale.rumble.DownloadRow
import dev.robocode.tankroyale.rumble.DownloadState
import dev.robocode.tankroyale.rumble.DownloadTexts
import dev.robocode.tankroyale.rumble.DownloadedState
import dev.robocode.tankroyale.rumble.EnvironmentChoice
import dev.robocode.tankroyale.rumble.HttpGitHubApi
import dev.robocode.tankroyale.rumble.LibraryDownloadPort
import dev.robocode.tankroyale.rumble.PlatformDownloads
import dev.robocode.tankroyale.rumble.PythonSetupResult
import dev.robocode.tankroyale.rumble.RumbleLicenses
import dev.robocode.tankroyale.rumble.RumbleMenuSwitch
import dev.robocode.tankroyale.rumble.Tone
import net.miginfocom.swing.MigLayout
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import java.awt.Desktop
import java.awt.GridLayout
import java.awt.Point
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.nio.file.Path
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import javax.swing.AbstractButton
import javax.swing.ButtonGroup
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JFileChooser
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.JRadioButton
import javax.swing.JScrollPane
import javax.swing.SwingUtilities
import javax.swing.border.EmptyBorder

/**
 * The Download bots screen. It only shows the state of a [DownloadController], which holds all the behavior and does
 * every read and write away from the event thread. This class runs on the event thread and never waits for anything.
 * The bots it downloads are only put in the folder: nothing here runs them or adds them to a list of bots to start.
 */
class RumbleDownloadDialog private constructor(environmentName: String, controllerFor: (RumbleDownloadDialog) -> DownloadController) :
    RcDialog(MainFrame, "rumble_download_dialog") {

    private val content = WindowWidthPanel()
    private val scroll = JScrollPane(content, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER).apply { border = null }
    private val actions = JPanel(GridLayout(1, 0, 8, 0)).apply { border = EmptyBorder(6, 12, 12, 12) }
    private val controller: DownloadController = controllerFor(this)

    init {
        title = text("rumble.download.title", title, environmentName)
        contentPane.add(scroll, BorderLayout.CENTER)
        contentPane.add(actions, BorderLayout.SOUTH)
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
        actions.removeAll()
        when (state) {
            is DownloadState.Reading -> put(wrapped(text("rumble.download.reading")))
            is DownloadState.Choosing -> renderChoosing(state)
            is DownloadState.Downloading -> put(wrapped(text("rumble.download.downloading", state.count)))
            is DownloadState.SettingUpPython -> put(wrapped(text("rumble.download.setup.running")))
            is DownloadState.Ended -> renderEnded(state)
            is DownloadState.Failed -> renderFailed(state)
        }
        content.revalidate()
        content.repaint()
        actions.revalidate()
        actions.repaint()
        SwingUtilities.invokeLater { scroll.viewport.viewPosition = if (state is DownloadState.Choosing) position else Point(0, 0) }
    }

    private fun renderChoosing(state: DownloadState.Choosing) {
        put(wrapped(text("rumble.download.intro")))
        put(wrapped(text("rumble.download.warning"), bold = true))
        folderLine(state.folder)
        when (val setup = state.setup) {
            null -> {}
            is PythonSetupResult.Done -> put(status(true, text("rumble.download.setup.done")))
            is PythonSetupResult.Failed -> {
                put(status(false, text(DownloadTexts.forSetupFailure(setup.step))))
                put(wrapped(text(if (setup.leftBehind) "rumble.download.setup.left_behind" else "rumble.download.setup.nothing_left")))
                if (setup.details.isNotBlank()) put(wrapped(text("rumble.download.details", setup.details)))
            }
        }
        put(wrapped(text("rumble.download.which"), bold = true))
        val group = ButtonGroup()
        put(choice(JRadioButton().also { group.add(it) }, text("rumble.download.all"), state.all, true) { if (it) controller.setAll(true) })
        put(choice(JRadioButton().also { group.add(it) }, text("rumble.download.selected"), !state.all, true) { if (it) controller.setAll(false) })
        if (state.allPlatforms.isNotEmpty()) {
            put(wrapped(text("rumble.download.platforms"), bold = true))
            put(JPanel(MigLayout("insets 0, wrap 2", "[][]")).apply {
                state.allPlatforms.forEach { platform -> add(platformBox(platform, state)) }
            })
            if (!state.versionsKnown) put(wrapped(text("rumble.download.platform.versions_unknown")))
        }
        if (state.shown.isEmpty()) put(wrapped(text("rumble.download.none")))
        state.shown.forEach { put(row(it, state)) }
        act(
            button("rumble.download.licenses") { showLicenses() },
            button("rumble.download.button.download") { controller.download() }.apply { isEnabled = state.toDownload.isNotEmpty() },
            button("rumble.download.button.close") { dispose() },
        )
    }

    /**
     * The box of one platform. A platform this computer cannot run says why in a few words, and ticking it opens a dialog
     * that says what is missing and what can be done, instead of ticking it at once.
     */
    private fun platformBox(platform: String, state: DownloadState.Choosing): JCheckBox {
        val check = state.checkOf(platform)?.takeIf { !it.ready }
        val label = check?.let { text("rumble.download.platform.label", platform, text(DownloadTexts.forPlatformShort(it.reason))) } ?: platform
        return JCheckBox(label, platform in state.platforms).apply {
            addActionListener {
                if (isSelected && check != null) {
                    isSelected = false
                    RumblePlatformDialog(
                        this@RumbleDownloadDialog, check, controller.pythonSetupCommands(),
                        open = { open(it) },
                        setUp = { controller.setUpPython() },
                        showAnyway = { controller.setPlatform(platform, true) },
                    ).isVisible = true
                } else {
                    controller.setPlatform(platform, isSelected)
                }
            }
        }
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
        val here = row.versionHere?.takeIf { it != row.version }?.let { " " + text("rumble.download.row_here", it) }.orEmpty()
        return line + here + row.license?.let { " " + text("rumble.download.row_license", it, CopyLevelTexts.wordsOf(it)) }.orEmpty()
    }

    private fun renderEnded(state: DownloadState.Ended) {
        val summary = text("rumble.download.ended", state.placed, state.results.size)
        put(status(state.results.none { DownloadTexts.toneOf(it) == Tone.BAD }, summary))
        state.results.forEach { result -> put(toned(resultText(result), DownloadTexts.toneOf(result))) }
        put(wrapped(state.folder.toString()))
        put(button("rumble.download.button.show_folder") { showFolder(state.folder) })
        val label = Strings.get("rumble_downloads_label")
        put(wrapped(when (state.folderOutcome) {
            FolderOutcome.ADDED -> text("rumble.download.folder_added", label)
            FolderOutcome.LABELLED -> text("rumble.download.folder_in_new_battle", label)
            FolderOutcome.SWITCHED_OFF -> text("rumble.download.folder_switched_off", label)
            FolderOutcome.OWN_FOLDER -> text("rumble.download.folder_own")
            FolderOutcome.OWN_FOLDER_SWITCHED_OFF -> text("rumble.download.folder_own_switched_off")
            FolderOutcome.ADDED_AS_OWN -> text("rumble.download.folder_added_own")
            FolderOutcome.NOT_REMEMBERED -> text("rumble.download.folder_not_remembered")
            FolderOutcome.COMMA_IN_PATH -> text("rumble.download.folder_comma")
            FolderOutcome.NOT_LISTED, null -> text("rumble.download.add_folder_hint")
        }))
        act(
            button("rumble.download.licenses") { showLicenses() },
            button("rumble.download.button.back") { controller.start() },
            button("rumble.download.button.close") { dispose() },
        )
    }

    private fun resultText(result: DownloadResult): String {
        val line = text("rumble.download.result_line", result.bot.name, result.bot.version, text(DownloadTexts.forResult(result)))
        return when (result) {
            is DownloadResult.Placed -> line + " " + (result.license?.let { text("rumble.download.license_of", it, CopyLevelTexts.wordsOf(it)) } ?: text("rumble.download.no_license"))
            is DownloadResult.NotPlaced -> if (result.message.isBlank()) line else line + " " + text("rumble.download.details", result.message)
            is DownloadResult.KeptAside -> line + " " + text("rumble.download.kept_at", result.keptAt.toString())
            else -> line
        }
    }

    private fun renderFailed(state: DownloadState.Failed) {
        put(status(false, text(DownloadTexts.forFailure(state.failure))))
        if (state.details.isNotBlank()) put(wrapped(text("rumble.download.details", state.details)))
        folderLine(state.folder)
        act(button("rumble.download.button.try_again") { controller.start() }, button("rumble.download.button.close") { dispose() })
    }

    private fun folderLine(folder: Path) {
        put(wrapped(text("rumble.download.folder", folder.toString())))
        put(button("rumble.download.button.change_folder") { chooseFolder(folder) })
    }


    /** The licenses in a small window of their own, so they are there when wanted and take no room on the screen. */
    private fun showLicenses() {
        val window = RcDialog(this, "rumble_licenses_dialog")
        val inside = WindowWidthPanel().apply { add(RumbleLicensePanel { open(it) }, "growx, wmin 10") }
        window.contentPane.add(JScrollPane(inside, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER).apply { border = null }, BorderLayout.CENTER)
        window.contentPane.add(JPanel(GridLayout(1, 0)).apply {
            border = EmptyBorder(6, 12, 12, 12)
            add(button("rumble.download.button.close") { window.dispose() })
        }, BorderLayout.SOUTH)
        window.setSize(580, 560)
        window.setLocationRelativeTo(this)
        window.isModal = true
        window.isVisible = true
    }

    /** Opens one of the fixed pages about the licenses in the browser, and never any other address. */
    private fun open(address: String) {
        if (RumbleLicenses.isFixedPage(address) || PlatformDownloads.isFixedPage(address)) Browser.browse(address)
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

    /** The buttons at the bottom of the window, always in view however long the list is. */
    private fun act(vararg buttons: JButton) = buttons.forEach { actions.add(it) }

    /** Adds a component that is as wide as the window and never wider, so nothing runs out over the edge. */
    private fun put(component: Component) {
        content.add(component, "growx, wmin 10")
    }

    private fun wrapped(text: String, bold: Boolean = false) = wrappedText(text, bold)

    private fun button(key: String, action: () -> Unit) = JButton(text(key)).apply { addActionListener { action() } }

    companion object {
        /** Opens the screen for the environment the variable names. It needs no sign-in, and it never waits for the network. */
        fun open() {
            when (val choice = RumbleMenuSwitch.environment(System.getenv(RumbleMenuSwitch.ENVIRONMENT_VARIABLE))) {
                is EnvironmentChoice.Unknown -> JOptionPane.showMessageDialog(MainFrame, String.format(Messages.get("rumble.account.unknown_environment"), choice.name))
                is EnvironmentChoice.Chosen -> show(choice)
            }
        }

        /** Puts the program's own Python Bot API where pip can install it from, under the name pip requires. */
        private fun writePythonBotApi(target: Path) {
            val resource = RumbleDownloadDialog::class.java.classLoader.getResourceAsStream(BotApiPlatform.PYTHON.resourceName)
                ?: throw IOException(BotApiPlatform.PYTHON.resourceName)
            Files.createDirectories(target.parent)
            resource.use { Files.copy(it, target, StandardCopyOption.REPLACE_EXISTING) }
        }

        /**
         * Adds the download folder to the bot directories, switched on and labelled, so its bots are in New Battle, unless
         * it is there already, and saves the folders and labels in one write. Called away from the event thread: the
         * folder is looked into there, and the settings are read and saved on the event thread, where every other part
         * of the program reads them and listens for them being saved. When the folder cannot be looked into or the
         * settings cannot be saved, nothing is remembered, the reason is logged, and the result asks the person to add
         * the folder by hand.
         */
        private fun rememberFolder(folder: Path, placed: Boolean): FolderOutcome? {
            val onlyDownloads = runCatching { placed && BotDownloads.holdsOnlyDownloads(folder) }
            var outcome: FolderOutcome? = null
            try {
                SwingUtilities.invokeAndWait {
                    outcome = DownloadFolders.rememberAfterDownload(
                        folder,
                        onlyDownloads = { onlyDownloads.getOrThrow() },
                        read = { ConfigSettings.botFolders to ConfigSettings.botDirectoryLabels },
                        save = { ConfigSettings.saveBotFolders(it.folders, it.labels) },
                        onFailure = { it.printStackTrace() },
                        placed = placed,
                    )
                    // New Battle lists the new bots also when the folder was already among the bot directories, so
                    // the settings did not change.
                    if (placed) BotSelectionEvents.onBotsPlaced(Unit)
                }
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
                e.printStackTrace()
            } catch (e: java.lang.reflect.InvocationTargetException) {
                e.printStackTrace()
            }
            return outcome
        }

        private fun show(choice: EnvironmentChoice.Chosen) {
            val environment = choice.environment
            val background = Executors.newCachedThreadPool { runnable -> Thread(runnable, "rumble-download").apply { isDaemon = true } }
            val post = Executor { SwingUtilities.invokeLater(it) }
            // The suggestion comes from the person's own bot folders, so it never moves next to a download folder.
            val botFolders = DownloadFolders.ownFolders(ConfigSettings.botFolders, ConfigSettings.botDirectoryLabels)
            val folder = BotDownloads.defaultFolder(botFolders, Path.of(System.getProperty("user.home")))
            RumbleDownloadDialog(environment.displayName) { self ->
                val port = LibraryDownloadPort(
                    environment, HttpGitHubApi(),
                    pythonBotApiName = BotApiPlatform.PYTHON.newFileName(),
                    writePythonBotApi = ::writePythonBotApi,
                    rememberFolder = ::rememberFolder,
                )
                DownloadController(port, folder, background, post) { state -> self.render(state) }
            }.apply {
                // A download that is running is let finish; nothing new is started after the screen is closed. The pool
                // is shut down after the events already waiting, which may still hand it work, the same as the other screens.
                addWindowListener(object : WindowAdapter() {
                    override fun windowClosed(e: WindowEvent) = SwingUtilities.invokeLater { background.shutdown() }
                })
            }.isVisible = true
        }
    }
}
