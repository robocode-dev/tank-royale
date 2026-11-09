package dev.robocode.tankroyale.gui.ui.rumble

import dev.robocode.tankroyale.gui.settings.ConfigSettings
import dev.robocode.tankroyale.gui.ui.Messages
import dev.robocode.tankroyale.rumble.DownloadTexts
import dev.robocode.tankroyale.rumble.RumbleLicense
import dev.robocode.tankroyale.rumble.RumbleLicenses
import net.miginfocom.swing.MigLayout
import java.awt.Component
import java.awt.Dimension
import java.awt.Font
import java.awt.Rectangle
import javax.swing.JButton
import javax.swing.JComboBox
import javax.swing.JPanel
import javax.swing.JTextArea
import javax.swing.Scrollable
import javax.swing.UIManager
import javax.swing.text.DefaultCaret

/** A panel that is as wide as the window, so wrapped texts wrap at the window's edge and never run out over it. */
internal class WindowWidthPanel : JPanel(MigLayout("insets 12, wrap 1, fillx", "[grow, fill]")), Scrollable {
    override fun getPreferredScrollableViewportSize(): Dimension = preferredSize
    override fun getScrollableUnitIncrement(visibleRect: Rectangle, orientation: Int, direction: Int) = 16
    override fun getScrollableBlockIncrement(visibleRect: Rectangle, orientation: Int, direction: Int) = 64
    override fun getScrollableTracksViewportWidth() = true
    override fun getScrollableTracksViewportHeight() = false
}

/**
 * A text that wraps at the window's edge and can be selected and copied. Its caret never follows new text, because a
 * caret that moves makes the scroll pane around it jump to where the text area is.
 */
internal fun wrappedText(text: String, bold: Boolean = false) = JTextArea().apply {
    (caret as? DefaultCaret)?.updatePolicy = DefaultCaret.NEVER_UPDATE
    this.text = text
    lineWrap = true
    wrapStyleWord = true
    isEditable = false
    isOpaque = false
    border = null
    font = UIManager.getFont("Label.font").let { if (bold) it.deriveFont(Font.BOLD) else it }
}

/**
 * The list box of the licenses the Rumble allows and the short explanation of the one that is chosen. Choosing another
 * replaces the explanation. The pages are opened through [open], which belongs to the screen.
 */
internal class RumbleLicensePanel(private val open: (String) -> Unit) : JPanel(MigLayout("insets 0, wrap 1, fillx", "[grow, fill]")) {

    val box = JComboBox(RumbleLicenses.ALL.map { it.id }.toTypedArray())
    val explanation = JPanel(MigLayout("insets 0, wrap 1, fillx", "[grow, fill]"))

    init {
        add(wrappedText(text("rumble.download.licenses_intro")), "growx, wmin 10")
        add(JPanel(MigLayout("insets 0", "[][grow, fill]")).apply {
            add(wrappedText(text("rumble.download.licenses_label")), "wmin 10")
            add(box)
        }, "growx, wmin 10")
        add(explanation, "growx, wmin 10")
        box.addActionListener { explain(RumbleLicenses.ALL.first { it.id == box.selectedItem }) }
        explain(RumbleLicenses.ALL.first())
    }

    private fun explain(license: RumbleLicense) {
        fun line(component: Component) = explanation.add(component, "growx, wmin 10")
        fun part(name: String) = text(DownloadTexts.forLicense(license, name))
        explanation.removeAll()
        line(wrappedText(text("rumble.download.for_author"), bold = true))
        line(wrappedText(part("author")))
        line(wrappedText(text("rumble.download.author_later")))
        line(wrappedText(text("rumble.download.for_user"), bold = true))
        line(wrappedText(text("rumble.download.may", part("may"))))
        line(wrappedText(text("rumble.download.must", part("must"))))
        line(wrappedText(text("rumble.download.note", part("note"))))
        line(JPanel(MigLayout("insets 0, fillx", "[grow, fill, sg][grow, fill, sg]")).apply {
            add(link("rumble.download.button.official") { open(license.officialPage) }, "wmin 10")
            add(link("rumble.download.button.explanation") { open(license.explanationPage(ConfigSettings.language.lowercase())) }, "wmin 10")
        })
        line(wrappedText(text("rumble.download.credit")))
        explanation.revalidate()
        explanation.repaint()
    }

    private fun link(key: String, action: () -> Unit) = JButton(text(key)).apply { addActionListener { action() } }

    private fun text(key: String, vararg arguments: Any): String = String.format(Messages.get(key), *arguments)
}
