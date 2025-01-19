package dev.robocode.tankroyale.botapi.internal;

import dev.robocode.tankroyale.botapi.util.JsonUtil;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import static java.nio.charset.StandardCharsets.UTF_8;

final class RecordingPrintStream extends PrintStream {

    private final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    private final PrintStream printStream = new PrintStream(byteArrayOutputStream);

    public RecordingPrintStream(OutputStream out) {
        super(out);
    }

    @Override
    public void write(int b) {
        synchronized (this) {
            super.write(b);
            printStream.write(b);
        }
    }

    @Override
    public void write(byte[] buffer) {
        write(buffer, 0, buffer.length);
    }

    @Override
    public void write(byte[] buffer, int offset, int length) {
        synchronized (this) {
            super.write(buffer, offset, length);
            printStream.write(buffer, offset, length);
        }
    }

    @Override
    public void flush() {
        synchronized (this) {
            super.flush();
            printStream.flush();
        }
    }

    public String readNext() {
        synchronized (this) {
            String output = JsonUtil.escaped(byteArrayOutputStream.toString(UTF_8));
            byteArrayOutputStream.reset();
            return output;
        }
    }
}
