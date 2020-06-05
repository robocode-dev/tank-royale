package dev.robocode.tankroyale.server.util;

import java.util.regex.Pattern;

/** Utility class holding regular expressions. */
public final class RegExp {

  public static Pattern HEX_DEC_COLOR_CODE =
      Pattern.compile("^#[0-9A-F]{3,6}$", Pattern.CASE_INSENSITIVE);
}
