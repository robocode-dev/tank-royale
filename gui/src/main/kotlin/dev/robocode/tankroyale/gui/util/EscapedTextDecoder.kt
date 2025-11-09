package dev.robocode.tankroyale.gui.util

/**
 * Represents the result of parsing an escape sequence.
 * This sealed class ensures type-safe handling of all possible parse outcomes.
 */
sealed class UnescapeResult {
    /**
     * Represents a successfully parsed escape sequence.
     * @property char The unescaped character
     * @property charsConsumed Number of characters used in the original string
     */
    data class Success(val char: Char, val charsConsumed: Int) : UnescapeResult()

    /**
     * Represents a character that should be treated literally (no escaping).
     * @property char The character to be used as-is
     */
    data class Literal(val char: Char) : UnescapeResult()
}

/**
 * Handles the parsing of escape sequences in strings.
 * Supports both Unicode escapes (\uXXXX) and common escapes (\n, \t, etc.).
 */
class EscapeSequenceParser {
    private companion object {
        /**
         * Maps escape characters to their actual character values.
         * For example: 'n' -> '\n' for newline
         */
        private val ESCAPE_MAPPINGS = mapOf(
            'n' to '\n',    // Newline
            'r' to '\r',    // Carriage return
            't' to '\t',    // Tab
            'b' to '\b',    // Backspace
            'f' to '\u000c',// Form feed
            '"' to '\"',    // Double quote
            '\'' to '\'',   // Single quote
            '\\' to '\\'    // Backslash
        )
    }

    /**
     * Parses a potential escape sequence starting at the given index.
     *
     * @param input The input string containing the escape sequence
     * @param startIndex The index where the potential escape sequence begins
     * @return [UnescapeResult] indicating how the sequence should be handled
     */
    fun parseEscapeSequence(input: String, startIndex: Int): UnescapeResult {
        if (!isValidEscapeSequence(input, startIndex)) {
            return UnescapeResult.Literal(input[startIndex])
        }

        val nextChar = input[startIndex + 1]
        return when {
            isUnicodeEscape(nextChar) -> parseUnicodeEscape(input, startIndex)
            isCommonEscape(nextChar) -> parseCommonEscape(nextChar)
            else -> parseUnknownEscape(input, startIndex)
        }
    }

    /**
     * Checks if there's a valid escape sequence at the given index.
     * A valid sequence must start with \ and have at least one character after it.
     */
    private fun isValidEscapeSequence(input: String, index: Int): Boolean =
        input[index] == '\\' && index + 1 < input.length

    /**
     * Checks if the character indicates a Unicode escape sequence (\u).
     */
    private fun isUnicodeEscape(char: Char): Boolean = char == 'u'

    /**
     * Checks if the character is one of the common escape characters.
     */
    private fun isCommonEscape(char: Char): Boolean = char in ESCAPE_MAPPINGS

    /**
     * Attempts to parse a Unicode escape sequence.
     * A valid Unicode sequence is in the format \uXXXX where X is a hex digit.
     */
    private fun parseUnicodeEscape(input: String, startIndex: Int): UnescapeResult {
        if (!hasCompleteUnicodeSequence(input, startIndex)) {
            return parseUnknownEscape(input, startIndex)
        }

        return try {
            val unicode = extractUnicodeValue(input, startIndex)
            UnescapeResult.Success(unicode, 6) // \uXXXX = 6 characters
        } catch (e: NumberFormatException) {
            parseUnknownEscape(input, startIndex)
        }
    }

    /**
     * Checks if there are enough characters remaining for a complete Unicode sequence.
     */
    private fun hasCompleteUnicodeSequence(input: String, startIndex: Int): Boolean =
        startIndex + 5 < input.length

    /**
     * Extracts and converts the Unicode hex value to its corresponding character.
     * @throws NumberFormatException if the hex value is invalid
     */
    private fun extractUnicodeValue(input: String, startIndex: Int): Char {
        val hexString = input.substring(startIndex + 2, startIndex + 6)
        return hexString.toInt(16).toChar()
    }

    /**
     * Parses a common escape sequence (like \n, \t, etc.).
     */
    private fun parseCommonEscape(escapeChar: Char): UnescapeResult =
        UnescapeResult.Success(
            ESCAPE_MAPPINGS.getValue(escapeChar),
            2 // \n = 2 characters
        )

    /**
     * Handles unknown escape sequences by treating them literally.
     */
    private fun parseUnknownEscape(input: String, startIndex: Int): UnescapeResult =
        UnescapeResult.Literal(input[startIndex])
}

/**
 * Main class for unescaping strings containing escape sequences.
 * Handles both Unicode escapes (\uXXXX) and common escapes (\n, \t, etc.).
 *
 * @property parser The parser used to handle escape sequences
 */
class EscapedTextDecoder(private val parser: EscapeSequenceParser = EscapeSequenceParser()) {
    /**
     * Unescapes a string containing escape sequences.
     *
     * @param input The string to unescape
     * @return The unescaped string, or null if input was null
     *
     * Example:
     * ```kotlin
     * val unescaper = StringUnescaper()
     * println(unescaper.unescape("Hello\\nWorld")) // Prints "Hello" followed by newline and "World"
     * println(unescaper.unescape("\\u0041BC")) // Prints "ABC"
     * ```
     */
    fun unescape(input: String?): String? {
        if (input.isNullOrEmpty() || !input.contains('\\')) {
            return input
        }

        return buildUnescapedString(input)
    }

    /**
     * Builds the unescaped string by processing each character.
     */
    private fun buildUnescapedString(input: String): String {
        val result = StringBuilder(input.length)
        var index = 0

        while (index < input.length) {
            val parseResult = when {
                isEscapeCharacter(input[index]) -> parser.parseEscapeSequence(input, index)
                else -> UnescapeResult.Literal(input[index])
            }

            when (parseResult) {
                is UnescapeResult.Success -> {
                    result.append(parseResult.char)
                    index += parseResult.charsConsumed
                }
                is UnescapeResult.Literal -> {
                    result.append(parseResult.char)
                    index++
                }
            }
        }

        return result.toString()
    }

    /**
     * Checks if the given character is the escape character (\).
     */
    private fun isEscapeCharacter(char: Char): Boolean = char == '\\'

    companion object {
        /**
         * Convenience method for static usage.
         * Creates a new StringUnescaper instance for each call.
         *
         * @param input The string to unescape
         * @return The unescaped string, or null if input was null
         */
        fun unescape(input: String?): String? = EscapedTextDecoder().unescape(input)
    }
}