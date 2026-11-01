using System;
using Newtonsoft.Json;
using Robocode.TankRoyale.BotApi.Graphics;
using Robocode.TankRoyale.BotApi.Util;

namespace Robocode.TankRoyale.BotApi.Internal.Json;

/// <summary>
/// Reads and writes a <see cref="Color"/> as hex colour text in a team message or other JSON payload.
/// Reading accepts the same four forms as <see cref="ColorUtil.FromHexColor"/>. Writing gives "#RRGGBB" when the
/// color is fully opaque and "#RRGGBBAA" otherwise, so transparency is kept.
/// </summary>
public class ColorJsonConverter : JsonConverter<Color>
{
    public override Color ReadJson(JsonReader reader, Type objectType, Color existingValue, bool hasExistingValue, JsonSerializer serializer)
    {
        if (reader.Value is string colorString)
        {
            try
            {
                return ColorUtil.FromHexColor(colorString) ?? throw new JsonSerializationException("Invalid color format");
            }
            catch (ArgumentException e)
            {
                throw new JsonSerializationException("Invalid color format: " + e.Message, e);
            }
        }

        throw new JsonSerializationException("Invalid color format");
    }

    public override void WriteJson(JsonWriter writer, Color value, JsonSerializer serializer)
    {
        writer.WriteValue(value.ToHexColor());
    }
}
