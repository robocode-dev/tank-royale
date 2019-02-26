package net.robocode2.gui.frames

import net.miginfocom.swing.MigLayout
import net.robocode2.gui.frames.Constants.MAX_ARENA_SIZE
import net.robocode2.gui.frames.Constants.MAX_GUN_COOLING
import net.robocode2.gui.frames.Constants.MAX_INACTIVITY_TURNS
import net.robocode2.gui.frames.Constants.MAX_NUM_PARTICIPANTS
import net.robocode2.gui.frames.Constants.MAX_NUM_ROUNDS
import net.robocode2.gui.frames.Constants.MIN_ARENA_SIZE
import net.robocode2.gui.frames.Constants.MIN_NUM_PARTICIPANTS
import net.robocode2.gui.frames.ResourceBundles.MESSAGES
import net.robocode2.gui.frames.ResourceBundles.STRINGS
import net.robocode2.gui.extensions.JComponentExt.addNewButton
import net.robocode2.gui.extensions.JComponentExt.addNewLabel
import net.robocode2.gui.extensions.JTextFieldExt.setInputVerifier
import net.robocode2.gui.settings.GameSetup
import net.robocode2.gui.settings.GamesSettings
import net.robocode2.gui.utils.Observable
import java.awt.EventQueue
import javax.swing.*

class SetupRulesDialog(frame: JFrame? = null) : JDialog(frame, ResourceBundles.WINDOW_TITLES.get("setup_rules")) {

    private val games = GamesSettings.games

    // Private events
    private val onOk = Observable<JButton>()
    private val onCancel = Observable<JButton>()
    private val onResetGameType = Observable<JButton>()

    private val gameTypeComboBox = JComboBox(games.keys.toTypedArray())
    private val widthTextField = JTextField(6)
    private val heightTextField = JTextField(6)
    private val minNumParticipantsTextField = JTextField(6)
    private val maxNumParticipantsTextField = JTextField(6)
    private val numberOfRoundsTextField = JTextField(6)
    private val gunCoolingRateTextField = JTextField(6)
    private val inactivityTurnsTextField = JTextField(6)

    private val selectedGameType: String
        get() = gameTypeComboBox.selectedItem as String

    private val gameSetup: GameSetup
        get() = GamesSettings.games[selectedGameType] as GameSetup

    init {
        defaultCloseOperation = DISPOSE_ON_CLOSE

        setLocationRelativeTo(null) // center on screen

        contentPane = JPanel(MigLayout())
        val upperPanel = JPanel(MigLayout())
        val lowerPanel = JPanel(MigLayout())

        val commonPanel = JPanel(MigLayout())
        val arenaPanel = JPanel(MigLayout())

        gameTypeComboBox.addActionListener { changeGameType() }

        contentPane.add(upperPanel, "wrap")
        contentPane.add(lowerPanel, "center")

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

        commonPanel.addNewLabel("gun_cooling_rate")
        commonPanel.add(gunCoolingRateTextField, "wrap")

        commonPanel.addNewLabel("inactivity_turns")
        commonPanel.add(inactivityTurnsTextField, "wrap")

        arenaPanel.border = BorderFactory.createTitledBorder(STRINGS.get("arena_size"))

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
        gunCoolingRateTextField.setInputVerifier { gunCoolingRateVerifier() }
        inactivityTurnsTextField.setInputVerifier { inactivityTurnsVerifier() }

        onOk.subscribe { saveSettings(); close() }
        onCancel.subscribe { close() }
        onResetGameType.subscribe { resetGameType() }

        pack()
    }

    private fun close() {
        isVisible = false
        dispose()
    }

    private fun saveSettings() {
        GamesSettings.save()
    }

    private fun resetGameType() {
        val default: GameSetup? = GamesSettings.defaultGameSetup[selectedGameType]
        if (default != null) {
            GamesSettings.games[selectedGameType] = default.copy()
            changeGameType()
        }
    }

    private fun changeGameType() {
        widthTextField.text = gameSetup.width.toString()
        heightTextField.text = gameSetup.height.toString()
        minNumParticipantsTextField.text = gameSetup.minNumParticipants.toString()
        maxNumParticipantsTextField.text = gameSetup.maxNumParticipants?.toString()

        numberOfRoundsTextField.text = gameSetup.numberOfRounds.toString()
        inactivityTurnsTextField.text = gameSetup.inactivityTurns.toString()
        gunCoolingRateTextField.text = gameSetup.gunCoolingRate.toString()
    }

    private fun widthVerifier(): Boolean {
        val width: Int? = try {
            widthTextField.text.trim().toInt()
        } catch (e: NumberFormatException) {
            null
        }
        val valid = width != null && width in MIN_ARENA_SIZE..MAX_ARENA_SIZE
        if (valid && width != null) {
            gameSetup.width = width
        } else {
            showMessage(String.format(MESSAGES.get("arena_size_range"), MIN_ARENA_SIZE, MAX_ARENA_SIZE))

            widthTextField.text = "" + gameSetup.width
        }
        return valid
    }

