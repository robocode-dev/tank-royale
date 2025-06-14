using System;
using Newtonsoft.Json;
using Robocode.TankRoyale.BotApi.Graphics;

namespace Robocode.TankRoyale.BotApi.Internal.Json;

public class ColorJsonConverter : JsonConverter<Color>
{
    public override Color ReadJson(JsonReader reader, Type objectType, Color existingValue, bool hasExistingValue, JsonSerializer serializer)
    {
        if (reader.Value is string colorString)
        {
            // Remove # if present
            colorString = colorString.TrimStart('#');

            // Handle both #RRGGBBAA and #RGBA formats
            if (colorString.Length == 8) // RRGGBBAA
            {
                uint r = Convert.ToUInt32(colorString.Substring(0, 2), 16);
                uint g = Convert.ToUInt32(colorString.Substring(2, 2), 16);
                uint b = Convert.ToUInt32(colorString.Substring(4, 2), 16);
                uint a = Convert.ToUInt32(colorString.Substring(6, 2), 16);
                return Color.FromRgba(r, g, b, a);
            }
            else if (colorString.Length == 4) // RGBA
            {
                uint r = Convert.ToUInt32(colorString[0].ToString() + colorString[0], 16);
                uint g = Convert.ToUInt32(colorString[1].ToString() + colorString[1], 16);
                uint b = Convert.ToUInt32(colorString[2].ToString() + colorString[2], 16);
                uint a = Convert.ToUInt32(colorString[3].ToString() + colorString[3], 16);
                return Color.FromRgba(r, g, b, a);
            }
        }
        
        throw new JsonSerializationException("Invalid color format");
    }

    public override void WriteJson(JsonWriter writer, Color value, JsonSerializer serializer)
    {
        // Write in #RRGGBBAA format
        writer.WriteValue($"#{value.R:X2}{value.G:X2}{value.B:X2}{value.A:X2}");
    }
}