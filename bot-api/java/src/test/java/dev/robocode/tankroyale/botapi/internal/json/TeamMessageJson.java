package dev.robocode.tankroyale.botapi.internal.json;

import com.google.gson.JsonParser;
import dev.robocode.tankroyale.botapi.graphics.Color;

/**
 * Test helper that exposes how a colour is written into a team message. The serializer is package-private, so this
 * helper lives in the same package.
 */
public final class TeamMessageJson {

    private static final class Holder {
        Color color;
    }

    private TeamMessageJson() {
    }

    /** Returns the text a colour becomes when it is a field of a team message payload. */
    public static String colorText(Color color) {
        Holder holder = new Holder();
        holder.color = color;
        String json = GsonFactory.getGson().toJson(holder);
        return JsonParser.parseString(json).getAsJsonObject().get("color").getAsString();
    }
}
