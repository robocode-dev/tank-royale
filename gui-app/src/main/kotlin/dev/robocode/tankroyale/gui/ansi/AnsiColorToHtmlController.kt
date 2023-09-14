package dev.robocode.tankroyale.gui.ansi

class AnsiColorToHtmlController {

    private val escapeSequenceRegex = Regex("\u001b\\[(\\d+;?)+m")

    private val state = State()

    fun process(text: String): String {
        val stringBuilder = StringBuilder()
        var index = 0

        escapeSequenceRegex.findAll(text, 0).forEach {
            stringBuilder.append(text.substring(index, it.range.first))
            stringBuilder.append(processEscapeSequence(it.value))
            index = it.range.last + 1
        }
        stringBuilder.append(text.substring(index))

        // Replace new-line and carriage-return characters
        return stringBuilder.toString()
    }

    private fun processEscapeSequence(escSeq: String): String {
        val codes = escSeq.subSequence(2, escSeq.length - 1).split(';').map { it.toInt() }
        return processCodes(codes)
    }

    private fun processCodes(codes: List<Int>): String {
        val stringBuilder = StringBuilder()

        if (!state.isNormal()) {
            // terminate earlier span
            stringBuilder.append("</span>")
        }

        val iterator = codes.iterator()
        do {
            when (val firstCode = iterator.next()) {
                0 -> state.reset() // all attributes off
                1 -> state.weight = 700 // bold
                2 -> state.weight = 300 // fault
                3 -> state.italic = true
                4 -> state.underline = 1 // singly underline
                21 -> state.underline = 2 // doubly underline
                22 -> state.weight = null // neither bold nor faint
                23 -> state.italic = false
                24 -> state.underline = null // neither singly nor doubly underlined
                in 30..39 -> state.fgColor = AnsiColor.webColorFrom(firstCode - 30)
                in 40..49 -> state.bgColor = AnsiColor.webColorFrom(firstCode - 40)
                in 90..97 -> state.fgColor = BrightAnsiColor.webColorFrom(firstCode - 90)
                in 100..107 -> state.bgColor = BrightAnsiColor.webColorFrom(firstCode - 100)
            }
        } while (iterator.hasNext())

        if (!state.isNormal()) {
            stringBuilder.append("<span style=\"")
            if (state.fgColor != null) {
                stringBuilder.append("color:${state.fgColor};")
            }
            if (state.bgColor != null) {
                stringBuilder.append("background-color:${state.bgColor};")
            }
            if (state.weight != null) {
                stringBuilder.append("font-weight:${state.weight};")
            }
            if (state.italic) {
                stringBuilder.append("font-style:italic;")
            }
            if (state.underline != null) {
                stringBuilder.append("text-decoration:underline ")
                if (state.underline == 1) {
                    stringBuilder.append("solid;")
                } else {
                    stringBuilder.append("double;")
                }
            }
            stringBuilder.append("\">")
        }
        return stringBuilder.toString()
    }

    private class State {
        var fgColor: String? = null
        var bgColor: String? = null
        var weight: Int? = null
        var italic: Boolean = false
        var underline: Int? = null

        fun reset() {
            fgColor = null
            bgColor = null
            weight = null
            italic = false
            underline = null
        }