    private fun heightVerifier(): Boolean {
        val height: Int? = try {
            heightTextField.text.trim().toInt()
        } catch (e: NumberFormatException) {
            null
        }
        val valid = height != null && height in MIN_ARENA_SIZE..MAX_ARENA_SIZE
        if (valid && height != null) {
            gameSetup.height = height
        } else {
            showMessage(String.format(MESSAGES.get("arena_size_range"), MIN_ARENA_SIZE, MAX_ARENA_SIZE))

            heightTextField.text = "" + gameSetup.height
        }
        return valid
    }

    private fun minNumParticipantsVerifier(): Boolean {
        val minNum: Int? = try {
            minNumParticipantsTextField.text.trim().toInt()
        } catch (e: NumberFormatException) {
            null
        }
        val valid = minNum != null && minNum in MIN_NUM_PARTICIPANTS..MAX_NUM_PARTICIPANTS
        if (valid && minNum != null) {
            gameSetup.minNumParticipants = minNum
        } else {
            showMessage(String.format(MESSAGES.get("min_num_participants"), MIN_NUM_PARTICIPANTS))

            minNumParticipantsTextField.text = "" + gameSetup.minNumParticipants
        }
        return valid
    }

    private fun maxNumParticipantsVerifier(): Boolean {
        if (maxNumParticipantsTextField.text.isBlank()) {
            gameSetup.maxNumParticipants = null
            return true
        }
        val minNum: Int? = try {
            minNumParticipantsTextField.text.trim().toInt()
        } catch (e: NumberFormatException) {
            null
        }
        val maxNum: Int? = try {
            maxNumParticipantsTextField.text.trim().toInt()
        } catch (e: NumberFormatException) {
            null
        }
        val valid = minNum != null && maxNum != null &&
                (minNum in MIN_NUM_PARTICIPANTS..MAX_NUM_PARTICIPANTS) && (maxNum in minNum..MAX_NUM_PARTICIPANTS)
        if (valid && maxNum != null) {
            gameSetup.maxNumParticipants = maxNum
        } else {
            if (maxNum == null || maxNum > MAX_NUM_PARTICIPANTS) {
                showMessage(String.format(MESSAGES.get("max_num_participants"), MAX_NUM_PARTICIPANTS))
            } else {
                showMessage(MESSAGES.get("max_num_participants_too_small"))
            }
            maxNumParticipantsTextField.text = "" + gameSetup.maxNumParticipants
        }
        return valid
    }

    private fun numberOfRoundsVerifier(): Boolean {
        val numRounds: Int? = try {
            numberOfRoundsTextField.text.trim().toInt()
        } catch (e: NumberFormatException) {
            null
        }
        val valid = numRounds != null && numRounds in 1..MAX_NUM_ROUNDS
        if (valid && numRounds != null) {
            gameSetup.numberOfRounds = numRounds
        } else {
            showMessage(String.format(MESSAGES.get("num_rounds_range"), MAX_NUM_ROUNDS))

            maxNumParticipantsTextField.text = "" + gameSetup.numberOfRounds
        }
        return valid
    }

    private fun gunCoolingRateVerifier(): Boolean {
        val rate: Double? = try {
            gunCoolingRateTextField.text.trim().toDouble()
        } catch (e: NumberFormatException) {
            null
        }
        val valid = rate != null && rate > 0 && rate <= MAX_GUN_COOLING
        if (valid && rate != null) {
            gameSetup.gunCoolingRate = rate
        } else {
            showMessage(String.format(MESSAGES.get("gun_cooling_range"), "" + MAX_GUN_COOLING))

            gunCoolingRateTextField.text = "" + gameSetup.gunCoolingRate
        }
        return valid
    }

    private fun inactivityTurnsVerifier(): Boolean {
        val turns: Int? = try {
            inactivityTurnsTextField.text.trim().toInt()
        } catch (e: NumberFormatException) {
            null
        }
        val valid = turns != null && turns in 0..MAX_INACTIVITY_TURNS
        if (valid && turns != null) {
            gameSetup.inactivityTurns = turns
        } else {
            showMessage(String.format(MESSAGES.get("num_inactivity_turns_range"), MAX_INACTIVITY_TURNS))

            inactivityTurnsTextField.text = "" + gameSetup.inactivityTurns
        }
        return valid
    }

    private fun showMessage(msg: String) {
        JOptionPane.showMessageDialog(this, msg)
    }
}

fun main() {
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())

    EventQueue.invokeLater {
        SetupRulesDialog().isVisible = true
    }
}