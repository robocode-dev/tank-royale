using System.Linq;
using System.Runtime.Serialization;

namespace Robocode.TankRoyale.BotApi.Util;

static class EnumUtil
{
    internal static string GetEnumMemberAttrValue<T>(T enumVal)
    {
        var enumType = typeof(T);
        var memInfo = enumType.GetMember(enumVal.ToString() ?? string.Empty);

        var attr = memInfo.FirstOrDefault()?.GetCustomAttributes(false).OfType<EnumMemberAttribute>()
            .FirstOrDefault();
        return attr?.Value;
    }
}