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
        void constructor_ShouldWork(int red, int green, int blue) {
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
        void constructor_ShouldThrowException(int red, int green, int blue) {
            assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(
                    () -> new Color(red, green, blue)
            );
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
        void fromString_ShouldWork(String str, int expectedRed, int expectedGreen, int expectedBlue) {
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
        void fromString_ShouldThrowException(String str) {
            assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(
                    () -> Color.fromString(str)
            );
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
        void fromHex_ShouldWork(String hex, int expectedRed, int expectedGreen, int expectedBlue) {
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
        void fromHex_ShouldThrowException(String hex) {
            assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(
                    () -> Color.fromHex(hex)
            );
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
        void toHex_ShouldWork(String hex) {
            var color = Color.fromHex(hex);
            assertThat(color.toHex()).isEqualToIgnoringCase(hex);
        }
    }

    @Nested
    class EqualsTests {
        @Test
        void equals_ShouldBeEqual() {
            assertThat(new Color(10, 20, 30)).isEqualTo(new Color(10, 20, 30));
            assertThat(new Color(11, 22, 33)).isEqualTo(new Color(11, 22, 33));
        }

        @Test
        void equals_ShouldNotBeEqual() {
            assertThat(new Color(10, 20, 30)).isNotEqualTo(new Color(11, 20, 30));
            assertThat(new Color(10, 20, 30)).isNotEqualTo(new Color(10, 22, 30));
            assertThat(new Color(10, 20, 30)).isNotEqualTo(new Color(10, 20, 33));
        }
    }

    @Nested
    class HashTests {
        @Test
        void hashCode_ShouldBeEqual() {
            assertThat(Color.fromHex("102030").hashCode()).isEqualTo(new Color(0x10, 0x20, 0x30).hashCode());
            assertThat(Color.fromHex("112233").hashCode()).isEqualTo(new Color(0x11, 0x22, 0x33).hashCode());
        }

        @Test
        void hashCode_ShouldNotBeEqual() {
            assertThat(new Color(10, 20, 30).hashCode()).isNotEqualTo(Color.fromHex("123456").hashCode());
        }
    }

    @Nested
    class ToStringTests {
        @Test
        void toString_ShouldBeEqual() {
            assertThat(Color.fromHex("FDB975").toString()).isEqualToIgnoringCase("FDB975");
        }
    }

    @Nested
    class ColorConstantsTests {
        @Test
        void white() {
            assertThat(Color.WHITE.toString()).isEqualToIgnoringCase("FFFFFF");
        }

        @Test
        void silver() {
            assertThat(Color.SILVER.toString()).isEqualToIgnoringCase("C0C0C0");
        }

        @Test
        void gray() {
            assertThat(Color.GRAY.toString()).isEqualToIgnoringCase("808080");
        }

        @Test
        void black() {
            assertThat(Color.BLACK.toString()).isEqualToIgnoringCase("000000");
        }

        @Test
        void red() {
            assertThat(Color.RED.toString()).isEqualToIgnoringCase("FF0000");
        }

        @Test
        void maroon() {
            assertThat(Color.MAROON.toString()).isEqualToIgnoringCase("800000");
        }

        @Test
        void yellow() {
            assertThat(Color.YELLOW.toString()).isEqualToIgnoringCase("FFFF00");
        }

        @Test
        void olive() {
            assertThat(Color.OLIVE.toString()).isEqualToIgnoringCase("808000");
        }

        @Test
        void lime() {
            assertThat(Color.LIME.toString()).isEqualToIgnoringCase("00FF00");
        }

        @Test
        void green() {
            assertThat(Color.GREEN.toString()).isEqualToIgnoringCase("008000");
        }

        @Test
        void cyan() {
            assertThat(Color.CYAN.toString()).isEqualToIgnoringCase("00FFFF");
        }

        @Test
        void teal() {
            assertThat(Color.TEAL.toString()).isEqualToIgnoringCase("008080");
        }

        @Test
        void blue() {
            assertThat(Color.BLUE.toString()).isEqualToIgnoringCase("0000FF");
        }

        @Test
        void navy() {
            assertThat(Color.NAVY.toString()).isEqualToIgnoringCase("000080");
        }

        @Test
        void fuchsia() {
            assertThat(Color.FUCHSIA.toString()).isEqualToIgnoringCase("FF00FF");
        }

        @Test
        void purple() {
            assertThat(Color.PURPLE.toString()).isEqualToIgnoringCase("800080");
        }

        @Test
        void orange() {
            assertThat(Color.ORANGE.toString()).isEqualToIgnoringCase("FF8000");
        }
    }
}
