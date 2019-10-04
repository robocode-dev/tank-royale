package dev.robocode.tankroyale.ui.desktop.ui.battle

import kotlinx.serialization.ImplicitReflectionSerializer
import net.miginfocom.swing.MigLayout
import dev.robocode.tankroyale.ui.desktop.extensions.JComponentExt.addNewButton
import dev.robocode.tankroyale.ui.desktop.extensions.JComponentExt.addNewLabel
import dev.robocode.tankroyale.ui.desktop.extensions.JComponentExt.showMessage
import dev.robocode.tankroyale.ui.desktop.extensions.JTextFieldExt.setInputVerifier
import dev.robocode.tankroyale.ui.desktop.settings.GamesSettings
import dev.robocode.tankroyale.ui.desktop.settings.MutableGameSetup
import dev.robocode.tankroyale.ui.desktop.ui.GameConstants.MAX_ARENA_SIZE
import dev.robocode.tankroyale.ui.desktop.ui.GameConstants.MAX_GUN_COOLING
import dev.robocode.tankroyale.ui.desktop.ui.GameConstants.MAX_INACTIVITY_TURNS
import dev.robocode.tankroyale.ui.desktop.ui.GameConstants.MAX_NUM_PARTICIPANTS
import dev.robocode.tankroyale.ui.desktop.ui.GameConstants.MAX_NUM_ROUNDS
import dev.robocode.tankroyale.ui.desktop.ui.GameConstants.MAX_READY_TIMEOUT
import dev.robocode.tankroyale.ui.desktop.ui.GameConstants.MAX_TURN_TIMEOUT
import dev.robocode.tankroyale.ui.desktop.ui.GameConstants.MIN_ARENA_SIZE
import dev.robocode.tankroyale.ui.desktop.ui.GameConstants.MIN_NUM_PARTICIPANTS
import dev.robocode.tankroyale.ui.desktop.ui.ResourceBundles.MESSAGES
import dev.robocode.tankroyale.ui.desktop.ui.ResourceBundles.STRINGS
import dev.robocode.tankroyale.ui.desktop.util.Event
import javax.swing.*

@ImplicitReflectionSerializer
class SetupRulesPanel : JPanel(MigLayout("fill")) {

    // Private events
    private val onSave = Event<JButton>()
    private val onCancel = Event<JButton>()
    private val onResetToDefault = Event<JButton>()

    private val gameTypeComboBox = GameTypeComboBox()
    private val widthTextField = JTextField(6)
    private val heightTextField = JTextField(6)
    private val minNumParticipantsTextField = JTextField(6)
    private val maxNumParticipantsTextField = JTextField(6)
    private val numberOfRoundsTextField = JTextField(6)
    private val gunCoolingRateTextField = JTextField(6)
    private val inactivityTurnsTextField = JTextField(6)
    private val readyTimeoutTextField = JTextField(6)
    private val turnTimeoutTextField = JTextField(6)

    private var gameSetup: MutableGameSetup
        get() = gameTypeComboBox.gameSetup.toMutableGameSetup()
        set(value) {
            gameTypeComboBox.gameSetup = value.copy().toGameSetup()
        }

    private var lastGameSetup = gameSetup.copy()

