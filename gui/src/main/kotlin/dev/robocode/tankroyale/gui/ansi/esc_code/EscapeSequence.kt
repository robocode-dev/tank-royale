package dev.robocode.tankroyale.gui.ansi.esc_code

/**
 * Encapsulates an escape sequence by a list of parameters, where the first parameter is the escape code command.
 * @param sequence is the escape sequence containing integer parameters.
 */
class EscapeSequence(private vararg val sequence: Int) {

    /**
     * Encapsulates an escape sequence with a command code and its parameters.
     * @param commandCode is a [CommandCode]
     * @param parameters is the list of parameters for the command.
     */
    constructor(commandCode: CommandCode, vararg parameters: Int) :
            this(*intArrayOf(commandCode.commandCode, *parameters))

    /**
     * Returns the command code of the escape sequence.
     * @return a [CommandCode].
     */
    fun commandCode(): CommandCode = if (sequence.isEmpty()) CommandCode.RESET else CommandCode.fromCode(sequence[0])

    /**
     * Returns the parameters of the escape sequence.
     * @return a list of integers containing the parameters for the command code.
     */
    fun parameters(): List<Int> =
        if (sequence.isEmpty())
            listOf()
        else {
            val list = sequence.toList()
            list.subList(1, list.size)
        }

    companion object {
        /**
         * parses an escape sequence from a string.
         * @param escapeSequence is the input string containing the escape sequence to parse.
         * @return an [EscapeSequence] representing an escape sequence.
         */
        fun parse(escapeSequence: String): EscapeSequence {
            with(escapeSequence) {
                require(startsWith("\u001b[") && endsWith("m")) { "Invalid escape sequence: $this" }
                val escSeq = removePrefix("\u001b[").removeSuffix("m")
                val parameters = escSeq.split(";").map {
                    require(it.matches(Regex("\\d*"))) { "Escape sequence parameter is not a number: $it" }
                    it.toIntOrNull() ?: 0
                }
                return EscapeSequence(*parameters.toIntArray())
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    override fun toString() = "\u001b[${sequence.joinToString(";")}m"
}