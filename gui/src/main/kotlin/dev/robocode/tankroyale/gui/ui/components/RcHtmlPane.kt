package dev.robocode.tankroyale.gui.ui.components

import java.awt.Insets
import javax.swing.JTextField
import javax.swing.JTextPane
import javax.swing.text.html.HTMLEditorKit
import javax.swing.text.html.StyleSheet

open class RcHtmlPane(private val styleSheetFactory: () -> StyleSheet = { StyleSheet() }) : JTextPane() {

    private var rawHtml: String? = null

    override fun setText(t: String?) {
        rawHtml = t
        super.setText(t)
    }

    init {
        isEditable = false
        isFocusable = false
        contentType = "text/html"
        border = plainBorder()
        margin = Insets(0, 0, 0, 0)
        minimumSize = JTextField().minimumSize
        editorKit = HTMLEditorKit().apply { styleSheet = styleSheetFactory() }
        inactiveBg()?.let { background = it }
    }

    override fun updateUI() {
        super.updateUI()
        // Guard: HTMLEditorKit is only present after our init block has run.
        if (editorKit !is HTMLEditorKit) return
        // Reinstalling the entire EditorKit forces JEditorPane to create a fresh
        // HTMLDocument initialised from the new stylesheet, then re-parse the HTML.
        // Background must be set AFTER editorKit assignment — HTMLEditorKit.install()
        // resets it to Color.WHITE on attach.
        border = plainBorder()
        val html = rawHtml ?: ""
        editorKit = HTMLEditorKit().apply { styleSheet = styleSheetFactory() }
        inactiveBg()?.let { background = it }
        super.setText(html)
    }
}
