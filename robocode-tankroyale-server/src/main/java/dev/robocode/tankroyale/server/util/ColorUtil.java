package dev.robocode.tankroyale.server.util;

public final class ColorUtil {

  public static Integer colorStringToRGB(String colorStr) {
    if (colorStr == null) {
      return null;
    }
    colorStr = colorStr.trim();
    if (!RegExp.HEX_DEC_COLOR_CODE.matcher(colorStr).matches()) {
      return null;
    }
    if (colorStr.length() == 7) {
      return hexToRgb16bit(colorStr);
    }
    if (colorStr.length() == 4) {
      return hexToRgb8bit(colorStr);
    }
    return null;
  }

  public static int hexToRgb16bit(String colorStr) {
    int r = Integer.valueOf(colorStr.substring(1, 3), 16);
    int g = Integer.valueOf(colorStr.substring(3, 5), 16);
    int b = Integer.valueOf(colorStr.substring(5, 7), 16);

    return r << 16 | g << 8 | b;
  }

  public static int hexToRgb8bit(String colorStr) {
    int r = Integer.valueOf(colorStr.substring(1, 2), 16);
    int g = Integer.valueOf(colorStr.substring(2, 3), 16);
    int b = Integer.valueOf(colorStr.substring(3, 4), 16);

    r = r << 4 | r;
    g = g << 4 | g;
    b = b << 4 | b;

    return r << 16 | g << 8 | b;
  }
}
