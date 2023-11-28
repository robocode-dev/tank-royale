package dev.robocode.tankroyale.gui.ansi2

import java.io.*
import javax.swing.text.*


class AnsiEditorKit : StyledEditorKit() {

    private val escCodeRegex = Regex("\u001b\\[(\\d+;?)+m")

    override fun getContentType() = "text/x-ansi"

    override fun read(inputStream: InputStream?, doc: Document?, pos: Int) {
        read(inputStream?.let { InputStreamReader(it) }, doc, pos)
    }

    override fun read(reader: Reader?, doc: Document?, pos: Int) {
        TODO("Not yet implemented")
    }

    override fun write(outputStream: OutputStream?, doc: Document?, pos: Int, len: Int) {
        write(outputStream?.let { OutputStreamWriter(it) }, doc, pos, len)
    }

    override fun write(writer: Writer?, doc: Document?, pos: Int, len: Int) {
        TODO("Not yet implemented")
    }

    fun insertANSI(doc: StyledDocument, offset: Int, ansiText: String) {
        require(offset >= 0) { "Offset cannot be negative. Was: $offset" }

        var attributes: MutableAttributeSet = SimpleAttributeSet(doc.getCharacterElement(offset).attributes)

        val match = escCodeRegex.find(ansiText, 0)
        if (match == null) {
            doc.insertString(offset, ansiText, null); // no ansi codes found
            return
        }

        var codeStart = match.range.first

        var text = ansiText.substring(0, codeStart)
        if (text.isNotEmpty()) {
            doc.insertString(0, text, null); // no ansi codes found
        }

        escCodeRegex.findAll(ansiText, codeStart).forEach { m ->
            val ansiCode = m.value
            codeStart = m.range.first
            val codeEnd = m.range.last + 1

            attributes = AnsiAttributesUtil.updateAttributeSet(ansiCode, attributes)

            val endMatch = escCodeRegex.find(ansiText, codeEnd)

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