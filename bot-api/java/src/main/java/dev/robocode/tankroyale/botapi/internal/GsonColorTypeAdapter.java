package dev.robocode.tankroyale.botapi.internal;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.awt.Color;
import java.io.IOException;

final class GsonColorTypeAdapter extends TypeAdapter<Color> {
    @Override
    public void write(JsonWriter out, Color color) throws IOException {
        if (color == null) {
            out.nullValue();
            return;
        }
        String hexColor = String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
        out.value(hexColor);
    }

    @Override
    public Color read(JsonReader in) throws IOException {
        String hexColor = in.nextString();
        return Color.decode(hexColor);
    }
}
