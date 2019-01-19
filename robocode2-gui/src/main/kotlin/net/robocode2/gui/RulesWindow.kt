package net.robocode2.gui

import io.reactivex.subjects.PublishSubject
import net.miginfocom.swing.MigLayout
import net.robocode2.gui.ResourceBundles.STRINGS
import net.robocode2.gui.ResourceBundles.MESSAGES
import net.robocode2.gui.extensions.JComponentExt.addNewLabel
import net.robocode2.gui.extensions.JComponentExt.addNewButton
import net.robocode2.gui.extensions.JTextFieldExt.setInputVerifier
import net.robocode2.gui.settings.GameSetupSettings
import net.robocode2.gui.settings.GameType
import java.awt.EventQueue
import java.text.NumberFormat
import javax.swing.*
import javax.swing.text.NumberFormatter
import net.robocode2.gui.Constants.MIN_ARENA_SIZE
import net.robocode2.gui.Constants.MAX_ARENA_SIZE
import net.robocode2.gui.Constants.MIN_NUM_PARTICIPANTS
import net.robocode2.gui.Constants.MAX_NUM_PARTICIPANTS
import net.robocode2.gui.Constants.MAX_NUM_ROUNDS


object RulesWindow : JDialog() {

    private val gameSetup = GameSetupSettings.gameSetup

    // Private events
    private val onClose: PublishSubject<Unit> = PublishSubject.create()
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

    private val gameType: GameType
        get() = GameSetupSettings.gameSetup[selectedGameType] as GameType

    init {
        defaultCloseOperation = DISPOSE_ON_CLOSE

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

        commonPanel.addNewLabel("min_num_of_participants")
        commonPanel.add(minNumParticipantsTextField, "wrap")

        commonPanel.addNewLabel("max_num_of_participants")
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
        arenaPanel.add(widthTextField, "wrap")
        arenaPanel.addNewLabel("height")
        arenaPanel.add(heightTextField)

        lowerPanel.addNewButton("ok", onOk, "tag ok")
        lowerPanel.addNewButton("cancel", onCancel, "tag cancel")
        lowerPanel.addNewButton("reset_game_type_to_default", onResetGameType, "tag apply")

        gameTypeComboBox.selectedIndex = 0

        widthTextField.setInputVerifier { widthVerifier() }
        heightTextField.setInputVerifier { heightVerifier() }
        minNumParticipantsTextField.setInputVerifier { minNumParticipantsVerifier() }
        maxNumParticipantsTextField.setInputVerifier { maxNumParticipantsVerifier() }
        numberOfRoundsTextField.setInputVerifier { numberOfRoundsVerifier() }

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
        widthTextField.text = gameType.width.toString()
        heightTextField.text = gameType.height.toString()
        minNumParticipantsTextField.text = gameType.minNumParticipants.toString()
        maxNumParticipantsTextField.text = gameType.maxNumParticipants?.toString()

        numberOfRoundsTextField.text = gameType.numberOfRounds.toString()
        inactivityTurnsTextField.text = gameType.inactivityTurns.toString()
        gunCoolingRateTextField.text = gameType.gunCoolingRate.toString()
    }

    private fun widthVerifier(): Boolean {
        val width = widthTextField.text.trim().toInt()
        val valid = sizeVerifier(width)
        if (!valid) {
            widthTextField.text = "" + gameType.width
        }
        return valid
    }

    private fun heightVerifier(): Boolean {
        val height = heightTextField.text.trim().toInt()
        val valid = sizeVerifier(height)
        if (!valid) {
            heightTextField.text = "" + gameType.height
        }
        return valid
    }

    private fun sizeVerifier(size: Int): Boolean {
        val valid = size in MIN_ARENA_SIZE..MAX_ARENA_SIZE
        if (!valid) {
            JOptionPane.showMessageDialog(this,
                    String.format(MESSAGES.get("arena_size_range"), MIN_ARENA_SIZE, MAX_ARENA_SIZE))
        }
        return valid
    }

    private fun minNumParticipantsVerifier(): Boolean {
        val num = minNumParticipantsTextField.text.trim().toInt()
        val valid = num in MIN_NUM_PARTICIPANTS..MAX_NUM_PARTICIPANTS
        if (!valid) {
            JOptionPane.showMessageDialog(this,
                    String.format(MESSAGES.get("min_num_participants"), MIN_NUM_PARTICIPANTS))

            minNumParticipantsTextField.text = "" + gameType.minNumParticipants
        }
        return valid
    }

    private fun maxNumParticipantsVerifier(): Boolean {
        val minNum = minNumParticipantsTextField.text.trim().toInt()
        val maxNum = maxNumParticipantsTextField.text.trim().toInt()
        val valid = (minNum in MIN_NUM_PARTICIPANTS..MAX_NUM_PARTICIPANTS) && (maxNum in minNum..MAX_NUM_PARTICIPANTS)
        if (!valid) {
            if (maxNum > MAX_NUM_PARTICIPANTS) {
                JOptionPane.showMessageDialog(this,
                        String.format(MESSAGES.get("max_num_participants"), MAX_NUM_PARTICIPANTS))
            } else {
                JOptionPane.showMessageDialog(this, MESSAGES.get("max_num_participants_too_small"))
            }
            maxNumParticipantsTextField.text = "" + gameType.maxNumParticipants
        }
        return valid
    }

    private fun numberOfRoundsVerifier(): Boolean {
        val rounds = numberOfRoundsTextField.text.trim().toInt()
        val valid = rounds in 1..MAX_NUM_ROUNDS
        if (!valid) {
            JOptionPane.showMessageDialog(this,
                    String.format(MESSAGES.get("num_of_rounds_range"), MAX_NUM_ROUNDS))

            maxNumParticipantsTextField.text = "" + gameType.numberOfRounds
        }
        return valid
    }

    private fun onSomethingChanged() {
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