package dev.robocode.tankroyale.botapi.internal;

/**
 * Utility class for JSON.
 */
public class JsonUtil {

    public static String escaped(String s) {

        return s.replaceAll("\b", "\\b") // backspace -> \b
                .replaceAll("\f", "\\f") // form feed -> \f
                .replaceAll("\n", "\\n") // newline -> \n
                .replaceAll("\r", "\\r") // carriage return -> \r
                .replaceAll("\t", "\\t") // tab -> \t
                .replaceAll("\"", "\\\"") // double-quote -> \"
                .replaceAll("\\\\", "\\\\") // backslash -> \\
                ;
    }
}
