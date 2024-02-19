package dev.robocode.tankroyale.botapi.internal;

import dev.robocode.tankroyale.botapi.util.JsonUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import static java.nio.charset.StandardCharsets.UTF_8;

public final class RecordingPrintStream {

    private final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    private final PrintStream customPrintStream;

    public RecordingPrintStream(OutputStream outputStream) {
        customPrintStream = new PrintStream(new CustomOutputStream(new PrintStream(outputStream), byteArrayOutputStream));
    }

    public PrintStream getOutput() {
        return customPrintStream;
    }

    public String flushAndReturnLastString() {
        customPrintStream.flush();
        String output = JsonUtil.escaped(byteArrayOutputStream.toString(UTF_8));
        byteArrayOutputStream.reset();
        return output.isEmpty() ? null : output;
    }

    // Custom OutputStream to write to two output streams simultaneously
    private static class CustomOutputStream extends OutputStream {

        private final OutputStream out1;
        private final OutputStream out2;

        public CustomOutputStream(OutputStream out1, OutputStream out2) {
            this.out1 = out1;
            this.out2 = out2;
        }

        @Override
        public void write(int b) throws IOException {
            out1.write(b);
            out2.write(b);
        }

        @Override
        public void flush() throws IOException {
            out1.flush();
            out2.flush();
        }
    }
}
