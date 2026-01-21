package dev.robocode.tankroyale.botapi.internal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TR-API-INT-001 RecordingPrintStream")
class RecordingPrintStreamTest {

    @Test
    @DisplayName("Should preserve newline characters without escaping")
    void testNewlineNotEscaped() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        RecordingPrintStream stream = new RecordingPrintStream(baos);

        stream.print("Hello\nWorld\n");
        stream.flush();

        String output = stream.readNext();

        // The output should contain actual newline characters, not the string "\\n"
        assertThat(output).isEqualTo("Hello\nWorld\n");
        // Verify it's not double-escaped
        assertThat(output).doesNotContain("\\\\n");
    }

    @Test
    @DisplayName("Should preserve carriage return characters")
    void testCarriageReturnPreserved() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        RecordingPrintStream stream = new RecordingPrintStream(baos);

        stream.print("Hello\rWorld");
        stream.flush();

        String output = stream.readNext();

        // The output should contain actual \r character
        assertThat(output).isEqualTo("Hello\rWorld");
        // Verify it's not double-escaped
        assertThat(output).doesNotContain("\\\\r");
    }

    @Test
    @DisplayName("Should preserve tab characters without escaping")
    void testTabNotEscaped() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        RecordingPrintStream stream = new RecordingPrintStream(baos);

        stream.print("Hello\tWorld");
        stream.flush();

        String output = stream.readNext();

        // The output should contain actual tab character, not escaped \t
        assertThat(output).contains("\t");
        assertThat(output).doesNotContain("\\t");
    }

    @Test
    @DisplayName("Should preserve backslashes")
    void testBackslashPreserved() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        RecordingPrintStream stream = new RecordingPrintStream(baos);

        stream.print("Path: C:\\temp\\file.txt");
        stream.flush();

        String output = stream.readNext();

        // Backslashes should be preserved as-is
        assertThat(output).isEqualTo("Path: C:\\temp\\file.txt");
    }

    @Test
    @DisplayName("Should reset output after reading")
    void testResetAfterRead() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        RecordingPrintStream stream = new RecordingPrintStream(baos);

        stream.print("First");
        stream.flush();
        String first = stream.readNext();

        stream.print("Second");
        stream.flush();
        String second = stream.readNext();

        assertThat(first).isEqualTo("First");
        assertThat(second).isEqualTo("Second");
    }
}
