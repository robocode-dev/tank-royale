package dev.robocode.tankroyale.gui.ui.about

import dev.robocode.tankroyale.common.Event
import dev.robocode.tankroyale.gui.ui.MainFrame
import dev.robocode.tankroyale.gui.ui.components.RcDialog
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.addOkButton
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.setDefaultButton
import dev.robocode.tankroyale.gui.ui.extensions.WindowExt.onActivated
import dev.robocode.tankroyale.gui.util.JavaVersion
import dev.robocode.tankroyale.common.util.Version
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

    private fun html(): String = """
        <table style="border-spacing: 10px; font-size: 10px">
            <tr>
                <td valign="top"><image width="64" height="64" src="$url"></td>
                <td><span style="font-family: Arial, Helvetica, sans-serif;">
                    <b>Robocode Tank Royale</b><br>
                    Version: $version<br>
                    Running on <strong>Java $javaVersion ($javaWordSize)</strong> by $javaVendor<br>
                    <br>
                    <b>Huge thanks to every
                    <a href="https://github.com/robocode-dev/tank-royale/graphs/contributors">contributor</a></b>
                     — you make this project shine! <font color="red">❤️</font><br>
                    <br>
                    Copyright © 2022 Flemming N&oslash;rnberg Larsen
                 </span></td>
            </tr>
        </table>
    """.trimIndent()

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

        onOk.subscribe(this) {
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
            } catch (ignore: Exception) {
            }
        }
    }
}
