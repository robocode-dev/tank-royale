package dev.robocode.tankroyale.botapi;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;

class ColorTest {

    @Nested
    class ConstructorTests {

        @ParameterizedTest
        @CsvSource({
                "0x00, 0x00, 0x00",
                "0xFF, 0xFF, 0xFF",
                "0x13, 0x9A, 0xF7"
        })
        void givenValidRedGreenBlue_whenCreatingColor_thenCreatedColorMustContainTheSameRedGreenBlue(int red, int green, int blue) {
            var color = new Color(red, green, blue);

            assertThat(color.getRed()).isEqualTo(red);
            assertThat(color.getGreen()).isEqualTo(green);
            assertThat(color.getBlue()).isEqualTo(blue);
        }

        @ParameterizedTest
        @CsvSource({
                "-1, 70, 100",    // negative number (1st param)
                "50, -100, 100",  // negative number (2nd param)
                "50, 70, -1000",  // negative number (3rd param)
                "256, 255, 255",  // number too big  (1st param)
                "255, 1000, 0",   // number too big  (2nd param)
                "50, 100, 300",   // number too big  (3rd param)
        })
        void givenInvalidRedGreenBlue_whenCreatingColor_thenThrowIllegalArgumentException(int red, int green, int blue) {
            assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> new Color(red, green, blue));
        }
    }

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

            var color = Color.fromString(str);

            assertThat(color.getRed()).isEqualTo(expectedRed);
            assertThat(color.getGreen()).isEqualTo(expectedGreen);
            assertThat(color.getBlue()).isEqualTo(expectedBlue);
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
            assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> Color.fromString(str));
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

            var color = Color.fromHex(hex);

            assertThat(color.getRed()).isEqualTo(expectedRed);
            assertThat(color.getGreen()).isEqualTo(expectedGreen);
            assertThat(color.getBlue()).isEqualTo(expectedBlue);
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
            assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> Color.fromHex(hex));
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
            var color = Color.fromHex(hex);
            assertThat(color.toHex()).isEqualToIgnoringCase(hex);
        }
    }

    @Nested
    class EqualsTests {
        @Test
        void givenTwoCreatedColorsWithSameRgbValues_whenCallingIsEqualTo_thenTheTwoColorsMustBeEqual() {
            assertThat(new Color(10, 20, 30)).isEqualTo(new Color(10, 20, 30));
            assertThat(new Color(11, 22, 33)).isEqualTo(new Color(11, 22, 33));
        }

        @Test
        void givenTwoCreatedColorsWithDifferentRgbValues_whenCallingIsEqualTo_thenTheTwoColorsMustNotBeEqual() {
            assertThat(new Color(10, 20, 30)).isNotEqualTo(new Color(11, 20, 30));
            assertThat(new Color(10, 20, 30)).isNotEqualTo(new Color(10, 22, 30));
            assertThat(new Color(10, 20, 30)).isNotEqualTo(new Color(10, 20, 33));
        }
    }

    @Nested
    class HashTests {
        @Test
        void givenTwoEqualColorsCreatedDifferently_whenCallingHashCodeOnEachColor_thenTheHashCodesMustBeEqual() {
            assertThat(Color.fromHex("102030").hashCode()).isEqualTo(new Color(0x10, 0x20, 0x30).hashCode());
            assertThat(Color.fromHex("112233").hashCode()).isEqualTo(new Color(0x11, 0x22, 0x33).hashCode());
        }

        @Test
        void givenTwoDifferentColors_whenCallingHashCodeOnEachColor_thenTheHashCodesMustNotBeEqual() {
            assertThat(new Color(10, 20, 30).hashCode()).isNotEqualTo(Color.fromHex("123456").hashCode());
        }
    }

    @Nested
    class ToStringTests {
        @Test
        void givenColorWithSpeficHexValue_whenCallingToString_thenReturnedStringMustBeSameHexValue() {
            assertThat(Color.fromHex("FDB975").toString()).isEqualToIgnoringCase("FDB975");
        }
    }

    @Nested
    class ColorConstantsTests {
        @Test
        void givenWhiteColorConstant_whenGettingTheColorValue_thenTheColorValueIsCorrect() {
            assertThat(Color.WHITE.toString()).isEqualToIgnoringCase("FFFFFF");
        }

        @Test
        void givenSilverColorConstant_whenGettingTheColorValue_thenTheColorValueIsCorrect() {
            assertThat(Color.SILVER.toString()).isEqualToIgnoringCase("C0C0C0");
        }

        @Test
        void givenGrayColorConstant_whenGettingTheColorValue_thenTheColorValueIsCorrect() {
            assertThat(Color.GRAY.toString()).isEqualToIgnoringCase("808080");
        }

        @Test
        void givenBlackColorConstant_whenGettingTheColorValue_thenTheColorValueIsCorrect() {
            assertThat(Color.BLACK.toString()).isEqualToIgnoringCase("000000");
        }

        @Test
        void givenRedColorConstant_whenGettingTheColorValue_thenTheColorValueIsCorrect() {
            assertThat(Color.RED.toString()).isEqualToIgnoringCase("FF0000");
        }

        @Test
        void givenMaroonColorConstant_whenGettingTheColorValue_thenTheColorValueIsCorrect() {
            assertThat(Color.MAROON.toString()).isEqualToIgnoringCase("800000");
        }

        @Test
        void givenYellowColorConstant_whenGettingTheColorValue_thenTheColorValueIsCorrect() {
            assertThat(Color.YELLOW.toString()).isEqualToIgnoringCase("FFFF00");
        }

        @Test
        void givenOliveColorConstant_whenGettingTheColorValue_thenTheColorValueIsCorrect() {
            assertThat(Color.OLIVE.toString()).isEqualToIgnoringCase("808000");
        }

        @Test
        void givenLimeColorConstant_whenGettingTheColorValue_thenTheColorValueIsCorrect() {
            assertThat(Color.LIME.toString()).isEqualToIgnoringCase("00FF00");
        }

        @Test
        void givenGreenColorConstant_whenGettingTheColorValue_thenTheColorValueIsCorrect() {
            assertThat(Color.GREEN.toString()).isEqualToIgnoringCase("008000");
        }

        @Test
        void givenCyanColorConstant_whenGettingTheColorValue_thenTheColorValueIsCorrect() {
            assertThat(Color.CYAN.toString()).isEqualToIgnoringCase("00FFFF");
        }

        @Test
        void givenTealColorConstant_whenGettingTheColorValue_thenTheColorValueIsCorrect() {
            assertThat(Color.TEAL.toString()).isEqualToIgnoringCase("008080");
        }

        @Test
        void givenBlueColorConstant_whenGettingTheColorValue_thenTheColorValueIsCorrect() {
            assertThat(Color.BLUE.toString()).isEqualToIgnoringCase("0000FF");
        }

        @Test
        void givenNavyColorConstant_whenGettingTheColorValue_thenTheColorValueIsCorrect() {
            assertThat(Color.NAVY.toString()).isEqualToIgnoringCase("000080");
        }

        @Test
        void givenFuchsiaColorConstant_whenGettingTheColorValue_thenTheColorValueIsCorrect() {
            assertThat(Color.FUCHSIA.toString()).isEqualToIgnoringCase("FF00FF");
        }

        @Test
        void givenPurpleColorConstant_whenGettingTheColorValue_thenTheColorValueIsCorrect() {
            assertThat(Color.PURPLE.toString()).isEqualToIgnoringCase("800080");
        }

        @Test
        void givenOrangeColorConstant_whenGettingTheColorValue_thenTheColorValueIsCorrect() {
            assertThat(Color.ORANGE.toString()).isEqualToIgnoringCase("FF8000");
        }
    }
}
