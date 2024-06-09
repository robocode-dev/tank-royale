package dev.robocode.tankroyale.botapi;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class InitialPositionTest {

    @ParameterizedTest
    @MethodSource("fromStringProvider")
    void givenValidInputString_whenCallingFromString_thenReturnedPositionFieldsMustBeSame(String str, Double x, Double y, Double angle) {
        var pos = InitialPosition.fromString(str);
        assertThat(pos.getX()).isEqualTo(x);
        assertThat(pos.getY()).isEqualTo(y);
        assertThat(pos.getDirection()).isEqualTo(angle);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "",
            " \t",
            " ,",
            ",,,",
            ", ,"
    })
    void givenEmptyOrBlankInputString_whenCallingFromString_thenReturnNull(String str) {
        var pos = InitialPosition.fromString(str);
        assertThat(pos).isNull();
    }

    @ParameterizedTest
    @MethodSource("toStringProvider")
    void givenValidInputString_whenCallingToString_thenReturnedStringIsFormattedAndMatchesInputString(String input, String expected) {
        var pos = InitialPosition.fromString(input);
        if (pos == null) {
            assertThat(expected).isEmpty();
        } else {
            assertThat(pos).hasToString(expected);
        }
    }

    static Stream<Arguments> fromStringProvider() {
        return Stream.of(
                arguments("50,50, 90", 50.0, 50.0, 90.0),
                arguments("12.23, -123.3, 45.5", 12.23, -123.3, 45.5),
                arguments(" 50 ", 50.0, null, null),
                arguments(" 50.1  70.2 ", 50.1, 70.2, null),
                arguments("50.1 70.2, 678.3", 50.1, 70.2, 678.3),
                arguments("50.1  , 70.2, 678.3", 50.1, 70.2, 678.3),
                arguments("50.1 70.2, 678.3 789.1", 50.1, 70.2, 678.3),
                arguments("50.1  , , 678.3", 50.1, null, 678.3),
                arguments(", , 678.3", null, null, 678.3)
        );
    }

    static Stream<Arguments> toStringProvider() {
        return Stream.of(
                arguments("50, 50, 90", "50.0,50.0,90.0"),
                arguments("12.23, -123.3, 45.5", "12.23,-123.3,45.5"),
                arguments(" 50 ", "50.0,,"),
                arguments(" 50.1  70.2 ", "50.1,70.2,"),
                arguments("50.1 70.2, 678.3", "50.1,70.2,678.3"),
                arguments("50.1  , 70.2, 678.3", "50.1,70.2,678.3"),
                arguments("50.1 70.2, 678.3 789.1", "50.1,70.2,678.3"),
                arguments("50.1  , , 678.3", "50.1,,678.3"),
                arguments(", , 678.3", ",,678.3"),
                arguments("", ""),
                arguments(" \t", ""),
                arguments(" ,", ""),
                arguments(",,,", ""),
                arguments(", ,", "")
        );
    }
}
