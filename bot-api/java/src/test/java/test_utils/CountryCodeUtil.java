package test_utils;

import com.neovisionaries.i18n.CountryCode;

import java.util.Locale;

public final class CountryCodeUtil {

    public static String getLocalCountryCode() {
        return CountryCode.getByLocale(Locale.getDefault()).getAlpha2();
    }
}
