using System;
using Newtonsoft.Json;

namespace Robocode.TankRoyale.BotApi.Internal.Json;

public static class JsonConverter
{
    private static readonly JsonSerializerSettings Settings;

    static JsonConverter()
    {
        Settings = new JsonSerializerSettings();
        Settings.Converters.Add(new ColorJsonConverter());
    }

    public static string ToJson(object obj)
    {
        return JsonConvert.SerializeObject(obj, Settings);
    }

    public static T FromJson<T>(string json) where T : class
    {
        return (T)JsonConvert.DeserializeObject(json, typeof(T), Settings);
    }

    public static object FromJson(string json, Type type)
    {
        return JsonConvert.DeserializeObject(json, type, Settings);
    }
}
