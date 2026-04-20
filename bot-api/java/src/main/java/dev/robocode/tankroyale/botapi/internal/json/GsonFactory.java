package dev.robocode.tankroyale.botapi.internal.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;
import dev.robocode.tankroyale.botapi.graphics.Color;
import dev.robocode.tankroyale.botapi.graphics.Point;
import dev.robocode.tankroyale.botapi.util.ColorUtil;
import dev.robocode.tankroyale.schema.Event;

import java.io.IOException;

/**
 * The GsonFactory class provides a singleton instance of the Gson object configured for
 * specific serialization and deserialization needs, including custom type adapters and
 * runtime type adapter factories.
 * <p>
 * This class ensures a single Gson instance is shared across the application, preventing
 * redundant creation and supporting custom JSON handling for various data types.
 */
final class GsonFactory {

    private static Gson gson;

    static Gson getGson() {
        if (gson == null) {
            gson = createGson();
        }
        return gson;
    }

    private static Gson createGson() {
        if (gson == null) {
            gson = new GsonBuilder()
                    .registerTypeAdapterFactory(getEventTypeFactory())
                    .registerTypeAdapter(Color.class, new ColorTypeAdapter())
                    .registerTypeAdapter(Point.class, new PointTypeAdapter())
                    // to avoid IllegalArgumentException: -Infinity is not a valid double value as per JSON specification
                    .serializeSpecialFloatingPointValues()
                    .create();
        }
        return gson;
    }

    private static RuntimeTypeAdapterFactory<Event> getEventTypeFactory() {
        return RuntimeTypeAdapterFactory.of(dev.robocode.tankroyale.schema.Event.class, "type", true)
                .registerSubtype(dev.robocode.tankroyale.schema.BotDeathEvent.class, "BotDeathEvent")
                .registerSubtype(dev.robocode.tankroyale.schema.BotHitBotEvent.class, "BotHitBotEvent")
                .registerSubtype(dev.robocode.tankroyale.schema.BotHitWallEvent.class, "BotHitWallEvent")
                .registerSubtype(dev.robocode.tankroyale.schema.BulletFiredEvent.class, "BulletFiredEvent")
                .registerSubtype(dev.robocode.tankroyale.schema.BulletHitBotEvent.class, "BulletHitBotEvent")
                .registerSubtype(dev.robocode.tankroyale.schema.BulletHitBulletEvent.class, "BulletHitBulletEvent")
                .registerSubtype(dev.robocode.tankroyale.schema.BulletHitWallEvent.class, "BulletHitWallEvent")
                .registerSubtype(dev.robocode.tankroyale.schema.ScannedBotEvent.class, "ScannedBotEvent")
                .registerSubtype(dev.robocode.tankroyale.schema.WonRoundEvent.class, "WonRoundEvent")
                .registerSubtype(dev.robocode.tankroyale.schema.TeamMessageEvent.class, "TeamMessageEvent");
    }

    private static final class ColorTypeAdapter extends TypeAdapter<Color> {
        @Override
        public void write(JsonWriter out, Color color) throws IOException {
            if (color == null) {
                out.nullValue();
                return;
            }
            String hexColor = String.format("#%02x%02x%02x", color.getR(), color.getG(), color.getB());
            out.value(hexColor);
        }

        @Override
        public Color read(JsonReader in) throws IOException {
            String webColor = in.nextString();
            return ColorUtil.fromHexColor(webColor);
        }
    }

    // Avoids Gson's reflective mutation of Point's final fields (blocked in future JDKs).
    private static final class PointTypeAdapter extends TypeAdapter<Point> {
        @Override
        public void write(JsonWriter out, Point point) throws IOException {
            if (point == null) {
                out.nullValue();
                return;
            }
            out.beginObject();
            out.name("x").value(point.getX());
            out.name("y").value(point.getY());
            out.endObject();
        }

        @Override
        public Point read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return null;
            }
            double x = 0, y = 0;
            in.beginObject();
            while (in.hasNext()) {
                String name = in.nextName();
                if ("x".equals(name)) {
                    x = in.nextDouble();
                } else if ("y".equals(name)) {
                    y = in.nextDouble();
                } else {
                    in.skipValue();
                }
            }
            in.endObject();
            return new Point(x, y);
        }
    }
}
