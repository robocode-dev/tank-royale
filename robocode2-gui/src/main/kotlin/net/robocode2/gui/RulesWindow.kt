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
import javax.swing.*
import net.robocode2.gui.Constants.MIN_ARENA_SIZE
import net.robocode2.gui.Constants.MAX_ARENA_SIZE
import net.robocode2.gui.Constants.MIN_NUM_PARTICIPANTS
import net.robocode2.gui.Constants.MAX_NUM_PARTICIPANTS
import net.robocode2.gui.Constants.MAX_NUM_ROUNDS
import net.robocode2.gui.Constants.MAX_GUN_COOLING
import net.robocode2.gui.Constants.MAX_INACTIVITY_TURNS
import kotlin.NumberFormatException


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
    private val gunCoolingRateTextField = JTextField(6)
    private val inactivityTurnsTextField = JTextField(6)

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

        commonPanel.addNewLabel("gun_cooling_rate")
        commonPanel.add(gunCoolingRateTextField, "wrap")

        commonPanel.addNewLabel("inactivity_turns")
        commonPanel.add(inactivityTurnsTextField, "wrap")

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
        gunCoolingRateTextField.setInputVerifier { gunCoolingRateVerifier() }
        inactivityTurnsTextField.setInputVerifier { inactivityTurnsVerifier() }

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
        val width: Int? = try { widthTextField.text.trim().toInt() } catch (e: NumberFormatException) { null }
        val valid = width != null && width in MIN_ARENA_SIZE..MAX_ARENA_SIZE
        if (valid && width != null) {
            gameType.width = width
        } else {
            showMessage(String.format(MESSAGES.get("arena_size_range"), MIN_ARENA_SIZE, MAX_ARENA_SIZE))

            widthTextField.text = "" + gameType.width
        }
        return valid
}

    private fun heightVerifier(): Boolean {
        val height: Int? = try { heightTextField.text.trim().toInt() } catch (e: NumberFormatException) { null }
        val valid = height != null && height in MIN_ARENA_SIZE..MAX_ARENA_SIZE
        if (valid && height != null) {
            gameType.height = height
        } else {
            showMessage(String.format(MESSAGES.get("arena_size_range"), MIN_ARENA_SIZE, MAX_ARENA_SIZE))

            heightTextField.text = "" + gameType.height
        }
        return valid
    }

    private fun minNumParticipantsVerifier(): Boolean {
        val minNum: Int? = try { minNumParticipantsTextField.text.trim().toInt() } catch (e: NumberFormatException) { null }
        val valid = minNum != null && minNum in MIN_NUM_PARTICIPANTS .. MAX_NUM_PARTICIPANTS
        if (valid && minNum != null) {
            gameType.minNumParticipants = minNum
        } else {
            showMessage(String.format(MESSAGES.get("min_num_participants"), MIN_NUM_PARTICIPANTS))

            minNumParticipantsTextField.text = "" + gameType.minNumParticipants
        }
        return valid
    }

    private fun maxNumParticipantsVerifier(): Boolean {
        if (maxNumParticipantsTextField.text.isBlank()) {
            gameType.maxNumParticipants = null
            return true
        }
        val minNum: Int? = try { minNumParticipantsTextField.text.trim().toInt() } catch (e: NumberFormatException) { null }
        val maxNum: Int? = try { maxNumParticipantsTextField.text.trim().toInt() } catch (e: NumberFormatException) { null }
        val valid = minNum != null && maxNum != null &&
                (minNum in MIN_NUM_PARTICIPANTS .. MAX_NUM_PARTICIPANTS) && (maxNum in minNum..MAX_NUM_PARTICIPANTS)
        if (valid && maxNum != null) {
            gameType.maxNumParticipants = maxNum
        } else {
            if (maxNum == null || maxNum > MAX_NUM_PARTICIPANTS) {
                showMessage(String.format(MESSAGES.get("max_num_participants"), MAX_NUM_PARTICIPANTS))
            } else {
                showMessage(MESSAGES.get("max_num_participants_too_small"))
            }
            maxNumParticipantsTextField.text = "" + gameType.maxNumParticipants
        }
        return valid
    }

    private fun numberOfRoundsVerifier(): Boolean {
        val numRounds: Int? = try { numberOfRoundsTextField.text.trim().toInt() } catch (e: NumberFormatException) { null }
        val valid = numRounds != null && numRounds in 1 .. MAX_NUM_ROUNDS
        if (valid && numRounds != null) {
            gameType.numberOfRounds = numRounds
        } else {
            showMessage(String.format(MESSAGES.get("num_rounds_range"), MAX_NUM_ROUNDS))

            maxNumParticipantsTextField.text = "" + gameType.numberOfRounds
        }
        return valid
    }

    private fun gunCoolingRateVerifier(): Boolean {
        val rate: Double? = try { gunCoolingRateTextField.text.trim().toDouble() } catch (e: NumberFormatException) { null }
        val valid = rate != null && rate > 0 && rate <= MAX_GUN_COOLING
        if (valid && rate != null) {
            gameType.gunCoolingRate = rate
        } else {
            showMessage(String.format(MESSAGES.get("gun_cooling_range"), "" + MAX_GUN_COOLING))

            gunCoolingRateTextField.text = "" + gameType.gunCoolingRate
        }
        return valid
    }

    private fun inactivityTurnsVerifier(): Boolean {
        val turns: Int? = try { inactivityTurnsTextField.text.trim().toInt() } catch (e: NumberFormatException) { null }
        val valid = turns != null && turns in 0 .. MAX_INACTIVITY_TURNS
        if (valid && turns != null) {
            gameType.inactivityTurns = turns
        } else {
            showMessage(String.format(MESSAGES.get("num_inactivity_turns_range"), MAX_INACTIVITY_TURNS))

            inactivityTurnsTextField.text = "" + gameType.inactivityTurns
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
        RulesWindow.isVisible = true
    }
}