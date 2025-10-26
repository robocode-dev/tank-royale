package dev.robocode.tankroyale.botapi.util;

import dev.robocode.tankroyale.botapi.graphics.Color;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;

@DisplayName("TR-API-UTL-001 ColorUtil conversions")
class ColorUtilTest {

    @Nested
    class FromStringTests {
        @ParameterizedTest
        @CsvSource(ignoreLeadingAndTrailingWhitespace = false, value = {
                "#000000,0x00,0x00,0x00",
                "#000,0x00,0x00,0x00",
                "#FfFfFf,0xFF,0xFF,0xFF",
                "#fFF,0xFF,0xFF,0xFF",
                "#1199cC,0x11,0x99,0xCC",
                "#19C,0x11,0x99,0xCC",
                "  #123456,0x12,0x34,0x56", // White spaces
                "#789aBc\t,0x78,0x9A,0xBC", // White space
                "  #123,0x11,0x22,0x33",    // White spaces
                "#AbC\t,0xAA,0xBB,0xCC"     // White space
        })
        void givenValidRgbString_whenCallingFromString_thenCreatedColorMustContainTheSameRedGreenBlue(
                String str, int expectedRed, int expectedGreen, int expectedBlue) {

            var color = ColorUtil.fromHexColor(str);

            assertThat(color.getR()).isEqualTo(expectedRed);
            assertThat(color.getG()).isEqualTo(expectedGreen);
            assertThat(color.getB()).isEqualTo(expectedBlue);
        }

        @ParameterizedTest
        @CsvSource({
                "#00000",    // Too short
                "#0000000",  // Too long
                "#0000 00",  // White space
                "#xxxxxx",   // Wrong letters
                "#abcdeG",   // Wrong letter
                "000000",    // Missing hash (#)
        })
        void givenInvalidRgbString_whenCallingFromString_thenThrowIllegalArgumentException(String str) {
            assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> ColorUtil.fromHexColor(str));
        }
    }

    @Nested
    class FromHexTests {
        @ParameterizedTest
        @CsvSource(ignoreLeadingAndTrailingWhitespace = false, value = {
                "000000,0x00,0x00,0x00",
                "000,0x00,0x00,0x00",
                "FfFfFf,0xFF,0xFF,0xFF",
                "fFF,0xFF,0xFF,0xFF",
                "1199cC,0x11,0x99,0xCC",
                "19C,0x11,0x99,0xCC",
                "  123456,0x12,0x34,0x56", // White spaces
                "789aBc\t,0x78,0x9A,0xBC", // White space
                "  123,0x11,0x22,0x33",    // White spaces
                "AbC\t,0xAA,0xBB,0xCC"     // White space
        })
        void givenValidRgbHexString_whenCallingFromHex_thenCreatedColorMustContainTheSameRedGreenBlue(
                String hex, int expectedRed, int expectedGreen, int expectedBlue) {

            var color = ColorUtil.fromHex(hex);

            assertThat(color.getR()).isEqualTo(expectedRed);
            assertThat(color.getG()).isEqualTo(expectedGreen);
            assertThat(color.getB()).isEqualTo(expectedBlue);
        }

        @ParameterizedTest
        @CsvSource({
                "00000",    // Too short
                "0000000",  // Too long
                "0000 00",  // White space
                "xxxxxx",   // Wrong letters
                "abcdeG",   // Wrong letter
        })
        void givenInvalidRgbHexString_whenCallingFromHex_thenThrowIllegalArgumentException(String hex) {
            assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> ColorUtil.fromHex(hex));
        }
    }

    @Nested
    class ToHexTests {
        @ParameterizedTest
        @CsvSource({
                "000000",
                "FEDCBA",
                "123456"
        })
        void givenValidRgbHexString_whenCallingToHex_thenReturnedHexStringMustBeTheSame(String hex) {
            var color = ColorUtil.fromHex(hex);
            assertThat(ColorUtil.toHex(color)).isEqualToIgnoringCase(hex);
        }
    }

    @Nested
    class EqualsTests {
        @Test
        void givenTwoCreatedColorsWithSameRgbValues_whenCallingIsEqualTo_thenTheTwoColorsMustBeEqual() {
            assertThat(Color.fromRgb(10, 20, 30)).isEqualTo(Color.fromRgb(10, 20, 30));
            assertThat(Color.fromRgb(11, 22, 33)).isEqualTo(Color.fromRgb(11, 22, 33));
        }

        @Test
        void givenTwoCreatedColorsWithDifferentRgbValues_whenCallingIsEqualTo_thenTheTwoColorsMustNotBeEqual() {
            assertThat(Color.fromRgb(10, 20, 30)).isNotEqualTo(Color.fromRgb(11, 20, 30));
            assertThat(Color.fromRgb(10, 20, 30)).isNotEqualTo(Color.fromRgb(10, 22, 30));
            assertThat(Color.fromRgb(10, 20, 30)).isNotEqualTo(Color.fromRgb(10, 20, 33));
        }
    }

    @Nested
    class HashTests {
        @Test
        void givenTwoEqualColorsCreatedDifferently_whenCallingHashCodeOnEachColor_thenTheHashCodesMustBeEqual() {
            assertThat(ColorUtil.fromHex("102030")).hasSameHashCodeAs(Color.fromRgb(0x10, 0x20, 0x30).hashCode());
            assertThat(ColorUtil.fromHex("112233")).hasSameHashCodeAs(Color.fromRgb(0x11, 0x22, 0x33).hashCode());
        }

        @Test
        void givenTwoDifferentColors_whenCallingHashCodeOnEachColor_thenTheHashCodesMustNotBeEqual() {
            assertThat(Color.fromRgb(10, 20, 30).hashCode()).isNotEqualTo(ColorUtil.fromHex("123456").hashCode());
        }
    }

    @Nested
    class ToStringTests {
        @Test
        void givenColorWithSpeficHexValue_whenCallingToString_thenReturnedStringMustBeSameHexValue() {
            assertThat(ColorUtil.fromHex("FDB975")).isEqualTo(Color.fromRgb(0xFD, 0xB9, 0x75));
        }
    }
}