        fun isNormal() = fgColor == null && bgColor == null && weight == null && !italic && underline == null
    }
}
/*
fun main() {
    val ansiToHtml = AnsiColorToHtmlController()

    var text = "\u001b[30m'Black'\u001B[39m 'Default color'\u001B[0m"
    println(text)
    println(ansiToHtml.process(text))

    text = "\u001b[31m'Red'\u001B[39m 'Default color'\u001B[0m"
    println(text)
    println(ansiToHtml.process(text))

    text = "\u001b[32m'Green'\u001B[39m 'Default color'\u001B[0m"
    println(text)
    println(ansiToHtml.process(text))

    text = "\u001b[33m'Yellow'\u001B[39m 'Default color'\u001B[0m"
    println(text)
    println(ansiToHtml.process(text))

    text = "\u001b[34m'Blue'\u001B[39m 'Default color'\u001B[0m"
    println(text)
    println(ansiToHtml.process(text))

    text = "\u001b[35m'Magenta'\u001B[39m 'Default color'\u001B[0m"
    println(text)
    println(ansiToHtml.process(text))

    text = "\u001b[36m'Cyan'\u001B[39m 'Default color'\u001B[0m"
    println(text)
    println(ansiToHtml.process(text))

    text = "\u001b[37m'White'\u001B[39m 'Default color'\u001B[0m"
    println(text)
    println(ansiToHtml.process(text))

    text = "\u001b[90m'Bright black'\u001B[39m 'Default color'\u001B[0m"
    println(text)
    println(ansiToHtml.process(text))

    text = "\u001b[91m'Bright red'\u001B[39m 'Default color'\u001B[0m"
    println(text)
    println(ansiToHtml.process(text))

    text = "\u001b[92m'Bright green'\u001B[39m 'Default color'\u001B[0m"
    println(text)
    println(ansiToHtml.process(text))

    text = "\u001b[93m'Bright yellow'\u001B[39m 'Default color'\u001B[0m"
    println(text)
    println(ansiToHtml.process(text))

    text = "\u001b[94m'Bright blue'\u001B[39m 'Default color'\u001B[0m"
    println(text)
    println(ansiToHtml.process(text))

    text = "\u001b[95m'Bright magenta'\u001B[39m 'Default color'\u001B[0m"
    println(text)
    println(ansiToHtml.process(text))

    text = "\u001b[96m'Bright cyan'\u001B[39m 'Default color'\u001B[0m"
    println(text)
    println(ansiToHtml.process(text))

    text = "\u001b[97m'Bright white'\u001B[39m 'Default color'\u001B[0m"
    println(text)
    println(ansiToHtml.process(text))

    text = "\u001b[40m'Black'\u001B[49m 'Default color'\u001B[0m"
    println(text)
    println(ansiToHtml.process(text))

    text = "\u001b[41m'Red'\u001B[49m 'Default color'\u001B[0m"
    println(text)
    println(ansiToHtml.process(text))

    text = "\u001b[42m'Green'\u001B[49m 'Default color'\u001B[0m"
    println(text)
    println(ansiToHtml.process(text))

    text = "\u001b[43m'Yellow'\u001B[49m 'Default color'\u001B[0m"
    println(text)
    println(ansiToHtml.process(text))

    text = "\u001b[44m'Blue'\u001B[49m 'Default color'\u001B[0m"
    println(text)
    println(ansiToHtml.process(text))

    text = "\u001b[45m'Magenta'\u001B[49m 'Default color'\u001B[0m"
    println(text)
    println(ansiToHtml.process(text))

    text = "\u001b[46m'Cyan'\u001B[49m 'Default color'\u001B[0m"
    println(text)
    println(ansiToHtml.process(text))

    text = "\u001b[47m'White'\u001B[49m 'Default color'\u001B[0m"
    println(text)
    println(ansiToHtml.process(text))

    text = "\u001b[100m'Bright black'\u001B[49m 'Default color'\u001B[0m"
    println(text)
    println(ansiToHtml.process(text))

    text = "\u001b[101m'Bright red'\u001B[49m 'Default color'\u001B[0m"
    println(text)
    println(ansiToHtml.process(text))

    text = "\u001b[102m'Bright green'\u001B[49m 'Default color'\u001B[0m"
    println(text)
    println(ansiToHtml.process(text))

    text = "\u001b[103m'Bright yellow'\u001B[49m 'Default color'\u001B[0m"
    println(text)
    println(ansiToHtml.process(text))

    text = "\u001b[104m'Bright blue'\u001B[49m 'Default color'\u001B[0m"
    println(text)
    println(ansiToHtml.process(text))

    text = "\u001b[105m'Bright magenta'\u001B[49m 'Default color'\u001B[0m"
    println(text)
    println(ansiToHtml.process(text))

    text = "\u001b[106m'Bright cyan'\u001B[49m 'Default color'\u001B[0m"
    println(text)
    println(ansiToHtml.process(text))

    text = "\u001b[107m'Bright white'\u001B[49m 'Default color'\u001B[0m"
    println(text)
    println(ansiToHtml.process(text))

    text = "\u001b[1m'Bold'\u001B[22m 'Not bold'\u001B[0m"
    println(text)
    println(ansiToHtml.process(text))

    text = "\u001b[2m'Faint'\u001B[22m 'Not faint'\u001B[0m"
    println(text)
    println(ansiToHtml.process(text))

    text = "\u001b[3m'Italic'\u001B[23m 'Not italic'\u001B[0m"
    println(text)
    println(ansiToHtml.process(text))

    text = "\u001b[4m'Singly underlined'\u001B[24m 'Not underlined'\u001B[0m"
    println(text)
    println(ansiToHtml.process(text))

    text = "\u001b[21m'Doubly underlined'\u001B[24m 'Not underlined'\u001B[0m"
    println(text)
    println(ansiToHtml.process(text))
}
*/