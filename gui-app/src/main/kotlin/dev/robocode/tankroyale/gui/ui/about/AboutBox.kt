package dev.robocode.tankroyale.gui.ui.about

import dev.robocode.tankroyale.gui.MainWindow
import dev.robocode.tankroyale.gui.ui.ResourceBundles
import dev.robocode.tankroyale.gui.util.JavaVersion
import dev.robocode.tankroyale.gui.util.Version
import java.awt.Container
import java.net.URL
import javax.swing.JDialog
import javax.swing.JEditorPane


object AboutBox : JDialog(MainWindow, ResourceBundles.UI_TITLES.get("about_dialog")) {

    private val url: URL? = AboutBox.javaClass.classLoader.getResource("gfx/Tank.png")?.toURI()?.toURL()
    private val version = Version.version
    private val javaVersion = JavaVersion.version
    private val javaVendor = JavaVersion.vendor
    private val javaWordSize = JavaVersion.wordSize

    init {
        contentPane = htmlPane()
        pack()
        setLocationRelativeTo(MainWindow) // center on main window
    }

    private fun htmlPane(): Container =
        JEditorPane("text/html; charset=UTF8", html()).apply {
            isEditable = false
        }

    private fun html(): String = """
        <table style="border-spacing: 10px">
            <tr>
                <td valign="top"><image width="64" height="64" src="$url"></td>
                <td><span style="font-family: Arial, Helvetica, sans-serif;">
                    <b>Robocode Tank Royale</b>
                    <br>
                    Version: $version<br>
                    <br>
                    Copyright © 2022 Flemming N&oslash;rnberg Larsen<br>
                    <br>
                    Running on <strong>Java $javaVersion ($javaWordSize)</strong> by $javaVendor
                 </span></td>
            </tr>
        </table>
    """.trimIndent()
}

fun main() {

    AboutBox.isVisible = true
}