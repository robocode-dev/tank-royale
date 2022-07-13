package dev.robocode.tankroyale.botapi.util;

import com.neovisionaries.i18n.CountryCode;

import java.util.Locale;

/**
 * Country code utility class.
 */
public final class CountryCodeUtil {

    // Hides constructor
    private CountryCodeUtil() {
    }

    public static String getLocalCountryCode() {
        return CountryCode.getByLocale(Locale.getDefault()).getAlpha2();
    }

    public static CountryCode toCountryCode(String code) {
        if (code == null || code.trim().length() != 2) {
            return null;
        }
        return CountryCode.getByCodeIgnoreCase(code.trim());
    }
}
