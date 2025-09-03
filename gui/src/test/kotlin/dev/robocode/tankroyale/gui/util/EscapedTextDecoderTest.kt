package dev.robocode.tankroyale.gui.util

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class EscapedTextDecoderTest : StringSpec({
    "should handle null input" {
        EscapedTextDecoder.unescape(null) shouldBe null
    }

    "should handle empty string" {
        EscapedTextDecoder.unescape("") shouldBe ""
    }

    "should handle string without escape sequences" {
        EscapedTextDecoder.unescape("Hello World") shouldBe "Hello World"
    }

    "should handle common escape sequences" {
        val testCases = mapOf(
            "new\\nline" to "new\nline",
            "carriage\\rreturn" to "carriage\rreturn",
            "\\ttab" to "\ttab",
            "back\\bspace" to "back\bspace",
            "form\\ffeed" to "form\u000cfeed",
            "\"quoted\"" to "\"quoted\"",
            "single\\\'quote" to "single'quote",
            "double\\\\backslash" to "double\\backslash"
        )

        testCases.forEach { (input, expected) ->
            EscapedTextDecoder.unescape(input) shouldBe expected
        }
    }

    "should handle multiple escape sequences in one string" {
        EscapedTextDecoder.unescape("line1\\nline2\\tindented\\nline3") shouldBe "line1\nline2\tindented\nline3"
    }

    "should handle Unicode escape sequences" {
        val testCases = mapOf(
            "\\u0041" to "A",                    // Latin Capital A
            "\\u00A9" to "©",                    // Copyright symbol
            "\\u2665" to "♥",                    // Heart symbol
            "\\u0048\\u0069" to "Hi",           // Multiple Unicode escapes
            "Hello\\u0020World" to "Hello World" // Space character
        )

        testCases.forEach { (input, expected) ->
            EscapedTextDecoder.unescape(input) shouldBe expected
        }
    }

    "should handle invalid Unicode escape sequences" {
        val testCases = mapOf(
            "\\uXXXX" to "\\uXXXX",         // Invalid hex digits
            "\\u123" to "\\u123",           // Incomplete sequence
            "\\u" to "\\u",                 // Just the prefix
            "\\u12GH" to "\\u12GH"          // Invalid hex digits
        )

        testCases.forEach { (input, expected) ->
            EscapedTextDecoder.unescape(input) shouldBe expected
        }
    }

    "should handle unknown escape sequences" {
        val testCases = mapOf(
            "\\x" to "\\x",     // Unknown escape sequence
            "\\1" to "\\1",     // Numeric escape (not supported)
            "\\?" to "\\?"      // Question mark escape (not supported)
        )

        testCases.forEach { (input, expected) ->
            EscapedTextDecoder.unescape(input) shouldBe expected
        }
    }

    "should handle escape sequences at string boundaries" {
        val testCases = mapOf(
            "\\n" to "\n",          // Start with escape
            "text\\n" to "text\n",  // End with escape
            "\\n\\n" to "\n\n"      // Multiple escapes only
        )

        testCases.forEach { (input, expected) ->
            EscapedTextDecoder.unescape(input) shouldBe expected
        }
    }
})