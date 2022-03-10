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
        void constructor_ShouldWork(String red, String green, String blue) {
            var redValue = Integer.decode(red);
            var greenValue = Integer.decode(green);
            var blueValue = Integer.decode(blue);
            var color = new Color(redValue, greenValue, blueValue);

            assertThat(color.getRed()).isEqualTo(redValue);
            assertThat(color.getGreen()).isEqualTo(greenValue);
            assertThat(color.getBlue()).isEqualTo(blueValue);
        }

        @ParameterizedTest
        @CsvSource({
                "-1, 70, 100",      // negative number (1st param)
                "50, -100, 100",    // negative number (2nd param)
                "50, 70, -1000",    // negative number (3rd param)
                "256, 255, 255",    // number too big  (1st param)
                "255, 1000, 0",     // number too big  (2nd param)
                "50, 100, 300",     // number too big  (3rd param)
        })
        void constructor_ShouldThrowException(String red, String green, String blue) {
            var redValue = Integer.decode(red);
            var greenValue = Integer.decode(green);
            var blueValue = Integer.decode(blue);

            assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(
                    () -> new Color(redValue, greenValue, blueValue)
            );
        }
    }

    @Nested
    class FromRgbIntTests {
        @ParameterizedTest
        @CsvSource({
                "0x000000, 0x00, 0x00, 0x00",
                "0xfFfFfF, 0xFF, 0xFF, 0xFF",
                "0x139aF7, 0x13, 0x9A, 0xF7"
        })
        void fromRgbInt_ShouldWork(String input, String expectedRed, String expectedGreen, String expectedBlue) {
            var color = Color.fromRgbInt(Integer.decode(input));

            var redValue = Integer.decode(expectedRed);
            var greenValue = Integer.decode(expectedGreen);
            var blueValue = Integer.decode(expectedBlue);

            assertThat(color.getRed()).isEqualTo(redValue);
            assertThat(color.getGreen()).isEqualTo(greenValue);
            assertThat(color.getBlue()).isEqualTo(blueValue);
        }

        @Test
        void fromRegInt_shouldReturnNullWhenInputIsNull() {
            assertThat(Color.fromRgbInt(null)).isNull();
        }
    }

    @Nested
    class FromHexTripletTests {
        @ParameterizedTest
        @CsvSource({
                "000000, 0x00, 0x00, 0x00",
                "000, 0x00, 0x00, 0x00",
                "FfFfFf, 0xFF, 0xFF, 0xFF",
                "fFF, 0xFF, 0xFF, 0xFF",
                "1199cC, 0x11, 0x99, 0xCC",
                "19C, 0x11, 0x99, 0xCC",
                "  123456, 0x12, 0x34, 0x56", // White spaces
                "789aBc\t, 0x78, 0x9A, 0xBC", // White space
                "  123, 0x11, 0x22, 0x33",    // White spaces
                "AbC\t, 0xAA, 0xBB, 0xCC"     // White space
        })
        void fromHexTriplet_ShouldWork(String hexTriplet, String expectedRed, String expectedGreen, String expectedBlue) {
            var color = Color.fromHexTriplet(hexTriplet);

            var redValue = Integer.decode(expectedRed);
            var greenValue = Integer.decode(expectedGreen);
            var blueValue = Integer.decode(expectedBlue);

            assertThat(color.getRed()).isEqualTo(redValue);
            assertThat(color.getGreen()).isEqualTo(greenValue);
            assertThat(color.getBlue()).isEqualTo(blueValue);
        }

        @ParameterizedTest
        @CsvSource({
                "00000",    // Too short
                "0000000",  // Too long
                "0000 00",  // White space
                "xxxxxx",   // Wrong letters
                "abcdeG",   // Wrong letter
        })
        void fromHexTriplet_ShouldThrowException(String hexTriplet) {
            assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(
                    () -> Color.fromHexTriplet(hexTriplet)
            );
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
            assertThat(Color.fromRgbInt(0x102030).hashCode()).isEqualTo(new Color(0x10, 0x20, 0x30).hashCode());
            assertThat(Color.fromRgbInt(0x112233).hashCode()).isEqualTo(new Color(0x11, 0x22, 0x33).hashCode());
        }

        @Test
        void hashCode_ShouldNotBeEqual() {
            assertThat(new Color(10, 20, 30).hashCode()).isNotEqualTo(Color.fromRgbInt(0x123456).hashCode());
        }
    }

    @Nested
    class ToStringTests {
        @Test
        void toString_ShouldBeEqual() {
            assertThat(Color.fromHexTriplet("FDB975").toString()).isEqualToIgnoringCase("FDB975");
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
