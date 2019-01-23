package net.robocode2.gui

import io.reactivex.subjects.PublishSubject
import net.miginfocom.swing.MigLayout
import net.robocode2.gui.extensions.JComponentExt.addNewButton
import java.awt.EventQueue
import javax.swing.*

class SelectBots(frame: JFrame? = null) : JDialog(frame, ResourceBundles.WINDOW_TITLES.get("select_bots")) {

    // Private events
    private val onAdd: PublishSubject<Unit> = PublishSubject.create()
    private val onAddAll: PublishSubject<Unit> = PublishSubject.create()
    private val onRemove: PublishSubject<Unit> = PublishSubject.create()
    private val onRemoveAll: PublishSubject<Unit> = PublishSubject.create()

    init {
        defaultCloseOperation = DISPOSE_ON_CLOSE

        setSize(400, 250)
        minimumSize = size
        setLocationRelativeTo(null) // center on screen

        contentPane = JPanel(MigLayout(
                "insets 10, fill",
                "[grow][][grow]"))

        val leftPanel = JPanel(MigLayout())
        val centerPanel = JPanel(MigLayout("insets 0"))
        val rightPanel = JPanel(MigLayout())
        contentPane.add(leftPanel, "grow")
        contentPane.add(centerPanel, "")
        contentPane.add(rightPanel, "grow")

        leftPanel.border = BorderFactory.createTitledBorder(ResourceBundles.STRINGS.get("available_bots"))
        rightPanel.border = BorderFactory.createTitledBorder(ResourceBundles.STRINGS.get("selected_bots"))

        val addPanel = JPanel(MigLayout("insets 0, fill", "[fill]"))
        val removePanel = JPanel(MigLayout("insets 0, fill", "[fill]"))

        val middlePanel = JPanel(MigLayout("fill"))

        centerPanel.add(addPanel, "north")
        centerPanel.add(middlePanel, "h 300")
        centerPanel.add(removePanel, "south")

        addPanel.addNewButton("add_arrow", onAdd, "cell 0 1")
        addPanel.addNewButton("add_all_arrow", onAddAll, "cell 0 2")
        removePanel.addNewButton("arrow_remove", onRemove, "cell 0 3")
        removePanel.addNewButton("arrow_remove_all", onRemoveAll, "cell 0 4")
    }

    private fun close() {
        isVisible = false
        dispose()

//        onClose.onNext(Unit)
    }
}

fun main() {
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())

    EventQueue.invokeLater {
        SelectBots().isVisible = true
    }
}