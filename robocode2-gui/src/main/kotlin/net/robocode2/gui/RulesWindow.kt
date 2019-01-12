package net.robocode2.gui

import io.reactivex.subjects.PublishSubject
import net.miginfocom.swing.MigLayout
import net.robocode2.gui.ResourceBundles.STRINGS
import net.robocode2.gui.extensions.JComponentExt.addNewLabel
import net.robocode2.gui.extensions.JComponentExt.addNewButton
import net.robocode2.gui.settings.GameType
import java.awt.EventQueue
import javax.swing.*

object RulesWindow : JFrame(ResourceBundles.WINDOW_TITLES.get("rules")) {

    val onClose: PublishSubject<Unit> = PublishSubject.create()
    private val onResetGameType: PublishSubject<Unit> = PublishSubject.create()

    private val gameSetup = mapOf(
            "classic" to GameType(),
            "1-vs-1" to GameType(width = 1000, height = 1000, maxNumParticipants = 2),
            "melee" to GameType(width = 1000, height = 1000, minNumParticipants = 10)
    )

    private val gameTypeComboBox = JComboBox(gameSetup.keys.toTypedArray())
    private val widthTextField = JTextField(6)
    private val heightTextField = JTextField(6)
    private val minNumParticipantsTextField = JTextField(6)
    private val maxNumParticipantsTextField = JTextField(6)
    private val numberOfRoundsTextField = JTextField(6)
    private val inactivityTurnsTextField = JTextField(6)
    private val gunCoolingRateTextField = JTextField(6)

    init {
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
        arenaPanel.add(widthTextField, "wrap")
        arenaPanel.addNewLabel("height")
        arenaPanel.add(heightTextField)

        lowerPanel.addNewButton("ok", onClose, "tag ok")
        lowerPanel.addNewButton("cancel", onClose, "tag cancel")
        lowerPanel.addNewButton("reset_game_type_to_default", onResetGameType, "tag apply")

        gameTypeComboBox.selectedIndex = 0
    }

    private fun onGameTypeChanged() {
        val key: String = gameTypeComboBox.selectedItem as String
        val gt: GameType = gameSetup[key] as GameType

        widthTextField.text = gt.width.toString()
        heightTextField.text = gt.height.toString()
        minNumParticipantsTextField.text = gt.minNumParticipants.toString()
        maxNumParticipantsTextField.text = gt.maxNumParticipants?.toString()

        numberOfRoundsTextField.text = gt.numberOfRounds.toString()
        inactivityTurnsTextField.text = gt.inactivityTurns.toString()
        gunCoolingRateTextField.text = gt.gunCoolingRate.toString()
    }
}

fun main() {
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())

    EventQueue.invokeLater {
        RulesWindow.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        RulesWindow.isVisible = true
    }
}