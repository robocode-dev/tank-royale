package net.robocode2.gui

import io.reactivex.subjects.PublishSubject
import net.miginfocom.swing.MigLayout
import net.robocode2.gui.ResourceBundles.STRINGS
import net.robocode2.gui.extensions.JComponentExt.addNewLabel
import net.robocode2.gui.extensions.JComponentExt.addNewButton
import net.robocode2.gui.extensions.JTextFieldExt.addChangeListener
import net.robocode2.gui.settings.GameSetupSettings
import net.robocode2.gui.settings.GameType
import java.awt.EventQueue
import java.text.NumberFormat
import javax.swing.*
import javax.swing.text.NumberFormatter

object RulesWindow : JFrame(ResourceBundles.WINDOW_TITLES.get("rules")) {

    private val gameSetup = GameSetupSettings.gameSetup

    // Public events
    val onClose: PublishSubject<Unit> = PublishSubject.create()

    // Private events
    private val onOk: PublishSubject<Unit> = PublishSubject.create()
    private val onCancel: PublishSubject<Unit> = PublishSubject.create()
    private val onResetGameType: PublishSubject<Unit> = PublishSubject.create()

    private val gameTypeComboBox = JComboBox(gameSetup.keys.toTypedArray())
    private val widthTextField = JTextField(6)
    private val heightTextField = JTextField(6)
    private val minNumParticipantsTextField = JTextField(6)
    private val maxNumParticipantsTextField = JTextField(6)
    private val numberOfRoundsTextField = JTextField(6)
    private val inactivityTurnsTextField = JTextField(6)
    private val gunCoolingRateTextField = JTextField(6)

    private val selectedGameType: String
        get() = gameTypeComboBox.selectedItem as String

    init {
        defaultCloseOperation = EXIT_ON_CLOSE

        setSize(400, 250)
        minimumSize = size
        setLocationRelativeTo(null) // center on screen

        contentPane = JPanel(MigLayout("insets 10, fill"))
        val upperPanel = JPanel(MigLayout())
        val lowerPanel = JPanel(MigLayout())

        val commonPanel = JPanel(MigLayout("insets 10"))
        val arenaPanel = JPanel(MigLayout("insets 10"))

        gameTypeComboBox.addActionListener { onGameTypeChanged() }

        contentPane.add(upperPanel, "north")
        contentPane.add(lowerPanel, "south")

        upperPanel.add(commonPanel, "west")
        upperPanel.add(arenaPanel, "north")

        commonPanel.addNewLabel("game_type")
        commonPanel.add(gameTypeComboBox, "wrap")

        commonPanel.addNewLabel("min_number_of_participants")
        commonPanel.add(minNumParticipantsTextField, "wrap")

        commonPanel.addNewLabel("max_number_of_participants")
        commonPanel.add(maxNumParticipantsTextField, "wrap")

        commonPanel.addNewLabel("number_of_rounds")
        commonPanel.add(numberOfRoundsTextField, "wrap")

        commonPanel.addNewLabel("inactivity_turns")
        commonPanel.add(inactivityTurnsTextField, "wrap")

        commonPanel.addNewLabel("gun_cooling_rate")
        commonPanel.add(gunCoolingRateTextField, "wrap")

        arenaPanel.border = BorderFactory.createTitledBorder(STRINGS.get("arena_size"))
        arenaPanel.layout = MigLayout("insets 10")

        arenaPanel.addNewLabel("width")
        arenaPanel.add(widthTextField as JTextField, "wrap")
        arenaPanel.addNewLabel("height")
        arenaPanel.add(heightTextField)

        lowerPanel.addNewButton("ok", onOk, "tag ok")
        lowerPanel.addNewButton("cancel", onCancel, "tag cancel")
        lowerPanel.addNewButton("reset_game_type_to_default", onResetGameType, "tag apply")

        gameTypeComboBox.selectedIndex = 0

        widthTextField.addChangeListener { onWidthChanged() }
        heightTextField.addChangeListener { onHeightChanged() }

        onOk.subscribe { saveSettings(); close() }
        onCancel.subscribe { close() }
    }

    private fun saveSettings() {
        GameSetupSettings.save()
    }

    private fun close() {
        isVisible = false
        dispose()

        onClose.onNext(Unit)
    }

    private fun onGameTypeChanged() {
        val gt: GameType = GameSetupSettings.gameSetup[selectedGameType] as GameType

        widthTextField.text = gt.width.toString()
        heightTextField.text = gt.height.toString()
        minNumParticipantsTextField.text = gt.minNumParticipants.toString()
        maxNumParticipantsTextField.text = gt.maxNumParticipants?.toString()

        numberOfRoundsTextField.text = gt.numberOfRounds.toString()
        inactivityTurnsTextField.text = gt.inactivityTurns.toString()
        gunCoolingRateTextField.text = gt.gunCoolingRate.toString()
    }

    private fun onWidthChanged() {
        val gameType = gameSetup[selectedGameType]
        if (gameType != null && widthTextField.text.trim().isNotEmpty()) {
            gameType.width = widthTextField.text.trim().toInt()
        }
    }

    private fun onHeightChanged() {
        val gameType = gameSetup[selectedGameType]
        if (gameType != null && heightTextField.text.trim().isNotEmpty()) {
            gameType.height = heightTextField.text.trim().toInt()
        }
    }

    private fun onSomethingChanged() {
        val gameType = gameSetup[selectedGameType]
        if (gameType != null) {
            if (minNumParticipantsTextField.text.trim().isNotEmpty()) {
                gameType.minNumParticipants = minNumParticipantsTextField.text.trim().toInt()
            }
            if (maxNumParticipantsTextField.text.trim().isNotEmpty()) {
                gameType.maxNumParticipants = maxNumParticipantsTextField.text.trim().toInt()
            } else {
                gameType.maxNumParticipants = null
            }
            if (numberOfRoundsTextField.text.trim().isNotEmpty()) {
                gameType.numberOfRounds = numberOfRoundsTextField.text.trim().toInt()
            }
            if (inactivityTurnsTextField.text.trim().isNotEmpty()) {
                gameType.inactivityTurns = inactivityTurnsTextField.text.trim().toInt()
            }
            if (gunCoolingRateTextField.text.trim().isNotEmpty()) {
                gameType.gunCoolingRate = gunCoolingRateTextField.text.trim().toDouble()
            }
        }
    }

    private fun integerFormat(min: Int = 0, max: Int = Int.MAX_VALUE,
                              allowInvalid: Boolean = false, commitsOnValidEdit: Boolean = false): NumberFormatter {

        val integerFormat = NumberFormat.getIntegerInstance()
        integerFormat.isGroupingUsed = false

        val formatter = NumberFormatter(integerFormat)
        formatter.minimum = min
        formatter.maximum = max
        formatter.allowsInvalid = allowInvalid
        formatter.commitsOnValidEdit = commitsOnValidEdit
        return formatter
    }
}

fun main() {
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())

    EventQueue.invokeLater {
        RulesWindow.isVisible = true
    }
}