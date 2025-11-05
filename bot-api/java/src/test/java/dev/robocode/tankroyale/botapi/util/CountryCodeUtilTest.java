package dev.robocode.tankroyale.botapi.util;

import com.neovisionaries.i18n.CountryCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static dev.robocode.tankroyale.botapi.util.CountryCodeUtil.getLocalCountryCode;
import static dev.robocode.tankroyale.botapi.util.CountryCodeUtil.toCountryCode;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TR-API-UTL-003 CountryCode utility")
class CountryCodeUtilTest {

    @Test
    @Tag("UTL")
    @Tag("TR-API-UTL-003")
    void test_TR_API_UTL_003_country_code_valid_examples() {
        String[] examples = new String[]{"GB", "gb", "dk", "us", "no", "SE", "FI"};
        for (String code : examples) {
            CountryCode cc = toCountryCode(code);
            assertThat(cc).as("Expected valid country for code=" + code).isNotNull();
            assertThat(cc.getAlpha2()).isEqualTo(code.trim().toUpperCase());
        }
    }

    @Test
    @Tag("UTL")
    @Tag("TR-API-UTL-003")
    void test_TR_API_UTL_003_country_code_local_detection() {
        String local = getLocalCountryCode();
        if (local != null) {
            assertThat(local).hasSize(2);
            assertThat(toCountryCode(local)).isNotNull();
        } else {
            // If no local code resolvable, ensure API returns null (acceptable per semantics)
            assertThat(local).isNull();
        }
    }
}
