package dev.robocode.tankroyale.botapi.util;

/**
 * Utility class for JSON.
 */
public final class JsonUtil {

    // Hide constructor to prevent instantiation
    private JsonUtil() {
    }

    public static String escaped(String s) {

        return s.replaceAll("\b", "\\\\b") // backspace -> \b
                .replaceAll("\f", "\\\\f") // form feed -> \f
                .replaceAll("\n", "\\\\n") // newline -> \n
                .replaceAll("\r", "") // carriage return -> remove character
                .replaceAll("\t", "\\\\t") // tab -> \t
                .replaceAll("\"", "\\\\\"") // double-quotes -> \"
                .replaceAll("\\\\", "\\\\") // single backslash (\) -> double backslash (\\)
                ;
    }
}