    init {
        val commonPanel = JPanel(MigLayout()).apply {
            addNewLabel("game_type")
            add(gameTypeComboBox, "wrap")

            addNewLabel("min_num_of_participants")
            add(minNumParticipantsTextField, "wrap")

            addNewLabel("max_num_of_participants")
            add(maxNumParticipantsTextField, "wrap")

            addNewLabel("number_of_rounds")
            add(numberOfRoundsTextField, "wrap")

            addNewLabel("gun_cooling_rate")
            add(gunCoolingRateTextField, "wrap")

            addNewLabel("max_inactivity_turns")
            add(inactivityTurnsTextField, "wrap")

            addNewLabel("ready_timeout");
            add(readyTimeoutTextField, "wrap")

            addNewLabel("turn_timeout");
            add(turnTimeoutTextField, "wrap")
        }
        val arenaPanel = JPanel(MigLayout()).apply {
            border = BorderFactory.createTitledBorder(STRINGS.get("arena_size"))

            addNewLabel("width")
            add(widthTextField, "wrap")
            addNewLabel("height")
            add(heightTextField)
        }
        val upperPanel = JPanel(MigLayout()).apply {
            add(commonPanel, "west")
            add(arenaPanel, "east")
        }
        val lowerPanel = JPanel(MigLayout()).apply {
            addNewButton("save", onSave, "tag ok")
            addNewButton("cancel", onCancel, "tag cancel")
            addNewButton("reset_to_default", onResetToDefault, "tag apply")
        }
        add(upperPanel, "center, wrap")
        add(lowerPanel, "center")

        gameTypeComboBox.selectedIndex = 0

        widthTextField.setInputVerifier { widthVerifier() }
        heightTextField.setInputVerifier { heightVerifier() }
        minNumParticipantsTextField.setInputVerifier { minNumParticipantsVerifier() }
        maxNumParticipantsTextField.setInputVerifier { maxNumParticipantsVerifier() }
        numberOfRoundsTextField.setInputVerifier { numberOfRoundsVerifier() }
        gunCoolingRateTextField.setInputVerifier { gunCoolingRateVerifier() }
        inactivityTurnsTextField.setInputVerifier { inactivityTurnsVerifier() }
        readyTimeoutTextField.setInputVerifier { readyTimeoutVerifier() }
        turnTimeoutTextField.setInputVerifier { turnTimeoutVerifier() }

        onSave.subscribe {
            lastGameSetup = gameSetup
            saveSettings()
        }
        onCancel.subscribe {
            gameSetup = lastGameSetup
            updateFieldsForGameType()
        }
        onResetToDefault.subscribe {
            gameTypeComboBox.resetGameType()
        }

        gameTypeComboBox.onGameTypeChanged.subscribe {
            updateFieldsForGameType()
        }
        updateFieldsForGameType()
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
        inactivityTurnsTextField.text = gameSetup.maxInactivityTurns.toString()
        gunCoolingRateTextField.text = gameSetup.gunCoolingRate.toString()
        readyTimeoutTextField.text = gameSetup.readyTimeout.toString()
        turnTimeoutTextField.text = gameSetup.turnTimeout.toString()
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
            maxNumParticipantsTextField.text =
                if (gameSetup.maxNumberOfParticipants == null) "" else "${gameSetup.maxNumberOfParticipants}"
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
            gameSetup.maxInactivityTurns = turns
        } else {
            showMessage(String.format(MESSAGES.get("num_inactivity_turns_range"), MAX_INACTIVITY_TURNS))

            inactivityTurnsTextField.text = "" + gameSetup.maxInactivityTurns
        }
        return valid
    }

    private fun readyTimeoutVerifier(): Boolean {
        val timeout: Int? = try {
            readyTimeoutTextField.text.trim().toInt()
        } catch (e: NumberFormatException) {
            null
        }
        val valid = timeout != null && timeout >= 0
        if (valid && timeout != null) {
            gameSetup.readyTimeout = timeout
        } else {
            showMessage(String.format(MESSAGES.get("ready_timeout_range"), MAX_READY_TIMEOUT))

            readyTimeoutTextField.text = "" + gameSetup.readyTimeout
        }
        return valid
    }

    private fun turnTimeoutVerifier(): Boolean {
        val timeout: Int? = try {
            turnTimeoutTextField.text.trim().toInt()
        } catch (e: NumberFormatException) {
            null
        }
        val valid = timeout != null && timeout >= 0
        if (valid && timeout != null) {
            gameSetup.turnTimeout = timeout
        } else {
            showMessage(String.format(MESSAGES.get("turn_timeout_range"), MAX_TURN_TIMEOUT))

            turnTimeoutTextField.text = "" + gameSetup.turnTimeout
        }
        return valid
    }
}
