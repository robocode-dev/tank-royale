package dev.robocode.tankroyale.botapi.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static dev.robocode.tankroyale.botapi.util.JsonUtil.escaped;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TR-API-UTL-002 JsonUtil serialization")
class JsonUtilTest {

    @Tag("Unit")
    @Test
    void testEscapedBackslash() {
        assertThat(escaped("\b")).isEqualTo("\\b");
    }

    @Tag("Unit")
    @Test
    void testEscapedFormFeed() {
        assertThat(escaped("\f")).isEqualTo("\\f");
    }

    @Tag("Unit")
    @Test
    void testEscapedNewline() {
        assertThat(escaped("\n")).isEqualTo("\\n");
    }

    @Tag("Unit")
    @Test
    void testEscapedCarriageReturn() {
        assertThat(escaped("\r")).isEmpty();
    }

    @Tag("Unit")
    @Test
    void testEscapedTab() {
        assertThat(escaped("\t")).isEqualTo("\\t");
    }

    @Tag("Unit")
    @Test
    void testEscapedDoubleQuotes() {
        assertThat(escaped("\"")).isEqualTo("\\\"");
    }

    @Tag("Unit")
    @Test
    void testEscapedDoubleBackslashes() {
        assertThat(escaped("\\\\")).isEqualTo("\\\\");
    }
}
