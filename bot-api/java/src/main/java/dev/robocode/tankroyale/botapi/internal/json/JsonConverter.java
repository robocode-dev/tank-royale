package dev.robocode.tankroyale.botapi.internal.json;

import com.google.gson.JsonObject;

public final class JsonConverter {

    public static String toJson(Object obj) {
        return GsonFactory.getGson().toJson(obj);
    }

    public static <T> T fromJson(String json, Class<T> type) {
        return GsonFactory.getGson().fromJson(json, type);
    }

    public static <T> T fromJson(JsonObject jsonObject, Class<T> type) {
        return GsonFactory.getGson().fromJson(jsonObject, type);
    }
}
