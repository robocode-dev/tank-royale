package dev.robocode.tankroyale.gui.ansi

import dev.robocode.tankroyale.gui.ansi.AnsiAttributesExt.updateAnsi
import dev.robocode.tankroyale.gui.ansi.esc_code.CommandCode
import dev.robocode.tankroyale.gui.ansi.esc_code.EscapeSequence
import java.awt.Color
import java.io.*
import javax.swing.text.*

/**
 * The AnsiEditorKit is a specialized [StyledEditorKit] that is able to create [StyledDocument]s based on text
 * containing ANSI escape sequences for styling the text.
 * The documents are created with the Monospaced font to simulate an old-fashioned text console for displaying ANSI
 * graphics.
 * @param fontSize is the monospaced font size to use across an entire document. Default is 14.
 * @param ansiColors is the [IAnsiColors] to use for the ANSI color schema. Default is the [DefaultAnsiColors] color
 * scheme.
 */
class AnsiEditorKit(
    private val fontSize: Int = 14,
    private val ansiColors: IAnsiColors = DefaultAnsiColors
) : StyledEditorKit() {

    private val ansiEscCodeRegex = Regex("\u001b\\[\\d+(;\\d+)*m")

    override fun getContentType() = "text/x-ansi"

    /** {@inheritDoc} */
    override fun read(inputStream: InputStream, doc: Document, pos: Int) {
        read(BufferedReader(InputStreamReader(inputStream)), doc, pos)
    }

    /** {@inheritDoc} */
    override fun read(reader: Reader, doc: Document, pos: Int) {
        require(doc is StyledDocument) { "The document must be a StyledDocument for this kit" }
        insertAnsi(doc, reader.readText(), pos)
    }

    /** {@inheritDoc} */
    override fun write(outputStream: OutputStream, doc: Document, pos: Int, len: Int) {
        write(BufferedWriter(OutputStreamWriter(outputStream)), doc, pos, len)
    }

    /** {@inheritDoc} */
    override fun write(writer: Writer, doc: Document, pos: Int, len: Int) {
        writer.write(doc.getText(pos, len))
    }

    /**
     * Inserts an ANSI text into a specific text position of the document.
     * ANSI escape codes are converted into [AttributeSet]s to style the inserted text.
     * @param doc is a [StyledDocument] the ANSI text is inserted into.
     * @param ansiText is the ANSI text to insert into the document.
     * @param offset is the offset into the document where the text will be inserted.
     */
    fun insertAnsi(doc: StyledDocument, ansiText: String, offset: Int = doc.length) {
        require(offset >= 0) { "Offset cannot be negative. Was: $offset" }

        var attributes: MutableAttributeSet = SimpleAttributeSet(doc.getCharacterElement(offset).attributes)
        StyleConstants.setFontFamily(attributes, "Monospaced")
        StyleConstants.setFontSize(attributes, fontSize)

        // Set the foreground color to the default ANSI color if no foreground color has been set previously
        if (StyleConstants.getForeground(attributes) == Color.black) { // if no foreground color is set, black is returned?!
            attributes = attributes.updateAnsi(EscapeSequence(CommandCode.DEFAULT), ansiColors)
        }

        val match = ansiEscCodeRegex.find(ansiText, 0)
        if (match == null) {
            doc.insertString(offset, ansiText, attributes) // no ansi codes found
            return
        }

        var codeStart = match.range.first

        var text = ansiText.substring(0, codeStart)
        if (text.isNotEmpty()) {
            doc.insertString(doc.length, text, attributes) // no ansi codes found
        }

        ansiEscCodeRegex.findAll(ansiText, codeStart).forEach { m ->
            val ansiCode = m.value
            codeStart = m.range.first
            val codeEnd = m.range.last + 1

            attributes = attributes.updateAnsi(EscapeSequence.parse(ansiCode), ansiColors)

            val endMatch = ansiEscCodeRegex.find(ansiText, codeEnd)

            text = if (endMatch == null) {
                ansiText.substring(codeEnd)
            } else {
                ansiText.substring(codeEnd, endMatch.range.first)
            }
            if (text.isNotEmpty()) {
                doc.insertString(doc.length, text, attributes)
            }
        }
    }
}