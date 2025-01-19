package dev.robocode.tankroyale.botapi.internal;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;
import dev.robocode.tankroyale.schema.game.Event;

import java.awt.*;
import java.io.IOException;

public final class GsonFactory {

    private static Gson gson;

    public static Gson createGson() {
        if (gson == null) {
            gson = new GsonBuilder()
                    .registerTypeAdapterFactory(getEventTypeFactory())
                    .registerTypeAdapter(Color.class, new ColorTypeAdapter()) // support for Color
                    // to avoid IllegalArgumentException: -Infinity is not a valid double value as per JSON specification
                    .serializeSpecialFloatingPointValues()
                    .create();
        }
        return gson;
    }

    private static RuntimeTypeAdapterFactory<Event> getEventTypeFactory() {
        return RuntimeTypeAdapterFactory.of(dev.robocode.tankroyale.schema.game.Event.class, "type")
                .registerSubtype(dev.robocode.tankroyale.schema.game.BotDeathEvent.class, "BotDeathEvent")
                .registerSubtype(dev.robocode.tankroyale.schema.game.BotHitBotEvent.class, "BotHitBotEvent")
                .registerSubtype(dev.robocode.tankroyale.schema.game.BotHitWallEvent.class, "BotHitWallEvent")
                .registerSubtype(dev.robocode.tankroyale.schema.game.BulletFiredEvent.class, "BulletFiredEvent")
                .registerSubtype(dev.robocode.tankroyale.schema.game.BulletHitBotEvent.class, "BulletHitBotEvent")
                .registerSubtype(dev.robocode.tankroyale.schema.game.BulletHitBulletEvent.class, "BulletHitBulletEvent")
                .registerSubtype(dev.robocode.tankroyale.schema.game.BulletHitWallEvent.class, "BulletHitWallEvent")
                .registerSubtype(dev.robocode.tankroyale.schema.game.ScannedBotEvent.class, "ScannedBotEvent")
                .registerSubtype(dev.robocode.tankroyale.schema.game.WonRoundEvent.class, "WonRoundEvent")
                .registerSubtype(dev.robocode.tankroyale.schema.game.TeamMessageEvent.class, "TeamMessageEvent");
    }

    private static final class ColorTypeAdapter extends TypeAdapter<Color> {
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
}
