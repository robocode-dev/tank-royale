package net.robocode2.gui.ui.battle

import net.miginfocom.swing.MigLayout
import net.robocode2.gui.extensions.JComponentExt.addNewButton
import net.robocode2.gui.extensions.JComponentExt.addNewLabel
import net.robocode2.gui.extensions.JTextFieldExt.setInputVerifier
import net.robocode2.gui.model.GameSetup
import net.robocode2.gui.settings.GamesSettings
import net.robocode2.gui.ui.Constants.MAX_ARENA_SIZE
import net.robocode2.gui.ui.Constants.MAX_GUN_COOLING
import net.robocode2.gui.ui.Constants.MAX_INACTIVITY_TURNS
import net.robocode2.gui.ui.Constants.MAX_NUM_PARTICIPANTS
import net.robocode2.gui.ui.Constants.MAX_NUM_ROUNDS
import net.robocode2.gui.ui.Constants.MIN_ARENA_SIZE
import net.robocode2.gui.ui.Constants.MIN_NUM_PARTICIPANTS
import net.robocode2.gui.ui.ResourceBundles.MESSAGES
import net.robocode2.gui.ui.ResourceBundles.STRINGS
import net.robocode2.gui.utils.Disposable
import net.robocode2.gui.utils.Observable
import javax.swing.*

class SetupRulesPanel : JPanel(MigLayout("fill")), AutoCloseable {

    // Private events
    private val onOk = Observable<JButton>()
    private val onCancel = Observable<JButton>()
    private val onResetGameType = Observable<JButton>()

    private val gameTypeComboBox = GameTypeComboBox()
    private val widthTextField = JTextField(6)
    private val heightTextField = JTextField(6)
    private val minNumParticipantsTextField = JTextField(6)
    private val maxNumParticipantsTextField = JTextField(6)
    private val numberOfRoundsTextField = JTextField(6)
    private val gunCoolingRateTextField = JTextField(6)
    private val inactivityTurnsTextField = JTextField(6)

    private val gameSetup: GameSetup
        get() = gameTypeComboBox.gameSetup

    private val disposables = ArrayList<Disposable>()

    init {
        val upperPanel = JPanel(MigLayout())
        val lowerPanel = JPanel(MigLayout())

        val commonPanel = JPanel(MigLayout())
        val arenaPanel = JPanel(MigLayout())

        add(upperPanel, "center, wrap")
        add(lowerPanel, "center")

        upperPanel.add(commonPanel, "west")
        upperPanel.add(arenaPanel, "east")

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

        onOk.subscribe { saveSettings() }
        onCancel.subscribe {}
        onResetGameType.subscribe { gameTypeComboBox.resetGameType() }

        gameTypeComboBox.onGameTypeChanged.subscribe { updateFieldsForGameType() }
        updateFieldsForGameType()
    }

    override fun close() {
        disposables.forEach { it.dispose() }
        disposables.clear()
    }

    private fun saveSettings() {
        GamesSettings.save()
    }

    private fun updateFieldsForGameType() {
        widthTextField.text = gameSetup.arenaWidth.toString()
        heightTextField.text = gameSetup.arenaHeight.toString()
        minNumParticipantsTextField.text = gameSetup.minNumberOfParticipants.toString()
        maxNumParticipantsTextField.text = gameSetup.maxNumberOfParticipants?.toString() ?: ""

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
            gameSetup.arenaWidth = width
        } else {
            showMessage(String.format(MESSAGES.get("arena_size_range"), MIN_ARENA_SIZE, MAX_ARENA_SIZE))

            widthTextField.text = "" + gameSetup.arenaWidth
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
            gameSetup.arenaHeight = height
        } else {
            showMessage(String.format(MESSAGES.get("arena_size_range"), MIN_ARENA_SIZE, MAX_ARENA_SIZE))

            heightTextField.text = "" + gameSetup.arenaHeight
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
            gameSetup.minNumberOfParticipants = minNum
        } else {
            showMessage(String.format(MESSAGES.get("min_num_participants"), MIN_NUM_PARTICIPANTS))

            minNumParticipantsTextField.text = "" + gameSetup.minNumberOfParticipants
        }
        return valid
    }

    private fun maxNumParticipantsVerifier(): Boolean {
        if (maxNumParticipantsTextField.text.isBlank()) {
            gameSetup.maxNumberOfParticipants = null
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
            gameSetup.maxNumberOfParticipants = maxNum
        } else {
            if (maxNum == null || maxNum > MAX_NUM_PARTICIPANTS) {
                showMessage(String.format(MESSAGES.get("max_num_participants"), MAX_NUM_PARTICIPANTS))
            } else {
                showMessage(MESSAGES.get("max_num_participants_too_small"))
            }
            maxNumParticipantsTextField.text = "" + gameSetup.maxNumberOfParticipants
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
