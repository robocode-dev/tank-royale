package dev.robocode.tankroyale.botapi.internal;

import dev.robocode.tankroyale.botapi.util.JsonUtil;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import static java.nio.charset.StandardCharsets.UTF_8;

public final class RecordingPrintStream extends PrintStream {

    private final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    private final PrintStream printStream = new PrintStream(byteArrayOutputStream, true, UTF_8);

    public RecordingPrintStream(OutputStream out) {
        super(out);
    }

    @Override
    public void write(int b) {
        super.write(b);
        printStream.write(b);
    }

    @Override
    public void write(byte[] buf, int off, int len) {
        super.write(buf, off, len);
        printStream.write(buf, off, len);
    }

    public String readNext() {
        String output = JsonUtil.escaped(byteArrayOutputStream.toString(UTF_8));
        byteArrayOutputStream.reset();
        return output;
    }
}
