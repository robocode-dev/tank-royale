package dev.robocode.tankroyale.gui.ui.about

import dev.robocode.tankroyale.common.event.On
import dev.robocode.tankroyale.common.event.Event
import dev.robocode.tankroyale.gui.ui.MainFrame
import dev.robocode.tankroyale.gui.ui.components.RcDialog
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.addOkButton
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.setDefaultButton
import dev.robocode.tankroyale.gui.ui.extensions.WindowExt.onActivated
import dev.robocode.tankroyale.gui.util.JavaVersion
import dev.robocode.tankroyale.common.util.Version
import dev.robocode.tankroyale.gui.ui.About
import net.miginfocom.swing.MigLayout
import java.awt.Container
import dev.robocode.tankroyale.gui.util.Browser
import java.net.URL
import javax.swing.JButton
import javax.swing.JEditorPane
import javax.swing.JPanel
import javax.swing.event.HyperlinkEvent
import javax.swing.event.HyperlinkListener
import javax.swing.text.html.HTMLEditorKit


object AboutBox : RcDialog(MainFrame, "about_dialog") {

    private fun html(): String = String.format(
        About.get("about_html"),
        url,
        version,
        javaVersion,
        javaWordSize,
        javaVendor
    )

    private val onOk = Event<JButton>()

    private val url: URL? = AboutBox.javaClass.classLoader.getResource("gfx/Tank.png")?.toURI()?.toURL()
    private val version = Version.version
    private val javaVersion = JavaVersion.version
    private val javaVendor = JavaVersion.vendor
    private val javaWordSize = JavaVersion.wordSize

    init {
        val panel = JPanel(MigLayout("fill, insets 0 0 5 0", "[center]"))
        panel.add(htmlPane(), "wrap")

        contentPane.add(panel)
        pack()

        val okButton = panel.addOkButton(onOk).apply {
            setDefaultButton(this)
        }
        pack()
        setLocationRelativeTo(owner) // center on the owner window

        isResizable = false

        onOk+= On(this) {
            dispose()
        }

        onActivated {
            okButton.requestFocus()
        }
    }

    private fun htmlPane(): Container =
        JEditorPane().apply {
            // Ensure HTML editor kit is installed before setting content
            isEditable = false
            contentType = "text/html"
            editorKit = HTMLEditorKit()
            text = html()
            isOpaque = true
            addHyperlinkListener(hyperLinkHandler())
        }

    fun hyperLinkHandler() = HyperlinkListener { event ->
        if (event.eventType == HyperlinkEvent.EventType.ACTIVATED) {
            try {
                val link = event.url?.toExternalForm() ?: event.description
                if (!link.isNullOrBlank()) {
                    Browser.browse(link)
                }
            } catch (_: Exception) {
            }
        }
    }
}
